# Unemployed Character & Job Interview System - Design Document

## Status: DESIGNED - NOT IMPLEMENTED

**Priority:** Medium (deferred to post-v1.0)  
**Estimated Implementation Time:** 2-3 weeks  
**Dependencies:** Match simulation, Club management, Financial system

---

## 1. Current State Analysis

### 1.1 What Exists
- ✅ **Profession System**: 8 professions defined (Player, Retired Player, Manager, Director, Scout, Agent, Investor, Club Owner)
- ✅ **Unemployed Detection**: Code checks for profession ID 2 (Retired Player) in `MainMenuManager`
- ✅ **Basic Menu Structure**: Unemployed players see limited menu (Home, Inbox, Personal Details, Authority)
- ✅ **Person/Club Relationship**: `Person.currentClubId` field exists (can be null)
- ✅ **Script System**: Framework exists for scripted events/messages

### 1.2 What's Missing
- ❌ **Job Board/Listings Screen**: No UI to view available jobs
- ❌ **Job Application System**: No way to apply for jobs
- ❌ **Job Interview Logic**: No interview mechanics
- ❌ **Job Offer System**: No way to receive/accept/reject offers
- ❌ **AI Job Generation**: Clubs don't create job openings
- ❌ **Job Requirements**: No system to match player qualifications to job requirements
- ❌ **Reputation System**: No reputation that affects job opportunities
- ❌ **Job Search Filters**: No way to filter jobs by league, country, salary, etc.

---

## 2. System Requirements

### 2.1 Functional Requirements

#### FR1: Job Board
- Display list of available jobs
- Filter by: profession, country, league, club reputation, salary range
- Sort by: salary, club reputation, application deadline
- Show job details: club, position, salary, contract length, requirements

#### FR2: Job Application
- Apply for jobs from job board
- Track application status (pending, rejected, interview scheduled, offer received)
- Limit applications per time period (e.g., 3 per week)
- Application history/status screen

#### FR3: Job Interview
- Receive interview invitations
- Interview scheduling (date/time)
- Interview questions/answers (text-based or multiple choice)
- Interview outcomes (success/failure with feedback)
- Multiple interview rounds for high-profile jobs

#### FR4: Job Offers
- Receive job offers after successful interview
- View offer details: salary, contract length, bonuses, expectations
- Accept/reject/negotiate offers
- Negotiation system (counter-offers)

#### FR5: AI Job Generation
- Clubs create job openings when:
  - Manager is fired/resigns
  - New club is created
  - Club expands (needs scout, director, etc.)
- Job openings expire after deadline
- AI selects best candidate if player doesn't apply

#### FR6: Job Requirements Matching
- Each job has requirements:
  - Minimum reputation/experience
  - Preferred nationality
  - Required licenses/certifications
  - Preferred playing style (for managers)
- Player qualifications checked against requirements
- Match percentage shown on job listing

#### FR7: Reputation System
- Player reputation affects:
  - Job availability (better jobs require higher rep)
  - Interview success chance
  - Salary negotiation power
- Reputation gained from:
  - Match results (as manager)
  - League position
  - Cup wins
  - Player development
  - Time unemployed (decreases slowly)

### 2.2 Non-Functional Requirements
- **Performance**: Job board should load quickly (< 1 second)
- **Scalability**: Support 100+ active job openings
- **User Experience**: Intuitive job search and application flow
- **Localization**: All job-related text translatable

---

## 3. Data Models

### 3.1 JobOpening
```java
public class JobOpening implements Serializable {
    private Long id;
    private Club club;
    private Profession profession;
    private LocalDateTime postedDate;
    private LocalDateTime applicationDeadline;
    private LocalDateTime interviewDate; // if scheduled
    private JobStatus status; // OPEN, INTERVIEW_SCHEDULED, FILLED, EXPIRED
    private JobOffer offer; // if offer made
    
    // Requirements
    private Integer minReputation;
    private Country preferredNationality;
    private List<License> requiredLicenses;
    private Map<String, Object> professionSpecificRequirements;
    
    // Compensation
    private BigDecimal salary;
    private Integer contractLengthMonths;
    private BigDecimal signingBonus;
    
    // Applicants
    private List<JobApplication> applications;
    private Person selectedCandidate; // if filled
}
```

