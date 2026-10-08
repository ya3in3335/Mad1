"""
Madak Spices – vertical promo video generator (1080x1920, 30 fps, ~25 s).

Renders every frame with Pillow (Arabic shaping via libraqm) from the real brand layers and real app
screenshots, synthesises an original music bed + sound design with numpy, and muxes with ffmpeg.

Usage:
    python3 docs/promo/make_promo.py <repo_root> <screens_dir> <out.mp4>

<screens_dir> holds the PNGs produced by AdScreenshotsTest
(./gradlew :app:testDebugUnitTest -PmadakAd=true --tests "*AdScreenshotsTest").
"""
import math
import os
import random
import subprocess
import sys
import tempfile
import wave
from multiprocessing import Pool

import numpy as np
from PIL import Image, ImageDraw, ImageFilter, ImageFont

W, H, FPS = 1080, 1920, 30
DURATION = 25.0
N_FRAMES = int(DURATION * FPS)

INK = (14, 12, 11)
CHARCOAL = (28, 24, 22)
CREAM = (250, 246, 240)
MAGENTA = (214, 36, 124)
GOLD = (201, 164, 106)
GOLD_SOFT = (234, 217, 184)
LEAF = (124, 194, 66)
SPICES = [(227, 162, 26), (200, 55, 31), (156, 107, 48), (139, 74, 43), (107, 165, 57), (201, 164, 106), (214, 36, 124)]

ROOT = SCREENS = None
A = {}  # assets, loaded per worker


# --------------------------------------------------------------------------- helpers
def seg(t, start, dur):
    return min(1.0, max(0.0, (t - start) / dur))


def ease_out_cubic(x):
    return 1 - (1 - x) ** 3


def ease_in_out(x):
    return 4 * x ** 3 if x < 0.5 else 1 - (-2 * x + 2) ** 3 / 2


def ease_out_back(x, s=1.7):
    p = x - 1
    return 1 + (s + 1) * p ** 3 + s * p ** 2


def font(weight, size):
    key = (weight, size)
    if key not in A["fonts"]:
        path = os.path.join(ROOT, "core/designsystem/src/main/res/font", f"tajawal_{weight}.ttf")
        A["fonts"][key] = ImageFont.truetype(path, size, layout_engine=ImageFont.Layout.RAQM)
    return A["fonts"][key]


def text_layer(text, size, color, weight="bold", rtl=True, spacing=0):
    """Renders text into a tight RGBA layer (cached)."""
    key = (text, size, color, weight, rtl, spacing)
    cache = A["text"]
    if key in cache:
        return cache[key]
    f = font(weight, size)
    kw = dict(direction="rtl", language="ar") if rtl else dict(direction="ltr")
    if spacing:
        # Latin letter-spacing: draw char by char.
        widths = [f.getbbox(c, **kw)[2] for c in text]
        w = int(sum(widths) + spacing * (len(text) - 1)) + 20
        img = Image.new("RGBA", (w, int(size * 1.6)), (0, 0, 0, 0))
        d = ImageDraw.Draw(img)
        x = 10
        for c, cw in zip(text, widths):
            d.text((x, size * 0.2), c, font=f, fill=color, **kw)
            x += cw + spacing
    else:
        bbox = f.getbbox(text, **kw)
        w, h = bbox[2] - bbox[0] + 40, bbox[3] - bbox[1] + int(size * 0.6)
        img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        ImageDraw.Draw(img).text((20 - bbox[0], int(size * 0.3) - bbox[1]), text, font=f, fill=color, **kw)
    cache[key] = img
    return img


def paste(base, layer, cx, cy, alpha=1.0, scale=1.0, rotate=0.0):
    """Alpha-composites `layer` centred at (cx, cy) with opacity, scale and rotation."""
    if alpha <= 0.002 or scale <= 0.01:
        return
    lyr = layer
    if scale != 1.0:
        lyr = lyr.resize((max(1, int(lyr.width * scale)), max(1, int(lyr.height * scale))), Image.BICUBIC)
    if rotate:
        lyr = lyr.rotate(rotate, resample=Image.BICUBIC, expand=True)
    if alpha < 0.999:
        a = lyr.getchannel("A").point(lambda v: int(v * alpha))
        lyr = lyr.copy()
        lyr.putalpha(a)
    base.alpha_composite(lyr, (int(cx - lyr.width / 2), int(cy - lyr.height / 2)))


