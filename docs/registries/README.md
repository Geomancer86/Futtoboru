# Registries

Machine-readable truth for features, blockers, and release timing. **Agents and devs read these.** PO reads [PO_BRIEF.md](../planning/PO_BRIEF.md).

| File | Purpose | Update when |
|------|---------|-------------|
| [features.yaml](features.yaml) | What exists, status, minimum release | Feature ships or scope changes |
| [blockers.yaml](blockers.yaml) | **Active** blockers only | Blocker opened or resolved |
| [release-roadmap.yaml](release-roadmap.yaml) | Release tranches + CM93-F mapping | PO approves new release scope |

**Rules:**

1. PO-facing docs use **plain English** — no CM93-F IDs in PO_BRIEF.
2. IDs live here and in feature specs under `docs/specs/`.
3. Resolved blockers are **removed** from `blockers.yaml` (not left as clutter).
4. Deep research stays in `docs/research/` — not duplicated here.

Legacy markdown registries in `docs/planning/` remain for history; prefer these YAML files for new work.
