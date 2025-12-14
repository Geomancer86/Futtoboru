# Testing Round #1 - Unemployed Job System

## Test Date: 2025-12-14
## Branch: `cursor/feature-unemployed-job-system`

---

## Pre-Testing Setup

1. ✅ Game builds successfully
2. ✅ Game runs without crashes
3. ✅ Start as "Retired Player" (unemployed profession)

---

## Test Cases

### Test 1: Job Board Display
**Steps:**
1. Start new game
2. Select "Retired Player" profession
3. Complete game setup
4. Click "Job Board" button

**Expected:**
- Job Board screen displays
- List of available job openings shown
- Each job shows: Club name, Position, Salary, Contract length, Deadline
- "Apply" button visible for each job

**Actual:** [ ] PASS / [ ] FAIL
**Notes:**

---

### Test 2: Job Opening Creation
**Steps:**
1. Check if job openings exist on game start
2. Verify jobs are for vacant positions (Manager, Director, Scout)

**Expected:**
- Job openings created automatically for all clubs with vacant positions
- At least some jobs visible in Job Board

**Actual:** [ ] PASS / [ ] FAIL
**Notes:**

---

### Test 3: Apply for Job
**Steps:**
1. Go to Job Board
2. Click "Apply" on a job
3. Check console/logs for application creation

**Expected:**
- Application created successfully
- Application appears in "My Applications"
- Status shows "PENDING"
- Match percentage displayed

**Actual:** [ ] PASS / [ ] FAIL
**Notes:**

---

### Test 4: Application Limit
**Steps:**
1. Apply for 3 jobs
2. Try to apply for a 4th job

**Expected:**
- First 3 applications succeed
- 4th application fails (limit reached)
- Error message or no action on 4th attempt

**Actual:** [ ] PASS / [ ] FAIL
**Notes:**

---

### Test 5: Offer Generation
**Steps:**
1. Apply for a job (with match >= 70%)
2. Check "My Applications" screen
3. Check Inbox

**Expected:**
- Application status changes to "OFFER_RECEIVED" (green)
- Job offer notification in Inbox
- Offer appears in Job Offer screen

**Actual:** [ ] PASS / [ ] FAIL
**Notes:**

---

### Test 6: View Job Offer
**Steps:**
1. Go to "My Applications"
2. Click "View Offer" button (if offer received)
3. OR go to Inbox and check offer message

**Expected:**
- Job Offer screen displays
- Shows: Club name, Salary, Contract length
- Shows: Accept, Reject, Negotiate buttons

**Actual:** [ ] PASS / [ ] FAIL
**Notes:**

---

### Test 7: Accept Offer
**Steps:**
1. View a job offer
2. Click "Accept" button

**Expected:**
- Offer accepted
- Player becomes employed
- Menu updates (manager screens appear)
- Player's currentClubId set
- Job opening marked as FILLED

**Actual:** [ ] PASS / [ ] FAIL
**Notes:**

---

### Test 8: Reject Offer
**Steps:**
1. View a job offer
2. Click "Reject" button

**Expected:**
- Offer rejected
- Offer removed from pending offers
- Player remains unemployed
- Can still apply for other jobs

**Actual:** [ ] PASS / [ ] FAIL
**Notes:**

---

### Test 9: Negotiate Offer - Basic
**Steps:**
1. View a job offer
2. Click "Negotiate" button
3. Adjust salary (within reasonable range)
4. Adjust contract length
5. Click "Submit Counter-Offer"

**Expected:**
- Negotiation screen displays
- Can input new salary and contract length
- Counter-offer submitted
- AI processes counter-offer
- If accepted: Original offer updated with new terms
- If rejected: Counter-offer rejected, original offer remains

**Actual:** [ ] PASS / [ ] FAIL
**Notes:**

---

### Test 10: Negotiation - AI Accepts
**Steps:**
1. Negotiate with counter-offer within 10% of original salary
2. Contract length within 3 months of original

**Expected:**
- AI accepts counter-offer
- Original offer updated with new terms
- Offer status returns to PENDING
- Can accept updated offer

**Actual:** [ ] PASS / [ ] FAIL
**Notes:**

---

### Test 11: Negotiation - AI Rejects
**Steps:**
1. Negotiate with counter-offer > 10% above original salary
2. OR contract length > 3 months different

**Expected:**
- AI rejects counter-offer
- Original offer remains unchanged
- Can try again (if rounds remaining)

**Actual:** [ ] PASS / [ ] FAIL
**Notes:**

---

### Test 12: Negotiation Round Limit
**Steps:**
1. Negotiate 3 times (submit 3 counter-offers)
2. Try to negotiate a 4th time

**Expected:**
- First 3 negotiations work
- 4th negotiation fails (max rounds reached)
- Negotiate button disabled or error shown

**Actual:** [ ] PASS / [ ] FAIL
**Notes:**

---

### Test 13: Full Flow - Apply → Offer → Negotiate → Accept
**Steps:**
1. Apply for a job
2. Receive offer
3. Negotiate offer (get better terms)
4. Accept negotiated offer

**Expected:**
- Complete flow works end-to-end
- Player becomes employed with negotiated terms
- Menu updates correctly
- Game state persists correctly

**Actual:** [ ] PASS / [ ] FAIL
**Notes:**

---

### Test 14: Daily Job Updates
**Steps:**
1. Start game
2. Continue game several days
3. Check Job Board periodically

**Expected:**
- New job openings created for newly vacant positions
- Expired job openings removed
- Job Board updates correctly

**Actual:** [ ] PASS / [ ] FAIL
**Notes:**

---

### Test 15: Multiple Offers
**Steps:**
1. Apply for multiple jobs (that meet requirements)
2. Receive multiple offers

**Expected:**
- All offers appear in Job Offer screen
- Can accept/reject/negotiate each independently
- Accepting one offer rejects others for same job

**Actual:** [ ] PASS / [ ] FAIL
**Notes:**

---

## Known Issues / Bugs Found

1. **Issue:** 
   **Steps to Reproduce:**
   **Expected:**
   **Actual:**
   **Severity:** [ ] Critical / [ ] High / [ ] Medium / [ ] Low

2. **Issue:**
   **Steps to Reproduce:**
   **Expected:**
   **Actual:**
   **Severity:** [ ] Critical / [ ] High / [ ] Medium / [ ] Low

---

## Test Results Summary

- **Total Tests:** 15
- **Passed:** ___
- **Failed:** ___
- **Blocked:** ___

**Critical Issues:** ___
**High Priority Issues:** ___
**Medium Priority Issues:** ___
**Low Priority Issues:** ___

---

## Next Steps

- [ ] Fix critical issues
- [ ] Fix high priority issues
- [ ] Re-test fixed issues
- [ ] Polish UI based on feedback
- [ ] Add missing features (if any)

---

**Tester:** _____________
**Date:** _____________
**Build Version:** 0.3.0-SNAPSHOT

