# Inbox System Analysis & Design v1.0

**Analysis Date:** 2025-12-14  
**Branch:** feature/match-simulation-v1  
**Priority:** HIGH - Critical for player information and game immersion

---

## Executive Summary

**Goal:** Design and implement a robust inbox system that provides players with timely, relevant information about league events, cup draws, authority announcements, and other game events. The system should reduce reliance on debugging/logs and provide an immersive Football Manager-style experience.

**Current State:**
- ✅ Basic `Message` data model exists
- ✅ `InboxScreenTable` UI structure exists (stub)
- ✅ `SaveGame.getAllMessages()` stores messages
- ✅ `JobManager` sends job offer messages
- ❌ **Inbox UI not implemented** (empty `updateDynamicComponents()`)
- ❌ **No message categories/types**
- ❌ **No scheduled messages system**
- ❌ **No league/cup/authority message support**

---

## 1. RESEARCH: Football Manager Inbox System

### 1.1 Historical Evolution

**Traditional Inbox (FM Pre-2025):**
- Centralized message hub for all game events
- Categories: News, Transfers, Match Reports, Training Updates
- Read/Unread status
- Delete functionality
- Filtering and search capabilities

**Modern Portal (FM 2025+):**
- Replaced traditional inbox with integrated "Portal"
- More immersive, context-aware interface
- Combines messaging with action items
- Better integration with other game systems

### 1.2 Key Features from Football Manager

#### Message Categories
1. **News & Updates**
   - League announcements
   - Cup draw notifications
   - Transfer news
   - Injury reports

2. **Match Reports**
   - Match results
   - Match previews
   - Post-match analysis

3. **Administrative**
   - Authority announcements
   - Rule changes
   - Competition schedules
   - Important dates

4. **Personal**
   - Job offers
   - Contract negotiations
   - Board communications

#### Message Features
- **Priority Levels**: Urgent, Normal, Low
- **Action Buttons**: Links to relevant screens (e.g., "View League", "View Match")
- **Scheduled Messages**: Pre-scheduled for future dates
- **Read/Unread Status**: Visual indicators
- **Grouping**: Messages grouped by date/category
- **Notifications**: Badge count for unread messages

### 1.3 Best Practices

1. **Timely Information**: Messages arrive at appropriate times (not all at once)
2. **Actionable Content**: Messages link to relevant game screens
3. **Clear Categorization**: Easy to find specific types of messages
4. **Visual Hierarchy**: Important messages stand out
5. **Scheduled Events**: Important dates announced in advance

---

## 2. CURRENT STATE ANALYSIS

### 2.1 What Exists ✅

#### Data Models
- ✅ **Message**: Basic message structure
  - `id`, `remitent` (Person), `messageTime`, `title`, `plainTextMessage`
  - `isRead`, `isDeleted` flags
  - **Missing**: Category/type, priority, action buttons, scheduled date

#### Storage
- ✅ **SaveGame.getAllMessages()**: List of all messages
- ✅ Messages persist in save game

#### UI Components
- ✅ **InboxScreenTable**: Basic structure exists
  - Three-panel layout: filters, list, detail
  - **Missing**: Actual implementation of display logic

#### Integration
- ✅ **JobManager.sendOfferNotification()**: Creates messages for job offers
- ✅ Messages added to `SaveGame.getAllMessages()`

### 2.2 What's Missing ❌

#### Message System
- ❌ **Message Categories/Types**: No way to categorize messages
- ❌ **Scheduled Messages**: No system for pre-scheduling messages
- ❌ **Message Priority**: No priority levels
- ❌ **Action Buttons**: No links to game screens
- ❌ **Message Templates**: No reusable message templates

#### Message Sources
- ❌ **League Messages**: No messages for league creation, fixture release, etc.
- ❌ **Cup Messages**: No messages for cup draws, round progression
- ❌ **Authority Messages**: No messages for authority announcements
- ❌ **Match Messages**: No match preview/result messages

#### UI Implementation
- ❌ **Message List Display**: Not implemented
- ❌ **Message Detail View**: Not implemented
- ❌ **Filtering**: Not implemented
- ❌ **Unread Badge**: Not implemented

#### Message Manager
- ❌ **MessageManager**: No centralized message creation/management
- ❌ **Scheduled Message System**: No system to schedule future messages

---

## 3. REQUIREMENTS

### 3.1 Message Types & Categories

