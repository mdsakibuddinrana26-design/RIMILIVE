---
name: Camera status versus video
description: Distinguishing room camera metadata from actual media capture and switching
---

Keep Settings camera and Front/Back controls visibly unavailable until a real camera capture/RTC pipeline exists. A room member's stored camera status is not proof of live video.

**Why:** Showing an enabled camera switch merely because a user occupies a camera seat can claim success without ever opening the device camera or sending media. Front/Back also requires selecting a physical capture device, not flipping that same status flag.

**How to apply:** Before enabling camera controls, verify permission, capture lifecycle, streaming, and physical device selection are connected end to end. Preserve unrelated room UI behavior while making unavailable states explicit.