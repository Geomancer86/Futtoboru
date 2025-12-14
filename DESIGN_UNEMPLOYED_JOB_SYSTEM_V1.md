# Unemployed Character & Job System - Detailed Design Document v1.0

## Branch: `cursor/feature-unemployed-job-system`

**Status:** DESIGN PHASE  
**Version:** v1.0 MVP  
**Priority:** High  
**Estimated Implementation:** 4-5 weeks

---

## Table of Contents

1. [Overview](#1-overview)
2. [Data Models](#2-data-models)
3. [Enums & Constants](#3-enums--constants)
4. [Manager Classes](#4-manager-classes)
5. [UI Components](#5-ui-components)
6. [Algorithms & Logic](#6-algorithms--logic)
7. [Integration Points](#7-integration-points)
8. [Implementation Details](#8-implementation-details)
9. [Testing Strategy](#9-testing-strategy)

---

## 1. Overview

### 1.1 Purpose
This document provides detailed design specifications for the unemployed character job system, enabling players to browse clubs, view job openings, apply for positions, receive offers, and negotiate contracts.

### 1.2 Scope (v1.0 MVP)
- Club browser screen
- Job board with filtering
- Job application system
- Job offer system (simplified - no interviews)
- Basic negotiation (salary + contract length)
- Club staff tracking

### 1.3 Out of Scope (v2.0+)
- Interview system
- Reputation system
- Advanced negotiation (bonuses, performance clauses)
- Job recommendations
- Networking system

---

## 2. Data Models

### 2.1 JobOpening

**Package:** `com.rndmodgames.futtoboru.data.jobs`

**Purpose:** Represents an open job position at a club

```java
package com.rndmodgames.futtoboru.data.jobs;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;

/**
 * Job Opening v1
 * 
 * Represents an open position at a club that unemployed players can apply for.
 * 
 * @author Geomancer86
 */
public class JobOpening implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    
    /**
     * Club offering the position
     */
    private Long clubId; // Store ID to avoid circular references in serialization
    
    /**
     * Profession/Position type
     */
    private Long professionId; // Store ID to avoid circular references
    
    /**
     * Dates
     */
    private LocalDateTime postedDate;
    private LocalDateTime applicationDeadline;
    
    /**
     * Status
     */
    private JobStatus status; // OPEN, FILLED, EXPIRED
    
    /**
     * Compensation (v1.0: basic)
     */
    private BigDecimal salary;
    private Integer contractLengthMonths; // Default: 12 months
    
    /**
     * Requirements (v1.0: simple - only reputation)
     */
    private Integer minReputation; // null = no requirement, range 0-100
    
    /**
     * Applicants
     */
    private List<Long> applicationIds = new ArrayList<>(); // Store IDs to avoid circular references
    
    /**
     * Selected candidate (if filled)
     */
    private Long selectedCandidateId; // null if not filled
    
    // Constructors
    public JobOpening() {
        this.status = JobStatus.OPEN;
        this.contractLengthMonths = 12; // Default 1 year
    }
    
    public JobOpening(Club club, Profession profession) {
        this();
        this.clubId = club.getId();
        this.professionId = profession.getId();
        this.postedDate = LocalDateTime.now();
        // Default deadline: 30 days from posting
        this.applicationDeadline = this.postedDate.plusDays(30);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getClubId() {
        return clubId;
    }
    
    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }
    
    public Long getProfessionId() {
        return professionId;
    }
    
    public void setProfessionId(Long professionId) {
        this.professionId = professionId;
    }
    
    public LocalDateTime getPostedDate() {
        return postedDate;
    }
    
    public void setPostedDate(LocalDateTime postedDate) {
        this.postedDate = postedDate;
    }
    
    public LocalDateTime getApplicationDeadline() {
        return applicationDeadline;
    }
    
    public void setApplicationDeadline(LocalDateTime applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }
    
    public JobStatus getStatus() {
        return status;
    }
    
    public void setStatus(JobStatus status) {
        this.status = status;
    }
    
    public BigDecimal getSalary() {
        return salary;
    }
    
    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }
    
    public Integer getContractLengthMonths() {
        return contractLengthMonths;
    }
    
    public void setContractLengthMonths(Integer contractLengthMonths) {
        this.contractLengthMonths = contractLengthMonths;
    }
    
    public Integer getMinReputation() {
        return minReputation;
    }
    
    public void setMinReputation(Integer minReputation) {
        this.minReputation = minReputation;
    }
    
    public List<Long> getApplicationIds() {
        return applicationIds;
    }
    
    public void setApplicationIds(List<Long> applicationIds) {
        this.applicationIds = applicationIds;
    }
    
    public Long getSelectedCandidateId() {
        return selectedCandidateId;
    }
    
    public void setSelectedCandidateId(Long selectedCandidateId) {
        this.selectedCandidateId = selectedCandidateId;
    }
    
    /**
     * Utility: Check if opening is still accepting applications
     */
    public boolean isAcceptingApplications() {
        return status == JobStatus.OPEN && 
               applicationDeadline.isAfter(LocalDateTime.now());
    }
    
    /**
     * Utility: Check if opening has expired
     */
    public boolean isExpired() {
        return applicationDeadline.isBefore(LocalDateTime.now()) && 
               status == JobStatus.OPEN;
    }
}
```

---

### 2.2 JobApplication

**Package:** `com.rndmodgames.futtoboru.data.jobs`

**Purpose:** Represents a player's application for a job opening

```java
package com.rndmodgames.futtoboru.data.jobs;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.rndmodgames.futtoboru.data.Person;

/**
 * Job Application v1
 * 
 * Represents a player's application for a job opening.
 * 
 * @author Geomancer86
 */
public class JobApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    
    /**
     * Applicant
     */
    private Long applicantId; // Store ID to avoid circular references
    
    /**
     * Job opening
     */
    private Long jobOpeningId; // Store ID to avoid circular references
    
    /**
     * Application date
     */
    private LocalDateTime applicationDate;
    
    /**
     * Status
     */
    private ApplicationStatus status; // PENDING, REJECTED, OFFER_RECEIVED, ACCEPTED, WITHDRAWN
    
    /**
     * Match percentage (how well applicant matches requirements)
     * v1.0: Simple calculation based on reputation
     */
    private Integer matchPercentage; // 0-100
    
    // Constructors
    public JobApplication() {
        this.status = ApplicationStatus.PENDING;
        this.applicationDate = LocalDateTime.now();
    }
    
    public JobApplication(Person applicant, JobOpening jobOpening) {
        this();
        this.applicantId = applicant.getId();
        this.jobOpeningId = jobOpening.getId();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getApplicantId() {
        return applicantId;
    }
    
    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }
    
    public Long getJobOpeningId() {
        return jobOpeningId;
    }
    
    public void setJobOpeningId(Long jobOpeningId) {
        this.jobOpeningId = jobOpeningId;
    }
    
    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }
    
    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }
    
    public ApplicationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
    
    public Integer getMatchPercentage() {
        return matchPercentage;
    }
    
    public void setMatchPercentage(Integer matchPercentage) {
        this.matchPercentage = matchPercentage;
    }
    
    /**
     * Utility: Check if application is still active
     */
    public boolean isActive() {
        return status == ApplicationStatus.PENDING || 
               status == ApplicationStatus.OFFER_RECEIVED;
    }
}
```

---

### 2.3 JobOffer

**Package:** `com.rndmodgames.futtoboru.data.jobs`

**Purpose:** Represents a job offer made to a player

```java
package com.rndmodgames.futtoboru.data.jobs;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.rndmodgames.futtoboru.data.Person;

/**
 * Job Offer v1
 * 
 * Represents a job offer made to a player after application.
 * v1.0: Simplified - no interviews, offers made directly after application.
 * 
 * @author Geomancer86
 */
public class JobOffer implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    
    /**
     * Related job opening
     */
    private Long jobOpeningId; // Store ID to avoid circular references
    
    /**
     * Recipient
     */
    private Long recipientId; // Store ID to avoid circular references
    
    /**
     * Dates
     */
    private LocalDateTime offerDate;
    private LocalDateTime expirationDate; // Default: 7 days from offer
    
    /**
     * Status
     */
    private OfferStatus status; // PENDING, ACCEPTED, REJECTED, NEGOTIATING, EXPIRED
    
    /**
     * Terms (v1.0: basic)
     */
    private BigDecimal salary;
    private Integer contractLengthMonths;
    
    /**
     * Negotiation (v1.0: simple)
     */
    private Long counterOfferId; // null if not negotiating, points to counter-offer
    private Integer negotiationRound; // 0 = original offer, 1-3 = counter-offers
    private boolean isCounterOffer; // true if this is a counter-offer
    
    /**
     * Original offer (if this is a counter-offer)
     */
    private Long originalOfferId; // null if this is the original offer
    
    // Constructors
    public JobOffer() {
        this.status = OfferStatus.PENDING;
        this.offerDate = LocalDateTime.now();
        this.expirationDate = this.offerDate.plusDays(7); // Default 7 days
        this.negotiationRound = 0;
        this.isCounterOffer = false;
    }
    
    public JobOffer(JobOpening jobOpening, Person recipient) {
        this();
        this.jobOpeningId = jobOpening.getId();
        this.recipientId = recipient.getId();
        this.salary = jobOpening.getSalary();
        this.contractLengthMonths = jobOpening.getContractLengthMonths();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getJobOpeningId() {
        return jobOpeningId;
    }
    
    public void setJobOpeningId(Long jobOpeningId) {
        this.jobOpeningId = jobOpeningId;
    }
    
    public Long getRecipientId() {
        return recipientId;
    }
    
    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }
    
    public LocalDateTime getOfferDate() {
        return offerDate;
    }
    
    public void setOfferDate(LocalDateTime offerDate) {
        this.offerDate = offerDate;
    }
    
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }
    
    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }
    
    public OfferStatus getStatus() {
        return status;
    }
    
    public void setStatus(OfferStatus status) {
        this.status = status;
    }
    
    public BigDecimal getSalary() {
        return salary;
    }
    
    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }
    
    public Integer getContractLengthMonths() {
        return contractLengthMonths;
    }
    
    public void setContractLengthMonths(Integer contractLengthMonths) {
        this.contractLengthMonths = contractLengthMonths;
    }
    
    public Long getCounterOfferId() {
        return counterOfferId;
    }
    
    public void setCounterOfferId(Long counterOfferId) {
        this.counterOfferId = counterOfferId;
    }
    
    public Integer getNegotiationRound() {
        return negotiationRound;
    }
    
    public void setNegotiationRound(Integer negotiationRound) {
        this.negotiationRound = negotiationRound;
    }
    
    public boolean isCounterOffer() {
        return isCounterOffer;
    }
    
    public void setCounterOffer(boolean isCounterOffer) {
        this.isCounterOffer = isCounterOffer;
    }
    
    public Long getOriginalOfferId() {
        return originalOfferId;
    }
    
    public void setOriginalOfferId(Long originalOfferId) {
        this.originalOfferId = originalOfferId;
    }
    
    /**
     * Utility: Check if offer has expired
     */
    public boolean isExpired() {
        return expirationDate.isBefore(LocalDateTime.now()) && 
               status == OfferStatus.PENDING;
    }
    
    /**
     * Utility: Check if offer can be negotiated
     */
    public boolean canNegotiate() {
        return status == OfferStatus.PENDING && 
               !isExpired() && 
               negotiationRound < 3; // Max 3 rounds for v1.0
    }
}
```

---

### 2.4 Club Extensions

**Package:** `com.rndmodgames.futtoboru.data`

**Changes to Club.java:**

```java
// Add to Club class:

import java.util.HashMap;
import java.util.Map;

/**
 * Club Staff Tracking (v1.0)
 * 
 * Maps profession to person holding that position.
 * null value means position is vacant.
 */
private Map<Long, Long> staff = new HashMap<>(); // Profession ID -> Person ID

// Getters and Setters
public Map<Long, Long> getStaff() {
    if (staff == null) {
        staff = new HashMap<>();
    }
    return staff;
}

public void setStaff(Map<Long, Long> staff) {
    this.staff = staff;
}

/**
 * Utility: Get staff member for a profession
 */
public Long getStaffId(Long professionId) {
    return staff != null ? staff.get(professionId) : null;
}

/**
 * Utility: Set staff member for a profession
 */
public void setStaffId(Long professionId, Long personId) {
    if (staff == null) {
        staff = new HashMap<>();
    }
    if (personId == null) {
        staff.remove(professionId);
    } else {
        staff.put(professionId, personId);
    }
}

/**
 * Utility: Check if position is vacant
 */
public boolean isPositionVacant(Long professionId) {
    return staff == null || staff.get(professionId) == null;
}
```

---

### 2.5 Person Extensions

**Package:** `com.rndmodgames.futtoboru.data`

**Changes to Person.java:**

```java
// Add to Person class:

import java.util.ArrayList;
import java.util.List;

/**
 * Reputation (v1.0: basic)
 * Range: 0-100
 * Default: 50 (neutral)
 */
private Integer reputation = 50;

/**
 * Job Applications (v1.0)
 * List of active application IDs
 */
private List<Long> activeApplicationIds = new ArrayList<>();

/**
 * Pending Job Offers (v1.0)
 * List of pending offer IDs
 */
private List<Long> pendingOfferIds = new ArrayList<>();

/**
 * Application tracking (v1.0)
 * Limit: 3 applications per week
 */
private LocalDateTime lastJobApplicationDate;
private Integer applicationsThisWeek = 0;

// Getters and Setters
public Integer getReputation() {
    if (reputation == null) {
        reputation = 50; // Default
    }
    return reputation;
}

public void setReputation(Integer reputation) {
    this.reputation = reputation;
}

public List<Long> getActiveApplicationIds() {
    if (activeApplicationIds == null) {
        activeApplicationIds = new ArrayList<>();
    }
    return activeApplicationIds;
}

public void setActiveApplicationIds(List<Long> activeApplicationIds) {
    this.activeApplicationIds = activeApplicationIds;
}

public List<Long> getPendingOfferIds() {
    if (pendingOfferIds == null) {
        pendingOfferIds = new ArrayList<>();
    }
    return pendingOfferIds;
}

public void setPendingOfferIds(List<Long> pendingOfferIds) {
    this.pendingOfferIds = pendingOfferIds;
}

public LocalDateTime getLastJobApplicationDate() {
    return lastJobApplicationDate;
}

public void setLastJobApplicationDate(LocalDateTime lastJobApplicationDate) {
    this.lastJobApplicationDate = lastJobApplicationDate;
}

public Integer getApplicationsThisWeek() {
    if (applicationsThisWeek == null) {
        applicationsThisWeek = 0;
    }
    return applicationsThisWeek;
}

public void setApplicationsThisWeek(Integer applicationsThisWeek) {
    this.applicationsThisWeek = applicationsThisWeek;
}

/**
 * Utility: Check if can apply for more jobs this week
 */
public boolean canApplyForJob() {
    // Reset counter if new week
    if (lastJobApplicationDate != null) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        if (lastJobApplicationDate.isBefore(weekAgo)) {
            applicationsThisWeek = 0;
        }
    }
    return applicationsThisWeek < 3; // Max 3 per week
}

/**
 * Utility: Increment application counter
 */
public void incrementApplicationCount() {
    if (canApplyForJob()) {
        applicationsThisWeek++;
        lastJobApplicationDate = LocalDateTime.now();
    }
}
```

---

### 2.6 SaveGame Extensions

**Package:** `com.rndmodgames.futtoboru.system`

**Changes to SaveGame.java:**

```java
// Add to SaveGame class:

import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.jobs.JobApplication;
import com.rndmodgames.futtoboru.data.jobs.JobOffer;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;

/**
 * Job System Data (v1.0)
 */
private List<JobOpening> activeJobOpenings = new ArrayList<>();
private List<JobApplication> allApplications = new ArrayList<>();
private List<JobOffer> pendingOffers = new ArrayList<>();

// Getters and Setters
public List<JobOpening> getActiveJobOpenings() {
    if (activeJobOpenings == null) {
        activeJobOpenings = new ArrayList<>();
    }
    return activeJobOpenings;
}

public void setActiveJobOpenings(List<JobOpening> activeJobOpenings) {
    this.activeJobOpenings = activeJobOpenings;
}

public List<JobApplication> getAllApplications() {
    if (allApplications == null) {
        allApplications = new ArrayList<>();
    }
    return allApplications;
}

public void setAllApplications(List<JobApplication> allApplications) {
    this.allApplications = allApplications;
}

public List<JobOffer> getPendingOffers() {
    if (pendingOffers == null) {
        pendingOffers = new ArrayList<>();
    }
    return pendingOffers;
}

public void setPendingOffers(List<JobOffer> pendingOffers) {
    this.pendingOffers = pendingOffers;
}
```

---

## 3. Enums & Constants

### 3.1 JobStatus

**Package:** `com.rndmodgames.futtoboru.data.jobs`

```java
package com.rndmodgames.futtoboru.data.jobs;

/**
 * Job Opening Status
 */
public enum JobStatus {
    OPEN,           // Accepting applications
    FILLED,         // Position filled
    EXPIRED         // Application deadline passed
}
```

---

### 3.2 ApplicationStatus

**Package:** `com.rndmodgames.futtoboru.data.jobs`

```java
package com.rndmodgames.futtoboru.data.jobs;

/**
 * Job Application Status
 */
public enum ApplicationStatus {
    PENDING,            // Awaiting club decision
    REJECTED,           // Application rejected
    OFFER_RECEIVED,     // Offer made (v1.0: simplified, no interview)
    ACCEPTED,           // Offer accepted
    WITHDRAWN           // Player withdrew application
}
```

---

### 3.3 OfferStatus

**Package:** `com.rndmodgames.futtoboru.data.jobs`

```java
package com.rndmodgames.futtoboru.data.jobs;

/**
 * Job Offer Status
 */
public enum OfferStatus {
    PENDING,        // Awaiting player response
    ACCEPTED,       // Offer accepted
    REJECTED,       // Offer rejected
    NEGOTIATING,    // Counter-offer submitted
    EXPIRED         // Offer expired
}
```

---

### 3.4 Constants

**Package:** `com.rndmodgames.futtoboru.data.jobs`

```java
package com.rndmodgames.futtoboru.data.jobs;

/**
 * Job System Constants (v1.0)
 */
public class JobConstants {
    
    // Application limits
    public static final int MAX_APPLICATIONS_PER_WEEK = 3;
    
    // Offer expiration
    public static final int OFFER_EXPIRATION_DAYS = 7;
    
    // Negotiation limits
    public static final int MAX_NEGOTIATION_ROUNDS = 3;
    
    // Job opening expiration
    public static final int JOB_OPENING_DEFAULT_DEADLINE_DAYS = 30;
    
    // Application processing delay (v1.0: simplified - instant for MVP, can add delay later)
    public static final int APPLICATION_PROCESSING_DELAY_DAYS = 0; // 0 = instant
    
    // AI negotiation tolerance (percentage)
    public static final double AI_NEGOTIATION_TOLERANCE = 0.10; // 10% above/below original offer
}
```

---

## 4. Manager Classes

### 4.1 ClubStaffManager

**Package:** `com.rndmodgames.futtoboru.engine.jobs`

**Purpose:** Manage club staff positions and detect vacancies

```java
package com.rndmodgames.futtoboru.engine.jobs;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Club Staff Manager v1
 * 
 * Manages club staff positions and detects vacancies.
 * 
 * @author Geomancer86
 */
public class ClubStaffManager {

    private Futtoboru gameInstance;
    private SaveGame currentGame;

    public ClubStaffManager(Futtoboru gameInstance) {
        this.gameInstance = gameInstance;
        this.currentGame = gameInstance.getCurrentGame();
    }

    /**
     * Get staff member for a profession at a club
     * 
     * @param club The club
     * @param profession The profession
     * @return Person holding the position, or null if vacant
     */
    public Person getClubStaff(Club club, Profession profession) {
        if (club == null || profession == null) {
            return null;
        }
        
        Long personId = club.getStaffId(profession.getId());
        if (personId == null) {
            return null;
        }
        
        // Find person in SaveGame
        for (Person person : currentGame.getAllPersons()) {
            if (person.getId().equals(personId)) {
                return person;
            }
        }
        
        return null;
    }

    /**
     * Set staff member for a profession at a club
     * 
     * @param club The club
     * @param profession The profession
     * @param person The person (null to vacate position)
     */
    public void setClubStaff(Club club, Profession profession, Person person) {
        if (club == null || profession == null) {
            return;
        }
        
        Long personId = person != null ? person.getId() : null;
        club.setStaffId(profession.getId(), personId);
        
        // Update person's current club
        if (person != null) {
            person.setCurrentClubId(club.getId());
        }
    }

    /**
     * Check if a position is vacant
     * 
     * @param club The club
     * @param profession The profession
     * @return true if position is vacant
     */
    public boolean isPositionVacant(Club club, Profession profession) {
        if (club == null || profession == null) {
            return false;
        }
        
        return club.isPositionVacant(profession.getId());
    }

    /**
     * Get all vacant positions for a club
     * 
     * @param club The club
     * @return List of professions that are vacant
     */
    public List<Profession> getVacantPositions(Club club) {
        List<Profession> vacant = new ArrayList<>();
        
        if (club == null) {
            return vacant;
        }
        
        // Check all selectable professions (v1.0: Manager, Director, Scout)
        List<Profession> selectableProfessions = DatabaseLoader.getSelectableProfessions();
        
        for (Profession profession : selectableProfessions) {
            // Skip Player and Retired Player (not staff positions)
            if (profession.getId().equals(1L) || profession.getId().equals(2L)) {
                continue;
            }
            
            if (isPositionVacant(club, profession)) {
                vacant.add(profession);
            }
        }
        
        return vacant;
    }

    /**
     * Fire staff member (creates vacancy)
     * 
     * @param club The club
     * @param profession The profession
     */
    public void fireStaff(Club club, Profession profession) {
        if (club == null || profession == null) {
            return;
        }
        
        Person currentStaff = getClubStaff(club, profession);
        if (currentStaff != null) {
            // Remove from club
            setClubStaff(club, profession, null);
            
            // Set person as unemployed
            currentStaff.setCurrentClubId(null);
            
            Gdx.app.log("ClubStaffManager", "Fired " + profession.getName() + 
                       " from " + club.getName() + ": " + currentStaff.getName());
        }
    }

    /**
     * Hire staff member (fills vacancy)
     * 
     * @param club The club
     * @param profession The profession
     * @param person The person to hire
     */
    public void hireStaff(Club club, Profession profession, Person person) {
        if (club == null || profession == null || person == null) {
            return;
        }
        
        // Set staff
        setClubStaff(club, profession, person);
        
        // Update person's profession if needed
        if (person.getPrimaryProfession() == null || 
            !person.getPrimaryProfession().getId().equals(profession.getId())) {
            person.setPrimaryProfession(profession);
        }
        
        Gdx.app.log("ClubStaffManager", "Hired " + person.getName() + 
                   " as " + profession.getName() + " at " + club.getName());
    }
}
```

---

### 4.2 JobManager

**Package:** `com.rndmodgames.futtoboru.engine.jobs`

**Purpose:** Manage job openings, applications, offers, and negotiations

```java
package com.rndmodgames.futtoboru.engine.jobs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.jobs.ApplicationStatus;
import com.rndmodgames.futtoboru.data.jobs.JobApplication;
import com.rndmodgames.futtoboru.data.jobs.JobConstants;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;
import com.rndmodgames.futtoboru.data.jobs.JobStatus;
import com.rndmodgames.futtoboru.data.jobs.JobOffer;
import com.rndmodgames.futtoboru.data.jobs.OfferStatus;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Job Manager v1
 * 
 * Manages job openings, applications, offers, and negotiations.
 * 
 * @author Geomancer86
 */
public class JobManager {

    private Futtoboru gameInstance;
    private SaveGame currentGame;
    private ClubStaffManager clubStaffManager;
    private static long nextId = 1L; // Simple ID generation (v1.0)

    public JobManager(Futtoboru gameInstance, ClubStaffManager clubStaffManager) {
        this.gameInstance = gameInstance;
        this.currentGame = gameInstance.getCurrentGame();
        this.clubStaffManager = clubStaffManager;
    }

    // ========== Job Openings ==========

    /**
     * Get all available job openings
     * 
     * @return List of open job openings
     */
    public List<JobOpening> getAvailableJobs() {
        return currentGame.getActiveJobOpenings().stream()
            .filter(job -> job.isAcceptingApplications())
            .collect(Collectors.toList());
    }

    /**
     * Get available jobs filtered by profession
     * 
     * @param profession The profession to filter by
     * @return List of matching job openings
     */
    public List<JobOpening> getAvailableJobs(Profession profession) {
        if (profession == null) {
            return getAvailableJobs();
        }
        
        return getAvailableJobs().stream()
            .filter(job -> job.getProfessionId().equals(profession.getId()))
            .collect(Collectors.toList());
    }

    /**
     * Get available jobs for a specific club
     * 
     * @param club The club
     * @return List of job openings at the club
     */
    public List<JobOpening> getAvailableJobs(Club club) {
        if (club == null) {
            return new ArrayList<>();
        }
        
        return getAvailableJobs().stream()
            .filter(job -> job.getClubId().equals(club.getId()))
            .collect(Collectors.toList());
    }

    /**
     * Create a job opening for a vacant position
     * 
     * @param club The club
     * @param profession The profession
     * @return The created job opening, or null if creation failed
     */
    public JobOpening createJobOpening(Club club, Profession profession) {
        if (club == null || profession == null) {
            return null;
        }
        
        // Check if position is actually vacant
        if (!clubStaffManager.isPositionVacant(club, profession)) {
            Gdx.app.log("JobManager", "Position not vacant: " + profession.getName() + 
                       " at " + club.getName());
            return null;
        }
        
        // Check if opening already exists
        for (JobOpening existing : getAvailableJobs(club)) {
            if (existing.getProfessionId().equals(profession.getId())) {
                Gdx.app.log("JobManager", "Job opening already exists: " + profession.getName() + 
                           " at " + club.getName());
                return existing;
            }
        }
        
        // Create new job opening
        JobOpening opening = new JobOpening(club, profession);
        opening.setId(nextId++);
        
        // Calculate salary based on club finances and profession (v1.0: simple)
        opening.setSalary(calculateJobSalary(club, profession));
        
        // Set requirements (v1.0: simple - only reputation)
        opening.setMinReputation(calculateMinReputation(club, profession));
        
        // Add to SaveGame
        currentGame.getActiveJobOpenings().add(opening);
        
        Gdx.app.log("JobManager", "Created job opening: " + profession.getName() + 
                   " at " + club.getName());
        
        return opening;
    }

    /**
     * Calculate job salary (v1.0: simple calculation)
     * 
     * @param club The club
     * @param profession The profession
     * @return Calculated salary
     */
    private BigDecimal calculateJobSalary(Club club, Profession profession) {
        // v1.0: Simple calculation based on club balance
        // Base salary: 1% of club balance per year, adjusted by profession
        
        BigDecimal baseSalary = club.getClubBalance()
            .multiply(new BigDecimal("0.01"))
            .divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP); // Monthly
        
        // Adjust by profession (v1.0: simple multipliers)
        double multiplier = 1.0;
        if (profession.getId().equals(3L)) { // Manager
            multiplier = 1.5;
        } else if (profession.getId().equals(4L)) { // Director
            multiplier = 1.2;
        } else if (profession.getId().equals(5L)) { // Scout
            multiplier = 0.8;
        }
        
        return baseSalary.multiply(new BigDecimal(multiplier));
    }

    /**
     * Calculate minimum reputation requirement (v1.0: simple)
     * 
     * @param club The club
     * @param profession The profession
     * @return Minimum reputation (0-100), or null if no requirement
     */
    private Integer calculateMinReputation(Club club, Profession profession) {
        // v1.0: Simple calculation - higher reputation clubs require higher reputation
        // For now, return null (no requirement) - can be enhanced in v2.0
        return null;
    }

    /**
     * Update job openings (called daily by game engine)
     * - Expire old openings
     * - Create openings for vacant positions
     */
    public void updateJobOpenings() {
        LocalDateTime now = currentGame.getGameDate();
        
        // Expire old openings
        for (JobOpening opening : new ArrayList<>(currentGame.getActiveJobOpenings())) {
            if (opening.isExpired()) {
                opening.setStatus(JobStatus.EXPIRED);
                Gdx.app.log("JobManager", "Expired job opening: " + opening.getId());
            }
        }
        
        // Create openings for vacant positions
        for (Club club : currentGame.getAllClubs()) {
            List<Profession> vacant = clubStaffManager.getVacantPositions(club);
            
            for (Profession profession : vacant) {
                // Check if opening already exists
                boolean exists = false;
                for (JobOpening existing : getAvailableJobs(club)) {
                    if (existing.getProfessionId().equals(profession.getId())) {
                        exists = true;
                        break;
                    }
                }
                
                if (!exists) {
                    createJobOpening(club, profession);
                }
            }
        }
    }

    // ========== Applications ==========

    /**
     * Apply for a job opening
     * 
     * @param applicant The applicant
     * @param jobOpening The job opening
     * @return The created application, or null if application failed
     */
    public JobApplication applyForJob(Person applicant, JobOpening jobOpening) {
        if (applicant == null || jobOpening == null) {
            return null;
        }
        
        // Validate application
        if (!applicant.canApplyForJob()) {
            Gdx.app.log("JobManager", "Application limit reached for " + applicant.getName());
            return null;
        }
        
        if (!jobOpening.isAcceptingApplications()) {
            Gdx.app.log("JobManager", "Job opening not accepting applications: " + jobOpening.getId());
            return null;
        }
        
        // Check if already applied
        for (JobApplication existing : getPlayerApplications(applicant)) {
            if (existing.getJobOpeningId().equals(jobOpening.getId()) && 
                existing.isActive()) {
                Gdx.app.log("JobManager", "Already applied for job: " + jobOpening.getId());
                return existing;
            }
        }
        
        // Create application
        JobApplication application = new JobApplication(applicant, jobOpening);
        application.setId(nextId++);
        
        // Calculate match percentage (v1.0: simple - based on reputation)
        application.setMatchPercentage(calculateMatchPercentage(applicant, jobOpening));
        
        // Add to SaveGame
        currentGame.getAllApplications().add(application);
        
        // Add to job opening
        jobOpening.getApplicationIds().add(application.getId());
        
        // Update applicant
        applicant.getActiveApplicationIds().add(application.getId());
        applicant.incrementApplicationCount();
        
        Gdx.app.log("JobManager", "Application created: " + applicant.getName() + 
                   " for job " + jobOpening.getId());
        
        // Process application (v1.0: instant processing)
        if (JobConstants.APPLICATION_PROCESSING_DELAY_DAYS == 0) {
            processApplication(application);
        }
        
        return application;
    }

    /**
     * Calculate match percentage (v1.0: simple)
     * 
     * @param applicant The applicant
     * @param jobOpening The job opening
     * @return Match percentage (0-100)
     */
    private Integer calculateMatchPercentage(Person applicant, JobOpening jobOpening) {
        // v1.0: Simple calculation based on reputation
        Integer applicantRep = applicant.getReputation();
        Integer minRep = jobOpening.getMinReputation();
        
        if (minRep == null) {
            // No requirement - 100% match
            return 100;
        }
        
        if (applicantRep >= minRep) {
            // Meets requirement - 100% match
            return 100;
        } else {
            // Below requirement - calculate percentage
            return Math.max(0, (applicantRep * 100) / minRep);
        }
    }

    /**
     * Get all applications for a player
     * 
     * @param player The player
     * @return List of applications
     */
    public List<JobApplication> getPlayerApplications(Person player) {
        if (player == null) {
            return new ArrayList<>();
        }
        
        return currentGame.getAllApplications().stream()
            .filter(app -> app.getApplicantId().equals(player.getId()))
            .collect(Collectors.toList());
    }

    /**
     * Process an application (v1.0: simplified - instant offer if meets requirements)
     * 
     * @param application The application
     */
    private void processApplication(JobApplication application) {
        if (application.getStatus() != ApplicationStatus.PENDING) {
            return;
        }
        
        JobOpening jobOpening = getJobOpeningById(application.getJobOpeningId());
        if (jobOpening == null) {
            return;
        }
        
        Person applicant = getPersonById(application.getApplicantId());
        if (applicant == null) {
            return;
        }
        
        // v1.0: Simple logic - make offer if match percentage >= 70%
        if (application.getMatchPercentage() >= 70) {
            // Make offer
            JobOffer offer = makeOffer(jobOpening, applicant);
            if (offer != null) {
                application.setStatus(ApplicationStatus.OFFER_RECEIVED);
                Gdx.app.log("JobManager", "Offer made for application: " + application.getId());
            } else {
                application.setStatus(ApplicationStatus.REJECTED);
            }
        } else {
            // Reject application
            application.setStatus(ApplicationStatus.REJECTED);
            Gdx.app.log("JobManager", "Application rejected: " + application.getId());
        }
    }

    /**
     * Withdraw an application
     * 
     * @param application The application
     */
    public void withdrawApplication(JobApplication application) {
        if (application == null) {
            return;
        }
        
        if (application.getStatus() == ApplicationStatus.PENDING) {
            application.setStatus(ApplicationStatus.WITHDRAWN);
            
            // Remove from applicant's active applications
            Person applicant = getPersonById(application.getApplicantId());
            if (applicant != null) {
                applicant.getActiveApplicationIds().remove(application.getId());
            }
            
            Gdx.app.log("JobManager", "Application withdrawn: " + application.getId());
        }
    }

    // ========== Offers ==========

    /**
     * Make a job offer
     * 
     * @param jobOpening The job opening
     * @param candidate The candidate
     * @return The created offer, or null if creation failed
     */
    public JobOffer makeOffer(JobOpening jobOpening, Person candidate) {
        if (jobOpening == null || candidate == null) {
            return null;
        }
        
        // Create offer
        JobOffer offer = new JobOffer(jobOpening, candidate);
        offer.setId(nextId++);
        
        // Add to SaveGame
        currentGame.getPendingOffers().add(offer);
        
        // Add to candidate's pending offers
        candidate.getPendingOfferIds().add(offer.getId());
        
        // Send notification to inbox
        sendOfferNotification(candidate, offer);
        
        Gdx.app.log("JobManager", "Offer made: " + offer.getId() + 
                   " to " + candidate.getName());
        
        return offer;
    }

    /**
     * Get all offers for a player
     * 
     * @param player The player
     * @return List of offers
     */
    public List<JobOffer> getPlayerOffers(Person player) {
        if (player == null) {
            return new ArrayList<>();
        }
        
        return currentGame.getPendingOffers().stream()
            .filter(offer -> offer.getRecipientId().equals(player.getId()))
            .filter(offer -> offer.getStatus() == OfferStatus.PENDING || 
                            offer.getStatus() == OfferStatus.NEGOTIATING)
            .collect(Collectors.toList());
    }

    /**
     * Accept a job offer
     * 
     * @param offer The offer
     */
    public void acceptOffer(JobOffer offer) {
        if (offer == null || offer.getStatus() != OfferStatus.PENDING) {
            return;
        }
        
        JobOpening jobOpening = getJobOpeningById(offer.getJobOpeningId());
        if (jobOpening == null) {
            return;
        }
        
        Person recipient = getPersonById(offer.getRecipientId());
        if (recipient == null) {
            return;
        }
        
        Club club = currentGame.getClubById(jobOpening.getClubId());
        if (club == null) {
            return;
        }
        
        Profession profession = DatabaseLoader.getProfessionById(jobOpening.getProfessionId());
        if (profession == null) {
            return;
        }
        
        // Hire the person
        clubStaffManager.hireStaff(club, profession, recipient);
        
        // Update offer status
        offer.setStatus(OfferStatus.ACCEPTED);
        
        // Update job opening
        jobOpening.setStatus(JobStatus.FILLED);
        jobOpening.setSelectedCandidateId(recipient.getId());
        
        // Update all applications for this job
        for (JobApplication app : currentGame.getAllApplications()) {
            if (app.getJobOpeningId().equals(jobOpening.getId())) {
                if (app.getApplicantId().equals(recipient.getId())) {
                    app.setStatus(ApplicationStatus.ACCEPTED);
                } else {
                    app.setStatus(ApplicationStatus.REJECTED);
                }
            }
        }
        
        // Remove from pending offers
        recipient.getPendingOfferIds().remove(offer.getId());
        
        Gdx.app.log("JobManager", "Offer accepted: " + offer.getId() + 
                   " by " + recipient.getName());
    }

    /**
     * Reject a job offer
     * 
     * @param offer The offer
     */
    public void rejectOffer(JobOffer offer) {
        if (offer == null || offer.getStatus() != OfferStatus.PENDING) {
            return;
        }
        
        offer.setStatus(OfferStatus.REJECTED);
        
        Person recipient = getPersonById(offer.getRecipientId());
        if (recipient != null) {
            recipient.getPendingOfferIds().remove(offer.getId());
        }
        
        Gdx.app.log("JobManager", "Offer rejected: " + offer.getId());
    }

    // ========== Negotiation ==========

    /**
     * Submit a counter-offer
     * 
     * @param originalOffer The original offer
     * @param newSalary The new salary
     * @param newContractLength The new contract length (months)
     * @return The counter-offer, or null if negotiation failed
     */
    public JobOffer submitCounterOffer(JobOffer originalOffer, BigDecimal newSalary, Integer newContractLength) {
        if (originalOffer == null || !originalOffer.canNegotiate()) {
            return null;
        }
        
        if (newSalary == null || newContractLength == null) {
            return null;
        }
        
        // Check negotiation round limit
        if (originalOffer.getNegotiationRound() >= JobConstants.MAX_NEGOTIATION_ROUNDS) {
            Gdx.app.log("JobManager", "Max negotiation rounds reached for offer: " + originalOffer.getId());
            return null;
        }
        
        // Create counter-offer
        JobOffer counterOffer = new JobOffer();
        counterOffer.setId(nextId++);
        counterOffer.setJobOpeningId(originalOffer.getJobOpeningId());
        counterOffer.setRecipientId(originalOffer.getRecipientId());
        counterOffer.setSalary(newSalary);
        counterOffer.setContractLengthMonths(newContractLength);
        counterOffer.setCounterOffer(true);
        counterOffer.setOriginalOfferId(originalOffer.getId());
        counterOffer.setNegotiationRound(originalOffer.getNegotiationRound() + 1);
        counterOffer.setStatus(OfferStatus.NEGOTIATING);
        counterOffer.setOfferDate(currentGame.getGameDate());
        counterOffer.setExpirationDate(counterOffer.getOfferDate().plusDays(JobConstants.OFFER_EXPIRATION_DAYS));
        
        // Link to original offer
        originalOffer.setCounterOfferId(counterOffer.getId());
        originalOffer.setStatus(OfferStatus.NEGOTIATING);
        
        // Add to SaveGame
        currentGame.getPendingOffers().add(counterOffer);
        
        Gdx.app.log("JobManager", "Counter-offer submitted: " + counterOffer.getId() + 
                   " (round " + counterOffer.getNegotiationRound() + ")");
        
        // Process counter-offer (AI response)
        processCounterOffer(counterOffer);
        
        return counterOffer;
    }

    /**
     * Process counter-offer (AI response)
     * 
     * @param counterOffer The counter-offer
     */
    private void processCounterOffer(JobOffer counterOffer) {
        if (counterOffer == null) {
            return;
        }
        
        JobOffer originalOffer = getOfferById(counterOffer.getOriginalOfferId());
        if (originalOffer == null) {
            return;
        }
        
        JobOpening jobOpening = getJobOpeningById(originalOffer.getJobOpeningId());
        if (jobOpening == null) {
            return;
        }
        
        // v1.0: Simple AI logic - accept if within tolerance
        BigDecimal originalSalary = originalOffer.getSalary();
        BigDecimal counterSalary = counterOffer.getSalary();
        
        BigDecimal tolerance = originalSalary.multiply(new BigDecimal(JobConstants.AI_NEGOTIATION_TOLERANCE));
        BigDecimal minAcceptable = originalSalary.subtract(tolerance);
        BigDecimal maxAcceptable = originalSalary.add(tolerance);
        
        boolean salaryAcceptable = counterSalary.compareTo(minAcceptable) >= 0 && 
                                   counterSalary.compareTo(maxAcceptable) <= 0;
        
        // Check contract length (v1.0: accept if within 3 months of original)
        Integer originalLength = originalOffer.getContractLengthMonths();
        Integer counterLength = counterOffer.getContractLengthMonths();
        boolean lengthAcceptable = Math.abs(counterLength - originalLength) <= 3;
        
        if (salaryAcceptable && lengthAcceptable) {
            // Accept counter-offer - update original offer with new terms
            originalOffer.setSalary(counterSalary);
            originalOffer.setContractLengthMonths(counterLength);
            originalOffer.setStatus(OfferStatus.PENDING);
            originalOffer.setCounterOfferId(null);
            
            // Remove counter-offer (terms merged into original)
            currentGame.getPendingOffers().remove(counterOffer);
            
            Gdx.app.log("JobManager", "Counter-offer accepted: " + counterOffer.getId());
        } else {
            // Reject counter-offer
            counterOffer.setStatus(OfferStatus.REJECTED);
            originalOffer.setStatus(OfferStatus.PENDING);
            originalOffer.setCounterOfferId(null);
            
            Gdx.app.log("JobManager", "Counter-offer rejected: " + counterOffer.getId());
        }
    }

    // ========== Utility Methods ==========

    /**
     * Get job opening by ID
     */
    private JobOpening getJobOpeningById(Long id) {
        if (id == null) {
            return null;
        }
        
        return currentGame.getActiveJobOpenings().stream()
            .filter(job -> job.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    /**
     * Get person by ID
     */
    private Person getPersonById(Long id) {
        if (id == null) {
            return null;
        }
        
        return currentGame.getAllPersons().stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    /**
     * Get offer by ID
     */
    private JobOffer getOfferById(Long id) {
        if (id == null) {
            return null;
        }
        
        return currentGame.getPendingOffers().stream()
            .filter(o -> o.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    /**
     * Send offer notification to inbox
     */
    private void sendOfferNotification(Person recipient, JobOffer offer) {
        if (recipient == null || offer == null) {
            return;
        }
        
        JobOpening jobOpening = getJobOpeningById(offer.getJobOpeningId());
        if (jobOpening == null) {
            return;
        }
        
        Club club = currentGame.getClubById(jobOpening.getClubId());
        if (club == null) {
            return;
        }
        
        // Create message
        Message message = new Message();
        message.setId(nextId++);
        message.setRemitent(null); // System message
        message.setMessageTime(currentGame.getGameDate());
        message.setTitle("Job Offer Received");
        message.setPlainTextMessage("You have received a job offer from " + club.getName() + 
                                   ".\n\nSalary: $" + offer.getSalary() + 
                                   "\nContract Length: " + offer.getContractLengthMonths() + " months" +
                                   "\n\nPlease respond within 7 days.");
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        // Add to inbox
        if (currentGame.getAllMessages() == null) {
            currentGame.setAllMessages(new ArrayList<>());
        }
        currentGame.getAllMessages().add(message);
    }
}
```

---

## 5. UI Components

### 5.1 Screen IDs

**Add to MainMenuManager.java:**

```java
// Job System Screens (v1.0)
public static final int CLUB_BROWSER_SCREEN = 10001;
public static final int JOB_BOARD_SCREEN = 10002;
public static final int MY_APPLICATIONS_SCREEN = 10003;
public static final int JOB_OFFER_SCREEN = 10004;
public static final int NEGOTIATION_SCREEN = 10005;
```

---

### 5.2 Club Browser Screen

**File:** `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/tables/jobs/ClubBrowserScreenTable.java`

**Purpose:** Display all clubs with filtering options

**Key Features:**
- List all clubs
- Filter by country
- Sort by name, league
- Show open positions indicator
- Click to view club details

**Implementation Notes:**
- Reuse existing `VisTable` patterns
- Use `VisScrollPane` for club list
- Add filter dropdowns for country
- Add sort buttons

---

### 5.3 Job Board Screen

**File:** `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/tables/jobs/JobBoardScreenTable.java`

**Purpose:** Display all open job positions

**Key Features:**
- List all job openings
- Filter by profession, country
- Sort by salary, deadline
- Show job details
- Apply button

**Implementation Notes:**
- Display: Club name, Position, Salary, Deadline, Match %
- Filter dropdowns for profession and country
- Sort buttons for salary and deadline
- Apply button calls `JobManager.applyForJob()`

---

### 5.4 My Applications Screen

**File:** `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/tables/jobs/MyApplicationsScreenTable.java`

**Purpose:** Track job applications

**Key Features:**
- List all applications
- Show status (Pending, Rejected, Offer Received)
- Filter by status
- Withdraw button
- View details button

**Implementation Notes:**
- Display: Club, Position, Date Applied, Status, Match %
- Color-code status (green for offer, red for rejected)
- Withdraw button only for PENDING status

---

### 5.5 Job Offer Screen

**File:** `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/tables/jobs/JobOfferScreenTable.java`

**Purpose:** View and respond to job offers

**Key Features:**
- Display offer details
- Show terms (salary, contract length)
- Accept button
- Reject button
- Negotiate button

**Implementation Notes:**
- Large, clear display of offer terms
- Three buttons: Accept, Reject, Negotiate
- Show expiration date
- Show negotiation rounds remaining

---

### 5.6 Negotiation Screen

**File:** `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/tables/jobs/NegotiationScreenTable.java`

**Purpose:** Negotiate job offer terms

**Key Features:**
- Display current offer
- Adjust salary (input field or slider)
- Adjust contract length (input field or slider)
- Submit counter-offer button
- Cancel button
- Show negotiation history

**Implementation Notes:**
- Two input fields: Salary, Contract Length
- Show original offer for comparison
- Show AI tolerance range (v1.0: simple display)
- Submit button calls `JobManager.submitCounterOffer()`

---

### 5.7 Menu Integration

**Changes to MainMenuManager.java:**

```java
// Add buttons for unemployed menu
private VisTextButton clubBrowserButton = null;
private VisTextButton jobBoardButton = null;
private VisTextButton myApplicationsButton = null;

// Add screen tables
private ClubBrowserScreenTable clubBrowserScreenTable = null;
private JobBoardScreenTable jobBoardScreenTable = null;
private MyApplicationsScreenTable myApplicationsScreenTable = null;
private JobOfferScreenTable jobOfferScreenTable = null;
private NegotiationScreenTable negotiationScreenTable = null;

// In setDynamicButtonsMenu() for unemployed:
if (currentGame.getOwner().getPrimaryProfession().getId().equals(2L)) {
    // ... existing buttons ...
    
    // Job System buttons
    buttonsMenu.add(clubBrowserButton).fill();
    buttonsMenu.row();
    
    buttonsMenu.add(jobBoardButton).fill();
    buttonsMenu.row();
    
    buttonsMenu.add(myApplicationsButton).fill();
    buttonsMenu.row();
}

// In setActiveMainScreen():
case CLUB_BROWSER_SCREEN:
    clubBrowserScreenTable.updateDynamicComponents();
    parentTable.add(clubBrowserScreenTable).grow();
    break;

case JOB_BROWSER_SCREEN:
    jobBoardScreenTable.updateDynamicComponents();
    parentTable.add(jobBoardScreenTable).grow();
    break;

// ... etc
```

---

## 6. Algorithms & Logic

### 6.1 Job Salary Calculation

**Algorithm:**
```
baseSalary = clubBalance * 0.01 / 12  // 1% of balance per year, monthly
multiplier = professionMultiplier     // Manager: 1.5, Director: 1.2, Scout: 0.8
salary = baseSalary * multiplier
```

**v1.0:** Simple calculation, can be enhanced in v2.0 with reputation, league level, etc.

---

### 6.2 Application Processing

**Algorithm (v1.0 - Simplified):**
```
1. Player applies for job
2. Calculate match percentage (based on reputation)
3. If matchPercentage >= 70:
   - Make offer immediately
   - Set application status to OFFER_RECEIVED
4. Else:
   - Reject application
   - Set application status to REJECTED
```

**v2.0:** Add delay (1-3 days) and interview system

---

### 6.3 AI Negotiation Logic

**Algorithm (v1.0 - Simple):**
```
1. Player submits counter-offer with newSalary and newContractLength
2. Calculate tolerance:
   tolerance = originalSalary * 0.10  // 10%
   minAcceptable = originalSalary - tolerance
   maxAcceptable = originalSalary + tolerance
3. Check salary:
   if newSalary >= minAcceptable AND newSalary <= maxAcceptable:
       salaryAcceptable = true
4. Check contract length:
   if abs(newContractLength - originalLength) <= 3:
       lengthAcceptable = true
5. If salaryAcceptable AND lengthAcceptable:
   - Accept counter-offer
   - Update original offer with new terms
   - Set status to PENDING
6. Else:
   - Reject counter-offer
   - Set status to REJECTED
```

**v2.0:** Add AI personality, multiple negotiation strategies

---

### 6.4 Match Percentage Calculation

**Algorithm (v1.0 - Simple):**
```
if minReputation == null:
    return 100  // No requirement
if applicantReputation >= minReputation:
    return 100  // Meets requirement
else:
    return (applicantReputation * 100) / minReputation  // Percentage match
```

**v2.0:** Add nationality, licenses, experience factors

---

## 7. Integration Points

### 7.1 Game Engine Integration

**File:** `FuttoboruGameEngine.java`

**Changes:**
```java
// Add JobManager
private JobManager jobManager;

// In constructor:
this.jobManager = new JobManager(gameInstance, clubStaffManager);

// In continueGame():
// Update job openings daily
jobManager.updateJobOpenings();
```

---

### 7.2 Futtoboru Integration

**File:** `Futtoboru.java`

**Changes:**
```java
// Add JobManager and ClubStaffManager
private JobManager jobManager;
private ClubStaffManager clubStaffManager;

// In create():
this.clubStaffManager = new ClubStaffManager(this);
this.jobManager = new JobManager(this, clubStaffManager);

// Getters
public JobManager getJobManager() {
    return jobManager;
}

public ClubStaffManager getClubStaffManager() {
    return clubStaffManager;
}
```

---

### 7.3 Inbox Integration

**File:** `InboxScreenTable.java`

**Changes:**
- Job offer notifications automatically added via `JobManager.sendOfferNotification()`
- Messages use existing `Message` class
- No changes needed to inbox display logic

---

### 7.4 Save/Load Integration

**File:** `SaveLoadSystem.java`

**Changes:**
- All new data models implement `Serializable`
- SaveGame extensions automatically serialized
- No custom serializers needed for v1.0

---

## 8. Implementation Details

### 8.1 ID Generation

**v1.0:** Simple incrementing counter
```java
private static long nextId = 1L;
```

**v2.0:** Consider UUID or database-generated IDs

---

### 8.2 Date Handling

- Use `LocalDateTime` for all dates
- Use `currentGame.getGameDate()` for current game time
- All calculations use game time, not system time

---

### 8.3 Error Handling

- Null checks for all parameters
- Log errors using `Gdx.app.log()` and `Gdx.app.error()`
- Graceful degradation (return null instead of throwing exceptions)

---

### 8.4 Performance Considerations

- Use streams for filtering (already in design)
- Cache frequently accessed data (club staff, job openings)
- Limit list sizes (pagination for v2.0)

---

## 9. Testing Strategy

### 9.1 Unit Tests

**Test Classes:**
- `JobManagerTest`
- `ClubStaffManagerTest`
- `JobOpeningTest`
- `JobApplicationTest`
- `JobOfferTest`

**Key Test Cases:**
- Create job opening for vacant position
- Apply for job (success and failure cases)
- Process application (offer vs reject)
- Accept/reject offer
- Negotiation (accept/reject counter-offer)
- Application limits (3 per week)

---

### 9.2 Integration Tests

**Test Scenarios:**
- Full flow: Browse → Apply → Receive Offer → Negotiate → Accept
- Multiple applications
- Offer expiration
- Job opening expiration
- Staff hiring/firing

---

### 9.3 Manual Testing Checklist

- [ ] Can browse all clubs
- [ ] Can filter clubs by country
- [ ] Can view job board
- [ ] Can filter jobs by profession
- [ ] Can apply for job
- [ ] Application limit enforced (3 per week)
- [ ] Receive offer notification in inbox
- [ ] Can view offer details
- [ ] Can accept offer
- [ ] Can reject offer
- [ ] Can negotiate offer
- [ ] AI accepts reasonable counter-offers
- [ ] AI rejects unreasonable counter-offers
- [ ] Max negotiation rounds enforced
- [ ] Accepting offer updates game state correctly
- [ ] Job opening expires after deadline
- [ ] Vacant positions create job openings

---

## 10. Future Enhancements (v2.0+)

### 10.1 Interview System
- Interview invitations
- Interview questions
- Interview results
- Multiple interview rounds

### 10.2 Reputation System
- Calculate reputation from achievements
- Reputation affects job availability
- Reputation affects negotiation power

### 10.3 Enhanced Negotiation
- Signing bonuses
- Performance bonuses
- Contract expectations
- AI personality affects negotiation

### 10.4 Job Requirements Matching
- Nationality requirements
- License requirements
- Experience requirements
- Match percentage calculation

---

**Document Version:** 1.0  
**Last Updated:** 2025-12-14  
**Status:** Design Complete - Ready for Implementation