#### 3.1.1 League Messages
**Category:** `LEAGUE`

**Message Types:**
1. **League Creation**
   - Title: "English Football League Created"
   - Content: "The English Football League has been formed with 12 founding clubs..."
   - Scheduled: 17 April 1888 (league creation date)
   - Action: Link to league standings screen

2. **Fixture Release**
   - Title: "League Fixtures Released"
   - Content: "The complete fixture list for the 1888-89 season has been released..."
   - Scheduled: After fixture generation
   - Action: Link to schedule screen

3. **Season Start Reminder**
   - Title: "Season Starts Soon"
   - Content: "The 1888-89 season begins on [DATE]..."
   - Scheduled: 1 week before season start

4. **Matchday Reminder**
   - Title: "Upcoming Matchday"
   - Content: "Your next league match is on [DATE] against [OPPONENT]..."
   - Scheduled: 3 days before match

#### 3.1.2 Cup Messages
**Category:** `CUP`

**Message Types:**
1. **Cup Draw Date Announcement**
   - Title: "FA Cup Draw Date Announced"
   - Content: "The draw for the First Round of the FA Cup will take place on [DATE]..."
   - Scheduled: 2 weeks before draw date

2. **Cup Draw Results**
   - Title: "FA Cup First Round Draw"
   - Content: "The draw has been completed. Your club will face [OPPONENT]..."
   - Scheduled: On draw date
   - Action: Link to cup draw screen

3. **Cup Round Progression**
   - Title: "Advance to Next Round"
   - Content: "Congratulations! Your club has advanced to the Second Round..."
   - Scheduled: After match completion

#### 3.1.3 Authority Messages
**Category:** `AUTHORITY`

**Message Types:**
1. **Rule Changes**
   - Title: "New Competition Rules"
   - Content: "The Football Association has announced new rules for the upcoming season..."
   - Scheduled: Before season start

2. **Important Dates**
   - Title: "Important Dates Announced"
   - Content: "Key dates for the season: Transfer deadline [DATE], Cup final [DATE]..."
   - Scheduled: Season start

3. **General Announcements**
   - Title: "Football Association Announcement"
   - Content: "[ANNOUNCEMENT TEXT]..."
   - Scheduled: As needed

#### 3.1.4 Match Messages
**Category:** `MATCH`

**Message Types:**
1. **Match Preview**
   - Title: "Upcoming Match: [HOME] vs [AWAY]"
   - Content: "Your next match is scheduled for [DATE] at [STADIUM]..."
   - Scheduled: 3 days before match

2. **Match Result**
   - Title: "Match Result: [HOME] [SCORE] - [SCORE] [AWAY]"
   - Content: "Full match report and statistics..."
   - Scheduled: After match completion
   - Action: Link to match result screen

#### 3.1.5 Job Messages
**Category:** `JOB` (Already implemented)

**Message Types:**
1. **Job Offer** (✅ Implemented)
2. **Application Status** (Future)
3. **Contract Expiry** (Future)

### 3.2 Message Properties

#### Required Fields
- `id`: Unique identifier
- `category`: Message category (LEAGUE, CUP, AUTHORITY, MATCH, JOB)
- `type`: Specific message type within category
- `title`: Message title
- `content`: Message body text
- `messageTime`: When message was created/received
- `isRead`: Read status
- `isDeleted`: Deleted status
- `remitent`: Sender (Person or null for system messages)

#### Optional Fields
- `priority`: Priority level (URGENT, NORMAL, LOW)
- `scheduledDate`: For scheduled messages (when to show)
- `actionScreen`: Screen ID to link to (e.g., LEAGUE_STANDINGS_SCREEN)
- `actionData`: Data to pass to action screen (e.g., league ID)
- `expirationDate`: When message expires (for time-sensitive messages)

### 3.3 Scheduled Messages System

#### Requirements
1. **Pre-scheduling**: Messages can be created with a future `scheduledDate`
2. **Daily Check**: System checks daily for messages to deliver
3. **Event-based**: Messages can be triggered by game events
4. **Template System**: Reusable message templates

#### Scheduled Message Examples
```java
// League creation announcement (scheduled for 17 April 1888)
Message leagueCreation = MessageTemplate.createLeagueCreationMessage(league);
leagueCreation.setScheduledDate(LocalDateTime.of(1888, 4, 17, 10, 0));
messageManager.scheduleMessage(leagueCreation);

// Cup draw date announcement (2 weeks before draw)
Message cupDrawAnnouncement = MessageTemplate.createCupDrawAnnouncement(cup, drawDate);
cupDrawAnnouncement.setScheduledDate(drawDate.minusWeeks(2));
messageManager.scheduleMessage(cupDrawAnnouncement);
```

