# BolloFacile — TODO

Status as of 2026-10-14 (commit `a1f07c4`): project builds (assembleDebug/assembleRelease/lint OK), 38/38 unit tests pass. Remaining steps below.

## Done ✅
- [x] Initial app scaffolding (Kotlin + Jetpack Compose, data/domain/rules/ui architecture)
- [x] Repaired project to a compiling, tested state
- [x] Migrated dates from kotlinx-datetime to java.time (with desugaring)
- [x] Fully rebuilt Gradle config (Gradle 8.8 wrapper)
- [x] 38/38 unit tests green; lint clean
- [x] Export feature and i18n groundwork (values/strings.xml + values-en/strings.xml)
- [x] Privacy policy (PRIVACY_POLICY.md)

## 1. i18n / UI polish
- [ ] Finish the HomeScreen rewrite (Scaffold + stringResource) — stopped mid-way
- [ ] Verify every screen compiles and renders correctly: BolloCalc, Revisione, Sanzioni, VeicoloDetail/Form, Settings, Pro
- [ ] Ensure all hardcoded strings use string resources (IT + EN)
- [ ] Translate all code-level Italian (comments, identifiers, docs) to English

## 2. User-selectable day/night theme
- [ ] Wire `SettingsStore.theme` (0 system / 1 light / 2 dark) into `Theme(darkTheme=...)` in MainActivity — currently it only follows `isSystemInDarkTheme()`

## 3. Instrumented tests
- [ ] Re-run `connectedDebugAndroidTest` on the emulator ("pf" AVD died twice; `waitUntil` added but unverified for AddVeicoloFlowTest)
- [ ] Make tests locale-independent via `targetContext.getString(...)`

## 4. Settings screen
- [ ] Implement stub buttons: "Unlock Pro", "Restore purchases", privacy, licenses
- [ ] Add the navigation callback to the Pro screen

## 5. Business-rules review
- [ ] Review `ravvedimento dataLimite` and the date-dependent regional tests (testRavvedimentoDeadlines may encode wrong arithmetic)

## 6. Release prep
- [ ] Replace test AdMob IDs with real ones
- [ ] Create a real `keystore.jks` and set up env vars / keystore.properties
- [ ] Real launcher icon (current one is a solid-color placeholder PNG)
- [ ] Signed release build + final test on a device
- [ ] Google Play publishing (store listing, screenshots, data safety)
