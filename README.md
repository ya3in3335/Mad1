<div align="center">

<img src="docs/brand/yourtech-icon-512.png" width="120" alt="YOURTECH SYSTEME logo" />

# YOURTECH SYSTEME — Smart Security Solutions

**An Android app for a security-systems company, plus a separate secure Admin app (لوحة التحكم).**

Kotlin · Jetpack Compose · Material 3 · MVVM · Hilt · Room · Retrofit · Coil · Navigation Compose

العربية (RTL, default) · Français · English

</div>

---

## Contents

1. [Overview](#overview)
3. [Features](#features)
4. [Content and verification rules](#content-and-verification-rules)
5. [Tech stack](#tech-stack)
6. [Project structure](#project-structure)
7. [Local development](#local-development)
8. [GitHub Actions and downloading the APKs](#github-actions-and-downloading-the-apks)
9. [Release builds and secrets](#release-builds-and-secrets)
10. [Admin security](#admin-security)
11. [Backend integration (required for production)](#backend-integration-required-for-production)
12. [Company configuration checklist](#company-configuration-checklist)
13. [Credits and licences](#credits-and-licences)

---

## Overview

| App | Module | Application ID | Purpose |
|---|---|---|---|
| **YOURTECH SYSTEME** | `:app` | `com.yourtech.systeme` | Customer app: solutions, equipment catalog, installation, maintenance and quotation requests with tracking, portfolio, contact |
| **YOURTECH Admin** | `:admin` | `com.yourtech.systeme.admin` | Back office: requests, appointments, products, inventory, categories, services, portfolio, promotions, testimonials, business info, accounts, audit log |

Both apps share the data layer (`:core:data`) and the design system (`:core:designsystem`). The design system holds the official logo, the brand palette and the motion-graphics splash. The palette is deep navy `#071426`, security blue `#176BFF`, electric cyan `#00D9FF` and dark surfaces `#101F35`.

**Company (default data, editable in Admin → Business info):**
- Address: YOURTECH SYSTEME, Bir El Djir, Oran, Algeria. GPS: 35.7190252, -0.5664739.
- Phone / WhatsApp: +213 561 03 41 49.
- Hours: Saturday–Thursday 08:00–17:30. Closed Friday.

## Features

### Customer app
- **Motion-graphics splash.** It draws the official logo: the circuit "Y" is drawn by a scan line, the nodes pulse, then the bar and the wordmark appear over a radar grid, with the tagline "Smart Security Solutions".
- **Home dashboard:**
  - hero section with shortcuts for installation, support, consultation, WhatsApp and call;
  - live open/closed status (Algiers time zone);
  - featured security solutions and equipment;
  - offers, verified testimonials and published projects, each shown only when present;
  - visit card with an offline OpenStreetMap preview.
- **Security solutions:** CCTV, alarms, access control (RFID/NFC and biometrics), smart security, intercom, and installation/maintenance. Each solution has an animated vector illustration, a description, benefits, use cases and specs. Each also has buttons to **request a quote**, **request an installation** or **contact a technician** on WhatsApp.
- **Equipment catalog:**
  - search, category filters and favorites;
  - each product shows its photo, brand/model, specs, price in DZD (or "price on request"), stock status, warranty and installation availability;
  - quote request and WhatsApp inquiry buttons.
- **Requests** (installation, maintenance/support, consultation, quote):
  - validated forms: Algerian phone format, all 58 wilayas with search, commune, address, property type, system or equipment, number of devices, preferred date, notes, and up to 6 photos;
  - tracking timeline: Submitted → Under review → Quotation prepared → Approved → Scheduled → In progress → Completed;
  - cancellation while the request is still Submitted;
  - local notifications.
- **Hand-off to the company:** each request gets a reference such as `YT-INS-261008-1234`. The customer sends it to YOURTECH in one tap as a pre-filled WhatsApp message, and can share the photos too.
- **Portfolio:** only projects marked as both *client-permission confirmed* and *published* appear.
- **Contact:** map, Google Maps directions, WhatsApp, call, email, weekly hours (Friday closed) and social links, each shown only when set.
- Language switch (AR/FR/EN), full RTL, accessible touch targets, skeleton loading and empty states.

### Admin app
- **First launch** forces creation of the owner account. There are **no default credentials**.
- **Roles:**

  | Role | Access |
  |---|---|
  | Owner | Everything |
  | Manager | Everything except accounts |
  | Sales staff | Requests, appointments, products, inventory |
  | Technician | Requests, appointments |

- **Dashboard:** today's requests, awaiting review, open, today's appointments, completed, quick actions and latest requests.
- **Requests:**
  - filter by status and type;
  - customer call/WhatsApp;
  - status transitions (forward only, cancel until work starts) with a note for the customer;
  - appointment date and time, quote amount, assigned technician and company note;
  - timeline.
- **Appointments** grouped by day.
- **Catalog:**
  - products: create/edit/delete, gallery photo, https URL or illustration;
  - inventory: stock status and quantity;
  - equipment categories;
  - **service categories**: trilingual content plus an activation switch, so a service is shown to customers only after the company confirms it.
- **Content:**
  - portfolio: approved photos only; publishing is blocked until client permission is ticked; general location only;
  - promotions with duration;
  - testimonials: hidden until verified;
  - **business info**: validated phone numbers, https-only links, email, coordinates and weekly hours.
- **Settings:** change password, accounts (create with a temporary password, enable/disable, reset), **audit log** and logout.

## Content and verification rules

The apps follow the brief's rules. **Nothing has been invented.**

- **No fake data:**
  - **No products, prices, brands, stock levels or warranties are shipped.** The catalog starts empty and the company fills it in from the Admin app.
  - **No projects, testimonials, ratings or promotions are shipped.**
  - No social links are pre-filled. They appear only once entered in Admin.
- **Services are a proposal.** Six common categories are active. Two of them, *gates and barriers* and *security consulting*, are present but **hidden** until confirmed. The company must review all of them in Admin → Catalog → Services and disable any it does not offer.
- **Visuals:** the app uses animated **vector illustrations** instead of stock photos. The stock photos I found showed specific brands (e.g. Hikvision, ANNKE) or random street cameras, which would suggest a partnership or work that does not exist. Real product and project photos are uploaded from the Admin app.
- **Portfolio privacy:** it never shows live camera feeds, exact addresses, access codes or identifiable customer information. The admin UI reminds staff of this, and the data layer refuses to publish without client permission.
- **Company details:** all of them (name, phone, address, GPS, hours) are configurable data and **must be re-verified before production**.

## Tech stack

| Area | Library |
|---|---|
| Language / build | Kotlin 2.2, AGP 8.13, Gradle 8.14 (version catalog), JDK 17 |
| UI | Jetpack Compose (BOM 2025.10), Material 3, Navigation Compose |
| Architecture | MVVM, Hilt DI, Kotlin Flow / StateFlow, Repository pattern |
| Storage | Room 2.7 (single schema shared by both apps) |
| Network (ready) | Retrofit 2.11 + Gson (`YourTechApi`) |
| Images | Coil 2.7; Photo Picker plus re-encoding through `ImageImporter` |
| Tests | JUnit 4 (data-layer and password-hashing unit tests) |

minSdk 24 · target/compile SDK 36.

## Project structure

```
app/                     Customer app (com.yourtech.systeme)
  feature/home           Home dashboard, open status, visit card, map
  feature/solutions      Security solution list + detail
  feature/products       Catalog, product detail, favorites
  feature/requests       Request form, list, tracking, WhatsApp message
  feature/more           More, contact, about, privacy, notifications, portfolio
  navigation/            NavHost + bottom navigation
admin/                   Admin app (com.yourtech.systeme.admin)
  auth/                  PBKDF2 hashing, accounts, roles/permissions, lock-out
  data/AdminRepository   Permission check → validation → write → audit log
  feature/               Auth, dashboard, requests, catalog, content, settings
core/data/               Room entities/DAOs, repositories, validation, business hours, seed, image importer, API contract
core/designsystem/       Theme, logo, components, illustrations, splash, Tajawal font
docs/brand/              Original logo, launcher icons, font licence
```

## Local development

```bash
# Android Studio Ladybug+ or command line with JDK 17 and Android SDK 36
./gradlew testDebugUnitTest     # unit tests (optional)
./gradlew assembleDebug         # app/build/outputs/apk/debug/yourtech-systeme-debug.apk
                                # admin/build/outputs/apk/debug/yourtech-admin-debug.apk
```

## GitHub Actions and downloading the APKs

`.github/workflows/android.yml` runs on every push and pull request:

1. JDK 17, Android SDK 36, Gradle cache.
2. `assembleDebug` (builds both apps).
3. Uploads the artifacts **`yourtech-systeme-debug-apk`** and **`yourtech-admin-debug-apk`**.

**Download:** GitHub → *Actions* → latest green run → *Artifacts* → download the zip → install the APK. On the phone, allow "install unknown apps".

## Release builds and secrets

The `release` job runs on `main` only when these repository secrets exist. **Nothing secret is stored in the repository.**

| Secret | Content |
|---|---|
| `KEYSTORE_BASE64` | `base64 -w0 yourtech-release.jks` |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

Create a keystore with:

```bash
keytool -genkeypair -v -keystore yourtech-release.jks -alias yourtech -keyalg RSA -keysize 4096 -validity 10000
```

## Admin security

- **No hardcoded credentials.** The owner is created at first launch and later accounts get temporary passwords that **must be changed** at first sign-in.
- **Password hashing:** salted **PBKDF2-HMAC-SHA256** (SHA-1 variant only on API 24–25), 120,000 iterations, constant-time comparison. Policy: at least 8 characters with a letter and a digit.
- **Brute-force protection:**
  - after 5 failed attempts the account locks for 30 s, doubling up to 15 min;
  - unknown usernames get the same response and timing as wrong passwords, so the screen cannot be used to discover usernames.
- **Authorization is checked twice:**
  - at the **route level** (screens hidden or blocked);
  - again in `AdminRepository` before **every write**.
- **Audit log** of logins, failed logins, password changes, account changes and every create/update/delete.
- **Input validation** before any write:
  - names and lengths;
  - Algerian phone numbers;
  - https-only URLs;
  - email;
  - GPS range;
  - price and quantity range;
  - control characters are stripped.
- **Secure image uploads:**
  - Android Photo Picker, so no storage permission is needed;
  - MIME allow-list (JPEG/PNG/WEBP/HEIC) and a 20 MB cap;
  - the image is decoded and re-encoded to JPEG at 1600 px or less, **which removes EXIF and GPS metadata**;
  - files get random names in app-private storage;
  - deletion is restricted to that folder.
- **Device protections:**
  - `FLAG_SECURE`: no screenshots, screen recording or recents preview;
  - **auto-lock** after 5 minutes in the background;
  - backups and device transfer are disabled (`allowBackup=false` plus data extraction rules).

## Backend integration (required for production)

> ⚠️ **Important:** for now each app keeps its own on-device Room database. Products added in Admin do **not** reach customers' phones, and requests submitted by customers do **not** reach the Admin app automatically. Until a backend is added:
> - requests reach the company through the pre-filled **WhatsApp** message (with reference and details) and shared photos;
> - the Admin app works as a single-device back office.

Recommended: **Supabase** (Postgres + Row Level Security + Storage) or **Firebase** (Firestore + Security Rules + Storage + Auth custom claims).

1. Implement `core/data/remote/YourTechApi`, or use the vendor SDKs. The base URL comes from `BuildConfig.YT_API_BASE_URL`; inject it from CI, never commit keys.
2. Make the repositories sync: Room becomes a cache, and the server is the source of truth.
3. Move admin authentication to the backend (Supabase Auth / Firebase Auth). Map `AdminRole` to server claims and enforce the **same permissions in RLS / Security Rules**. Never trust the client.
4. Customers submit requests to a `service_requests` table that only they can read, and staff can read according to role. Store photos in a private bucket with signed URLs.
5. Push status changes with FCM.

## Company configuration checklist

Before publishing, in **Admin**:

- [ ] Business info: verify phone, WhatsApp, address in all 3 languages, GPS, hours, email, social links.
- [ ] Services: confirm each category; disable what YOURTECH does not provide; edit the texts.
- [ ] Equipment categories: adjust to the real catalog.
- [ ] Products: real names, brands, prices, stock and warranty only.
- [ ] Portfolio: only approved photos, with written client permission.
- [ ] Testimonials: only real, verified customers.
- [ ] Create staff and technician accounts with the right roles.

## Credits and licences

- Logo © YOURTECH SYSTEME (supplied by the company).
- Tajawal font, SIL Open Font License 1.1 (`docs/brand/Tajawal-OFL.txt`).
- Map preview © OpenStreetMap contributors (ODbL); the attribution is shown on the map.
- Security illustrations are original vector drawings made in Compose (`SecurityIllustration`).
