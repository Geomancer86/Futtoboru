# Post-Application Management Features - Analysis

## Overview

After a player applies for a job, there should be a comprehensive system to manage the application, negotiate terms, and handle the entire process until acceptance or rejection.

## Current State (v1.0)

### What Works:
- ✅ Player can apply for jobs
- ✅ Applications are created and stored
- ✅ Applications are processed (instant in v1.0)
- ✅ Offers are generated if match percentage >= 70%
- ✅ Basic offer acceptance/rejection
- ✅ Basic negotiation (counter-offers)

### What's Missing:
- ❌ Application management UI (view, edit, cancel applications)
- ❌ Detailed proposal customization (salary, bonuses, expectations, position preferences)
- ❌ Application status tracking and updates
- ❌ Withdrawal of applications
- ❌ Editing applications before processing
- ❌ Multiple application management
- ❌ Application history

## Feature Scope Analysis

### 1. Application Management (Core Features)

#### 1.1 View Application Details
**Priority:** HIGH  
**Complexity:** LOW  
**Description:**
- View full application details (club, position, date applied, status, match percentage)
- See application history for the same position
- View related offers (if any)

**UI Requirements:**
- Expandable application row in "My Applications" screen
- Detailed view modal/dialog
- Show all relevant information

**Data Model:**
- Already exists: `JobApplication` has all needed fields
- May need: `applicationNotes` field for player notes

**Estimated Time:** 2 hours

---

#### 1.2 Cancel/Withdraw Application
**Priority:** HIGH  
**Complexity:** LOW  
**Description:**
- Allow player to withdraw an application before it's processed
- After processing, applications can only be withdrawn if status is PENDING
- Once OFFER_RECEIVED, must accept/reject/negotiate offer instead

**UI Requirements:**
- "Withdraw" button on PENDING applications
- Confirmation dialog
- Update application status to WITHDRAWN
- Remove from active applications list

**Data Model:**
- Already exists: `ApplicationStatus.WITHDRAWN`
- Already exists: `JobManager.withdrawApplication()`

**Estimated Time:** 1 hour

---

#### 1.3 Edit Application (Pre-Processing)
**Priority:** MEDIUM  
**Complexity:** MEDIUM  
**Description:**
- Allow editing application details before it's processed
- Once processed (status != PENDING), cannot edit
- May need to re-submit application with new details

**UI Requirements:**
- "Edit" button on PENDING applications
- Edit dialog with application fields
- Save changes

**Data Model:**
- May need: `applicationVersion` or `lastModifiedDate`
- May need: track if application was edited

**Estimated Time:** 3 hours

---

### 2. Proposal Customization (Advanced Features)

#### 2.1 Salary Negotiation in Application
**Priority:** MEDIUM  
**Complexity:** MEDIUM  
**Description:**
- Player can specify desired salary range in application
- Club may consider this when making offer
- If player's desired salary is too high, may affect match percentage or offer

**UI Requirements:**
- Salary input field when applying
- Min/Max salary range
- Display desired salary in application details

**Data Model:**
- Add to `JobApplication`: `desiredSalary` (BigDecimal)
- May need: `salaryNegotiable` (boolean)

**Estimated Time:** 2 hours

---

#### 2.2 Contract Length Preferences
**Priority:** MEDIUM  
**Complexity:** LOW  
**Description:**
- Player can specify preferred contract length
- Club considers this when making offer
- Affects negotiation process

**UI Requirements:**
- Contract length slider/input when applying
- Display in application details

**Data Model:**
- Add to `JobApplication`: `desiredContractLengthMonths` (Integer)

**Estimated Time:** 1 hour

---

#### 2.3 Bonuses and Benefits
**Priority:** LOW (v2.0+)  
**Complexity:** HIGH  
**Description:**
- Signing bonus
- Performance bonuses
- Other benefits (housing, car, etc.)
- Very complex, should be deferred

**UI Requirements:**
- Complex form with multiple bonus types
- Calculation logic
- Display in offers

**Data Model:**
- New class: `ContractBenefits`
- Multiple fields for different bonus types

**Estimated Time:** 8+ hours

---

#### 2.4 Position Preferences
**Priority:** LOW (v2.0+)  
**Complexity:** MEDIUM  
**Description:**
- Player may want specific role (e.g., "First Team Manager" vs "Youth Team Manager")
- May want specific responsibilities
- Very specific, may not be needed for v1.0

**UI Requirements:**
- Dropdown/selection for position type
- Text field for responsibilities

**Data Model:**
- Add to `JobApplication`: `positionPreferences` (String or enum)

**Estimated Time:** 3 hours

---

