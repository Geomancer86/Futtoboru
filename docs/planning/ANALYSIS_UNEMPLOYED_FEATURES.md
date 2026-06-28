# Unemployed Character Features - Analysis Document

## Branch: `cursor/feature-unemployed-job-system`

**Status:** ANALYSIS PHASE  
**Priority:** High (Core Feature for v1.0+)  
**Evolution Plan:** Core system in v1.0, expand in v2.0, polish in v3.0

---

## 1. Feature Requirements Analysis

### 1.1 Core Features (User Requested)

#### Feature 1: View All Active Teams
**Description:** Unemployed players can see all clubs/teams in the game world

**Current State:**
- ✅ Clubs are loaded in `DatabaseLoader`
- ✅ Clubs stored in `SaveGame.getAllClubs()`
- ✅ Clubs accessible via `DatabaseLoader.getClubsByCountry()`
- ❌ No UI to browse/view all clubs
- ❌ No club browsing screen for unemployed players

**What's Needed:**
- [ ] Club Browser/List Screen
- [ ] Filter clubs by country, league, reputation
- [ ] Display club information (name, league, reputation, finances)
- [ ] Navigation from unemployed menu

**Dependencies:**
- ✅ Club data exists
- ⚠️ League data (may need league assignment)

**Complexity:** Low - Mostly UI work

---

#### Feature 2: View Open Positions
**Description:** Unemployed players can see all available job openings

**Current State:**
- ❌ No job opening system exists
- ❌ Clubs don't track staff positions
- ❌ No way to determine if a position is vacant
- ❌ No job board/listings

**What's Needed:**
- [ ] Job opening data structure (`JobOpening`)
- [ ] Club staff tracking (who holds each position)
- [ ] Job opening generation (when positions become vacant)
- [ ] Job Board UI screen
- [ ] Job opening persistence in SaveGame

**Dependencies:**
- ⚠️ Club staff system (needs to track current staff)
- ⚠️ Position vacancy detection
- ⚠️ AI job generation logic

**Complexity:** Medium - Requires new data structures and AI logic

---

#### Feature 3: Send Job Proposals/Applications
**Description:** Unemployed players can apply for open positions

**Current State:**
- ❌ No application system
- ❌ No way to submit applications
- ❌ No application tracking

**What's Needed:**
- [ ] Job application data structure (`JobApplication`)
- [ ] Application submission logic
- [ ] Application status tracking
- [ ] Application limits (e.g., 3 per week)
- [ ] Application history screen
- [ ] Application validation (meets requirements?)

**Dependencies:**
- ✅ Job opening system (Feature 2)
- ⚠️ Requirement matching system
- ⚠️ Reputation system (for validation)

**Complexity:** Medium - Application logic + UI

---

#### Feature 4: Receive Job Offers
**Description:** Unemployed players receive job offers after successful application/interview

**Current State:**
- ❌ No job offer system
- ❌ No offer generation
- ❌ No offer notification

**What's Needed:**
- [ ] Job offer data structure (`JobOffer`)
- [ ] Offer generation logic (after interview success)
- [ ] Offer notification system (inbox integration)
- [ ] Offer display screen
- [ ] Offer expiration handling

**Dependencies:**
- ✅ Job application system (Feature 3)
- ⚠️ Interview system (or simplified offer generation)
- ✅ Inbox system (for notifications)

**Complexity:** Medium - Offer logic + notification integration

---

#### Feature 5: Negotiation System (Core Feature)
**Description:** Players can negotiate job offers - salary, contract length, bonuses, expectations

**Current State:**
- ❌ No negotiation system
- ❌ No counter-offer mechanism
- ❌ No negotiation UI

**What's Needed:**
- [ ] Negotiation data structure (counter-offers)
- [ ] Negotiation logic (AI response to counter-offers)
- [ ] Negotiation UI (adjust terms, submit counter-offer)
- [ ] Negotiation limits (max rounds, AI walk-away)
- [ ] Negotiation history

**Dependencies:**
- ✅ Job offer system (Feature 4)
- ⚠️ Financial system (for salary calculations)
- ⚠️ AI negotiation logic

**Complexity:** High - Complex logic + multiple UI states

**Evolution Plan:**
- **v1.0:** Basic negotiation (salary, contract length)
- **v2.0:** Add bonuses, performance clauses
- **v3.0:** Advanced terms, multiple negotiation rounds, AI personality

---

## 2. Current System Analysis

### 2.1 What Exists

