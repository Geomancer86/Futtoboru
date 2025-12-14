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
        if (currentGame == null || currentGame.getActiveJobOpenings() == null) {
            return new ArrayList<>();
        }
        
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
        
        if (club.getClubBalance() == null || club.getClubBalance().compareTo(BigDecimal.ZERO) <= 0) {
            // Default minimum salary if club has no balance
            return new BigDecimal("10.00");
        }
        
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
        if (currentGame == null || currentGame.getAllClubs() == null) {
            return;
        }
        
        Gdx.app.log("JobManager", "Initializing job openings for all clubs...");
        
        int openingsCreated = 0;
        
        // Create openings for vacant positions in all clubs
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
                    JobOpening opening = createJobOpening(club, profession);
                    if (opening != null) {
                        openingsCreated++;
                    }
                }
            }
        }
        
        Gdx.app.log("JobManager", "Initialized " + openingsCreated + " job openings");
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
        
        // Expire old openings
        for (JobOpening opening : new ArrayList<>(currentGame.getActiveJobOpenings())) {
            if (opening.isExpired()) {
                opening.setStatus(JobStatus.EXPIRED);
                Gdx.app.log("JobManager", "Expired job opening: " + opening.getId());
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
        application.setApplicationDate(currentGame.getGameDate());
        
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
        if (player == null || currentGame == null || currentGame.getAllApplications() == null) {
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
        if (application.getMatchPercentage() != null && application.getMatchPercentage() >= 70) {
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
        offer.setOfferDate(currentGame.getGameDate());
        offer.setExpirationDate(currentGame.getGameDate().plusDays(JobConstants.OFFER_EXPIRATION_DAYS));
        
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
        if (player == null || currentGame == null || currentGame.getPendingOffers() == null) {
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
        counterOffer.setExpirationDate(currentGame.getGameDate().plusDays(JobConstants.OFFER_EXPIRATION_DAYS));
        
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
            .filter(p -> p.getId().equals(id))
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

