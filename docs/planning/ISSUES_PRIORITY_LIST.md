# Futtoboru - Known Issues & Priority List

## Critical Issues (Must Fix First)

### 0. **Continue Button Double-Click Bug** 🔴 CRITICAL
**Problem:** Rapid double-clicks on the Continue button can still process two days in a row, even with disabled state checks and listener removal.

**Root Cause:**
- LibGDX event queue may still process queued events even after listener removal
- Disabled state checks may not prevent all queued events
- Synchronized blocks and flag checks are not fully preventing race conditions

**Current Status:**
- Attempted fixes include: listener removal, disabled state checks, synchronized blocks, processing flags
- Bug persists - needs further investigation

**Impact:**
- Users can accidentally advance game by multiple days
- Critical for gameplay integrity

**Location:**
- `MainGameMenuTable.java` - `continueGame()` method and InputListener implementation
- TODO comment added to code

**Fix Required:**
- Investigate LibGDX event queue handling
- Consider alternative approaches (button swapping, event filtering at Stage level)
- May require deep understanding of LibGDX's event system internals

**Files:**
- `MainGameMenuTable.java`

**Estimated Time:** Unknown (requires research)

---

### 1. **Button Click Listeners Not Working** 🔴 CRITICAL
**Problem:** Apply buttons in JobBoardScreenTable and ClubDetailScreenTable don't respond to clicks. No logs appear when clicking.

**Root Cause:** 
- Using `addListener(new ClickListener())` instead of `addCaptureListener(new InputListener())`
- Working buttons in codebase use `addCaptureListener` with `InputListener` and `touchUp()` method
- Example: `HomeButton`, `JobBoardButton` use `addCaptureListener(new InputListener())`

**Impact:** 
- Cannot apply for jobs from any screen
- Job system is completely non-functional from UI perspective

**Fix Required:**
- Change all Apply buttons to use `addCaptureListener(new InputListener())`
- Use `touchUp()` method instead of `clicked()`
- Follow the pattern from `HomeButton.java` and `JobBoardButton.java`

**Files to Fix:**
- `JobBoardScreenTable.java` - Apply buttons
- `ClubDetailScreenTable.java` - Apply buttons
- `MyApplicationsScreenTable.java` - View Offer button
- `JobOfferScreenTable.java` - Accept/Reject/Negotiate buttons
- `NegotiationScreenTable.java` - Submit/Cancel buttons

**Estimated Time:** 30 minutes

---

### 2. **My Applications Screen Not Updating** 🟠 HIGH
**Problem:** My Applications screen shows nothing or doesn't update after applying.

**Root Cause:**
- Screen may not be refreshing after application is created
- `updateDynamicComponents()` may not be called after applying
- Applications may not be properly saved to SaveGame

**Impact:**
- User cannot see their applications
- Cannot track application status
- Cannot view offers

**Fix Required:**
- Ensure `updateDynamicComponents()` is called after applying
- Verify applications are saved to `currentGame.getAllApplications()`
- Add refresh mechanism when returning to My Applications screen

**Files to Fix:**
- `MyApplicationsScreenTable.java`
- `JobBoardScreenTable.java` (refresh after apply)
- `ClubDetailScreenTable.java` (refresh after apply)

**Estimated Time:** 20 minutes

---

### 3. **Logging Not Visible in Debug Console** 🟡 MEDIUM
**Problem:** Comprehensive logging added but not appearing in debug console.

**Root Cause:**
- `Gdx.app.log()` may not be outputting to console
- `System.out.println()` should work but may be buffered
- Debug console may not be capturing all output

**Impact:**
- Cannot debug issues effectively
- Cannot track user actions
- Hard to diagnose problems

**Fix Required:**
- Verify `run-game-debug.bat` is capturing all output
- Add explicit `System.out.flush()` calls
- Consider adding file logging as backup
- Test that logs appear when buttons are created (constructor logs)

**Files to Fix:**
- `run-game-debug.bat` (verify output redirection)
- All screen tables (add flush calls)
- `DesktopLauncher.java` (verify logging setup)

**Estimated Time:** 15 minutes

---

## Functional Issues (Fix After Critical)

### 4. **Job Application Flow Not Complete** 🟡 MEDIUM
**Problem:** After applying, user doesn't get feedback or see results.

