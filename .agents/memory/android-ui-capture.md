---
name: Android UI capture in Robolectric
description: Limits of image capture during JVM Compose tests in this runner
---

Compose semantics visibility assertions succeeded for a compact portrait Robolectric UI test, but `captureToImage()` timed out waiting for a redraw. A default Roborazzi `captureRoboImage()` call completed without emitting a PNG; a Gradle property attempt was cached and did not establish a working recording path.

**Why:** A passing Compose UI test can establish that elements are displayed within the test viewport, but it is not proof of pixel-level similarity to a reference screenshot or behavior on a physical phone.

**How to apply:** Use visibility assertions for layout regressions, and only claim a visual screenshot comparison after an actual rendered image is obtained. Do not keep retrying the same image capture method without a new configuration or runtime.