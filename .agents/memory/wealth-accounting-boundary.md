---
name: Wealth accounting boundary
description: Keep wealth progress separate from spendable coins and unverified client data
---

Wealth level is based on lifetime eligible purchased coins verified by a trusted payment backend, not the current Coin wallet balance. Do not show a numeric level when that verified lifetime total is unknown.

**Why:** Spending coins should not erase earned purchase progress, and an account owner who can edit their profile cannot be trusted to assert purchase totals. A visible wallet balance is not a purchase ledger.

**How to apply:** When payment processing is added, maintain a server-owned purchase ledger and derived lifetime total, then connect the level screen to that verified source. Leave wallet and level data separate; avoid client-side credits or a fallback LV0 for missing accounting.