### 3.2 JobApplication
```java
public class JobApplication implements Serializable {
    private Long id;
    private Person applicant;
    private JobOpening jobOpening;
    private LocalDateTime applicationDate;
    private ApplicationStatus status; // PENDING, REJECTED, INTERVIEW_SCHEDULED, OFFER_RECEIVED, ACCEPTED, WITHDRAWN
    private String coverLetter; // optional
    private Integer matchPercentage; // how well applicant matches requirements
}
```

### 3.3 JobInterview
```java
public class JobInterview implements Serializable {
    private Long id;
    private JobApplication application;
    private LocalDateTime scheduledDate;
    private InterviewStatus status; // SCHEDULED, COMPLETED, CANCELLED
    private List<InterviewQuestion> questions;
    private Map<InterviewQuestion, String> answers; // player's answers
    private InterviewResult result; // SUCCESS, FAILURE
    private String feedback; // why succeeded/failed
    private Integer score; // 0-100
}
```

### 3.4 JobOffer
```java
public class JobOffer implements Serializable {
    private Long id;
    private JobOpening jobOpening;
    private Person recipient;
    private LocalDateTime offerDate;
    private LocalDateTime expirationDate;
    private OfferStatus status; // PENDING, ACCEPTED, REJECTED, NEGOTIATING, EXPIRED
    
    // Terms
    private BigDecimal salary;
    private Integer contractLengthMonths;
    private BigDecimal signingBonus;
    private BigDecimal performanceBonus;
    private List<String> expectations; // e.g., "Finish top 3", "Avoid relegation"
    
    // Negotiation
    private JobOffer counterOffer; // if negotiating
    private Integer negotiationRound;
}
```

### 3.5 Person Extensions
```java
// Add to Person class:
private Integer reputation; // 0-100
private List<License> licenses;
private List<JobApplication> activeApplications;
private List<JobOffer> pendingOffers;
private LocalDateTime lastJobApplicationDate;
private Integer applicationsThisWeek;
```

---

## 4. System Architecture

### 4.1 Components

#### 4.1.1 JobManager
**Responsibility**: Manage job openings, applications, interviews, offers
- Create job openings (AI-driven)
- Process applications
- Schedule interviews
- Generate offers
- Match requirements

**Key Methods:**
```java
public class JobManager {
    public List<JobOpening> getAvailableJobs(Profession profession, Country country);
    public void createJobOpening(Club club, Profession profession, JobRequirements requirements);
    public JobApplication applyForJob(Person applicant, JobOpening job);
    public void scheduleInterview(JobApplication application, LocalDateTime date);
    public InterviewResult conductInterview(JobInterview interview);
    public JobOffer makeOffer(JobOpening job, Person candidate);
    public void processJobOffer(JobOffer offer, OfferResponse response);
    public void updateJobOpenings(); // called daily by game engine
}
```

#### 4.1.2 ReputationManager
**Responsibility**: Calculate and update player reputation
- Calculate reputation based on achievements
- Update reputation over time
- Provide reputation breakdown

**Key Methods:**
```java
public class ReputationManager {
    public Integer calculateReputation(Person person);
    public void updateReputation(Person person);
    public ReputationBreakdown getReputationBreakdown(Person person);
    public Integer getReputationForProfession(Person person, Profession profession);
}
```

#### 4.1.3 RequirementMatcher
**Responsibility**: Match player qualifications to job requirements
- Calculate match percentage
- Check if player meets minimum requirements
- Provide detailed match breakdown

**Key Methods:**
```java
public class RequirementMatcher {
    public Integer calculateMatchPercentage(Person person, JobOpening job);
    public boolean meetsMinimumRequirements(Person person, JobOpening job);
    public MatchBreakdown getMatchBreakdown(Person person, JobOpening job);
}
```

