# Client PR Structure Refactor Implementation Plan

**Goal:** Integrate the Figma interface into the architecture of `origin/main` without changing its screens or adding backend functionality.

**Architecture:** Keep the existing Java/XML application and its `activities`, `fragments`, `adapters`, `repository`, `models`, `utils`, and `widgets` packages. Restore Android Navigation for full-screen destinations; retain a separate overlay container so panels display the previous screen. Reuse `SessionManager`, `ChatAdapter`, and `ChatMessage` rather than duplicating their responsibilities.

**Tech stack:** Existing AndroidX Navigation, Java 11 source compatibility, native Android drawable XML and the existing Material components. No new dependencies.

**Spec:** User-provided PR refactor request (2026-10-26), PR #19.

## Constraints and impact

- Preserve `main` history, Portuguese product localization, local preference keys, API-unavailable states, themes, photo handling, maps, and accessibility fixes.
- Technical code, documentation, PR text, and commit messages must be English.
- Every new PR commit must change at most 1,500 files; use substantially smaller coherent commits.
- Preserve the original PR commit in a backup branch. Close PR #19 and publish a replacement from `feat/client-native-xml` with a normal push; do not rewrite the old remote branch.
- No data schema, server contract, framework version, or unrelated open PR changes.

## Tasks

- [x] Audit the main tree and PR call paths; identify unused/duplicate resources.
- [x] Implement artwork as native Android vector/drawable XML, remove SVG resources and the SVG renderer, update references, and give screen layouts English semantic names.
- [x] Move repositories, reusable widgets, and form/typography helpers into established packages; restore `NavHostFragment`/`nav_graph.xml` and centralize local preferences in `SessionManager`.
- [x] Reuse chat adapter/model rendering and maintain author labels, header action, keyboard behavior, and theme styling.
- [x] Translate technical documentation; retain localized product copy and Figma source labels as data.
- [x] Validate resource links, Java compilation, lint, navigation/back-stack/overlay behavior, layouts and existing instrumented flows.
- [x] Rebuild the PR history into resource, runtime, testing, and documentation commits. Check file counts and build intermediate application states.
- [x] Review the final diff and prepare the replacement PR in English using the existing template; report actual results.

## Review focus

- Restoring state after theme changes must preserve the current destination.
- Back must dismiss the overlay before navigating away from its underlying screen.
- Local account/onboarding state must survive the refactor; never migrate or expose passwords.
- All 230 layout variants must resolve their artwork and custom widget classes.
- Chat author labels and compact bubbles must work at 320 dp with enlarged fonts.

## Execution record

Initial audit: PR #19 has one commit (`9e5ed7b`) changing 1,527 files. `origin/main` is `c2d20ff`; no other author commits or human reviews are present on the PR. A local backup branch preserves the original commit. The existing reference uses Java/XML, AndroidX Navigation, layer packages, lower_snake_case layouts, and English technical identifiers.


Refactor result before publication: all 230 variants use native Android views and drawable XML, with four photographic references retained as images. The runtime uses the original Navigation graph and existing session/chat abstractions; obsolete export copies and duplicate documentation inventories are excluded. No backend contract or application identifier was changed.

User clarification: exported SVG files must not serve as the implementation. The final interface uses native XML views, vector paths, shape appearances and drawable masks; no SVG files, parser or export/download tools are shipped. Temporary migration scripts remain outside the deliverable.

## Validation evidence

- Native implementation: `testDebugUnitTest lintDebug assembleDebug connectedDebugAndroidTest` succeeded; all 28 instrumented tests passed. JVM unit tests have no source files.
- After removing unreachable code/resources: seven selected instrumented tests passed, including every themed screen render, 460 width/theme combinations, navigation and native-resource guards.
- Compared 268 previous SVG-rendered captures with native XML captures: zero image-size mismatches. Worst mean absolute RGBA difference was 0.591 on a 0–255 scale; the largest fraction of pixels differing by more than 16 channel levels was below 0.89%. This is close visual agreement, not pixel identity.
- Cleanup removed 38 unused legacy files and 25 unused resource values, in addition to retiring exported SVGs and all import/download tooling. Four photographic references remain images.
- The API is unavailable. Production empty states and disconnected assistant feedback remain explicit; local UI tests do not establish server integration.

## Final commit structure

The replacement branch starts at the unchanged `origin/main` history. Its four commits change 288, 298, 7 and 5 files respectively, below the 1,500-file limit per commit:

1. Native XML artwork, typography/form helpers and reusable drawing containers.
2. Client screens, existing Navigation/session/chat integration, and removal of unreachable legacy implementation.
3. Six instrumented test classes and ignored local validation cache configuration.
4. English implementation, corrections and validation documentation.

Final `testDebugUnitTest lintDebug assembleDebug` completed successfully after the final cleanup: zero lint errors, 523 other lint warnings and zero unused-resource warnings. The warnings are not claimed to be resolved. No new workflow or runtime dependency was added.

The first resource commit (2b94195) was exported into a clean snapshot and independently passed assembleDebug. Legacy screens still compile before the integration commit.
