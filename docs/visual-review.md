# Visual review

The application has 115 native screen/panel definitions and two original theme variants per definition. Comparison inputs and instrumented renders remain in the ignored `.design-cache/visual-review` directory. Stale preview images from the earlier prototype-only implementation are not distributed as current product evidence.

`DesignLayoutTest` measures 460 combinations: 115 screens at 320/600 dp, light/dark themes, font scale 130%. `DesignVisualReviewTest` renders all 230 variants through the actual Activity and records text geometry and scroll endings. `DesignFlowTest` exercises prototype transitions; `ClientCorrectionsTest` checks production empty states and the user's annotated corrections. `NavigationStructureTest` checks graph destinations, recreation, panel dismissal order and previous onboarding preference compatibility.

System-window screenshots require the app window to have focus, so a System UI ANR overlay cannot silently become an accepted capture. View-render captures omit Android system bars and other applications. Automated geometry checks do not establish pixel identity or replace accessibility and physical-device review.

Original references and temporary comparison scripts remain in the ignored validation cache. Native XML is maintained directly; the application has no export/import pipeline dependency. Image difference measurements are review aids, not acceptance thresholds.

See [refactor-plan.md](refactor-plan.md) for the latest completed commands and actual results. Earlier instrumented results are historical evidence only; interrupted runs are not described as passed.
NativeResourcesTest verifies that the built APK contains no SVG exports or SVG rendering library and that screen image views use native Android drawables.

Latest native comparison: 268 matching capture dimensions; worst mean absolute RGBA difference 0.591/255 and less than 0.89% of pixels over a 16-channel-level difference. Both light and dark variants were rendered. See the validation evidence in refactor-plan.md.
