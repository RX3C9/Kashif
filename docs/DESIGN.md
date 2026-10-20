# DESIGN.md — UI spec (clean-room, modelled on Truecaller's public UX)

Source: Truecaller's public Play Store screenshots (Oct 2026), saved in `docs/design-ref/` for internal reference only (never shipped).
Rule: copy the **layout, flow and colour logic** 1:1. Never ship Truecaller's name, wordmark, logo, icons, illustrations, fonts or screenshots (trademark / trade-dress). Our brand mark, wordmark and icon set are original.

## Colour logic (the core of the look)
| State | Full-screen gradient (top → bottom) | Usage |
|---|---|---|
| Normal / identified person | `#1565FF` → `#0B2A6B` → `#08152F` | incoming call, caller card |
| Spam / scam | `#FF3B30` → `#C0182A` → `#2A0E1A` (dark navy bottom) | spam calls, scam SMS banner |
| Verified business | `#1FA35B` → `#0E6B3D` → `#0A2A22` | verified business calls |
| Accept button | `#2BB55A` circle, white phone glyph | |
| Decline button | `#FF5A4E` circle, white hang-up glyph | |
| App primary | `#0A84FF` (search bar accent, FAB, links) | |
| Surfaces light | `#FFFFFF`, `#F3F6FB` list bg; dark `#0E1116`, `#161B22` | |

## Screens
1. **Home / Calls tab**: top rounded search pill "ابحث بالأسماء والأرقام…" with avatar left and ⋮ right; promo/protection card (family protection); call-log list (avatar or red shield for spam, name, sub-line: `Scam · 587 spam reports · 11:10 am` in red for spam, call icon right); square blue dialpad FAB bottom-right; bottom nav: Calls · Messages · Contacts · Premium(→"المزايا") · Protection.
2. **Search screen**: back arrow + field + camera(OCR) + location icons; "Search history" list with red shield rows showing "1590 Spam reports".
3. **Incoming call full-screen**: status line "Identified by <Brand>" / "Call from +20 … · Egypt"; big circular avatar (spam = dark-red disk with white shield-!); name 28sp; subtitle tag `Sales · 3390 spam reports`; chip "⚠ Warning: Potential fraud"; pill "View profile"; bottom accept/decline circles; "Message" pill with quick replies sheet (dark rounded rows).
4. **Caller-ID popup (overlay)**: rounded dark-translucent card: number, "<Brand> Caller ID:", emoji tag + label, bottom row [red decline] [blue wide View] [green accept].
5. **Mid-call alert notification**: "Mom is on a call with a potential scammer" + Hang up / Dismiss (family protection).
6. **SMS scam heads-up**: red header bar brand + "SMS · 12:30", sender, body, VIEW MESSAGE | BLOCK.
7. **Verified business**: green gradient, bank glyph in white ring, name + blue verified tick, "View business profile" pill.
8. **Wear OS**: round screen, colour ring by state, big name, accept/decline.

## Typography & shape
- Font: our choice of open font with excellent Arabic: **IBM Plex Sans Arabic** (OFL) + Inter for Latin digits.
- Radius: cards 20dp, pills full, FAB 16dp square-round. Elevation soft, blur on overlays.
- Full RTL support; numbers stay LTR.
