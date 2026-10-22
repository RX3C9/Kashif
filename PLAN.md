# PLAN.md — Truecaller alternative (name: Kashif — كاشف)

## 1. Positioning
Free, ad-free, privacy-first caller ID & spam blocker for Egypt/Arab world. Never uploads the phonebook. All Truecaller Premium features free. UI follows Truecaller's familiar layout and colour logic (see docs/DESIGN.md) with our own brand.

## 2. Architecture
```
Android app (Kotlin, Compose, M3, minSdk 29, target 35)
 ├─ app                 : nav, theme, DI (Hilt), roles onboarding
 ├─ core-data           : Room DB, repositories, hashed-number API client, offline spam DB sync
 ├─ feature-dialer      : default dialer (InCallService), call UI, T9 AR/EN, call log, contacts
 ├─ feature-callerid    : CallScreeningService, overlay popup, lookup, block rules, SMS filter
 ├─ feature-ai          : on-device TFLite scam scorer, Egyptian-Arabic patterns, transcripts, screening assistant
 └─ admin-web           : Next.js dashboard (moderation, reports, businesses, memberships)
Backend: Supabase (Postgres + RLS + Edge Functions)  [Firebase as fallback]
```
Call path: ring → `CallScreeningService.onScreenCall` → local rules → local spam DB (bloom filter + Room) → optional online lookup by **SHA-256(pepper ‖ E.164)** prefix (k-anonymity: send first 5 hex chars, match locally) → allow / silence / reject → overlay / full-screen UI.

## 3. Privacy design
- No contact upload, ever. Contacts used only on-device.
- Lookups use hash-prefix k-anonymity; server never sees full number of who you look up.
- Community reports are opt-in, stored as hashed numbers + category + coarse region; reporter id is salted per-report.
- Names for unknown numbers come only from: community-submitted *business* names (verified) and user-chosen public profiles (opt-in). No scraped phonebooks.
- Incognito lookup = no history kept, no "who viewed" signals sent.
- Call recordings/transcripts stay on device; AI runs on-device by default; cloud AI only with explicit opt-in.
- Data export + delete account in-app. Egypt PDPL (Law 151/2020) + GDPR aligned.

## 4. Data model
Local (Room): `CallLogEntry(id, e164, ts, dir, duration, simSlot, verdict, label)`, `Contact cache`, `BlockRule(id, type[exact|prefix|country|regex|unknown|hidden], pattern, action, schedule, simSlot)`, `SpamEntry(hashPrefix, hash, score, category, reports, updatedAt)`, `Note(e164, text, remindAt)`, `SmsVerdict`, `Transcript(callId, text, summary)`.
Server: `profiles(user_id, username, display_name, public_number_hash?, tier)`, `numbers(hash, score, category, reports_count, last_report)`, `reports(id, number_hash, category, comment, region, created_at, reporter_salted)`, `businesses(id, number_hash, name, logo, verified, category, docs_status)`, `abuse_reports`, `memberships`, `admin_audit`.

## 5. Milestones
- [x] M0 Plan, design spec, reuse map, scaffold (this commit)
- [~] M1 MVP: default-dialer role, in-call UI (Truecaller-style colour states), call log, T9 dialer, CallScreeningService, block rules, offline spam DB, one-tap report, overlay caller-ID, Arabic/English
  - [x] 1a Core logic: libphonenumber normaliser (EG default, Arabic-Indic digits), hashed number ids, Room DB (BlockRule/SpamEntry/LocalReport/CallNote + DAOs), block-rule matcher (exact/prefix/country/regex/hidden/not-in-contacts), offline spam seed parser, SpamRepository one-tap report (local), ScreeningDecider + real CallScreeningService (contacts check on-device, 3s fail-open), VerdictCache → coloured in-call screen, T9 AR/EN matcher; unit tests; CI runs testDebugUnitTest
  - [x] 1b Default-dialer role request onboarding (RoleManager ROLE_DIALER + ROLE_CALL_SCREENING), permissions flow
  - [ ] 1c Call log tab from CallLog provider with verdict colouring + one-tap report/block sheet
  - [ ] 1d Dialpad screen (T9 results) + outgoing calls via TelecomManager.placeCall; in-call controls (mute/speaker/hold/keypad)
  - [ ] 1e Block-rules management screen; seed import of bundled offline spam pack
  - [ ] 1f Caller-ID overlay popup (SYSTEM_ALERT_WINDOW) when not default dialer; full AR/EN strings
- [ ] M2 Backend: Supabase schema + RLS, hash-prefix lookup, reports, business directory, Google + username/password login, admin-web v1
- [ ] M3 AI: Egyptian scam-pattern model (fake bank/InstaPay/prize/“مكالمة من خدمة العملاء”), SMS phishing scanner, anti-spoof warning, after-call notes/summaries
- [ ] M4 Assistant: AI screening assistant answering in Egyptian Arabic with live transcript; recording+transcript (where legal); announce caller
- [ ] M5 Family protection, DND schedules + VIP, dual-SIM rules, widgets, Wear OS, themes & caller-screen designer, heatmap/stats, encrypted backup
- [ ] M6 Polish, screenshots, release on GitHub/F-Droid style, landing page

## 6. Feature list (all free)
Live caller ID (community + business directory) · Egyptian scam-pattern AI · AI call screening assistant (Egyptian Arabic, live transcript) · Call recording + transcript + summary (where legal) · Auto-block by country/prefix/pattern · SMS spam & phishing link scanner (OTP fraud warning) · WhatsApp/Telegram lookup via share sheet · Reverse lookup without uploading contacts · Privacy report of who flagged your number (replaces "who viewed you") · Verified business badges · Emergency mode & family protection (elderly/kids) · Smart dialer T9 AR+EN · Encrypted contacts backup · Call reminders & notes · Announce caller name · Dual-SIM rules · Scheduled DND + VIP allowlist · Offline spam DB updates · Spam heatmap/stats · Themes & caller-screen designer · Ad-free forever · Incognito lookup · One-tap report · Anti-spoofing warning · Wear OS · Widgets · Admin moderation panel · Ghost call · Quick-reply on reject.

## 7. Legal / clean-room
No decompiling or copying Truecaller APK/code/data/assets. Design reference = public screenshots only (layout/colour logic). Reuse only permissive or license-compatible open source (see docs/REUSE_MAP.md). Call recording only where Android + local law permit; Egypt requires consent → in-app beep/announce option.
