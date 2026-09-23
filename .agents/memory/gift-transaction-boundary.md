---
name: Gift transaction boundary
description: Safety constraint on Party Room gifting until server-authoritative transactions exist
---

The Party Room gift catalog can show preview names, categories, a read-only account coin balance, and a quantity selection, but it must not present a successful send or deduct coins without server-authoritative item prices and an atomic verified transaction.

**Why:** A displayed client-side coin balance is not proof of sufficient funds and could become stale. The initial gift entry had no secure purchase/send implementation; making the visual panel look complete should not imply payments are wired.

**How to apply:** When real gifts arrive, connect the Send action only after verifying prices and coin debits on the server. Keep UI previews and unavailable states honest until then; do not invent a local success path.