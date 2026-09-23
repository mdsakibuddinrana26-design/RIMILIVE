---
name: Party Room background preference
description: Scope and default behavior for selectable room artwork
---

Party Room background artwork is the viewer's local visual preference, scoped to the signed-in user on that device. The original orange gradient remains the default and must stay selectable; changing artwork should not update shared room data or affect other viewers.

**Why:** The background feature was requested as an individual visual choice, not a host-controlled room setting. Sharing it through Firestore would unexpectedly change other users' rooms and create unnecessary writes.

**How to apply:** When adding or changing room backgrounds, preserve the original room UI as a layer above the artwork, use aspect-ratio-preserving crop, and keep the local selection across leaving and returning to a room. Do not create a second competing background preference.