def radial_glow(color, radius, strength):
    key = ("glow", color, radius)
    if key not in A["text"]:
        size = radius * 2
        yy, xx = np.mgrid[0:size, 0:size]
        d = np.sqrt((xx - radius) ** 2 + (yy - radius) ** 2) / radius
        a = np.clip(1 - d, 0, 1) ** 2
        arr = np.zeros((size, size, 4), np.uint8)
        arr[..., :3] = color
        arr[..., 3] = (a * 255).astype(np.uint8)
        A["text"][key] = Image.fromarray(arr, "RGBA")
    return A["text"][key], strength


def draw_glow(base, color, cx, cy, radius, strength):
    g, s = radial_glow(color, radius, strength)
    paste(base, g, cx, cy, alpha=s)


def ring(base, cx, cy, r, width, color, alpha):
    if alpha <= 0 or r <= 1:
        return
    lyr = Image.new("RGBA", base.size, (0, 0, 0, 0))
    ImageDraw.Draw(lyr).ellipse([cx - r, cy - r, cx + r, cy + r], outline=color + (int(255 * alpha),), width=max(1, int(width)))
    base.alpha_composite(lyr)


def particles(base, t, start, cx, cy, count, seed, spread, life=1.4, gravity=80):
    rnd = random.Random(seed)
    d = ImageDraw.Draw(base)
    for _ in range(count):
        ang = rnd.random() * math.tau
        dist = (0.35 + rnd.random() * 0.65) * spread
        size = 4 + rnd.random() * 10
        color = rnd.choice(SPICES + [(255, 255, 255)])
        delay = rnd.random() * 0.25
        p = seg(t, start + delay, life)
        if p <= 0 or p >= 1:
            continue
        tr = ease_out_cubic(p) * dist
        x = cx + math.cos(ang) * tr
        y = cy + math.sin(ang) * tr + p * p * gravity
        r = size * (1 - p * 0.5)
        a = int(255 * (1 - p))
        d.ellipse([x - r, y - r, x + r, y + r], fill=color + (a,))


def floating_dots(base, t, seed, n, alpha=0.35, area=None):
    rnd = random.Random(seed)
    d = ImageDraw.Draw(base)
    x0, y0, x1, y1 = area or (0, 0, W, H)
    for _ in range(n):
        x = x0 + rnd.random() * (x1 - x0)
        y = y0 + rnd.random() * (y1 - y0)
        sp = 20 + rnd.random() * 50
        ph = rnd.random() * math.tau
        r = 3 + rnd.random() * 9
        y = (y - t * sp) % (y1 - y0) + y0
        x += math.sin(t * 0.8 + ph) * 18
        c = rnd.choice(SPICES)
        d.ellipse([x - r, y - r, x + r, y + r], fill=c + (int(255 * alpha),))


def rounded(img, radius):
    mask = Image.new("L", img.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, img.width - 1, img.height - 1], radius=radius, fill=255)
    out = img.convert("RGBA")
    out.putalpha(mask)
    return out


