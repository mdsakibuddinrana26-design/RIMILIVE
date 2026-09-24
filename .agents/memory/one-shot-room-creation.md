---
name: One-shot room creation navigation
description: Avoid replaying a pending room-creation action when revisiting Party
---

Treat Create Room as a one-shot navigation intent, not persistent Party screen state. Consume it before starting asynchronous room creation, and never rerun it merely because the Party tab was recomposed or revisited.

**Why:** A pending create intent can survive after leaving a room; tapping Home then returns to Party and unexpectedly starts room creation again. This feels like a wrong Home click target even when the bottom navigation callback is correct.

**How to apply:** When adding creation, join, or similar room-entry intents, check for stale pending events across Home → Party → Room → Back → Home. Keep return navigation separate from explicit room entry.