**Root Cause:**
- No success/error messages after applying
- No automatic navigation to My Applications screen
- No toast notifications

**Impact:**
- Poor user experience
- User doesn't know if application was successful

**Fix Required:**
- Add toast notification after successful application
- Optionally navigate to My Applications screen
- Show error messages if application fails

**Files to Fix:**
- `JobBoardScreenTable.java`
- `ClubDetailScreenTable.java`
- `MainGameScreen.java` (toast manager)

**Estimated Time:** 20 minutes

---

### 5. **Job Offers Not Appearing** 🟡 MEDIUM
**Problem:** After applying, offers may not be generated or displayed.

**Root Cause:**
- `processApplication()` may not be creating offers
- Match percentage calculation may be too strict
- Offers may not be saved to SaveGame properly

**Impact:**
- Users apply but never get offers
- Job system appears broken

**Fix Required:**
- Verify `JobManager.processApplication()` is called
- Check match percentage threshold (currently 70%)
- Verify offers are added to `currentGame.getPendingOffers()`
- Add logging to offer creation process

**Files to Fix:**
- `JobManager.java` - `processApplication()`, `makeOffer()`
- `MyApplicationsScreenTable.java` - display offers

**Estimated Time:** 30 minutes

---

### 6. **Club Browser Screen Missing Functionality** 🟡 MEDIUM
**Problem:** Club Browser may not have all required features.

**Root Cause:**
- Right-click functionality may not be implemented
- Apply buttons may be missing
- Navigation may be broken

**Impact:**
- User cannot easily apply for jobs from club list
- Missing requirement: "right click send job offer to the teams when seen on the UI"

**Fix Required:**
- Implement right-click context menu
- Add Apply buttons to club browser
- Ensure navigation to Club Detail works

**Files to Fix:**
- `ClubBrowserScreenTable.java`

**Estimated Time:** 30 minutes

---

## Code Quality Issues (Fix After Functional)

### 7. **Inconsistent Button Pattern** 🟢 LOW
**Problem:** Some buttons use `addCaptureListener`, others use `addListener`.

**Root Cause:**
- Mixed patterns in codebase
- No clear standard

**Impact:**
- Code inconsistency
- Potential bugs from wrong pattern

**Fix Required:**
- Standardize on `addCaptureListener(new InputListener())` pattern
- Update all buttons to use same pattern
- Document the pattern

**Files to Fix:**
- All button implementations

**Estimated Time:** 1 hour

---

### 8. **Missing Error Handling** 🟢 LOW
**Problem:** Some operations don't have proper error handling.

**Root Cause:**
- Error handling added but may be incomplete
- Some edge cases not handled

**Impact:**
- Potential crashes
- Poor error messages

**Fix Required:**
- Review all error handling
- Add user-friendly error messages
- Ensure all exceptions are caught

**Files to Fix:**
- All job system files

**Estimated Time:** 1 hour

---

## Testing & Validation Issues

### 9. **No End-to-End Test** 🟡 MEDIUM
**Problem:** Cannot verify full flow works.

**Root Cause:**
- No test script or checklist
- Manual testing required

**Impact:**
- Hard to verify fixes work
- May miss edge cases

**Fix Required:**
- Create test checklist
- Document expected behavior
- Add automated tests if possible

**Estimated Time:** 30 minutes

---

## Summary

### Priority Order:
1. **Fix Button Click Listeners** (30 min) - CRITICAL
2. **Fix My Applications Screen** (20 min) - HIGH
3. **Fix Logging Visibility** (15 min) - MEDIUM
4. **Complete Application Flow** (20 min) - MEDIUM
5. **Fix Job Offers** (30 min) - MEDIUM
6. **Complete Club Browser** (30 min) - MEDIUM
7. **Standardize Button Pattern** (1 hour) - LOW
8. **Improve Error Handling** (1 hour) - LOW
9. **Create Test Checklist** (30 min) - MEDIUM

### Total Estimated Time: ~4.5 hours

### Immediate Action Plan:
1. Fix all Apply buttons to use `addCaptureListener(new InputListener())` pattern
2. Test that buttons respond to clicks
3. Verify logging appears in debug console
4. Test full application flow
5. Fix remaining issues in priority order