# --------------------------------------------------------------------------- assets
def load_assets(root, screens):
    global ROOT, SCREENS
    ROOT, SCREENS = root, screens
    A.clear()
    A["fonts"], A["text"] = {}, {}
    res = os.path.join(root, "core/designsystem/src/main/res/drawable-nodpi")
    lw = 640
    chef = Image.open(os.path.join(res, "madak_logo_chef.png")).convert("RGBA")
    A["chef"] = chef.resize((lw, int(chef.height * lw / chef.width)), Image.LANCZOS)
    ww = int(lw * 670 / 680)
    for k in ("word", "leaf"):
        im = Image.open(os.path.join(res, f"madak_logo_{k}.png")).convert("RGBA")
        A[k] = im.resize((ww, int(im.height * ww / im.width)), Image.LANCZOS)
    full = Image.open(os.path.join(res, "madak_logo.png")).convert("RGBA")
    A["full"] = full.resize((420, int(full.height * 420 / full.width)), Image.LANCZOS)
    leaf = A["leaf"]
    pw, ph = int(leaf.width * 0.4), int(leaf.height * 0.4)
    A["leaf_pad"] = Image.new("RGBA", (leaf.width + 2 * pw, leaf.height + 2 * ph), (0, 0, 0, 0))
    A["leaf_pad"].alpha_composite(leaf, (pw, ph))
    A["leaf_pivot"] = (pw + leaf.width * 0.15, ph + leaf.height * 0.70)
    # word mask for the shimmer
    A["word_alpha"] = np.asarray(A["word"].getchannel("A"), dtype=np.float32) / 255.0

    # phone mockups
    sw = 600
    A["phones"] = {}
    for name in ("ad_home", "ad_cook", "ad_detail", "ad_cart", "ad_tracking"):
        sc = Image.open(os.path.join(screens, f"{name}.png")).convert("RGB")
        sh = int(sc.height * sw / sc.width)
        sc = rounded(sc.resize((sw, sh), Image.LANCZOS), 44)
        bez = 18
        frame = Image.new("RGBA", (sw + bez * 2, sh + bez * 2), (0, 0, 0, 0))
        ImageDraw.Draw(frame).rounded_rectangle([0, 0, frame.width - 1, frame.height - 1], radius=62, fill=(18, 16, 15, 255))
        ImageDraw.Draw(frame).rounded_rectangle([3, 3, frame.width - 4, frame.height - 4], radius=60, outline=(70, 62, 56, 255), width=3)
        frame.alpha_composite(sc, (bez, bez))
        # shadow
        shadow = Image.new("RGBA", (frame.width + 120, frame.height + 120), (0, 0, 0, 0))
        ImageDraw.Draw(shadow).rounded_rectangle([60, 80, 60 + frame.width, 80 + frame.height], radius=62, fill=(0, 0, 0, 110))
        shadow = shadow.filter(ImageFilter.GaussianBlur(30))
        shadow.alpha_composite(frame, (60, 60))
        A["phones"][name] = shadow

    # backgrounds
    yy = np.linspace(0, 1, H)[:, None, None]
    dark = np.zeros((H, W, 3), np.float32)
    dark[:] = np.array(INK) * (1 - yy) + np.array(CHARCOAL) * yy
    A["bg_dark"] = Image.fromarray(dark.astype(np.uint8), "RGB").convert("RGBA")
    light = np.zeros((H, W, 3), np.float32)
    light[:] = np.array(CREAM) * (1 - yy) + np.array((239, 227, 208)) * yy
    A["bg_light"] = Image.fromarray(light.astype(np.uint8), "RGB").convert("RGBA")


