# Mottainai Customer App

Native Android customer application for the Mottainai ecosystem. Layouts, text, forms, controls, icons and illustrations are implemented with Android views and drawable XML; the application ships no SVG files or SVG rendering dependency. Product copy remains in Brazilian Portuguese. Technical documentation and source identifiers are English.

The [App Cliente / Ready for Dev](https://www.figma.com/design/DFHemxAp7isLLKf33YQve5/Mottainai---2%C2%B0-Ano?node-id=2005-3809) interface includes 115 screens and panels in both light and dark themes (230 XML variants) within one application.

## Project structure

The application follows the package structure already present in the repository:

- `activities`: onboarding, assistant and offer detail entry points.
- `fragments`: primary screen fragments and shared XML screen binding.
- `adapters` and `models`: list/chat presentation and existing domain objects.
- `repository`: bundled screen/prototype definitions and existing mock repositories.
- `utils`: local session preferences, form controls, typography and empty-state binding.
- `widgets`: reusable artwork containers, SVG images and maps.
- `res/layout`, `res/layout-night`, `res/navigation`: native screens and the AndroidX Navigation graph.
- `res/drawable`: native vector/shape XML for icons, branding and illustrations. `res/font` and `assets/licenses` contain DM Sans and its license.

## Current behavior and integration limits

Onboarding, local forms/preferences, theme switching, photo selection/capture and interactive OpenStreetMap viewing are available. The app uses native Android permissions; locating the device requires authorization. Panels dim the actual underlying screen. Chat labels identify the customer as “Você” and the assistant as “Lô”.

The customer API is not ready. Authentication remains a local session; offers, partner stores, balances, savings and history show empty/unavailable states. Google OAuth, email delivery, remote account synchronization, AI replies, offer notifications and geofencing require future services. Granting notification permission does not implement delivery. The assistant clearly states that it is not connected.

Figma fixture data is available only in explicit debug visual previews. Release builds ignore `design_preview`. Prototype success states are not evidence of server operations.

## Build and checks

Use the Android SDK and a JDK compatible with the existing Gradle/Android plugin versions:

```powershell
.\gradlew.bat --no-daemon testDebugUnitTest lintDebug assembleDebug
```

There are currently no JVM unit test sources; the unit-test task reports `NO-SOURCE`. Instrumented tests require an emulator or dedicated test device. Set `ANDROID_SERIAL` to the emulator before running:

```powershell
.\gradlew.bat --no-daemon connectedDebugAndroidTest
```

Instrumented tests may reset application data. Never target a user's personal device unintentionally.

See [implementation details](docs/figma-implementation.md), [phone corrections](docs/phone-corrections.md), [visual review](docs/visual-review.md), and the [refactor plan and evidence](docs/refactor-plan.md).