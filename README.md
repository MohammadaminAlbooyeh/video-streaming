# BolloFacile

Calcolatore bollo auto, superbollo, revisione e sanzioni per l'Italia. App Android nativa scritta in Kotlin + Jetpack Compose.

## Funzionalità

- **Calcolo Bollo**: Nazione, regioni, superbollo (>185 kW/250 CV), esenzioni veicoli storici/ibridi/elettrici
- **Superbollo**: Calcolo automatico per potenze superiori a 185 kW
- **Revisione**: Scadenze 4+2 anni, veicoli storici, esenzioni
- **Sanzioni**: Ravvedimento operoso con calcolo interessi e sanzioni ridotte
- **Veicoli salvati**: Database Room con storico pagamenti (Pro)
- **Promemoria locali**: AlarmManager per scadenze bollo/revisione
- **Acquisti in-app**: Sblocco funzionalità Pro (storico, export, niente ads)
- **Consenso UMP**: GDPR/CCPA compliance per AdMob
- **Tema Material 3**: Light/Dark/System

## Architettura

```
app/
├── data/           # Room, Repository, DataStore
├── domain/         # Use Cases (business logic pura)
├── rules/          # Regolamenti 2026 (Kotlin puro, no Android)
├── reminder/       # AlarmManager + Notification
├── billing/        # Play Billing Library
├── ads/            # AdMob + UMP Consent
├── ui/             # Compose screens, ViewModels, Navigation
└── util/           # Formattazione, date, share
```

## Requisiti

- Android Studio Ladybug | 2024.2.1+
- JDK 17
- Android SDK 35
- Min SDK 24 (Android 7.0)

## Build

```bash
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

## Varianti

- `debug` - Con logging, test ads
- `release` - ProGuard/R8, signing config da `keystore.properties`

## CI

GitHub Actions: build, test, lint ad ogni push/PR.

## Licenza

Proprietario - Tutti i diritti riservati.

## Privacy

Vedi [PRIVACY_POLICY.md](PRIVACY_POLICY.md)