#### Club System
- ✅ Clubs loaded and stored
- ✅ Club basic info (name, country, league, balance)
- ✅ Club players list
- ❌ **No staff tracking** - Critical gap
- ❌ **No position tracking** - Critical gap

#### Person System
- ✅ Person class with profession
- ✅ `Person.currentClubId` (can be null for unemployed)
- ✅ `Person.primaryProfession`
- ❌ **No reputation system**
- ❌ **No application tracking**
- ❌ **No offer tracking**

#### Profession System
- ✅ 8 professions defined:
  1. Player
  2. Retired Player (unemployed)
  3. Manager
  4. Director
  5. Scout
  6. Agent
  7. Investor
  8. Club Owner

#### Menu System
- ✅ Unemployed menu exists (Home, Inbox, Personal Details, Authority)
- ❌ **No Job Board button**
- ❌ **No Applications button**
- ❌ **No Club Browser button**

### 2.2 Critical Gaps

1. **Club Staff Tracking** - Clubs don't track who holds each position
2. **Position Vacancy Detection** - No way to know if a position is open
3. **Job Opening System** - No job opening data structure
4. **Application System** - No way to apply for jobs
5. **Offer System** - No job offer mechanism
6. **Negotiation System** - No negotiation capability
7. **Reputation System** - No player reputation (affects job opportunities)

---

## 3. Feature Breakdown by Version

### v1.0: Core Foundation (MVP)

**Goal:** Basic unemployed gameplay - can view teams, see jobs, apply, receive offers, basic negotiation

#### Must Have:
1. **Club Browser Screen**
   - List all clubs
   - Filter by country
   - View club basic info
   - See if club has open positions (indicator)

2. **Job Board Screen**
   - List all open positions
   - Filter by profession, country
   - Show job details (club, salary, requirements)
   - Apply button

3. **Job Application System**
   - Submit application
   - Track application status
   - Application limit (3 per week)
   - Application history screen

4. **Basic Job Offer System**
   - Receive offers (after application - simplified, no interviews for v1.0)
   - View offer details
   - Accept/reject

5. **Basic Negotiation (v1.0)**
   - Negotiate salary (simple +/- buttons)
   - Negotiate contract length
   - Submit counter-offer
   - AI accepts/rejects counter-offer (simple logic)
   - Max 2-3 negotiation rounds

**Estimated Time:** 2-3 weeks

---

### v2.0: Enhanced Features

**Goal:** Add interviews, better negotiation, reputation system

#### New Features:
1. **Interview System**
   - Interview invitations
   - Interview questions
   - Interview results
   - Multiple interview rounds

2. **Enhanced Negotiation**
   - Signing bonuses
   - Performance bonuses
   - Contract expectations
   - More negotiation rounds
   - Better AI negotiation logic

3. **Reputation System**
   - Calculate reputation
   - Reputation affects job availability
   - Reputation affects negotiation power

4. **Job Requirements Matching**
   - Match player to job requirements
   - Show match percentage
   - Filter jobs by match percentage

**Estimated Time:** 2-3 weeks

---

### v3.0: Polish & Advanced Features

**Goal:** Advanced negotiation, job recommendations, networking

#### New Features:
1. **Advanced Negotiation**
   - Complex contract terms
   - Multiple negotiation strategies
   - AI personality affects negotiation
   - Negotiation history

2. **Job Recommendations**
   - AI suggests jobs based on profile
   - Job alerts for new matching jobs

3. **Networking System**
   - Relationships affect opportunities
   - References from previous employers

4. **Advanced Features**
   - Agent system (hire agent to find jobs)
   - Job search history
   - Application analytics

**Estimated Time:** 2-3 weeks

---

## 4. Data Model Requirements

### 4.1 Club Staff Tracking (NEW - Critical)

**Problem:** Clubs don't track who holds each position

**Solution:** Add staff tracking to Club

```java
// Add to Club class:
private Person manager;           // Current manager (null if vacant)
private Person director;          // Current director (null if vacant)
private Person scout;             // Current scout (null if vacant)
// Or use a Map:
private Map<Profession, Person> staff = new HashMap<>();
```

**Alternative:** Create `ClubStaff` class
```java
public class ClubStaff implements Serializable {
    private Club club;
    private Map<Profession, Person> positions = new HashMap<>();
    private LocalDateTime lastUpdated;
}
```

**Decision Needed:** Which approach? Map in Club or separate ClubStaff?

---

### 4.2 Job Opening (NEW)

