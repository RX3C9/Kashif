# REUSE_MAP.md — open-source references

| Repo | License | What to learn / reuse | Rule |
|---|---|---|---|
| aj3423/SpamBlocker (github) | **MIT** | Kotlin+Compose call/SMS screening: `CallScreeningService.kt`, `PublicSMSScreeningService`, regex rule engine (`RegexTable`, presets), STIR/SHAKEN check (`Stir.kt`), SMS bomb/alert, API-query presets | **Can copy code** with MIT notice kept → best base for rule engine & STIR anti-spoof |
| FossifyOrg/Phone (github) | GPL-3.0 | Full dialer: `CallService` (InCallService), `CallManager`, `CallActivity`, `DialpadActivity` + T9, `SimpleCallScreeningService`, conference calls, call notifications | Study patterns only; copying code forces the whole app to GPL-3.0. Decide with Omar (open-source app is fine → then we can reuse) |
| xynngh/YetAnotherCallBlocker (gitlab) | AGPL-3.0 | Offline crowdsourced DB with delta updates (`DbUpdater`, `YacbDbOpenHelper`), `CallScreeningServiceImpl`, advanced blocking via `CallMonitoringService` | Concept only (AGPL). Its DB is a 3rd-party proprietary dataset → **do not use** |
| bitfireAT/NoPhoneSpam | GPL-3.0 | Wildcard prefix blocking, blocked-call stats | Concept only |

Decision (Omar 2026-10-20): app is **open source, GPL-3.0**. Fossify Phone dialer internals may be reused directly (keep headers); SpamBlocker MIT pieces with notice; YACB concept only.