---

## 4. SYSTEM DESIGN

### 4.1 Message Data Model Enhancement

#### Updated Message Class
```java
public class Message implements Serializable {
    // Existing fields
    private Long id;
    private Person remitent;
    private LocalDateTime messageTime;
    private String title;
    private String plainTextMessage;
    private Boolean isRead;
    private Boolean isDeleted;
    
    // New fields
    private MessageCategory category;  // LEAGUE, CUP, AUTHORITY, MATCH, JOB
    private String messageType;        // Specific type within category
    private MessagePriority priority;  // URGENT, NORMAL, LOW
    private LocalDateTime scheduledDate; // For scheduled messages
    private Integer actionScreen;       // Screen ID to link to
    private Serializable actionData;    // Data for action screen
    private LocalDateTime expirationDate; // When message expires
}
```

#### Enums
```java
public enum MessageCategory {
    LEAGUE,
    CUP,
    AUTHORITY,
    MATCH,
    JOB,
    SYSTEM
}

public enum MessagePriority {
    URGENT,    // Red indicator, appears at top
    NORMAL,    // Default priority
    LOW        // Grayed out, appears at bottom
}
```

### 4.2 MessageManager Class

#### Responsibilities
1. **Message Creation**: Factory methods for creating different message types
2. **Scheduled Messages**: Manage scheduled message delivery
3. **Message Templates**: Reusable message templates
4. **Message Filtering**: Filter messages by category, date, etc.

#### Key Methods
```java
public class MessageManager {
    // Message creation
    public Message createLeagueCreationMessage(League league);
    public Message createFixtureReleaseMessage(League league);
    public Message createCupDrawAnnouncement(Cup cup, LocalDateTime drawDate);
    public Message createCupDrawResultMessage(Cup cup, List<Match> drawResults);
    public Message createAuthorityAnnouncement(String title, String content);
    public Message createMatchPreviewMessage(Match match);
    public Message createMatchResultMessage(Match match);
    
    // Scheduled messages
    public void scheduleMessage(Message message);
    public void deliverScheduledMessages(LocalDateTime currentDate);
    
    // Message queries
    public List<Message> getUnreadMessages();
    public List<Message> getMessagesByCategory(MessageCategory category);
    public int getUnreadCount();
}
```

### 4.3 Scheduled Message System

#### Architecture
1. **Scheduled Messages List**: Store in `SaveGame` separately from delivered messages
2. **Daily Delivery**: `MessageManager.deliverScheduledMessages()` called daily in game engine
3. **Delivery Logic**: Check `scheduledDate <= currentDate`, move to `allMessages`

#### Implementation
```java
// In SaveGame
private List<Message> scheduledMessages = new ArrayList<>(); // Not yet delivered
private List<Message> allMessages = new ArrayList<>();       // Delivered messages

// In MessageManager
public void deliverScheduledMessages(LocalDateTime currentDate) {
    List<Message> toDeliver = new ArrayList<>();
    
    for (Message message : currentGame.getScheduledMessages()) {
        if (message.getScheduledDate() != null && 
            !message.getScheduledDate().isAfter(currentDate)) {
            toDeliver.add(message);
        }
    }
    
    // Move to delivered messages
    for (Message message : toDeliver) {
        currentGame.getScheduledMessages().remove(message);
        currentGame.getAllMessages().add(message);
    }
}
```

### 4.4 Integration Points

#### Game Engine Integration
```java
// In FuttoboruGameEngine.continueGame()
// Deliver scheduled messages daily
messageManager.deliverScheduledMessages(currentGame.getGameDate());
```

#### League Creation Integration
```java
// In ScriptsManager.createLeague()
// Schedule league creation announcement
Message announcement = messageManager.createLeagueCreationMessage(league);
announcement.setScheduledDate(leagueCreationDate);
messageManager.scheduleMessage(announcement);

// Schedule fixture release message (1 day after creation)
Message fixtureRelease = messageManager.createFixtureReleaseMessage(league);
fixtureRelease.setScheduledDate(leagueCreationDate.plusDays(1));
messageManager.scheduleMessage(fixtureRelease);
```