# --------------------------------------------------------------------------- scenes
def scene_logo(t):
    """0.0–3.8 s: the motion-graphics logo reveal (same choreography as the in-app splash)."""
    img = A["bg_dark"].copy()
    cx, cy = W // 2, 760
    draw_glow(img, MAGENTA, cx, cy, 900, 0.55 * seg(t, 0, 0.9))
    r1 = seg(t, 0.05, 1.0)
    if 0 < r1 < 1:
        ring(img, cx, cy, 950 * ease_out_cubic(r1), 8 * (1 - r1) + 1, GOLD, 1 - r1)
    particles(img, t, 0.45, cx, cy, 70, 42, 900)
    ci = seg(t, 0.15, 0.7)
    paste(img, A["chef"], cx, cy + (1 - ease_out_cubic(ci)) * 140,
          alpha=seg(t, 0.15, 0.3), scale=max(0.01, 0.55 + 0.45 * ease_out_back(ci)), rotate=(1 - ease_out_cubic(ci)) * 8)
    # grains falling from the hand into the pot
    d = ImageDraw.Draw(img)
    rnd = random.Random(7)
    chef_w, chef_h = A["chef"].size
    ox, oy = cx - chef_w / 2, cy - chef_h / 2
    for _ in range(26):
        dx, delay, sz = rnd.random() * 0.06 - 0.03, rnd.random() * 0.8, 3 + rnd.random() * 4
        p = seg(t, 0.8 + delay, 0.55)
        if 0 < p < 1:
            x = ox + chef_w * (0.10 + dx + 0.07 * p)
            y = oy + chef_h * (0.47 + 0.27 * p * p)
            d.ellipse([x - sz, y - sz, x + sz, y + sz], fill=(255, 255, 255, int(255 * (1 - 0.6 * p))))
    # wordmark reveal (right to left) + landing
    wy = cy + chef_h / 2 + 30 + A["word"].height / 2
    rev = ease_in_out(seg(t, 1.0, 0.55))
    if rev > 0:
        word = A["word"].copy()
        cut = int(word.width * (1 - rev))
        if cut > 0:
            word.paste((0, 0, 0, 0), (0, 0, cut, word.height))
        land = seg(t, 1.0, 0.45)
        sh = seg(t, 2.05, 0.65)
        if 0 < sh < 1:
            arr = np.asarray(word).astype(np.float32)
            xs = np.arange(word.width)[None, :]
            ys = np.arange(word.height)[:, None]
            band = -word.width * 0.4 + word.width * 1.8 * sh
            dist = np.abs((xs + ys * 0.6) - band) / (word.width * 0.12)
            k = np.clip(1 - dist, 0, 1) * A["word_alpha"]
            for i, c in enumerate(GOLD_SOFT):
                arr[..., i] = arr[..., i] * (1 - k) + c * k
            word = Image.fromarray(arr.astype(np.uint8), "RGBA")
        paste(img, word, cx, wy + (1 - ease_out_back(land, 1.2)) * 60, alpha=seg(t, 1.0, 0.2))
    lf = seg(t, 1.75, 0.6)
    if lf > 0:
        sc = max(0.01, ease_out_back(lf, 2.2))
        th = math.radians((1 - ease_out_cubic(lf)) * 40)
        pad = A["leaf_pad"]
        px, py = A["leaf_pivot"]
        c, si = math.cos(th), math.sin(th)
        # inverse affine: input = pivot + R(-th) * (out - pivot) / sc
        a, b = c / sc, si / sc
        d_, e = -si / sc, c / sc
        coeffs = (a, b, px - a * px - b * py, d_, e, py - d_ * px - e * py)
        grown = pad.transform(pad.size, Image.AFFINE, coeffs, resample=Image.BICUBIC)
        paste(img, grown, cx, wy, alpha=seg(t, 1.75, 0.12))
    tg = seg(t, 2.3, 0.6)
    paste(img, text_layer("MADAK SPICES", 44, GOLD, rtl=False, spacing=int(6 + 10 * ease_out_cubic(tg))), cx,
          wy + A["word"].height / 2 + 90 + (1 - ease_out_cubic(tg)) * 20, alpha=tg)
    out = seg(t, 3.4, 0.4)
    if out > 0:
        img = Image.blend(img, A["bg_dark"], ease_in_out(out))
    return img


