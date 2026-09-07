# Changelog

- Add Android 17 local-network runtime permission handling and regression coverage.

## 2026-09-07 — Android 17 local-network permission

- Declared the API 37 `ACCESS_LOCAL_NETWORK` runtime permission required for NsdManager discovery/advertising and direct local TCP/UDP traffic.
- Added it as a mandatory Android 17 onboarding requirement, with an explicit user-facing explanation; denial now follows the existing launcher and Quick Settings tile permission gates instead of starting the normal receiver path without LAN access.
- Added API-level and manifest regression coverage. XML parsing, source assertions, and diff checks passed; Android compilation and on-device Android 17 UI validation were not run in this change.

## 2026-08-15 — Exact APK-derived Tap to Share UI demo

- Replaced the reconstructed contact motion with the Pixel GMS 26.30.32 Material modal-sheet owner, state split, and hide-before-callback lifecycle recovered from `ContactExchangeChimeraActivity` and its Pixel renderer.
- Replaced the reconstructed photo animation placement with Quick Share's owning unified branches: the exact bundled scanner and receive-ready JSON payloads at their source-owned 60 dp and 24 dp sizes, a top-pivot target-list reveal, and the exact 500 ms `(0.65, 0, 0.35, 1)` progress easing.
- Removed `transfer_success_lottie` after its actual owner proved to be eSIM transfer rather than Quick Share; Quick Share completion remains its unified `Received` card/status state.
- Preserved the existing two-part production edge glow and kept all contact/photo data synthetic. The isolated `.uidemo` package starts no NFC, radio, discovery, provider, Name Card, or transfer path and retains only `VIBRATE`.
- `ktlintCheck`, `detekt`, and `assembleUiDemo` passed. Runtime UI playback was intentionally not used as source-fidelity evidence and remains unverified for this build.
- Verified the ordinary debug variant after enabling the module-wide Compose compiler. A BOM-managed `compileOnly` runtime satisfies compilation without defining Compose packages in the ordinary debug APK; full Compose/Lottie packaging remains `uiDemo`-only.
- Canonical demo artifact: `bada-fork-uidemo.apk` (29,265,815 bytes; SHA-256 `4ba8eb9e5b948881dc812f2e77b56903175abfd2ed4779425fc50009739091e1`).

## 2026-08-14 — Google Tap to Share file handoff

- Added an independent Google-compatible Tap to Share file-transfer path using the primary Gesture Exchange AID, exact Noise handshake, encrypted protobuf handover, and live Wi-Fi Direct credentials.
- Routed the preconnected Wi-Fi Direct stream into SuperDrop's existing outbound/inbound transfer, consent, progress, and teardown engines.
- Added a separate Google Tap to Share setting and HCE service. The proprietary Name Card feature remains SuperDrop-to-SuperDrop only and was not replaced.
- Added the mapped bezel edge-light and haptic states plus bounded, automatically uploaded semantic diagnostics.
- Preserved the legacy Quick Share NFC path as the fallback when the new Google-compatible setting is disabled.
- Added ordered Android Share Sheet URI intake, first-class UTF-8 text/link payloads, exact-length staging for unknown-size providers, storage/stall limits, and terminal cleanup.
- Added one-role/one-tag session gating, radio-readiness arming, strict handover validation, API-37 capability-gated NFC policy, reduced-motion rendering, and replayable private diagnostic fallback.
- Added deterministic Noise corruption/replay/limit/exhaustion coverage and a complete outbound text-to-inbound Quick Share loopback test.
- Raised compile/target SDK to 37 while retaining minSdk 24; API-37 references remain isolated behind runtime gates.
- Canonical debug artifact: `bada-fork-debug.apk` (SHA-256 `2523cb55b93a1cb82097a5a7da6c817c3bd506fa0330f1b40ef91c3de3c7e762`).