#### Cup Draw Integration
```java
// In CupDrawGenerator (future)
// Schedule cup draw announcement (2 weeks before)
Message announcement = messageManager.createCupDrawAnnouncement(cup, drawDate);
announcement.setScheduledDate(drawDate.minusWeeks(2));
messageManager.scheduleMessage(announcement);

// Create draw result message (on draw date)
Message drawResult = messageManager.createCupDrawResultMessage(cup, drawResults);
drawResult.setScheduledDate(drawDate);
messageManager.scheduleMessage(drawResult);
```

---

## 5. UI DESIGN

### 5.1 Inbox Screen Layout

#### Three-Panel Layout
```
┌─────────────────────────────────────────────────────────────┐
│ Inbox                                    [X Unread]          │
├──────────────┬──────────────────────┬───────────────────────┤
│ Filters      │ Message List         │ Message Detail        │
│              │                      │                       │
│ [All]        │ [●] League Created  │ Title: League Created│
│ [League]     │     17 Apr 1888     │                       │
│ [Cup]        │                      │ The English Football │
│ [Authority]  │ [ ] Fixtures Release │ League has been...   │
│ [Match]      │     18 Apr 1888     │                       │
│ [Job]        │                      │ [View League]        │
│              │ [ ] Cup Draw Date    │                       │
│              │     1 May 1888      │                       │
│              │                      │                       │
│              │ [Scrollable]         │                       │
└──────────────┴──────────────────────┴───────────────────────┘
```

#### Components
1. **Left Panel (Filters)**
   - Category buttons (All, League, Cup, Authority, Match, Job)
   - Unread count badge
   - Search box (future)

2. **Middle Panel (Message List)**
   - Scrollable list of messages
   - Each message shows:
     - Unread indicator (● for unread, ○ for read)
     - Title
     - Date
     - Category icon/color
     - Priority indicator (for urgent messages)
   - Click to select message

3. **Right Panel (Message Detail)**
   - Full message content
   - Action button (if applicable)
   - Mark as read/unread button
   - Delete button

### 5.2 Message List Item Design

```
┌─────────────────────────────────────────────┐
│ [●] League Fixtures Released                │
│     18 April 1888  |  [LEAGUE]              │
│     The complete fixture list...            │
└─────────────────────────────────────────────┘
```

**Visual Indicators:**
- **Unread**: Bold text, colored dot (●)
- **Read**: Normal text, gray dot (○)
- **Urgent**: Red border or background tint
- **Category**: Colored badge or icon

### 5.3 Action Buttons

**Examples:**
- "View League" → Navigate to league standings screen
- "View Fixtures" → Navigate to schedule screen
- "View Cup Draw" → Navigate to cup draw screen
- "View Match" → Navigate to match result screen

**Implementation:**
```java
// In InboxScreenTable
if (selectedMessage.getActionScreen() != null) {
    VisTextButton actionButton = new VisTextButton("View " + actionName);
    actionButton.addListener(new InputListener() {
        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            menuManager.setActiveMainScreen(selectedMessage.getActionScreen());
            // Pass actionData if needed
        }
    });
}
```

---

## 6. IMPLEMENTATION PLAN

### Phase 1: Message Model Enhancement (Priority 1)

**Tasks:**
1. Add new fields to `Message` class:
   - `category` (MessageCategory enum)
   - `messageType` (String)
   - `priority` (MessagePriority enum)
   - `scheduledDate` (LocalDateTime)
   - `actionScreen` (Integer)
   - `actionData` (Serializable)
   - `expirationDate` (LocalDateTime)

2. Create enums:
   - `MessageCategory`
   - `MessagePriority`

3. Update `SaveGame`:
   - Add `scheduledMessages` list
   - Add getters/setters

**Estimated Time:** 2-3 hours

### Phase 2: MessageManager Implementation (Priority 1)

**Tasks:**
1. Create `MessageManager` class
2. Implement message creation methods:
   - League messages (creation, fixtures, reminders)
   - Cup messages (draw announcements, results)
   - Authority messages (announcements)
   - Match messages (preview, result)
3. Implement scheduled message system:
   - `scheduleMessage()`
   - `deliverScheduledMessages()`
4. Integrate with game engine

**Estimated Time:** 4-6 hours

### Phase 3: Inbox UI Implementation (Priority 1)

**Tasks:**
1. Implement `InboxScreenTable.updateDynamicComponents()`:
   - Display message list
   - Display message detail
   - Category filtering
   - Unread indicators