```java
public class JobOpening implements Serializable {
    private Long id;
    private Club club;
    private Profession profession;
    private LocalDateTime postedDate;
    private LocalDateTime applicationDeadline;
    private JobStatus status; // OPEN, FILLED, EXPIRED
    
    // Compensation
    private BigDecimal salary;
    private Integer contractLengthMonths;
    
    // Requirements (v1.0: simple)
    private Integer minReputation; // null = no requirement
    
    // Applicants
    private List<JobApplication> applications = new ArrayList<>();
    private Person selectedCandidate; // if filled
}
```

---

### 4.3 Job Application (NEW)

```java
public class JobApplication implements Serializable {
    private Long id;
    private Person applicant;
    private JobOpening jobOpening;
    private LocalDateTime applicationDate;
    private ApplicationStatus status; // PENDING, REJECTED, OFFER_RECEIVED, ACCEPTED, WITHDRAWN
}
```

---

### 4.4 Job Offer (NEW)

```java
public class JobOffer implements Serializable {
    private Long id;
    private JobOpening jobOpening;
    private Person recipient;
    private LocalDateTime offerDate;
    private LocalDateTime expirationDate; // 7 days default
    private OfferStatus status; // PENDING, ACCEPTED, REJECTED, NEGOTIATING, EXPIRED
    
    // Terms (v1.0: basic)
    private BigDecimal salary;
    private Integer contractLengthMonths;
    
    // Negotiation (v1.0: simple)
    private JobOffer counterOffer; // if negotiating
    private Integer negotiationRound; // max 3 for v1.0
    private boolean isCounterOffer; // true if this is a counter-offer
}
```

---

### 4.5 Person Extensions

```java
// Add to Person class:
private Integer reputation = 50; // Default 50, range 0-100
private List<JobApplication> activeApplications = new ArrayList<>();
private List<JobOffer> pendingOffers = new ArrayList<>();
private LocalDateTime lastJobApplicationDate;
private Integer applicationsThisWeek = 0;
```

---

## 5. System Architecture

### 5.1 New Components Needed

#### JobManager (NEW)
**Responsibility:** Manage job openings, applications, offers, negotiations

**Key Methods:**
```java
public class JobManager {
    // Job Openings
    public List<JobOpening> getAvailableJobs(Profession profession);
    public List<JobOpening> getAvailableJobs(Club club);
    public void createJobOpening(Club club, Profession profession);
    public void updateJobOpenings(); // Daily update - expire, fill, etc.
    
    // Applications
    public JobApplication applyForJob(Person applicant, JobOpening job);
    public List<JobApplication> getPlayerApplications(Person player);
    public void processApplications(); // AI processes applications
    
    // Offers
    public JobOffer makeOffer(JobOpening job, Person candidate);
    public List<JobOffer> getPlayerOffers(Person player);
    public void processOfferResponse(JobOffer offer, OfferResponse response);
    
    // Negotiation
    public JobOffer submitCounterOffer(JobOffer originalOffer, BigDecimal newSalary, Integer newContractLength);
    public OfferResponse processCounterOffer(JobOffer counterOffer); // AI response
}
```

#### ClubStaffManager (NEW)
**Responsibility:** Track club staff positions, detect vacancies

**Key Methods:**
```java
public class ClubStaffManager {
    public Person getClubStaff(Club club, Profession profession);
    public void setClubStaff(Club club, Profession profession, Person person);
    public boolean isPositionVacant(Club club, Profession profession);
    public List<Profession> getVacantPositions(Club club);
    public void fireStaff(Club club, Profession profession); // Creates job opening
}
```

#### ReputationManager (NEW - v2.0, but plan for v1.0)
**Responsibility:** Calculate and manage player reputation

**Key Methods:**
```java
public class ReputationManager {
    public Integer calculateReputation(Person person);
    public void updateReputation(Person person);
    public boolean meetsReputationRequirement(Person person, Integer minReputation);
}
```

---

## 6. UI Screens Required

### 6.1 Club Browser Screen (NEW)
**Purpose:** View all active teams/clubs

**Features:**
- List all clubs
- Filter by country, league
- Sort by name, reputation, league
- Show club info (name, league, reputation indicator)
- Show open positions indicator
- Click to view club details or open positions

**Screen ID:** `CLUB_BROWSER_SCREEN` (new)

---

### 6.2 Job Board Screen (NEW)
**Purpose:** View all open job positions

