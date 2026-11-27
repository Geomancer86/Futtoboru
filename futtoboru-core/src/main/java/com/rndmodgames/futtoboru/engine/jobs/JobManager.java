package com.rndmodgames.futtoboru.engine.jobs;

import java.math.BigDecimal;
import com.rndmodgames.futtoboru.engine.finances.Money;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import com.rndmodgames.futtoboru.system.Log;

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

    public JobManager(Futtoboru gameInstance, ClubStaffManager clubStaffManager) {
        this.gameInstance = gameInstance;
        this.currentGame = gameInstance.getCurrentGame();
        this.clubStaffManager = clubStaffManager;
    }

    /**
     * Ids of openings, applications, offers and messages come from the game's allocator (P2-4)
     */
    private long nextId() {
        SaveGame game = gameInstance != null && gameInstance.getCurrentGame() != null
                ? gameInstance.getCurrentGame() : currentGame;
        return game.getIdAllocator().nextId();
    }

    // ========== Job Openings ==========

    /**
     * Get all available job openings
     * 
     * @return List of open job openings
     */
    public List<JobOpening> getAvailableJobs() {
        if (currentGame == null || currentGame.getActiveJobOpenings() == null) {
            return new ArrayList<>();
        }
        
        LocalDateTime gameDate = currentGame.getGameDate();
        return currentGame.getActiveJobOpenings().stream()
            .filter(job -> job.isAcceptingApplications(gameDate))
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
        try {
            if (club == null || profession == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "Cannot create job opening: club or profession is null");
                return null;
            }
            
            if (club.getId() == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "Cannot create job opening: club.getId() is null for club: " + club.getName());
                return null;
            }
            
            if (profession.getId() == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "Cannot create job opening: profession.getId() is null for profession: " + profession.getName());
                return null;
            }
            
            if (clubStaffManager == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "Cannot create job opening: clubStaffManager is null");
                return null;
            }
            
            // Check if position is actually vacant
            if (!clubStaffManager.isPositionVacant(club, profession)) {
                Log.info(Log.ENGINE_JOBS, "JobManager", "Position not vacant: " + profession.getName() + 
                           " at " + club.getName());
                return null;
            }
            
            // Check if opening already exists
            for (JobOpening existing : getAvailableJobs(club)) {
                if (existing != null && existing.getProfessionId() != null && 
                    existing.getProfessionId().equals(profession.getId())) {
                    Log.info(Log.ENGINE_JOBS, "JobManager", "Job opening already exists: " + profession.getName() + 
                               " at " + club.getName());
                    return existing;
                }
            }
            
            // Create new job opening with game date
            LocalDateTime gameDate = currentGame.getGameDate();
            if (gameDate == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "Cannot create job opening: gameDate is null");
                return null;
            }
            JobOpening opening = new JobOpening(club, profession, gameDate);
            opening.setId(nextId());
            
            // Calculate salary based on club finances and profession (v1.0: simple)
            opening.setSalary(calculateJobSalary(club, profession));
            
            // Set requirements (v1.0: simple - only reputation)
            opening.setMinReputation(calculateMinReputation(club, profession));
            
            // Add to SaveGame
            if (currentGame.getActiveJobOpenings() == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "Cannot add job opening: getActiveJobOpenings() is null");
                return null;
            }
            
            currentGame.getActiveJobOpenings().add(opening);
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Created job opening: " + profession.getName() + 
                       " at " + club.getName());
            
            return opening;
        } catch (Exception e) {
            Log.error(Log.ENGINE_JOBS, "JobManager", "ERROR in createJobOpening() for " + 
                         (profession != null ? profession.getName() : "null profession") + 
                         " at " + (club != null ? club.getName() : "null club"), e);
            Log.error(Log.ENGINE_JOBS, e);
            return null;
        }
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
        
        if (club.getClubBalance() == null || club.getClubBalance().compareTo(BigDecimal.ZERO) <= 0) {
            // Default minimum salary if club has no balance
            return new BigDecimal("10.00");
        }
        
        BigDecimal baseSalary = club.getClubBalance()
            .multiply(new BigDecimal("0.01"))
            .divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP); // Monthly
        
        // Adjust by profession (v1.0: simple multipliers)
        double multiplier = 1.0;
        if (profession.is(Profession.MANAGER)) { // Manager
            multiplier = 1.5;
        } else if (profession.is(Profession.DIRECTOR)) { // Director
            multiplier = 1.2;
        } else if (profession.is(Profession.SCOUT)) { // Scout
            multiplier = 0.8;
        }
        
        BigDecimal result = baseSalary.multiply(new BigDecimal(multiplier));
        
        // Ensure minimum salary
        if (result.compareTo(new BigDecimal("5.00")) < 0) {
            result = new BigDecimal("5.00");
        }
        
        return result;
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
     * Initialize job openings for all vacant positions (called when game starts)
     * 
     * Creates job openings for all clubs that have vacant positions.
     */
    public void initializeJobOpenings() {
        try {
            if (currentGame == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "Cannot initialize job openings: currentGame is null");
                return;
            }
            
            if (currentGame.getAllClubs() == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "Cannot initialize job openings: getAllClubs() is null");
                return;
            }
            
            if (clubStaffManager == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "Cannot initialize job openings: clubStaffManager is null");
                return;
            }
            
            if (currentGame.getActiveJobOpenings() == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "Cannot initialize job openings: getActiveJobOpenings() is null");
                return;
            }
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Initializing job openings for all clubs...");
            Log.info(Log.ENGINE_JOBS, "JobManager", "Total clubs: " + currentGame.getAllClubs().size());
            
            int openingsCreated = 0;
            
            // Create openings for vacant positions in all clubs
            for (Club club : currentGame.getAllClubs()) {
                if (club == null) {
                    Log.info(Log.ENGINE_JOBS, "JobManager", "Skipping null club");
                    continue;
                }
                
                try {
                    List<Profession> vacant = clubStaffManager.getVacantPositions(club);
                    
                    if (vacant == null) {
                        Log.info(Log.ENGINE_JOBS, "JobManager", "getVacantPositions returned null for club: " + club.getName());
                        continue;
                    }
                    
                    for (Profession profession : vacant) {
                        if (profession == null) {
                            Log.info(Log.ENGINE_JOBS, "JobManager", "Skipping null profession");
                            continue;
                        }
                        
                        // Check if opening already exists
                        boolean exists = false;
                        try {
                            for (JobOpening existing : getAvailableJobs(club)) {
                                if (existing != null && existing.getProfessionId() != null && 
                                    existing.getProfessionId().equals(profession.getId())) {
                                    exists = true;
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            Log.error(Log.ENGINE_JOBS, "JobManager", "Error checking existing jobs for club: " + club.getName(), e);
                            continue;
                        }
                        
                        if (!exists) {
                            try {
                                JobOpening opening = createJobOpening(club, profession);
                                if (opening != null) {
                                    openingsCreated++;
                                }
                            } catch (Exception e) {
                                Log.error(Log.ENGINE_JOBS, "JobManager", "Error creating job opening for " + profession.getName() + 
                                           " at " + club.getName(), e);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.error(Log.ENGINE_JOBS, "JobManager", "Error processing club: " + club.getName(), e);
                    continue;
                }
            }
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Initialized " + openingsCreated + " job openings");
        } catch (Exception e) {
            Log.error(Log.ENGINE_JOBS, "JobManager", "CRITICAL ERROR in initializeJobOpenings()", e);
            Log.error(Log.ENGINE_JOBS, e);
        }
    }

    /**
     * Update job openings (called daily by game engine)
     * - Expire old openings
     * - Create openings for vacant positions
     */
    public void updateJobOpenings() {
        if (currentGame == null || currentGame.getGameDate() == null) {
            return;
        }
        
        LocalDateTime gameDate = currentGame.getGameDate();
        
        Log.info(Log.ENGINE_JOBS, "JobManager", "=== updateJobOpenings START ===");
        Log.info(Log.ENGINE_JOBS, "JobManager", "Daily update: " + gameDate);
        
        // Expire old openings
        for (JobOpening opening : new ArrayList<>(currentGame.getActiveJobOpenings())) {
            if (opening.isExpired(gameDate)) {
                opening.setStatus(JobStatus.EXPIRED);
                Log.info(Log.ENGINE_JOBS, "JobManager", "Expired job opening: " + opening.getId());
            }
        }
        
        // Process pending applications (v1.0: instant processing, but check daily for any missed)
        if (currentGame.getAllApplications() != null) {
            int processedCount = 0;
            for (JobApplication application : new ArrayList<>(currentGame.getAllApplications())) {
                if (application == null) {
                    continue;
                }
                
                // Process applications that are still pending
                if (application.getStatus() == ApplicationStatus.PENDING) {
                    try {
                        Log.info(Log.ENGINE_JOBS, "JobManager", "Processing pending application: " + application.getId());
                        processApplication(application);
                        processedCount++;
                    } catch (Exception e) {
                        Log.error(Log.ENGINE_JOBS, "JobManager", "ERROR processing application " + application.getId() + " in daily update!", e);
                        Log.error(Log.ENGINE_JOBS, e);
                    }
                }
            }
            if (processedCount > 0) {
                Log.info(Log.ENGINE_JOBS, "JobManager", "Processed " + processedCount + " pending applications");
            }
        }
        
        // Create openings for vacant positions
        for (Club club : currentGame.getAllClubs()) {
            if (club == null) {
                continue;
            }
            
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
        
        Log.info(Log.ENGINE_JOBS, "JobManager", "=== updateJobOpenings COMPLETE ===");
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
        try {
            Log.info(Log.ENGINE_JOBS, "JobManager", "=== applyForJob START ===");
            Log.info(Log.ENGINE_JOBS, "JobManager", "Applicant: " + (applicant != null ? applicant.getName() : "NULL"));
            Log.info(Log.ENGINE_JOBS, "JobManager", "JobOpening: " + (jobOpening != null ? jobOpening.getId() : "NULL"));
            
            if (applicant == null || jobOpening == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "applyForJob: applicant or jobOpening is null!");
                return null;
            }
            
            if (currentGame == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "applyForJob: currentGame is null!");
                return null;
            }
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Step 1: Getting game date...");
            LocalDateTime gameDate = currentGame.getGameDate();
            if (gameDate == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "applyForJob: gameDate is null!");
                Log.error(Log.ENGINE_JOBS, "JobManager", "ERROR: gameDate is null!");
                return null;
            }
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Step 2: Validating application limits...");
            // Validate application (use game date, not real-world time)
            if (!applicant.canApplyForJob(gameDate)) {
                Log.info(Log.ENGINE_JOBS, "JobManager", "Application limit reached for " + applicant.getName() + 
                           " (applications this week: " + applicant.getApplicationsThisWeek() + "/3)");
                Log.error(Log.ENGINE_JOBS, "JobManager", "ERROR: Application limit reached! " + 
                                 applicant.getApplicationsThisWeek() + "/3 applications this week.");
                return null;
            }
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Step 3: Checking if job opening accepts applications...");
            if (gameDate == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "applyForJob: gameDate is null!");
                Log.error(Log.ENGINE_JOBS, "JobManager", "ERROR: gameDate is null!");
                return null;
            }
            
            if (!jobOpening.isAcceptingApplications(gameDate)) {
                Log.info(Log.ENGINE_JOBS, "JobManager", "Job opening not accepting applications: " + jobOpening.getId());
                return null;
            }
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Step 3: Checking for existing applications...");
            // Check if already applied
            List<JobApplication> playerApplications = getPlayerApplications(applicant);
            if (playerApplications != null) {
                for (JobApplication existing : playerApplications) {
                    if (existing != null && existing.getJobOpeningId() != null && 
                        existing.getJobOpeningId().equals(jobOpening.getId()) && 
                        existing.isActive()) {
                        Log.info(Log.ENGINE_JOBS, "JobManager", "Already applied for job: " + jobOpening.getId());
                        return existing;
                    }
                }
            }
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Step 5: Validating applicant and jobOpening IDs...");
            // Validate applicant and jobOpening have IDs before creating application
            if (applicant.getId() == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "CRITICAL: Applicant ID is null! Cannot create application.");
                return null;
            }
            if (jobOpening.getId() == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "CRITICAL: JobOpening ID is null! Cannot create application.");
                Log.error(Log.ENGINE_JOBS, "JobManager", "CRITICAL ERROR: JobOpening ID is null!");
                return null;
            }
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Step 6: Creating new application...");
            // Create application
            JobApplication application;
            try {
                application = new JobApplication(applicant, jobOpening);
            } catch (IllegalArgumentException e) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "Failed to create JobApplication: " + e.getMessage(), e);
                Log.error(Log.ENGINE_JOBS, "JobManager", "ERROR: Failed to create JobApplication: " + e.getMessage());
                Log.error(Log.ENGINE_JOBS, e);
                return null;
            }
            
            if (application == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "Failed to create JobApplication object!");
                Log.error(Log.ENGINE_JOBS, "JobManager", "ERROR: Failed to create JobApplication!");
                return null;
            }
            
            // Double-check applicantId was set correctly
            if (application.getApplicantId() == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "CRITICAL: Application created but applicantId is null! Setting manually...");
                application.setApplicantId(applicant.getId());
                if (application.getApplicantId() == null) {
                    Log.error(Log.ENGINE_JOBS, "JobManager", "CRITICAL: Could not set applicantId! Aborting application creation.");
                    Log.error(Log.ENGINE_JOBS, "JobManager", "CRITICAL ERROR: Could not set applicantId!");
                    return null;
                }
            }
            
            application.setId(nextId());
            application.setApplicationDate(gameDate);
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Step 7: Calculating match percentage...");
            // Calculate match percentage (v1.0: simple - based on reputation)
            Integer matchPercentage = calculateMatchPercentage(applicant, jobOpening);
            application.setMatchPercentage(matchPercentage);
            Log.info(Log.ENGINE_JOBS, "JobManager", "Match percentage: " + matchPercentage + "%");
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Step 8: Adding application to SaveGame...");
            // Add to SaveGame
            if (currentGame.getAllApplications() == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "getAllApplications() returned null! Initializing...");
                Log.error(Log.ENGINE_JOBS, "JobManager", "ERROR: getAllApplications() is null!");
                // This should not happen, but let's be safe
                return null;
            }
            currentGame.getAllApplications().add(application);
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Step 9: Adding application ID to job opening...");
            // Add to job opening
            if (jobOpening.getApplicationIds() == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "jobOpening.getApplicationIds() returned null!");
                Log.error(Log.ENGINE_JOBS, "JobManager", "ERROR: jobOpening.getApplicationIds() is null!");
                return null;
            }
            jobOpening.getApplicationIds().add(application.getId());
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Step 10: Updating applicant records...");
            // Update applicant
            if (applicant.getActiveApplicationIds() == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "applicant.getActiveApplicationIds() returned null!");
                Log.error(Log.ENGINE_JOBS, "JobManager", "ERROR: applicant.getActiveApplicationIds() is null!");
                return null;
            }
            applicant.getActiveApplicationIds().add(application.getId());
            applicant.incrementApplicationCount(gameDate); // Use game date, not real-world time
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Application created successfully: " + applicant.getName() + 
                       " for job " + jobOpening.getId());
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Step 11: Processing application...");
            // Process application (v1.0: instant processing)
            if (JobConstants.APPLICATION_PROCESSING_DELAY_DAYS == 0) {
                try {
                    processApplication(application);
                    Log.info(Log.ENGINE_JOBS, "JobManager", "Application processed successfully.");
                } catch (Exception e) {
                    Log.error(Log.ENGINE_JOBS, "JobManager", "ERROR processing application!", e);
                    Log.error(Log.ENGINE_JOBS, "JobManager", "ERROR processing application:");
                    Log.error(Log.ENGINE_JOBS, e);
                    // Don't return null - application was created, just processing failed
                }
            }
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "=== applyForJob SUCCESS ===");
            return application;
            
        } catch (Exception e) {
            Log.error(Log.ENGINE_JOBS, "JobManager", "CRITICAL ERROR in applyForJob! Exception: " + e.getClass().getName() + ", Message: " + e.getMessage(), e);
            return null;
        }
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
        if (player == null || player.getId() == null || currentGame == null || currentGame.getAllApplications() == null) {
            Log.info(Log.ENGINE_JOBS, "JobManager", "getPlayerApplications: Invalid parameters, returning empty list");
            return new ArrayList<>();
        }
        
        Long playerId = player.getId();
        return currentGame.getAllApplications().stream()
            .filter(app -> app != null && app.getApplicantId() != null && app.getApplicantId().equals(playerId))
            .collect(Collectors.toList());
    }

    /**
     * Process an application (v1.0: simplified - instant offer if meets requirements)
     * 
     * @param application The application
     */
    private void processApplication(JobApplication application) {
        try {
            Log.info(Log.ENGINE_JOBS, "JobManager", "=== processApplication START ===");
            Log.info(Log.ENGINE_JOBS, "JobManager", "Application ID: " + (application != null ? application.getId() : "NULL"));
            
            if (application == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "processApplication: application is null!");
                Log.error(Log.ENGINE_JOBS, "JobManager", "ERROR: processApplication called with null application!");
                return;
            }
            
            if (application.getStatus() != ApplicationStatus.PENDING) {
                Log.info(Log.ENGINE_JOBS, "JobManager", "Application status is not PENDING: " + application.getStatus());
                return;
            }
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Step 1: Getting job opening...");
            JobOpening jobOpening = getJobOpeningById(application.getJobOpeningId());
            if (jobOpening == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "processApplication: jobOpening not found for ID: " + application.getJobOpeningId());
                Log.error(Log.ENGINE_JOBS, "JobManager", "ERROR: jobOpening not found!");
                return;
            }
            Log.info(Log.ENGINE_JOBS, "JobManager", "Job opening found: " + jobOpening.getId());
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Step 2: Getting applicant...");
            Person applicant = getPersonById(application.getApplicantId());
            if (applicant == null) {
                Log.error(Log.ENGINE_JOBS, "JobManager", "processApplication: applicant not found for ID: " + application.getApplicantId());
                Log.error(Log.ENGINE_JOBS, "JobManager", "ERROR: applicant not found!");
                return;
            }
            Log.info(Log.ENGINE_JOBS, "JobManager", "Applicant found: " + applicant.getName());
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Step 3: Checking match percentage...");
            Integer matchPercentage = application.getMatchPercentage();
            Log.info(Log.ENGINE_JOBS, "JobManager", "Match percentage: " + (matchPercentage != null ? matchPercentage + "%" : "NULL"));
            
            // v1.0: Simple logic - make offer if match percentage >= 70%
            if (matchPercentage != null && matchPercentage >= 70) {
                Log.info(Log.ENGINE_JOBS, "JobManager", "Match percentage is sufficient, making offer...");
                // Make offer
                try {
                    JobOffer offer = makeOffer(jobOpening, applicant);
                    if (offer != null) {
                        application.setStatus(ApplicationStatus.OFFER_RECEIVED);
                        Log.info(Log.ENGINE_JOBS, "JobManager", "Offer made successfully for application: " + application.getId());
                    } else {
                        Log.error(Log.ENGINE_JOBS, "JobManager", "makeOffer returned null for application: " + application.getId());
                        Log.error(Log.ENGINE_JOBS, "JobManager", "ERROR: makeOffer returned null!");
                        application.setStatus(ApplicationStatus.REJECTED);
                    }
                } catch (Exception e) {
                    Log.error(Log.ENGINE_JOBS, "JobManager", "ERROR in makeOffer!", e);
                    Log.error(Log.ENGINE_JOBS, e);
                    application.setStatus(ApplicationStatus.REJECTED);
                }
            } else {
                // Reject application
                Log.info(Log.ENGINE_JOBS, "JobManager", "Match percentage too low, rejecting application...");
                application.setStatus(ApplicationStatus.REJECTED);
                Log.info(Log.ENGINE_JOBS, "JobManager", "Application rejected: " + application.getId() + " (match: " + matchPercentage + "%)");
                Log.info(Log.ENGINE_JOBS, "JobManager", "Application rejected: " + application.getId());
            }
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "=== processApplication SUCCESS ===");
            
        } catch (Exception e) {
            Log.error(Log.ENGINE_JOBS, "JobManager", "CRITICAL ERROR in processApplication! Exception: " + e.getClass().getName() + ", Message: " + e.getMessage(), e);
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
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Application withdrawn: " + application.getId());
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
        offer.setId(nextId());
        offer.setOfferDate(currentGame.getGameDate());
        offer.setExpirationDate(currentGame.getGameDate().plusDays(JobConstants.OFFER_EXPIRATION_DAYS));
        
        // Add to SaveGame
        currentGame.getPendingOffers().add(offer);
        
        // Add to candidate's pending offers
        candidate.getPendingOfferIds().add(offer.getId());
        
        // Send notification to inbox
        sendOfferNotification(candidate, offer);
        
        Log.info(Log.ENGINE_JOBS, "JobManager", "Offer made: " + offer.getId() + 
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
        if (player == null || player.getId() == null || currentGame == null || currentGame.getPendingOffers() == null) {
            Log.info(Log.ENGINE_JOBS, "JobManager", "getPlayerOffers: Invalid parameters, returning empty list");
            return new ArrayList<>();
        }
        
        Long playerId = player.getId();
        return currentGame.getPendingOffers().stream()
            .filter(offer -> offer != null && offer.getRecipientId() != null && offer.getRecipientId().equals(playerId))
            .filter(offer -> offer.getStatus() == OfferStatus.PENDING || 
                            offer.getStatus() == OfferStatus.NEGOTIATING)
            .collect(Collectors.toList());
    }

    /**
     * Accept a job offer
     * 
     * CRITICAL: Only allows one active job at a time. Accepting a new job automatically:
     * - Cancels all other pending offers
     * - Rejects all other pending applications
     * - Removes person from previous club (if employed)
     * 
     * @param offer The offer
     * @return true if accepted successfully, false if failed
     */
    public boolean acceptOffer(JobOffer offer) {
        if (offer == null || offer.getStatus() != OfferStatus.PENDING) {
            Log.error(Log.ENGINE_JOBS, "JobManager", "acceptOffer: Invalid offer or status not PENDING");
            return false;
        }
        
        JobOpening jobOpening = getJobOpeningById(offer.getJobOpeningId());
        if (jobOpening == null) {
            Log.error(Log.ENGINE_JOBS, "JobManager", "acceptOffer: Job opening not found");
            return false;
        }
        
        Person recipient = getPersonById(offer.getRecipientId());
        if (recipient == null) {
            Log.error(Log.ENGINE_JOBS, "JobManager", "acceptOffer: Recipient not found");
            return false;
        }
        
        Club club = currentGame.getClubById(jobOpening.getClubId());
        if (club == null) {
            Log.error(Log.ENGINE_JOBS, "JobManager", "acceptOffer: Club not found");
            return false;
        }
        
        Profession profession = DatabaseLoader.getProfessionById(jobOpening.getProfessionId());
        if (profession == null) {
            Log.error(Log.ENGINE_JOBS, "JobManager", "acceptOffer: Profession not found");
            return false;
        }
        
        Log.info(Log.ENGINE_JOBS, "JobManager", "=== acceptOffer START ===");
        Log.info(Log.ENGINE_JOBS, "JobManager", "Recipient: " + recipient.getName());
        Log.info(Log.ENGINE_JOBS, "JobManager", "Club: " + club.getName());
        Log.info(Log.ENGINE_JOBS, "JobManager", "Profession: " + profession.getName());
        
        // CRITICAL FIX: Check if person already has a job
        if (recipient.getCurrentClubId() != null) {
            Club currentClub = currentGame.getClubById(recipient.getCurrentClubId());
            if (currentClub != null) {
                Log.info(Log.ENGINE_JOBS, "JobManager", "Person already employed at: " + currentClub.getName());
                Log.info(Log.ENGINE_JOBS, "JobManager", "WARNING: Person already employed at " + currentClub.getName() + 
                                 ". Removing from previous club...");
                
                // Remove from previous club (set staff to null for that profession)
                clubStaffManager.setClubStaff(currentClub, profession, null);
                recipient.setCurrentClubId(null);
                Log.info(Log.ENGINE_JOBS, "JobManager", "Removed from previous club");
            }
        }
        
        // CRITICAL FIX: Cancel ALL other pending offers
        Log.info(Log.ENGINE_JOBS, "JobManager", "Cancelling all other pending offers...");
        int cancelledOffers = 0;
        for (JobOffer otherOffer : new ArrayList<>(currentGame.getPendingOffers())) {
            if (otherOffer != null && 
                otherOffer.getRecipientId() != null && 
                otherOffer.getRecipientId().equals(recipient.getId()) &&
                !otherOffer.getId().equals(offer.getId()) &&
                (otherOffer.getStatus() == OfferStatus.PENDING || otherOffer.getStatus() == OfferStatus.NEGOTIATING)) {
                
                otherOffer.setStatus(OfferStatus.REJECTED);
                recipient.getPendingOfferIds().remove(otherOffer.getId());
                cancelledOffers++;
                Log.info(Log.ENGINE_JOBS, "JobManager", "Cancelled offer: " + otherOffer.getId());
            }
        }
        Log.info(Log.ENGINE_JOBS, "JobManager", "Cancelled " + cancelledOffers + " other pending offers");
        
        // CRITICAL FIX: Reject ALL other pending applications
        Log.info(Log.ENGINE_JOBS, "JobManager", "Rejecting all other pending applications...");
        int rejectedApps = 0;
        for (JobApplication app : currentGame.getAllApplications()) {
            if (app != null &&
                app.getApplicantId() != null &&
                app.getApplicantId().equals(recipient.getId()) &&
                !app.getJobOpeningId().equals(jobOpening.getId()) &&
                (app.getStatus() == ApplicationStatus.PENDING || app.getStatus() == ApplicationStatus.OFFER_RECEIVED)) {
                
                app.setStatus(ApplicationStatus.REJECTED);
                recipient.getActiveApplicationIds().remove(app.getId());
                rejectedApps++;
                Log.info(Log.ENGINE_JOBS, "JobManager", "Rejected application: " + app.getId());
            }
        }
        Log.info(Log.ENGINE_JOBS, "JobManager", "Rejected " + rejectedApps + " other pending applications");
        
        // Hire the person at the new club
        clubStaffManager.hireStaff(club, profession, recipient);
        recipient.setCurrentClubId(club.getId());
        
        // Update offer status
        offer.setStatus(OfferStatus.ACCEPTED);
        
        // Update job opening
        jobOpening.setStatus(JobStatus.FILLED);
        jobOpening.setSelectedCandidateId(recipient.getId());
        
        // Update all applications for this job
        for (JobApplication app : currentGame.getAllApplications()) {
            if (app != null && app.getJobOpeningId() != null && app.getJobOpeningId().equals(jobOpening.getId())) {
                if (app.getApplicantId() != null && app.getApplicantId().equals(recipient.getId())) {
                    app.setStatus(ApplicationStatus.ACCEPTED);
                } else {
                    app.setStatus(ApplicationStatus.REJECTED);
                }
            }
        }
        
        // Remove from pending offers
        recipient.getPendingOfferIds().remove(offer.getId());
        
        Log.info(Log.ENGINE_JOBS, "JobManager", "=== acceptOffer SUCCESS ===");
        Log.info(Log.ENGINE_JOBS, "JobManager", "Offer accepted: " + offer.getId() + " by " + recipient.getName() + 
                         " | Cancelled " + cancelledOffers + " offers | Rejected " + rejectedApps + " applications");
        
        return true;
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
        
        Log.info(Log.ENGINE_JOBS, "JobManager", "Offer rejected: " + offer.getId());
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
        LocalDateTime gameDate = currentGame != null ? currentGame.getGameDate() : null;
        if (originalOffer == null || !originalOffer.canNegotiate(gameDate)) {
            return null;
        }
        
        if (newSalary == null || newContractLength == null) {
            return null;
        }
        
        // Check negotiation round limit
        if (originalOffer.getNegotiationRound() >= JobConstants.MAX_NEGOTIATION_ROUNDS) {
            Log.info(Log.ENGINE_JOBS, "JobManager", "Max negotiation rounds reached for offer: " + originalOffer.getId());
            return null;
        }
        
        // Create counter-offer
        JobOffer counterOffer = new JobOffer();
        counterOffer.setId(nextId());
        counterOffer.setJobOpeningId(originalOffer.getJobOpeningId());
        counterOffer.setRecipientId(originalOffer.getRecipientId());
        counterOffer.setSalary(newSalary);
        counterOffer.setContractLengthMonths(newContractLength);
        counterOffer.setCounterOffer(true);
        counterOffer.setOriginalOfferId(originalOffer.getId());
        counterOffer.setNegotiationRound(originalOffer.getNegotiationRound() + 1);
        counterOffer.setStatus(OfferStatus.NEGOTIATING);
        counterOffer.setOfferDate(currentGame.getGameDate());
        counterOffer.setExpirationDate(currentGame.getGameDate().plusDays(JobConstants.OFFER_EXPIRATION_DAYS));
        
        // Link to original offer
        originalOffer.setCounterOfferId(counterOffer.getId());
        originalOffer.setStatus(OfferStatus.NEGOTIATING);
        
        // Add to SaveGame
        currentGame.getPendingOffers().add(counterOffer);
        
        Log.info(Log.ENGINE_JOBS, "JobManager", "Counter-offer submitted: " + counterOffer.getId() + 
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
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Counter-offer accepted: " + counterOffer.getId());
        } else {
            // Reject counter-offer
            counterOffer.setStatus(OfferStatus.REJECTED);
            originalOffer.setStatus(OfferStatus.PENDING);
            originalOffer.setCounterOfferId(null);
            
            Log.info(Log.ENGINE_JOBS, "JobManager", "Counter-offer rejected: " + counterOffer.getId());
        }
    }

    // ========== Utility Methods ==========

    /**
     * Get job opening by ID
     */
    private JobOpening getJobOpeningById(Long id) {
        if (id == null || currentGame == null || currentGame.getActiveJobOpenings() == null) {
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
        if (id == null || currentGame == null || currentGame.getAllPersons() == null) {
            return null;
        }
        
        return currentGame.getAllPersons().stream()
            .filter(p -> p != null && id.equals(p.getId()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Get offer by ID
     */
    private JobOffer getOfferById(Long id) {
        if (id == null || currentGame == null || currentGame.getPendingOffers() == null) {
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
        message.setId(nextId());
        message.setRemitent(null); // System message
        message.setMessageTime(currentGame.getGameDate());
        message.setTitle("Job Offer Received");
        message.setPlainTextMessage("You have received a job offer from " + club.getName() + 
                                   ".\n\nSalary: " + Money.format(offer.getSalary()) + 
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

