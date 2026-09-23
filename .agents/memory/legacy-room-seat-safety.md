---
name: Legacy room seat safety
description: Data-preserving treatment of occupied seats from older room layouts
---

Convert legacy room layouts in the displayed and joinable seat model, but do not automatically delete seat documents outside the new layout. A returning occupant may still need to release their former seat when moving or leaving.

**Why:** Silently deleting old Firestore seats during a layout conversion can disrupt active members or discard user data, even when those seat numbers no longer appear in the UI.

**How to apply:** For future layout migrations, filter unsupported positions from discovery and admission, retain the current user's old seat identifier for cleanup on an intentional move or leave, and use a separately designed migration for any eventual server-side cleanup.