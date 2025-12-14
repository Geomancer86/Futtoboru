# Futtoboru - Project Management & Community Management Repository

This is a separate repository dedicated to **Project Management** and **Community Management** work for the Futtoboru project.

## Purpose

This repository allows the PM/CM AI agent to work in parallel with development agents without conflicts. All project management dashboards, Patreon updates, community engagement materials, and project tracking documents are maintained here.

## Repository Structure

```
futtoboru_cursor_pm/
├── README.md                          # This file
├── .gitignore                         # Git ignore rules
├── project-management/                # PM documents
│   ├── PROJECT_MANAGEMENT.md         # Main PM dashboard
│   ├── weekly-reports/               # Weekly progress reports
│   ├── sprint-plans/                 # Sprint planning documents
│   └── metrics/                      # Project metrics and KPIs
├── community-management/             # CM documents
│   ├── patreon/                      # Patreon updates
│   │   ├── PATREON_UPDATES.md        # Update archive and templates
│   │   ├── drafts/                   # Draft updates
│   │   └── published/                # Published updates
│   ├── social-media/                 # Social media content
│   └── community-feedback/           # Community feedback tracking
└── documentation/                    # PM/CM documentation
    ├── templates/                    # Document templates
    └── guidelines/                   # Writing and process guidelines
```

## What's Tracked Here

### Project Management
- Project status dashboards
- Sprint planning and tracking
- Issue prioritization
- Progress metrics
- Risk assessments
- Milestone tracking

### Community Management
- Patreon update drafts and archives
- Social media content planning
- Community feedback tracking
- Engagement metrics
- Update schedules

## How to Use

### For PM/CM Agent
1. Update `project-management/PROJECT_MANAGEMENT.md` regularly
2. Create weekly reports in `project-management/weekly-reports/`
3. Draft Patreon updates in `community-management/patreon/drafts/`
4. Track community feedback in `community-management/community-feedback/`

### For Developers
- Reference this repo for:
  - Current project priorities
  - Sprint goals
  - Community update schedules
  - Project status

## Main Futtoboru Repository

The main codebase is located at:
- **Path:** `G:\git\futtoboru_cursor\futtoboru`
- **GitHub:** [Repository URL]

## Workflow

1. **PM Agent** monitors main repo and updates PM dashboard
2. **PM Agent** creates Patreon updates based on progress
3. **CM Agent** manages community engagement
4. All work happens in this separate repo to avoid conflicts

## Notes

- This repo is independent of the main codebase
- Updates can be synced manually or via scripts
- PM/CM work doesn't interfere with code development
- All community-facing content is versioned here

---

**Created:** 2024-12-14  
**Purpose:** Separate PM/CM workspace for parallel agent work  
**Maintainer:** AI Project Manager & Community Manager
