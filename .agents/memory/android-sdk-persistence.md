---
name: Android SDK persistence
description: Why this imported Android project keeps its SDK outside the runner home
---

Keep downloaded Android SDK tools in an ignored workspace directory rather than depending on a one-time installation under the runner's home directory.

**Why:** An earlier SDK installation under the runner home disappeared after a workspace mode/environment switch, even though project files persisted. This caused a subsequent build to start without its required Android platform.

**How to apply:** For future Android build setup, retain an idempotent project-local bootstrap step and avoid assuming that `$HOME` survives an environment transition.