### 4.2 Integration Points

#### 4.2.1 Game Engine Integration
- **Daily Update**: `JobManager.updateJobOpenings()` called each day
- **Event Triggers**: Job openings created when:
  - Manager fired (via `Club.fireManager()`)
  - New club created
  - Club expands staff

#### 4.2.2 Script System Integration
- Welcome message for unemployed players with link to Job Board
- Job offer notifications via inbox
- Interview invitation messages

#### 4.2.3 Save Game Integration
- All job-related data stored in `SaveGame`:
  - `List<JobOpening> activeJobOpenings`
  - `List<JobApplication> allApplications`
  - `List<JobOffer> pendingOffers`

---

## 5. UI/UX Design

### 5.1 Job Board Screen

**Layout:**
```
┌─────────────────────────────────────────────────────────┐
│ Job Board                                    [Refresh]   │
├─────────────────────────────────────────────────────────┤
│ Filters:                                                 │
│ [Profession ▼] [Country ▼] [League ▼] [Reputation ▼]   │
│ [Salary Range: ___ to ___] [Apply Filters]              │
├─────────────────────────────────────────────────────────┤
│ Sort by: [Salary ▼] [Reputation ▼] [Deadline ▼]        │
├─────────────────────────────────────────────────────────┤
│ Available Jobs (23)                                      │
│                                                          │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Manchester United FC                                 │ │
│ │ Manager Position                                     │ │
│ │ Salary: £50,000/year | Contract: 2 years            │ │
│ │ Requirements: Reputation 70+ | English preferred    │ │
│ │ Match: 85% | Deadline: 15 Jan 1889                  │ │
│ │ [View Details] [Apply]                              │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                          │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ ... (more jobs)                                     │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

**Features:**
- Filter panel at top
- Job cards with key information
- Match percentage indicator (color-coded)
- Quick apply button
- View details modal

### 5.2 Job Details Modal

**Shows:**
- Full club information
- Complete job requirements
- Detailed match breakdown
- Salary and contract details
- Application deadline
- Current number of applicants (if visible)

### 5.3 Application Status Screen

**Layout:**
```
┌─────────────────────────────────────────────────────────┐
│ My Applications                                         │
├─────────────────────────────────────────────────────────┤
│ Active Applications (2)                                 │
│                                                          │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Manchester United FC - Manager                      │ │
│ │ Applied: 10 Jan 1889 | Status: Interview Scheduled  │ │
│ │ Interview: 20 Jan 1889, 14:00                       │ │
│ │ [View Details] [Withdraw]                           │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                          │
│ Past Applications (5)                                    │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ ... (rejected/expired applications)                 │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 5.4 Interview Screen

**Layout:**
```
┌─────────────────────────────────────────────────────────┐
│ Interview: Manchester United FC                         │
├─────────────────────────────────────────────────────────┤
│                                                          │
│ Question 1 of 5:                                        │
│ "What is your preferred tactical approach?"            │
│                                                          │
│ [ ] Attacking football                                  │
│ [ ] Defensive stability                                 │
│ [ ] Possession-based                                    │
│ [ ] Counter-attacking                                   │
│                                                          │
│ [Previous] [Next]                                       │
│                                                          │
│ Progress: ████████░░░░░░░░░░ 40%                        │
└─────────────────────────────────────────────────────────┘
```

**Features:**
- Multiple choice or text input questions
- Progress indicator
- Can't go back after answering
- Results shown after completion

### 5.5 Job Offer Screen

**Layout:**
```
┌─────────────────────────────────────────────────────────┐
│ Job Offer: Manchester United FC                         │
├─────────────────────────────────────────────────────────┤
│                                                          │
│ Position: Manager                                       │
│ Salary: £50,000/year                                    │
│ Contract: 2 years                                      │
│ Signing Bonus: £5,000                                  │
│                                                          │
│ Expectations:                                           │
│ • Finish in top 3 of First Division                    │
│ • Reach FA Cup quarter-finals                          │
│                                                          │
│ Offer expires: 25 Jan 1889                             │
│                                                          │
│ [Accept] [Reject] [Negotiate]                          │
└─────────────────────────────────────────────────────────┘
```