**Features:**
- List all job openings
- Filter by profession, country, league, salary range
- Sort by salary, deadline, match percentage
- Show job details (club, position, salary, requirements)
- Apply button
- Match percentage indicator

**Screen ID:** `JOB_BOARD_SCREEN` (new)

---

### 6.3 My Applications Screen (NEW)
**Purpose:** Track job applications

**Features:**
- List all applications
- Show application status
- Filter by status
- Withdraw application button
- View application details

**Screen ID:** `MY_APPLICATIONS_SCREEN` (new)

---

### 6.4 Job Offer Screen (NEW)
**Purpose:** View and respond to job offers

**Features:**
- Display offer details
- Show terms (salary, contract length)
- Accept button
- Reject button
- Negotiate button (opens negotiation screen)

**Screen ID:** `JOB_OFFER_SCREEN` (new)

---

### 6.5 Negotiation Screen (NEW)
**Purpose:** Negotiate job offer terms

**Features:**
- Display current offer
- Adjust salary (slider or input)
- Adjust contract length (slider or input)
- Submit counter-offer button
- Cancel negotiation button
- Show negotiation history

**Screen ID:** `NEGOTIATION_SCREEN` (new)

---

## 7. Integration Points

### 7.1 Game Engine Integration
- **Daily Update:** `JobManager.updateJobOpenings()` called in `FuttoboruGameEngine.continueGame()`
- **Job Generation:** Triggered when:
  - Manager fired (via `ClubStaffManager.fireStaff()`)
  - New club created
  - Position becomes vacant

### 7.2 Menu Integration
- **Unemployed Menu:** Add buttons:
  - "Job Board" → `JOB_BOARD_SCREEN`
  - "My Applications" → `MY_APPLICATIONS_SCREEN`
  - "Browse Clubs" → `CLUB_BROWSER_SCREEN`

### 7.3 Inbox Integration
- **Job Offer Notifications:** Add to inbox when offer received
- **Interview Invitations:** Add to inbox (v2.0)
- **Application Updates:** Add to inbox (status changes)

### 7.4 Save Game Integration
- Add to `SaveGame`:
  - `List<JobOpening> activeJobOpenings`
  - `List<JobApplication> allApplications`
  - `List<JobOffer> pendingOffers`
  - Club staff data (or separate `ClubStaff` collection)

---

## 8. Implementation Phases

### Phase 1: Foundation (Week 1)
**Goal:** Basic data structures and club staff tracking

- [ ] Create `JobOpening` class
- [ ] Create `JobApplication` class
- [ ] Create `JobOffer` class
- [ ] Add staff tracking to `Club` (or create `ClubStaff`)
- [ ] Create `ClubStaffManager`
- [ ] Add to `SaveGame` serialization
- [ ] Test data structures

**Deliverable:** Data models ready, clubs can track staff

---

### Phase 2: Job Generation (Week 1-2)
**Goal:** Clubs create job openings when positions are vacant

- [ ] Implement `ClubStaffManager.isPositionVacant()`
- [ ] Implement job opening creation logic
- [ ] Add job opening to `SaveGame`
- [ ] Test job generation (create test vacancies)

**Deliverable:** Job openings are created automatically

---

### Phase 3: UI - Club Browser (Week 2)
**Goal:** Unemployed players can browse all clubs

- [ ] Create `ClubBrowserScreenTable`
- [ ] Implement club listing
- [ ] Add filters (country, league)
- [ ] Add to unemployed menu
- [ ] Test club browsing

**Deliverable:** Can view all clubs

---

### Phase 4: UI - Job Board (Week 2-3)
**Goal:** Unemployed players can see open positions

- [ ] Create `JobBoardScreenTable`
- [ ] Implement job listing
- [ ] Add filters (profession, country, salary)
- [ ] Add apply button
- [ ] Add to unemployed menu
- [ ] Test job board

**Deliverable:** Can view and apply for jobs

---

### Phase 5: Application System (Week 3)
**Goal:** Players can apply for jobs

- [ ] Implement `JobManager.applyForJob()`
- [ ] Add application validation (limits, requirements)
- [ ] Create `MyApplicationsScreenTable`
- [ ] Track application status
- [ ] Add to unemployed menu
- [ ] Test applications

**Deliverable:** Can apply for jobs, track applications

---

### Phase 6: Offer System (Week 3-4)
**Goal:** Players receive job offers

- [ ] Implement `JobManager.makeOffer()` (simplified - no interview for v1.0)
- [ ] Create `JobOfferScreenTable`
- [ ] Add offer notifications to inbox
- [ ] Implement accept/reject
- [ ] Test offers

