"""Procedurally synthesises the Madak splash sound design (timed to the motion splash)."""
import sys, wave, numpy as np
SR = 44100; DUR = 3.6
t = np.arange(int(SR*DUR)) / SR
out = np.zeros_like(t)
rng = np.random.default_rng(7)
def env(start, attack, decay, shape=4.0):
    x = t - start
    e = np.where(x < 0, 0, np.where(x < attack, x/attack, np.exp(-(x-attack)*shape/decay)))
    return e
def tone(freq, start, attack, decay, amp, shape=4.0, harmonics=((1,1.0),)):
    e = env(start, attack, decay, shape); s = np.zeros_like(t)
    for h, ha in harmonics: s += ha*np.sin(2*np.pi*freq*h*(t-start))
    return amp*e*s
# 1) whoosh: band-passed noise sweeping up (0.0 - 0.75s)
noise = rng.standard_normal(len(t))
def onepole(x, a):
    y = np.zeros_like(x); acc = 0.0
    for i in range(len(x)):
        acc += a[i]*(x[i]-acc); y[i] = acc
    return y
seg = t < 0.9
cut = np.clip(0.02 + 0.25*(t/0.75), 0.02, 0.3)
lp = onepole(noise*seg, cut*seg + 1e-6)
hp = lp - onepole(lp, np.full_like(t, 0.01))
wh = np.where(t<0.9, np.sin(np.pi*np.clip(t/0.85,0,1))**2, 0)
out += 0.55*hp*wh
# 2) low warm boom when chef lands (~0.55s)
out += tone(55, 0.50, 0.01, 0.9, 0.55, 5, ((1,1),(2,0.35)))
# 3) spice sprinkle ticks (0.8 - 1.6s)
for i in range(26):
    st = 0.8 + rng.random()*0.8; f = 2600 + rng.random()*3400
    out += tone(f, st, 0.002, 0.06, 0.06 + rng.random()*0.05, 6)
# 4) soft pop/thump when the wordmark lands (~1.45s)
out += tone(140, 1.42, 0.005, 0.25, 0.4, 6, ((1,1),(1.5,0.3)))
# 5) leaf pluck (~1.85s) - plucked string glide
x = t - 1.85; pluck = np.where(x>0, np.sin(2*np.pi*(880*x + 120*x*x)) * np.exp(-x*7), 0)
out += 0.22*pluck
# 6) shimmer sparkle (2.05 - 2.6s): fast rising arpeggio
notes = [1318.5, 1568.0, 1975.5, 2349.3, 2637.0, 3136.0]
for i, f in enumerate(notes):
    out += tone(f, 2.05 + i*0.07, 0.004, 0.5, 0.07, 5, ((1,1),(2,0.2)))
# 7) final warm chord (D major add9) 2.35s
for f, a in [(146.8,0.18),(220.0,0.14),(293.7,0.13),(370.0,0.10),(440.0,0.08),(659.3,0.05)]:
    out += tone(f, 2.35, 0.08, 1.25, a, 3.2, ((1,1),(2,0.25),(3,0.1)))
# simple stereo-free reverb: feedback comb
d = int(0.083*SR); rev = out.copy()
for k in range(1,6): rev[d*k:] += out[:-d*k] * (0.35**k)
out = 0.75*out + 0.25*rev
# fade tail, normalise
out *= np.where(t > DUR-0.4, (DUR-t)/0.4, 1)
out = out / np.max(np.abs(out)) * 0.85
pcm = (out*32767).astype(np.int16)
with wave.open(sys.argv[1], 'wb') as w:
    w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR); w.writeframes(pcm.tobytes())
print('ok', len(pcm)/SR)