### 5.6 Menu Integration

**Unemployed Menu:**
- Home
- Job Board (NEW)
- My Applications (NEW)
- Inbox
- Personal Details
- Authority

---

## 6. Gameplay Flow

### 6.1 Job Application Flow

```
1. Player opens Job Board
2. Filters/searches for jobs
3. Views job details
4. Clicks "Apply"
   - Check: Has player applied this week? (limit 3)
   - Check: Does player meet minimum requirements?
   - Calculate match percentage
   - Create JobApplication (status: PENDING)
5. Application appears in "My Applications"
6. AI processes application (after 1-3 days)
   - If match < 50%: REJECTED
   - If match 50-70%: May schedule interview
   - If match > 70%: Likely to schedule interview
7. If interview scheduled:
   - Notification in inbox
   - Interview appears in "My Applications"
8. Player attends interview (on scheduled date)
9. Interview results calculated
10. If successful: Job offer received
11. Player accepts/rejects/negotiates offer
12. If accepted: Player becomes employed, menu changes
```

### 6.2 AI Job Generation Flow

```
1. Daily update (Game Engine):
   - Check for clubs without managers
   - Check for clubs needing staff
   - Check for new clubs
2. For each club needing staff:
   - Determine job requirements based on club level
   - Create JobOpening
   - Set deadline (7-14 days)
3. Job appears on Job Board
4. If deadline passes without suitable applicant:
   - AI selects best available candidate
   - Or job expires and reposted later
```

---

## 7. Interview System Design

### 7.1 Interview Questions

**Question Types:**
1. **Tactical Questions** (for Managers)
   - Preferred formation
   - Playing style
   - Youth development approach

2. **Experience Questions**
   - Previous achievements
   - Handling pressure
   - Team management style

3. **Club-Specific Questions**
   - Knowledge of club history
   - Understanding of club goals
   - Fit with club culture

### 7.2 Scoring System

**Interview Score Calculation:**
- Base score: 50 (neutral)
- Correct answers: +10 each
- Partial answers: +5 each
- Wrong answers: -5 each
- Reputation bonus: +0 to +20
- Experience bonus: +0 to +15

**Success Threshold:**
- Score 60-70: Marginal (may get offer if no better candidates)
- Score 70-85: Good (likely to get offer)
- Score 85+: Excellent (definite offer, may negotiate)

### 7.3 Interview Outcomes

**Success:**
- Job offer received within 1-2 days
- Offer details depend on interview score

**Failure:**
- Feedback message explaining why
- Can reapply after cooldown period (30 days)

---

## 8. Implementation Phases

### Phase 1: Core Data Models (3-4 days)
- [ ] Create `JobOpening` class
- [ ] Create `JobApplication` class
- [ ] Create `JobInterview` class
- [ ] Create `JobOffer` class
- [ ] Extend `Person` with reputation/application fields
- [ ] Add to `SaveGame` serialization

### Phase 2: Job Manager & Matching (4-5 days)
- [ ] Implement `JobManager` class
- [ ] Implement `RequirementMatcher` class
- [ ] Implement `ReputationManager` class
- [ ] Job opening creation logic
- [ ] Application processing logic
- [ ] Requirement matching algorithm

### Phase 3: UI - Job Board (3-4 days)
- [ ] Create `JobBoardScreenTable` class
- [ ] Implement filtering UI
- [ ] Implement sorting
- [ ] Job card display
- [ ] Job details modal
- [ ] Apply button functionality

### Phase 4: UI - Applications & Interviews (3-4 days)
- [ ] Create `ApplicationsScreenTable` class
- [ ] Application status display
- [ ] Interview screen UI
- [ ] Interview question system
- [ ] Interview scoring logic

### Phase 5: Job Offers & Negotiation (2-3 days)
- [ ] Job offer display screen
- [ ] Accept/reject functionality
- [ ] Negotiation system
- [ ] Contract signing logic

