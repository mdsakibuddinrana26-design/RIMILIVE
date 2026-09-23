---
name: Main navigation consistency
description: Why main destinations share one stable navigation bar
---

Use one persistent five-destination bottom navigation design throughout the main app, including Profile. Follow, Party, Chat, and Top are sub-tabs under Home; the Party Room interior is a separate visual context and has no main bottom navigation.

**Why:** The older Profile-only navigation used different icons, labels, and actions from the other main screens, so moving to Profile unexpectedly changed the entire bar. The shared primary destinations keep existing main navigation targets stable; profile-specific Follow and Mission entries remain accessible from the profile content and home tabs.

**How to apply:** New main screens should reuse the same navigation component and change only its selected item. Do not introduce a screen-specific replacement bar or apply the main theme to the inside of a Party Room.