def scene_slogan(t):
    """3.8–6.4 s: kinetic slogan."""
    lt = t - 3.8
    img = A["bg_dark"].copy()
    draw_glow(img, MAGENTA, W // 2, 900, 820, 0.45)
    draw_glow(img, GOLD, W // 2, 1150, 600, 0.25)
    floating_dots(img, t, 3, 26, 0.35)
    particles(img, t, 3.85, W // 2, 900, 60, 11, 760, life=1.6)
    words = [("مذاق", 210, MAGENTA, 0.0), ("سرّ لذة", 130, (255, 255, 255), 0.35), ("الأطباق", 150, GOLD_SOFT, 0.7)]
    y = 720
    for w, size, color, delay in words:
        p = seg(lt, delay, 0.55)
        paste(img, text_layer(w, size, color, "extrabold"), W // 2, y + (1 - ease_out_cubic(p)) * 80,
              alpha=p, scale=0.7 + 0.3 * ease_out_back(p))
        y += size + 40
    p = seg(lt, 1.3, 0.5)
    paste(img, text_layer("توابل • أعشاب طبيعية • منتجات طبيعية", 52, GOLD, "medium"), W // 2, y + 60, alpha=p)
    out = seg(lt, 2.25, 0.35)
    if out > 0:
        img = Image.blend(img, A["bg_light"], ease_in_out(out))
    return img


SLIDES = [
    ("ad_home", "كل توابلك في مكان واحد", "تطبيق مذاق للتوابل"),
    ("ad_cook", "ماذا تطبخ اليوم؟", "نقترح لك التوابل المناسبة لطبقك"),
    ("ad_detail", "اختر الوزن الذي يناسبك", "50غ • 100غ • 250غ • 500غ"),
    ("ad_cart", "اطلب في ثوانٍ", "الدفع عند الاستلام"),
    ("ad_tracking", "تتبّع طلبك خطوة بخطوة", "توصيل إلى كل الولايات"),
]
APP_START, SLIDE_LEN = 6.4, 2.4


def scene_app(t):
    """6.4–18.4 s: real app screens in a phone mockup with captions."""
    lt = t - APP_START
    img = A["bg_light"].copy()
    draw_glow(img, MAGENTA, 160, 420, 520, 0.18)
    draw_glow(img, GOLD, 940, 1500, 560, 0.28)
    floating_dots(img, t, 5, 30, 0.28)
    i = min(len(SLIDES) - 1, int(lt // SLIDE_LEN))
    st = lt - i * SLIDE_LEN
    name, title, sub = SLIDES[i]
    # phone: slides in from the left (RTL reading order), floats, slides out
    pin = ease_out_cubic(seg(st, 0, 0.55))
    pout = ease_in_out(seg(st, SLIDE_LEN - 0.35, 0.35)) if i < len(SLIDES) - 1 else 0
    x = W // 2 + (1 - pin) * -W + pout * W
    y = 1150 + math.sin(t * 2.0) * 10
    paste(img, A["phones"][name], x, y, alpha=1.0, scale=0.93 + 0.04 * pin, rotate=(1 - pin) * 6 - pout * 6)
    # captions
    ct = seg(st, 0.15, 0.45)
    cout = seg(st, SLIDE_LEN - 0.3, 0.3) if i < len(SLIDES) - 1 else 0
    a = ct * (1 - cout)
    paste(img, text_layer(title, 84, INK, "extrabold"), W // 2, 250 + (1 - ease_out_cubic(ct)) * 40, alpha=a)
    chip = text_layer(sub, 46, (255, 255, 255), "bold")
    pill = Image.new("RGBA", (chip.width + 70, chip.height + 26), (0, 0, 0, 0))
    ImageDraw.Draw(pill).rounded_rectangle([0, 0, pill.width - 1, pill.height - 1], radius=pill.height // 2, fill=MAGENTA + (255,))
    pill.alpha_composite(chip, (35, 13))
    p2 = seg(st, 0.35, 0.45)
    paste(img, pill, W // 2, 380, alpha=p2 * (1 - cout), scale=0.85 + 0.15 * ease_out_back(p2))
    # progress dots
    d = ImageDraw.Draw(img)
    for k in range(len(SLIDES)):
        w = 46 if k == i else 14
        xx = W // 2 + (k - 2) * 40
        d.rounded_rectangle([xx - w / 2, 1840, xx + w / 2, 1854], radius=7, fill=(MAGENTA if k == i else (200, 190, 175)) + (255,))
    out = seg(t, 18.1, 0.3)
    if out > 0:
        img = Image.blend(img, A["bg_dark"], ease_in_out(out))
    return img


def icon(kind, size, color):
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    s = size
    if kind == "leaf":
        # lens-shaped leaf tilted 45°, with a stem and a central vein inside the blade
        leaf = Image.new("RGBA", (s, s), (0, 0, 0, 0))
        ld = ImageDraw.Draw(leaf)
        ld.ellipse([s * 0.30, s * 0.10, s * 0.70, s * 0.86], fill=color)
        ld.line([s * 0.5, s * 0.20, s * 0.5, s * 0.80], fill=(20, 60, 20, 200), width=max(2, s // 26))
        for k in range(3):
            yv = s * (0.34 + k * 0.14)
            ld.line([s * 0.5, yv + s * 0.06, s * 0.40, yv], fill=(20, 60, 20, 160), width=max(1, s // 40))
            ld.line([s * 0.5, yv + s * 0.06, s * 0.60, yv], fill=(20, 60, 20, 160), width=max(1, s // 40))
        ld.line([s * 0.5, s * 0.84, s * 0.5, s * 0.97], fill=color, width=max(2, s // 22))
        im.alpha_composite(leaf.rotate(-40, resample=Image.BICUBIC))
    elif kind == "cash":
        d.rounded_rectangle([s * 0.12, s * 0.28, s * 0.88, s * 0.72], radius=s // 12, fill=color)
        d.ellipse([s * 0.38, s * 0.36, s * 0.62, s * 0.64], fill=(255, 255, 255, 255))
    else:  # truck
        d.rounded_rectangle([s * 0.08, s * 0.3, s * 0.6, s * 0.68], radius=s // 20, fill=color)
        d.polygon([(s * 0.6, s * 0.4), (s * 0.8, s * 0.4), (s * 0.92, s * 0.55), (s * 0.92, s * 0.68), (s * 0.6, s * 0.68)], fill=color)
        for cxw in (0.25, 0.75):
            d.ellipse([s * cxw - s * 0.09, s * 0.62, s * cxw + s * 0.09, s * 0.8], fill=(255, 255, 255, 255))
    return im


def scene_features(t):
    """18.4–21.6 s: three promise cards."""
    lt = t - 18.4
    img = A["bg_dark"].copy()
    draw_glow(img, MAGENTA, W // 2, 960, 900, 0.35)
    floating_dots(img, t, 9, 22, 0.3)
    paste(img, text_layer("لماذا مذاق؟", 92, (255, 255, 255), "extrabold"), W // 2, 420, alpha=seg(lt, 0, 0.4))
    cards = [("leaf", "توابل وأعشاب طبيعية", LEAF), ("cash", "الدفع عند الاستلام", GOLD), ("truck", "توصيل لكل الولايات", MAGENTA)]
    for k, (kind, label, color) in enumerate(cards):
        p = seg(lt, 0.25 + k * 0.3, 0.55)
        card = Image.new("RGBA", (860, 220), (0, 0, 0, 0))
        cd = ImageDraw.Draw(card)
        cd.rounded_rectangle([0, 0, 859, 219], radius=48, fill=(255, 255, 255, 22), outline=color + (255,), width=3)
        cd.ellipse([860 - 190, 30, 860 - 30, 190], fill=color + (60,))
        card.alpha_composite(icon(kind, 120, color + (255,)), (860 - 170, 50))
        lbl = text_layer(label, 60, (255, 255, 255), "bold")
        card.alpha_composite(lbl, (860 - 220 - lbl.width, (220 - lbl.height) // 2))
        paste(img, card, W // 2 + (1 - ease_out_cubic(p)) * 300, 760 + k * 280, alpha=p, scale=0.9 + 0.1 * ease_out_back(p))
    out = seg(lt, 2.9, 0.3)
    if out > 0:
        img = Image.blend(img, A["bg_dark"], ease_in_out(out))
    return img


def scene_cta(t):
    """21.6–25 s: logo + call to action + real contact details."""
    lt = t - 21.6
    img = A["bg_dark"].copy()
    draw_glow(img, MAGENTA, W // 2, 640, 800, 0.5 + 0.08 * math.sin(t * 3))
    r = seg(lt, 0.0, 1.2)
    if 0 < r < 1:
        ring(img, W // 2, 640, 900 * ease_out_cubic(r), 6 * (1 - r) + 1, GOLD, 1 - r)
    particles(img, t, 21.7, W // 2, 640, 50, 23, 700)
    p = seg(lt, 0.0, 0.7)
    paste(img, A["full"], W // 2, 640, alpha=p, scale=0.6 + 0.4 * ease_out_back(p))
    p = seg(lt, 0.5, 0.5)
    btn_txt = text_layer("حمّل التطبيق الآن", 64, (255, 255, 255), "extrabold")
    btn = Image.new("RGBA", (btn_txt.width + 140, 140), (0, 0, 0, 0))
    ImageDraw.Draw(btn).rounded_rectangle([0, 0, btn.width - 1, 139], radius=70, fill=MAGENTA + (255,))
    btn.alpha_composite(btn_txt, (70, (140 - btn_txt.height) // 2))
    pulse = 1 + 0.03 * math.sin(max(0, lt - 1.0) * 6)
    paste(img, btn, W // 2, 1150, alpha=p, scale=(0.8 + 0.2 * ease_out_back(p)) * pulse)
    rows = [
        (text_layer("+213 664 71 70 29", 62, (255, 255, 255), "bold", rtl=False), 1340),
        (text_layer("TikTok  @madak.spices", 50, GOLD_SOFT, "medium", rtl=False), 1440),
        (text_layer("بالقرب من فندق M Suite – الدار البيضاء، الجزائر", 44, (220, 210, 195), "medium"), 1530),
    ]
    for k, (lyr, y) in enumerate(rows):
        q = seg(lt, 0.9 + k * 0.2, 0.45)
        paste(img, lyr, W // 2, y + (1 - ease_out_cubic(q)) * 30, alpha=q)
    q = seg(lt, 1.6, 0.5)
    paste(img, text_layer("واتساب واتصال على نفس الرقم", 40, GOLD, "medium"), W // 2, 1265, alpha=q)
    fade = seg(t, 24.4, 0.6)
    if fade > 0:
        img = Image.blend(img, Image.new("RGBA", (W, H), INK + (255,)), fade)
    return img


def render(i):
    t = i / FPS
    if t < 3.8:
        img = scene_logo(t)
    elif t < 6.4:
        img = scene_slogan(t)
    elif t < 18.4:
        img = scene_app(t)
    elif t < 21.6:
        img = scene_features(t)
    else:
        img = scene_cta(t)
    return img.convert("RGB").tobytes()


def init_worker(root, screens):
    load_assets(root, screens)


# --------------------------------------------------------------------------- audio
def make_audio(path, intro_wav):
    sr = 44100
    n = int(sr * DURATION)
    t = np.arange(n) / sr
    out = np.zeros(n)
    rng = np.random.default_rng(3)
    bpm = 104
    beat = 60 / bpm

    def env(st, a, dcy, shape=5.0):
        x = t - st
        return np.where(x < 0, 0, np.where(x < a, x / max(a, 1e-4), np.exp(-(x - a) * shape / dcy)))

    def add_tone(f, st, a, dcy, amp, shape=5.0, harm=((1, 1.0),)):
        nonlocal out
        i0 = int(st * sr)
        i1 = min(n, int((st + a + dcy * 2) * sr))
        if i0 >= n:
            return
        tt = t[i0:i1] - st
        e = np.where(tt < a, tt / max(a, 1e-4), np.exp(-(tt - a) * shape / dcy))
        s = sum(h * np.sin(2 * np.pi * f * k * tt) for k, h in harm)
        out[i0:i1] += amp * e * s

    music_start = 3.6
    # Hijaz-flavoured progression in D: Dm – Gm – A7 – Dm (roots)
    prog = [(146.83, [146.83, 174.61, 220.0]), (196.0, [196.0, 233.08, 293.66]), (220.0, [220.0, 277.18, 329.63]), (146.83, [146.83, 174.61, 220.0])]
    bar = beat * 4
    k = 0
    tt0 = music_start
    while tt0 < DURATION - 0.6:
        root, chord = prog[k % 4]
        for f in chord:  # pad
            add_tone(f, tt0, 0.25, bar * 0.9, 0.05, 2.0, ((1, 1), (2, 0.3), (3, 0.12)))
        for b in range(4):
            bt = tt0 + b * beat
            add_tone(root / 2, bt, 0.005, 0.35, 0.22, 6, ((1, 1), (2, 0.4)))  # bass
            # kick
            i0 = int(bt * sr)
            i1 = min(n, i0 + int(0.25 * sr))
            if i0 < n:
                tk = t[i0:i1] - bt
                out[i0:i1] += 0.5 * np.sin(2 * np.pi * (50 + 90 * np.exp(-tk * 30)) * tk) * np.exp(-tk * 14)
            if b in (1, 3):  # clap
                i1 = min(n, i0 + int(0.18 * sr))
                if i0 < n:
                    tk = t[i0:i1] - bt
                    out[i0:i1] += 0.16 * rng.standard_normal(i1 - i0) * np.exp(-tk * 28)
            for h in (0, 0.5):  # hats
                ht = bt + h * beat
                i0h = int(ht * sr)
                i1h = min(n, i0h + int(0.05 * sr))
                if i0h < n:
                    tk = t[i0h:i1h] - ht
                    noise = rng.standard_normal(i1h - i0h)
                    noise = noise - np.convolve(noise, np.ones(4) / 4, mode="same")
                    out[i0h:i1h] += 0.07 * noise * np.exp(-tk * 90)
        # Hijaz pluck melody (D Eb F# G A Bb C)
        scale = [293.66, 311.13, 369.99, 392.0, 440.0, 466.16, 523.25, 587.33]
        pattern = [0, 2, 3, 4, 3, 2, 1, 0] if k % 2 == 0 else [4, 5, 4, 3, 2, 3, 2, 0]
        for j, deg in enumerate(pattern):
            add_tone(scale[deg], tt0 + j * beat / 2, 0.004, 0.28, 0.07, 6, ((1, 1), (2, 0.25), (3, 0.1)))
        tt0 += bar
        k += 1
    # music fade in/out
    mf = np.clip((t - music_start) / 0.8, 0, 1) * np.clip((DURATION - t) / 1.2, 0, 1)
    out *= mf
    # whooshes at scene changes
    for st in (3.75, 6.3, 8.8, 11.2, 13.6, 16.0, 18.35, 21.55):
        i0 = int((st - 0.25) * sr)
        i1 = min(n, i0 + int(0.6 * sr))
        seg_n = rng.standard_normal(i1 - i0)
        smooth = np.convolve(seg_n, np.ones(12) / 12, mode="same")
        e = np.sin(np.linspace(0, np.pi, i1 - i0)) ** 2
        out[i0:i1] += 0.35 * (seg_n - smooth) * e
    # sparkle on the CTA
    for j, f in enumerate([1318.5, 1568.0, 1975.5, 2349.3, 2637.0]):
        add_tone(f, 22.1 + j * 0.07, 0.004, 0.5, 0.06, 5)
    # intro sound design from the app (first seconds)
    with wave.open(intro_wav) as w:
        intro = np.frombuffer(w.readframes(w.getnframes()), dtype=np.int16).astype(np.float64) / 32768
    m = min(len(intro), n)
    out[:m] += intro[:m] * 0.9
    out = out / max(1e-6, np.max(np.abs(out))) * 0.9
    pcm = (out * 32767).astype(np.int16)
    with wave.open(path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(sr)
        w.writeframes(pcm.tobytes())


def main():
    root, screens, out_path = sys.argv[1], sys.argv[2], sys.argv[3]
    tmp = tempfile.mkdtemp(prefix="madak_promo_")
    audio = os.path.join(tmp, "audio.wav")
    make_audio(audio, os.path.join(root, "core/designsystem/src/main/res/raw/madak_intro.wav"))
    ff = subprocess.Popen(
        ["ffmpeg", "-y", "-loglevel", "error", "-f", "rawvideo", "-pix_fmt", "rgb24", "-s", f"{W}x{H}", "-r", str(FPS), "-i", "-",
         "-i", audio, "-c:v", "libx264", "-preset", "medium", "-crf", "19", "-pix_fmt", "yuv420p",
         "-af", "loudnorm=I=-16:TP=-2:LRA=11", "-c:a", "aac", "-b:a", "192k", "-ar", "44100", "-shortest", "-movflags", "+faststart", out_path],
        stdin=subprocess.PIPE,
    )
    with Pool(os.cpu_count(), initializer=init_worker, initargs=(root, screens)) as pool:
        for k, frame in enumerate(pool.imap(render, range(N_FRAMES), chunksize=4)):
            ff.stdin.write(frame)
            if k % 150 == 0:
                print(f"frame {k}/{N_FRAMES}", flush=True)
    ff.stdin.close()
    ff.wait()
    print("done", out_path)


if __name__ == "__main__":
    main()