### Phase 6: AI Integration (2-3 days)
- [ ] Daily job generation
- [ ] AI candidate selection
- [ ] Job expiration logic
- [ ] Integration with club management

### Phase 7: Polish & Testing (2-3 days)
- [ ] Localization
- [ ] Error handling
- [ ] Edge case testing
- [ ] Performance optimization

**Total Estimated Time: 19-26 days (3.5-4 weeks)**

---

## 9. Technical Considerations

### 9.1 Performance
- **Job Board Loading**: Cache job list, only refresh on demand
- **Matching Algorithm**: Pre-calculate match percentages, cache results
- **Daily Updates**: Batch process all job updates in single pass

### 9.2 Data Persistence
- All job data saved in `SaveGame`
- Job openings persist across game sessions
- Application history maintained

### 9.3 Scalability
- Support 100+ active job openings
- Efficient filtering/sorting algorithms
- Lazy loading for job details

### 9.4 Localization
- All job-related text in language bundles
- Dynamic text generation for job descriptions
- Interview questions translatable

---

## 10. Future Enhancements (Post-v1.0)

### 10.1 Advanced Features
- **Job Recommendations**: AI suggests jobs based on player profile
- **Networking System**: Relationships affect job opportunities
- **Agent System**: Hire agent to find jobs/negotiate
- **Job History**: Track career progression
- **Reputation Breakdown**: Detailed reputation metrics

### 10.2 Gameplay Depth
- **Multiple Interview Rounds**: For high-profile jobs
- **Reference System**: Previous employers provide references
- **Job Security**: Risk of being fired affects job search
- **Contract Negotiations**: More complex negotiation system

### 10.3 UI Improvements
- **Job Alerts**: Notifications for new matching jobs
- **Saved Searches**: Save filter combinations
- **Application Templates**: Pre-written cover letters
- **Interview Preparation**: Tips based on club/position

---

## 11. Dependencies & Prerequisites

### 11.1 Required Systems (Must Exist First)
- ✅ Match simulation (for reputation calculation)
- ✅ Club management (for job creation)
- ✅ Financial system (for salary/contracts)
- ✅ Script system (for notifications)
- ✅ Inbox system (for messages)

### 11.2 Nice to Have (Can Add Later)
- Reputation visualization
- Advanced filtering
- Job search history
- Application analytics

---

## 12. Testing Scenarios

### 12.1 Test Cases

**TC1: Basic Job Application**
1. Player opens Job Board
2. Applies for job
3. Application appears in "My Applications"
4. Status updates correctly

**TC2: Interview Flow**
1. Application accepted for interview
2. Interview scheduled
3. Player completes interview
4. Results calculated correctly
5. Offer received if successful

**TC3: Job Offer Acceptance**
1. Player receives offer
2. Accepts offer
3. Player becomes employed
4. Menu updates correctly
5. Club relationship established

**TC4: AI Job Generation**
1. Manager fired
2. Job opening created automatically
3. Job appears on Job Board
4. AI fills position if no player applies

**TC5: Requirement Matching**
1. Player with low reputation
2. High-reputation job not visible
3. Player improves reputation
4. Job becomes visible

---

## 13. Open Questions / Decisions Needed

1. **Application Limits**: How many applications per week? (Suggested: 3)
2. **Interview Timing**: Real-time or instant? (Suggested: Instant with scheduled date)
3. **Negotiation Complexity**: Simple accept/reject or full negotiation? (Suggested: Start simple)
4. **Reputation Decay**: How fast does reputation decrease when unemployed? (Suggested: -1 per month)
5. **Job Visibility**: Show all jobs or filter by reputation? (Suggested: Filter by reputation)

---

## 14. References

- Football Manager job interview system
- Real-world football manager hiring process
- Existing code: `MainMenuManager.setDynamicButtonsMenu()`
- Existing code: `NewGameOverviewScreen` (unemployed start logic)

---

**Document Version:** 1.0  
**Last Updated:** 2025-12-14  
**Status:** Design Complete - Ready for Implementation (Post-v1.0)

