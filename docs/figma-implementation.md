# Customer layout implementation

## Scope

The reference is [App Cliente / Ready for Dev](https://www.figma.com/design/DFHemxAp7isLLKf33YQve5/Mottainai---2%C2%B0-Ano?node-id=2005-3809). There are 115 screen/panel definitions with their own original light and dark contexts, represented by 230 XML layouts. Phone feedback takes precedence over simulated prototype behavior.

The Java/XML application keeps the repository's layer packages, existing Activity entry points, primary Fragment classes, adapters/models, and AndroidX Navigation graph. `MainActivity` hosts `NavHostFragment`; full-screen transitions use `NavController`, while panels use a separate overlay container to retain the visible underlying destination. `ScreenFragment` shares form and prototype-action binding across XML variants. `ScreenRepository` loads offline screen definitions and prototype links. Source Figma node IDs and Portuguese labels remain metadata/localization, not Java identifiers.

Reusable drawing containers live in `widgets`; form, typography and empty-state helpers live in `utils`. `SessionManager` owns local session/theme preferences and preserves previous preference keys, including migration of the original onboarding flag. `ChatAdapter` and `ChatMessage` render both RecyclerView messages and the scrolling assistant transcript.

## Resources and responsive behavior

Layouts use native flow, scrolling, weighted columns, bounded width and system/keyboard insets. Proportional coordinates are limited to artwork composition. Font axes are applied through the TextView API supported by the minimum Android version. Layout filenames use English lower_snake_case names, with existing primary names such as `fragment_home` retained.

Icons, logos, illustrations and masks are native Android drawable XML. There are 171 vector definitions, with standard `AppCompatImageView` call sites and circular Material `ShapeableImageView` photo placements. Four photographic references remain raster images; product imagery and user photos remain data. No screen, form, button or text block is flattened into an image.

The SVG parser and rendering dependency have been removed. Mask composition uses native `Drawable` objects; small decorative glows use a native alpha-mask blur rendered at the actual view resolution, compatible with Android 8. Screen layouts use direct, compiled `R.layout` resource references through `ScreenRepository`, matching the existing Android resource pattern.

The XML files are the editable source of truth. Export/download/generation tools are not part of the delivery. Original design references and temporary migration/verification helpers remain only in the ignored working cache. The normal Android build has no Python, Node, Figma cache or image download requirement.

This does not guarantee pixel identity across every Android version, density, device or accessibility setting. Native renders must be checked against the design and previous renders before delivery.

## Integration boundaries

The user confirmed that the customer API is not ready. Production navigation shows empty states instead of fictional commercial values. Account edits, photos, region and consent remain local. Photo selection uses Android camera/gallery contracts and respects EXIF orientation. OpenStreetMap loads a real map; device centering requires location permission and never fabricates a partner marker. External navigation uses Android's geographic intent with a web fallback.

Google OAuth, email, server authentication/account synchronization, AI, offer notification delivery and geofencing are not implemented. The assistant states that it is disconnected. Debug visual fixtures and prototype success screens are not remote-operation confirmations.

See [phone corrections](phone-corrections.md) and [refactor evidence](refactor-plan.md) for validation results and remaining limits.