**Deliverable:** Can receive and respond to offers

---

### Phase 7: Basic Negotiation (Week 4)
**Goal:** Players can negotiate offers

- [ ] Implement `JobManager.submitCounterOffer()`
- [ ] Implement AI negotiation logic (simple)
- [ ] Create `NegotiationScreenTable`
- [ ] Add negotiation UI (salary, contract length)
- [ ] Test negotiation

**Deliverable:** Can negotiate job offers

---

### Phase 8: Integration & Polish (Week 4-5)
**Goal:** Complete integration and testing

- [ ] Integrate with game engine
- [ ] Add all screens to menu
- [ ] Test full flow (browse → apply → offer → negotiate → accept)
- [ ] Fix bugs
- [ ] Polish UI

**Deliverable:** Complete unemployed gameplay loop

---

## 9. Technical Decisions Needed

### 9.1 Club Staff Storage
**Question:** How to store club staff?

**Option A:** Add fields to Club
```java
private Person manager;
private Person director;
private Person scout;
```

**Option B:** Use Map in Club
```java
private Map<Profession, Person> staff = new HashMap<>();
```

**Option C:** Separate ClubStaff class
```java
// In SaveGame:
private Map<Long, ClubStaff> clubStaffs = new HashMap<>();
```

**Recommendation:** Option B (Map) - Most flexible, easy to extend

---

### 9.2 Job Opening Storage
**Question:** Where to store job openings?

**Option A:** In SaveGame as list
```java
private List<JobOpening> activeJobOpenings = new ArrayList<>();
```

**Option B:** Per club
```java
// In Club:
private List<JobOpening> jobOpenings = new ArrayList<>();
```

**Recommendation:** Option A - Easier to query, centralized management

---

### 9.3 Application Processing
**Question:** How to process applications? (v1.0 simplified)

**Option A:** Instant offer (no interview for v1.0)
- Apply → Immediate offer if meets requirements

**Option B:** Delayed processing
- Apply → Wait 1-3 days → AI processes → Offer/Reject

**Recommendation:** Option B - More realistic, but can start with Option A for v1.0 MVP

---

### 9.4 Negotiation Complexity (v1.0)
**Question:** How complex should v1.0 negotiation be?

**Option A:** Very Simple
- Only salary negotiation
- AI accepts/rejects (50/50 chance if reasonable)

**Option B:** Basic
- Salary + contract length
- Simple AI logic (accepts if within 10% of original)

**Recommendation:** Option B - Good balance for v1.0

---

## 10. Dependencies & Prerequisites

### 10.1 Required (Must Exist)
- ✅ Club system (exists)
- ✅ Person system (exists)
- ✅ Profession system (exists)
- ✅ Inbox system (exists)
- ✅ SaveGame system (exists)
- ⚠️ Financial system (for salary - can be basic for v1.0)

### 10.2 Nice to Have (Can Add Later)
- Reputation system (v2.0)
- Interview system (v2.0)
- Advanced negotiation (v3.0)

---

## 11. Risk Assessment

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

## 12. Success Criteria

**v1.0 MVP is complete when:**
1. ✅ Unemployed player can browse all clubs
2. ✅ Unemployed player can see open positions
3. ✅ Unemployed player can apply for jobs
4. ✅ Unemployed player receives job offers
5. ✅ Unemployed player can negotiate offers (basic)
6. ✅ Unemployed player can accept offers and become employed
7. ✅ Game state updates correctly after accepting offer

---

## 13. Open Questions

1. **Application Limits:** How many applications per week? (Suggested: 3)
2. **Offer Expiration:** How long do offers last? (Suggested: 7 days)
3. **Negotiation Rounds:** Max rounds for v1.0? (Suggested: 3)
4. **Job Generation Frequency:** How often check for vacancies? (Suggested: Daily)
5. **AI Offer Logic:** How does AI decide to make offer? (Suggested: Simple match percentage)
6. **AI Negotiation Logic:** How does AI respond to counter-offers? (Suggested: Accept if within 10% of original)

---

## 14. Next Steps

1. **Review this analysis** - Confirm approach and decisions
2. **Create detailed design document** - Based on approved approach
3. **Start Phase 1** - Data structures and club staff tracking
4. **Iterate** - Build and test each phase

---

**Document Version:** 1.0  
**Last Updated:** 2025-12-14  
**Status:** Analysis Complete - Ready for Design Phase

