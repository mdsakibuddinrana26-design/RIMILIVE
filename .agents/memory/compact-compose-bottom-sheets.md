---
name: Compact Compose bottom sheets
description: Why Android Compose modal sheets need explicit full expansion on compact portrait screens
---

Compose Material3 modal bottom sheets can start partially expanded on a compact portrait screen, leaving footer actions offscreen even when the content is scrollable.

**Why:** A 360 × 640 dp Robolectric room-menu check found bottom actions not displayed after scrolling; opting out of partial expansion made all entries reachable.

**How to apply:** For dense room panels, use a fully expanded modal sheet state and scrollable content; verify bottom entries on compact portrait sizes.