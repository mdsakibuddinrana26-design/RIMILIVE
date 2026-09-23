---
name: Room keyboard stability
description: Why room content must remain outside keyboard-driven layout remeasurement
---

For RIMILIVE's fixed-seat rooms, the IME inset must affect only a separate bottom control dock, not the root seat layout. Request resize-style IME insets while the room is visible and restore the prior window policy afterward.

**Why:** Applying keyboard padding at the root recalculates available room height and moves the approved header and seat geometry when the keyboard appears. A fixed root with a docked control row preserves those positions while keeping the message input directly above the keyboard.

**How to apply:** When changing room footers, verify header and seat bounds stay identical across simulated IME insets on compact portrait screens, the control dock rises by the inset, and it returns when the inset closes. This does not replace physical-device keyboard testing.