#### 2.5 Expectations and Goals
**Priority:** LOW (v2.0+)  
**Complexity:** MEDIUM  
**Description:**
- Player can state expectations (e.g., "Promotion in 2 years")
- Club may consider this when making offer
- Affects contract negotiations

**UI Requirements:**
- Text area for expectations
- May link to club goals system

**Data Model:**
- Add to `JobApplication`: `expectations` (String)

**Estimated Time:** 2 hours

---

### 3. Negotiation System (Enhanced)

#### 3.1 Multi-Round Negotiation
**Priority:** MEDIUM  
**Complexity:** MEDIUM  
**Description:**
- Current system supports 3 rounds (MAX_NEGOTIATION_ROUNDS)
- Need better UI to show negotiation history
- Show what was offered in each round
- Show why counter-offer was accepted/rejected

**UI Requirements:**
- Negotiation history view
- Show each round's offer
- Show AI reasoning (if available)

**Data Model:**
- Already exists: `JobOffer.negotiationRound`
- May need: `negotiationHistory` (List<NegotiationRound>)

**Estimated Time:** 4 hours

---

#### 3.2 Negotiation Reasons/Feedback
**Priority:** LOW (v2.0+)  
**Complexity:** MEDIUM  
**Description:**
- AI provides feedback on why offer was accepted/rejected
- Helps player understand negotiation better
- "Your counter-offer was too high" or "We accept your terms"

**UI Requirements:**
- Message display after negotiation
- May be in inbox or negotiation screen

**Data Model:**
- Add to `JobOffer`: `negotiationFeedback` (String)
- Or use `Message` system

**Estimated Time:** 2 hours

---

#### 3.3 Negotiation Deadlines
**Priority:** LOW  
**Complexity:** LOW  
**Description:**
- Show time remaining to respond to offer
- Warn player if deadline approaching
- Auto-reject if deadline passes

**UI Requirements:**
- Countdown timer on offers
- Warning messages
- Already implemented: `JobOffer.expirationDate`

**Data Model:**
- Already exists: `JobOffer.expirationDate`

**Estimated Time:** 1 hour

---

### 4. Application Status and Tracking

#### 4.1 Real-Time Status Updates
**Priority:** HIGH  
**Complexity:** LOW  
**Description:**
- Application status updates in real-time
- Notifications when status changes
- Visual indicators (colors, icons)

**UI Requirements:**
- Status badges with colors
- Auto-refresh application list
- Toast notifications on status change

**Data Model:**
- Already exists: `ApplicationStatus` enum
- Already exists: status tracking

**Estimated Time:** 2 hours

---

#### 4.2 Application Timeline
**Priority:** MEDIUM  
**Complexity:** MEDIUM  
**Description:**
- Show timeline of application events
- Date applied, date processed, date offer received, etc.
- Visual timeline view

**UI Requirements:**
- Timeline component
- Event markers
- Date labels

**Data Model:**
- May need: `ApplicationEvent` class
- Or use existing dates in `JobApplication` and `JobOffer`

**Estimated Time:** 3 hours

---

#### 4.3 Multiple Applications Management
**Priority:** HIGH  
**Complexity:** LOW  
**Description:**
- View all applications in one place
- Filter by status, club, position
- Sort by date, match percentage, etc.

**UI Requirements:**
- Enhanced "My Applications" screen
- Filter controls
- Sort controls
- Already partially implemented

**Data Model:**
- Already exists: `JobManager.getPlayerApplications()`

**Estimated Time:** 2 hours

---

## Implementation Priority

### Phase 1: Critical (v1.0)
1. **Cancel/Withdraw Application** (1 hour) - HIGH priority
2. **View Application Details** (2 hours) - HIGH priority
3. **Multiple Applications Management** (2 hours) - HIGH priority
4. **Real-Time Status Updates** (2 hours) - HIGH priority

**Total Phase 1:** ~7 hours

### Phase 2: Important (v1.1)
1. **Edit Application** (3 hours) - MEDIUM priority
2. **Salary Negotiation in Application** (2 hours) - MEDIUM priority
3. **Contract Length Preferences** (1 hour) - MEDIUM priority
4. **Multi-Round Negotiation UI** (4 hours) - MEDIUM priority

**Total Phase 2:** ~10 hours

### Phase 3: Nice to Have (v2.0+)
1. **Bonuses and Benefits** (8+ hours) - LOW priority
2. **Position Preferences** (3 hours) - LOW priority
3. **Expectations and Goals** (2 hours) - LOW priority
4. **Negotiation Reasons/Feedback** (2 hours) - LOW priority
5. **Application Timeline** (3 hours) - MEDIUM priority

**Total Phase 3:** ~18+ hours

---

## Technical Considerations