2. Add action buttons
3. Add mark as read/unread functionality
4. Add delete functionality (optional for v1.0)

**Estimated Time:** 6-8 hours

### Phase 4: Integration with Game Systems (Priority 2)

**Tasks:**
1. Integrate with `ScriptsManager`:
   - Schedule league creation message
   - Schedule fixture release message
2. Integrate with `LeagueFixtureGenerator`:
   - Schedule fixture release message after generation
3. Integrate with `AuthorityManager`:
   - Schedule authority announcements
4. Future: Integrate with cup system

**Estimated Time:** 3-4 hours

### Phase 5: Message Templates & Polish (Priority 3)

**Tasks:**
1. Create message templates for common messages
2. Add message formatting (markup support)
3. Add unread badge to inbox button
4. Add message notifications (toast messages)

**Estimated Time:** 2-3 hours

---

## 7. MESSAGE EXAMPLES

### 7.1 League Creation Message
```
Title: "English Football League Created"
Category: LEAGUE
Priority: NORMAL
Scheduled: 17 April 1888, 10:00 AM

Content:
"The English Football League has been formed with 12 founding clubs:
- Accrington
- Aston Villa
- Blackburn Rovers
- Bolton Wanderers
- Burnley
- Derby County
- Everton
- Notts County
- Preston North End
- Stoke
- West Bromwich Albion
- Wolverhampton Wanderers

The first season will begin on 8 September 1888."

Action: View League (LEAGUE_STANDINGS_SCREEN)
```

### 7.2 Fixture Release Message
```
Title: "League Fixtures Released"
Category: LEAGUE
Priority: NORMAL
Scheduled: 18 April 1888

Content:
"The complete fixture list for the 1888-89 English Football League season has been released.

Your club's first match is scheduled for 8 September 1888.

View the complete fixture list in the Schedule screen."

Action: View Schedule (SCHEDULE_SCREEN)
```

### 7.3 Cup Draw Announcement
```
Title: "FA Cup Draw Date Announced"
Category: CUP
Priority: NORMAL
Scheduled: 2 weeks before draw date

Content:
"The Football Association has announced that the draw for the First Round of the FA Cup will take place on [DATE] at [LOCATION].

All participating clubs will be notified of their opponents following the draw."

Action: None (informational)
```

### 7.4 Cup Draw Result
```
Title: "FA Cup First Round Draw"
Category: CUP
Priority: NORMAL
Scheduled: On draw date

Content:
"The draw for the First Round of the FA Cup has been completed.

Your club, [CLUB_NAME], has been drawn against [OPPONENT] in the First Round.

The match will be played on [DATE] at [STADIUM]."

Action: View Cup Draw (CUP_DRAW_SCREEN)
```

---

## 8. TESTING PLAN

### 8.1 Unit Tests
- [ ] Message creation for each category
- [ ] Scheduled message delivery
- [ ] Message filtering by category
- [ ] Unread count calculation

### 8.2 Integration Tests
- [ ] League creation triggers message
- [ ] Fixture generation triggers message
- [ ] Messages appear in inbox UI
- [ ] Action buttons navigate correctly

### 8.3 User Acceptance Tests
- [ ] Messages appear at correct times
- [ ] Messages are readable and clear
- [ ] Action buttons work correctly
- [ ] Unread indicators work
- [ ] Category filtering works

---

## 9. FUTURE ENHANCEMENTS (Post-v1.0)

1. **Message Search**: Full-text search across messages
2. **Message Grouping**: Group related messages (e.g., all match messages)
3. **Custom Filters**: User-defined filter combinations
4. **Message Templates**: User-editable message templates
5. **Email-style Features**: Reply, forward, archive
6. **Rich Text Support**: HTML/markdown formatting
7. **Attachments**: Attach match reports, player profiles, etc.
8. **Notifications**: Desktop/system notifications for urgent messages
9. **Message History**: Archive old messages
10. **Export Messages**: Export messages to file

---

## 10. ESTIMATED TIMELINE

- **Phase 1 (Message Model):** 2-3 hours
- **Phase 2 (MessageManager):** 4-6 hours
- **Phase 3 (Inbox UI):** 6-8 hours
- **Phase 4 (Integration):** 3-4 hours
- **Phase 5 (Polish):** 2-3 hours

**Total:** 17-24 hours (2-3 days)

---

*End of Analysis*

