# Phone feedback corrections

The user's annotated screenshots override the corresponding prototype simulation. All fixes apply to one Java/XML application with light and dark themes.

## Corrected behavior

- Login validation appears below fields, without a popup covering the password. Password fields have independent visibility controls and no fictional credentials.
- Sign-out uses a readable destructive button. The confirmation and other panels dim the actual underlying screen.
- Notification access uses Android's native permission flow instead of the simulated full-screen permission page.
- Profile images remain circular for square, portrait and landscape inputs. Gallery decoding samples large images and applies EXIF orientation; photo confirmation/save remains local.
- Theme buttons have a consistent minimum touch height and an accessible selected state. Marketing consent uses a real persistent checked checkbox.
- Savings, offer lists/suggestions, loyalty balance/history and profile identity do not display fictional customer data in normal navigation.
- Radius options retain 5, 10, 15 and 20 km independently of the saved selection.
- Stores show a real OpenStreetMap viewport, permission-gated device location and an action to open Maps. No fictional partner pin is placed.
- Points history values align to the right in visual fixtures.
- Chat bubbles fit their content and align by author; sending clears the introductory suggestions. Author labels remain “Você” and “Lô”. The history action sits at the top-right of the header with a minimum 48 dp touch area, including narrow screens and enlarged fonts.
- Conversation history and other panels retain the underlying screen under a transparent dim layer.

## Verification history

Before the structural refactor, instrumented rounds covered 460 layout combinations (115 screens, two widths, two themes, font scale 130%), the phone corrections, prototype transitions and three focused chat checks. An additional full repetition stopped after 20/23 tests without a final report; it is not counted as a completed passing run.

The earlier phone installation used `adb install -r` and returned `Success`, preserving user data. The last pre-refactor chat APK matched the installed APK SHA-256 `c1e8d96027bd6596364ae6f5823b19ba52cda309da4db52cd0301127bc900280`. This hash identifies that earlier build only.

Current refactor results are recorded in [refactor-plan.md](refactor-plan.md). Tests run on an emulator, not on the user's phone. The refactored APK has not been reinstalled on the phone.

## Remaining service limits

The customer API is not ready. Remote authentication, offers, partner stores, loyalty/savings/history, AI replies, notification delivery and geofencing remain pending. Local account/photo storage and native permission grants do not constitute remote integration.