### Data Model Changes Needed

#### JobApplication Enhancements:
```java
// Phase 1 (v1.0)
- No changes needed (all fields exist)

// Phase 2 (v1.1)
+ desiredSalary: BigDecimal
+ desiredContractLengthMonths: Integer
+ applicationNotes: String (for player notes)
+ lastModifiedDate: LocalDateTime

// Phase 3 (v2.0+)
+ positionPreferences: String
+ expectations: String
+ contractBenefits: ContractBenefits (new class)
```

#### New Classes (Phase 3):
```java
ContractBenefits {
    signingBonus: BigDecimal
    performanceBonus: BigDecimal
    housingAllowance: BigDecimal
    carAllowance: BigDecimal
    // etc.
}
```

### UI Screen Requirements

#### Enhanced My Applications Screen:
- Filter by: Status, Club, Position, Date
- Sort by: Date, Match %, Status
- View details button
- Withdraw button (for PENDING)
- Edit button (for PENDING, Phase 2)

#### Application Detail Dialog:
- Full application information
- Status and timeline
- Related offers
- Action buttons (withdraw, edit, etc.)

#### Enhanced Application Form (Phase 2):
- Salary input
- Contract length input
- Notes field
- Save/Cancel buttons

---

## Risk Assessment

### Low Risk:
- Cancel/Withdraw Application
- View Application Details
- Real-Time Status Updates
- Multiple Applications Management

### Medium Risk:
- Edit Application (may need to handle re-processing)
- Salary Negotiation (affects match calculation)
- Contract Length Preferences

### High Risk:
- Bonuses and Benefits (complex calculations)
- Position Preferences (may conflict with club needs)
- Negotiation Feedback (AI reasoning is complex)

---

## Success Criteria

### Phase 1 (v1.0):
- ✅ Player can view all applications
- ✅ Player can withdraw PENDING applications
- ✅ Application status updates correctly
- ✅ UI shows clear status indicators

### Phase 2 (v1.1):
- ✅ Player can edit PENDING applications
- ✅ Player can specify salary preferences
- ✅ Player can specify contract length preferences
- ✅ Negotiation UI shows history

### Phase 3 (v2.0+):
- ✅ Full proposal customization
- ✅ Complex benefits system
- ✅ Application timeline view
- ✅ AI negotiation feedback

---

## Recommendations

### For v1.0:
**Implement Phase 1 only:**
- Cancel/Withdraw Application
- View Application Details  
- Multiple Applications Management
- Real-Time Status Updates

**Rationale:**
- These are core features that complete the basic application flow
- Low complexity, high value
- Can be done quickly (~7 hours)
- Provides good user experience

### For v1.1:
**Implement Phase 2:**
- Edit Application
- Salary/Contract Preferences
- Enhanced Negotiation UI

**Rationale:**
- Builds on v1.0 foundation
- Adds customization without complexity
- Improves negotiation experience

### For v2.0+:
**Implement Phase 3:**
- Full benefits system
- Advanced preferences
- Timeline and feedback

**Rationale:**
- These are advanced features
- Require more design and testing
- Can be polished over multiple versions

---

## Next Steps

1. **Immediate (v1.0):**
   - Implement Phase 1 features
   - Test application management flow
   - Ensure UI is intuitive

2. **Short-term (v1.1):**
   - Design Phase 2 features
   - Implement salary/contract preferences
   - Enhance negotiation UI

3. **Long-term (v2.0+):**
   - Design benefits system
   - Implement advanced features
   - Polish and refine

---

## Questions to Consider

1. **Should applications be editable after processing?**
   - Current design: No (only PENDING can be edited)
   - Alternative: Allow editing but re-process application

2. **How detailed should proposal customization be?**
   - v1.0: Basic (salary, contract length)
   - v2.0: Advanced (bonuses, benefits, expectations)

3. **Should there be application limits?**
   - Current: 3 applications per week
   - Should this be configurable?

4. **How should negotiation history be displayed?**
   - Simple list?
   - Timeline view?
   - Detailed breakdown?

5. **Should AI provide negotiation feedback?**
   - Simple: "Accepted" / "Rejected"
   - Advanced: "Your salary request was 20% above our budget"

---

## Conclusion

The post-application management system is a **large feature set** that should be implemented in phases:

- **Phase 1 (v1.0):** Core management features (~7 hours)
- **Phase 2 (v1.1):** Customization features (~10 hours)  
- **Phase 3 (v2.0+):** Advanced features (~18+ hours)

**Total estimated time:** 35+ hours for full implementation

**Recommendation:** Start with Phase 1 for v1.0, then evaluate based on user feedback before proceeding to Phase 2.

