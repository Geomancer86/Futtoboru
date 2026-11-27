# Pre-Development Summary - Unemployed Job System

## Branch: `cursor/feature-unemployed-job-system`

**Date:** 2025-12-14  
**Status:** Analysis & Design Complete - Ready for Implementation

---

## 1. Work Completed

### 1.1 Analysis Phase ✅

**Document:** `ANALYSIS_UNEMPLOYED_FEATURES.md`

**Completed:**
- ✅ Analyzed all 5 requested features:
  1. View All Active Teams
  2. View Open Positions
  3. Send Job Proposals/Applications
  4. Receive Job Offers
  5. Negotiation System (Core Feature)
- ✅ Identified current system state (what exists vs. what's missing)
- ✅ Identified critical gaps (club staff tracking, job opening system, etc.)
- ✅ Created version breakdown (v1.0 MVP, v2.0 Enhanced, v3.0 Polish)
- ✅ Defined data model requirements
- ✅ Outlined system architecture
- ✅ Identified UI screen requirements
- ✅ Created 8-phase implementation plan
- ✅ Assessed risks and dependencies
- ✅ Defined success criteria

**Key Findings:**
- **Critical Gap:** Clubs don't track staff positions (must be fixed first)
- **Complexity:** Negotiation system is highest complexity (High risk)
- **Time Estimate:** 4-5 weeks for v1.0 MVP
- **Dependencies:** Club system, Person system, Inbox system (all exist ✅)

---

### 1.2 Design Phase ✅

**Document:** `DESIGN_UNEMPLOYED_JOB_SYSTEM_V1.md` (2,250+ lines)

**Completed:**
- ✅ Complete data model specifications with full Java code
- ✅ Enum definitions (JobStatus, ApplicationStatus, OfferStatus)
- ✅ Constants class (JobConstants)
- ✅ Manager class designs with full method signatures:
  - `ClubStaffManager` - Staff position management
  - `JobManager` - Job openings, applications, offers, negotiations
- ✅ UI component specifications (5 new screens)
- ✅ Algorithm specifications:
  - Job salary calculation
  - Application processing
  - AI negotiation logic
  - Match percentage calculation
- ✅ Integration points (Game Engine, Futtoboru, Inbox, Save/Load)
- ✅ Implementation details (ID generation, error handling, performance)
- ✅ Testing strategy (unit tests, integration tests, manual checklist)

**Design Decisions Made:**
1. **Club Staff Storage:** `Map<Long, Long>` in Club class (Profession ID → Person ID)
2. **Job Opening Storage:** `List<JobOpening>` in SaveGame (centralized)
3. **Application Processing:** Instant for v1.0 (no delay, simplified)
4. **Negotiation Complexity:** Basic (salary + contract length, 3 rounds max, 10% tolerance)

---

## 2. Feature Breakdown

### v1.0 MVP (Current Focus)

**Goal:** Basic unemployed gameplay - view teams, see jobs, apply, receive offers, basic negotiation

**Features:**
1. ✅ **Club Browser Screen** - View all clubs, filter by country
2. ✅ **Job Board Screen** - View open positions, filter by profession/country, apply
3. ✅ **Job Application System** - Submit applications, track status, limit (3/week)
4. ✅ **Job Offer System** - Receive offers, view details, accept/reject
5. ✅ **Basic Negotiation** - Negotiate salary & contract length, AI responds

**Estimated Time:** 4-5 weeks

---

### v2.0 Enhanced (Future)

**Features:**
- Interview system
- Enhanced negotiation (bonuses, performance clauses)
- Reputation system
- Job requirements matching

**Estimated Time:** 2-3 weeks

---

### v3.0 Polish (Future)

**Features:**
- Advanced negotiation
- Job recommendations
- Networking system
- Agent system

**Estimated Time:** 2-3 weeks

---

## 3. Data Models Designed

### 3.1 New Classes

1. **`JobOpening`** (`com.rndmodgames.futtoboru.data.jobs`)
   - Represents open job positions
   - Fields: clubId, professionId, salary, contractLength, minReputation, status
   - Methods: `isAcceptingApplications()`, `isExpired()`

2. **`JobApplication`** (`com.rndmodgames.futtoboru.data.jobs`)
   - Represents player applications
   - Fields: applicantId, jobOpeningId, status, matchPercentage
   - Methods: `isActive()`

3. **`JobOffer`** (`com.rndmodgames.futtoboru.data.jobs`)
   - Represents job offers
   - Fields: salary, contractLength, status, negotiationRound
   - Methods: `isExpired()`, `canNegotiate()`

### 3.2 Extensions to Existing Classes

1. **`Club`** - Add staff tracking:
   ```java
   private Map<Long, Long> staff; // Profession ID → Person ID
   ```

2. **`Person`** - Add job system tracking:
   ```java
   private Integer reputation = 50;
   private List<Long> activeApplicationIds;
   private List<Long> pendingOfferIds;
   private Integer applicationsThisWeek = 0;
   ```

3. **`SaveGame`** - Add job system data:
   ```java
   private List<JobOpening> activeJobOpenings;
   private List<JobApplication> allApplications;
   private List<JobOffer> pendingOffers;
   ```

### 3.3 Enums

- `JobStatus`: OPEN, FILLED, EXPIRED
- `ApplicationStatus`: PENDING, REJECTED, OFFER_RECEIVED, ACCEPTED, WITHDRAWN
- `OfferStatus`: PENDING, ACCEPTED, REJECTED, NEGOTIATING, EXPIRED

---

## 4. Manager Classes Designed

### 4.1 ClubStaffManager

**Purpose:** Manage club staff positions and detect vacancies

**Key Methods:**
- `getClubStaff(Club, Profession)` → Person
- `setClubStaff(Club, Profession, Person)`
- `isPositionVacant(Club, Profession)` → boolean
- `getVacantPositions(Club)` → List<Profession>
- `fireStaff(Club, Profession)`
- `hireStaff(Club, Profession, Person)`

---

### 4.2 JobManager

**Purpose:** Manage job openings, applications, offers, negotiations

**Key Methods:**

**Job Openings:**
- `getAvailableJobs()` → List<JobOpening>
- `getAvailableJobs(Profession)` → List<JobOpening>
- `getAvailableJobs(Club)` → List<JobOpening>
- `createJobOpening(Club, Profession)` → JobOpening
- `updateJobOpenings()` (called daily)

**Applications:**
- `applyForJob(Person, JobOpening)` → JobApplication
- `getPlayerApplications(Person)` → List<JobApplication>
- `withdrawApplication(JobApplication)`

**Offers:**
- `makeOffer(JobOpening, Person)` → JobOffer
- `getPlayerOffers(Person)` → List<JobOffer>
- `acceptOffer(JobOffer)`
- `rejectOffer(JobOffer)`

**Negotiation:**
- `submitCounterOffer(JobOffer, BigDecimal, Integer)` → JobOffer
- `processCounterOffer(JobOffer)` (AI response)

---

## 5. UI Screens Designed

### 5.1 New Screens

1. **Club Browser Screen** (`ClubBrowserScreenTable`)
   - List all clubs
   - Filter by country
   - Sort by name, league
   - Show open positions indicator

2. **Job Board Screen** (`JobBoardScreenTable`)
   - List all job openings
   - Filter by profession, country
   - Sort by salary, deadline
   - Apply button

3. **My Applications Screen** (`MyApplicationsScreenTable`)
   - List all applications
   - Show status
   - Filter by status
   - Withdraw button

4. **Job Offer Screen** (`JobOfferScreenTable`)
   - Display offer details
   - Accept/Reject/Negotiate buttons

5. **Negotiation Screen** (`NegotiationScreenTable`)
   - Adjust salary and contract length
   - Submit counter-offer
   - Show negotiation history

### 5.2 Menu Integration

**Changes to `MainMenuManager`:**
- Add 3 new buttons for unemployed menu:
  - "Browse Clubs" → `CLUB_BROWSER_SCREEN`
  - "Job Board" → `JOB_BOARD_SCREEN`
  - "My Applications" → `MY_APPLICATIONS_SCREEN`
- Add screen IDs (10001-10005)

---

## 6. Algorithms Designed

### 6.1 Job Salary Calculation

```
baseSalary = clubBalance * 0.01 / 12  // 1% per year, monthly
multiplier = professionMultiplier      // Manager: 1.5, Director: 1.2, Scout: 0.8
salary = baseSalary * multiplier
```

### 6.2 Application Processing (v1.0 Simplified)

```
1. Player applies for job
2. Calculate match percentage (based on reputation)
3. If matchPercentage >= 70:
   - Make offer immediately
   - Set status to OFFER_RECEIVED
4. Else:
   - Reject application
   - Set status to REJECTED
```

### 6.3 AI Negotiation Logic (v1.0 Simple)

```
1. Player submits counter-offer
2. Calculate tolerance (10% of original salary)
3. Check if salary within tolerance AND contract length within 3 months
4. If yes: Accept counter-offer
5. If no: Reject counter-offer
```

### 6.4 Match Percentage Calculation

```
if minReputation == null:
    return 100
if applicantReputation >= minReputation:
    return 100
else:
    return (applicantReputation * 100) / minReputation
```

---

## 7. Integration Points Defined

### 7.1 Game Engine Integration

**File:** `FuttoboruGameEngine.java`

**Changes:**
- Add `JobManager` instance
- Call `jobManager.updateJobOpenings()` in `continueGame()`

---

### 7.2 Futtoboru Integration

**File:** `Futtoboru.java`

**Changes:**
- Add `JobManager` and `ClubStaffManager` instances
- Initialize in `create()`
- Add getters

---

### 7.3 Inbox Integration

**File:** `InboxScreenTable.java`

**Changes:**
- Job offer notifications automatically added via `JobManager.sendOfferNotification()`
- Uses existing `Message` class
- No changes needed to inbox display

---

### 7.4 Save/Load Integration

**File:** `SaveLoadSystem.java`

**Changes:**
- All new classes implement `Serializable`
- SaveGame extensions automatically serialized
- No custom serializers needed

---

## 8. Implementation Phases

### Phase 1: Foundation (Week 1)
- [ ] Create `JobOpening` class
- [ ] Create `JobApplication` class
- [ ] Create `JobOffer` class
- [ ] Create enums (JobStatus, ApplicationStatus, OfferStatus)
- [ ] Add staff tracking to `Club`
- [ ] Add extensions to `Person`
- [ ] Add extensions to `SaveGame`
- [ ] Create `ClubStaffManager`
- [ ] Test data structures

**Deliverable:** Data models ready, clubs can track staff

---

### Phase 2: Job Generation (Week 1-2)
- [ ] Implement `ClubStaffManager.isPositionVacant()`
- [ ] Implement `JobManager.createJobOpening()`
- [ ] Implement `JobManager.updateJobOpenings()`
- [ ] Integrate with game engine
- [ ] Test job generation

**Deliverable:** Job openings created automatically for vacant positions

---

### Phase 3: UI - Club Browser (Week 2)
- [ ] Create `ClubBrowserScreenTable`
- [ ] Implement club listing
- [ ] Add filters (country)
- [ ] Add to unemployed menu
- [ ] Test club browsing

**Deliverable:** Can view all clubs

---

### Phase 4: UI - Job Board (Week 2-3)
- [ ] Create `JobBoardScreenTable`
- [ ] Implement job listing
- [ ] Add filters (profession, country)
- [ ] Add apply button
- [ ] Add to unemployed menu
- [ ] Test job board

**Deliverable:** Can view and apply for jobs

---

### Phase 5: Application System (Week 3)
- [ ] Implement `JobManager.applyForJob()`
- [ ] Add application validation (limits, requirements)
- [ ] Create `MyApplicationsScreenTable`
- [ ] Track application status
- [ ] Add to unemployed menu
- [ ] Test applications

**Deliverable:** Can apply for jobs, track applications

---

### Phase 6: Offer System (Week 3-4)
- [ ] Implement `JobManager.makeOffer()`
- [ ] Implement `JobManager.processApplication()`
- [ ] Create `JobOfferScreenTable`
- [ ] Add offer notifications to inbox
- [ ] Implement accept/reject
- [ ] Test offers

**Deliverable:** Can receive and respond to offers

---

### Phase 7: Basic Negotiation (Week 4)
- [ ] Implement `JobManager.submitCounterOffer()`
- [ ] Implement `JobManager.processCounterOffer()` (AI logic)
- [ ] Create `NegotiationScreenTable`
- [ ] Add negotiation UI (salary, contract length)
- [ ] Test negotiation

**Deliverable:** Can negotiate job offers

---

### Phase 8: Integration & Polish (Week 4-5)
- [ ] Integrate with game engine
- [ ] Add all screens to menu
- [ ] Test full flow (browse → apply → offer → negotiate → accept)
- [ ] Fix bugs
- [ ] Polish UI

**Deliverable:** Complete unemployed gameplay loop

---

## 9. Technical Decisions Summary

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **Club Staff Storage** | `Map<Long, Long>` in Club | Flexible, easy to extend, avoids circular references |
| **Job Opening Storage** | `List<JobOpening>` in SaveGame | Centralized, easier to query |
| **Application Processing** | Instant (no delay) | Simplified for v1.0, can add delay in v2.0 |
| **Negotiation Complexity** | Basic (salary + contract, 3 rounds, 10% tolerance) | Good balance for v1.0 |
| **ID Generation** | Simple incrementing counter | Sufficient for v1.0, can upgrade in v2.0 |
| **Date Handling** | `LocalDateTime` with game time | Consistent with existing codebase |

---

## 10. Constants & Configuration

**File:** `JobConstants.java`

```java
MAX_APPLICATIONS_PER_WEEK = 3
OFFER_EXPIRATION_DAYS = 7
MAX_NEGOTIATION_ROUNDS = 3
JOB_OPENING_DEFAULT_DEADLINE_DAYS = 30
APPLICATION_PROCESSING_DELAY_DAYS = 0  // Instant for v1.0
AI_NEGOTIATION_TOLERANCE = 0.10  // 10%
```

---

## 11. Success Criteria

**v1.0 MVP is complete when:**
1. ✅ Unemployed player can browse all clubs
2. ✅ Unemployed player can see open positions
3. ✅ Unemployed player can apply for jobs (max 3/week)
4. ✅ Unemployed player receives job offers
5. ✅ Unemployed player can negotiate offers (basic)
6. ✅ Unemployed player can accept offers and become employed
7. ✅ Game state updates correctly after accepting offer
8. ✅ All data persists in save/load system

---

## 12. Risk Assessment

### High Risk
- **Negotiation System:** Complex logic, many edge cases
  - **Mitigation:** Start simple, iterate

- **Job Generation:** AI needs to create realistic openings
  - **Mitigation:** Start with simple rules, improve later

### Medium Risk
- **Application Processing:** Need to balance realism vs simplicity
  - **Mitigation:** Start simple (instant), add delays later

- **UI Complexity:** Many new screens
  - **Mitigation:** Reuse existing screen patterns

### Low Risk
- **Club Browser:** Mostly UI work
- **Data Structures:** Straightforward

---

## 13. Dependencies

### Required (All Exist ✅)
- ✅ Club system
- ✅ Person system
- ✅ Profession system
- ✅ Inbox system
- ✅ SaveGame system
- ✅ Financial system (basic - exists)

### Nice to Have (Can Add Later)
- Reputation system (v2.0)
- Interview system (v2.0)
- Advanced negotiation (v3.0)

---

## 14. Files Created

1. **`ANALYSIS_UNEMPLOYED_FEATURES.md`** (819 lines)
   - Feature analysis
   - Current state assessment
   - Version breakdown
   - Implementation phases
   - Technical decisions

2. **`DESIGN_UNEMPLOYED_JOB_SYSTEM_V1.md`** (2,250+ lines)
   - Complete data models
   - Manager class designs
   - UI specifications
   - Algorithm details
   - Integration points
   - Testing strategy

3. **`SUMMARY_PRE_DEVELOPMENT.md`** (This document)
   - Summary of all pre-development work

---

## 15. Next Steps

### Ready to Start:
1. ✅ **Phase 1: Foundation** - Create data models
2. ✅ **Phase 2: Job Generation** - Implement job opening creation
3. ✅ **Phase 3-8: UI & Integration** - Build screens and integrate

### Before Starting:
- [ ] Review design document
- [ ] Confirm technical decisions
- [ ] Set up development environment
- [ ] Create feature branch (already done: `cursor/feature-unemployed-job-system`)

---

## 16. Key Takeaways

1. **Complete Design:** All data models, managers, UI screens, and algorithms are fully specified
2. **Clear Phases:** 8-phase implementation plan with clear deliverables
3. **Risk Mitigation:** Identified risks with mitigation strategies
4. **Version Strategy:** v1.0 MVP focused, with clear path to v2.0 and v3.0
5. **Integration Ready:** All integration points identified and specified
6. **Testing Strategy:** Unit tests, integration tests, and manual checklist defined

---

**Status:** ✅ **READY FOR DEVELOPMENT**

All analysis and design work is complete. The implementation can begin with Phase 1 (Foundation).

---

**Document Version:** 1.0  
**Last Updated:** 2025-12-14  
**Branch:** `cursor/feature-unemployed-job-system`

