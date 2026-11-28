package com.rndmodgames.futtoboru.engine.jobs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.data.MessagePriority;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.jobs.ApplicationStatus;
import com.rndmodgames.futtoboru.data.jobs.JobApplication;
import com.rndmodgames.futtoboru.data.jobs.JobConstants;
import com.rndmodgames.futtoboru.data.jobs.JobOffer;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;
import com.rndmodgames.futtoboru.data.jobs.JobStatus;
import com.rndmodgames.futtoboru.data.jobs.OfferStatus;
import com.rndmodgames.futtoboru.engine.finances.Money;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Job Manager v1
 *
 * Manages job openings, applications, offers, and negotiations.
 *
 * <p>J-01: one opening per vacant post ({@link #getOpenOpening}); the board lists only openings before their
 * deadline whose post is still vacant; one live offer per post and person; offers lapse after
 * {@link JobConstants#OFFER_EXPIRATION_DAYS}; a declined or lapsed offer closes the application; negotiation rounds
 * count. AI clubs (D31): at an opening's deadline the club appoints a candidate unless an offer to the player is
 * still open, and AI staff leave their posts now and then, so the posts are advertised again.
 *
 * @author Geomancer86
 */
public class JobManager {

    private Futtoboru gameInstance;
    private SaveGame currentGame;
    private ClubStaffManager clubStaffManager;

    /** Daily chance an AI staff member leaves his post (tests set it). */
    private double staffDeparturePerDay = JobConstants.AI_STAFF_DEPARTURE_PER_DAY;

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

    private RandomSource random() {
        return Futtoboru.randomOf(gameInstance);
    }

    private LocalDateTime today() {
        return currentGame != null ? currentGame.getGameDate() : null;
    }

    /** The daily chance an AI club's staff member leaves his post (default {@link JobConstants#AI_STAFF_DEPARTURE_PER_DAY}). */
    public void setStaffDeparturePerDay(double chance) {
        this.staffDeparturePerDay = chance;
    }

    // ========== Job Openings ==========

    /**
     * Get all available job openings
     *
     * @return the openings taking applications: before their deadline, for a post that is still vacant
     */
    public List<JobOpening> getAvailableJobs() {
        if (currentGame == null || currentGame.getActiveJobOpenings() == null) {
            return new ArrayList<>();
        }

        LocalDateTime gameDate = currentGame.getGameDate();
        Map<Long, Club> clubs = clubsById();
        return currentGame.getActiveJobOpenings().stream()
            .filter(job -> job != null && job.isAcceptingApplications(gameDate) && isPostVacant(job, clubs))
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
            .filter(job -> profession.getId().equals(job.getProfessionId()))
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
            .filter(job -> club.getId().equals(job.getClubId()))
            .collect(Collectors.toList());
    }

    /** @return the open opening of a post (also past its deadline, while the club has not appointed), or null */
    public JobOpening getOpenOpening(Club club, Profession profession) {
        if (club == null || profession == null || currentGame == null) {
            return null;
        }
        for (JobOpening job : currentGame.getActiveJobOpenings()) {
            if (job != null && job.getStatus() == JobStatus.OPEN && club.getId().equals(job.getClubId())
                    && profession.getId().equals(job.getProfessionId())) {
                return job;
            }
        }
        return null;
    }

    /**
     * Create a job opening for a vacant position
     *
     * @param club The club
     * @param profession The profession
     * @return the post's opening (the one already open, if any), or null if the post is not vacant
     */
    public JobOpening createJobOpening(Club club, Profession profession) {
        if (club == null || club.getId() == null || profession == null || profession.getId() == null
                || clubStaffManager == null || currentGame == null || currentGame.getGameDate() == null) {
            Log.error(Log.ENGINE_JOBS, "JobManager", "Cannot create job opening: missing club, profession or game");
            return null;
        }
        if (!ClubStaffManager.isStaffPosition(profession) || !clubStaffManager.isPositionVacant(club, profession)) {
            Log.debug(Log.ENGINE_JOBS, "JobManager", "Post not vacant: " + profession.getName() + " at " + club.getName());
            return null;
        }
        // one opening per post: also one past its deadline (an offer still open keeps it), so no second advert
        JobOpening existing = getOpenOpening(club, profession);
        if (existing != null) {
            return existing;
        }

        LocalDateTime gameDate = currentGame.getGameDate();
        JobOpening opening = new JobOpening(club, profession, gameDate);
        opening.setId(nextId());
        opening.setApplicationDeadline(gameDate.plusDays(advertDays(opening.getId())));
        opening.setSalary(calculateJobSalary(club, profession));
        opening.setMinReputation(calculateMinReputation(club, profession));
        currentGame.getActiveJobOpenings().add(opening);

        Log.debug(Log.ENGINE_JOBS, "JobManager", "Created job opening: " + profession.getName() + " at " + club.getName());
        return opening;
    }

    /** Days an advert runs: {@link JobConstants#JOB_OPENING_MIN_DEADLINE_DAYS} to MAX, seeded per opening. */
    private int advertDays(long openingId) {
        int span = JobConstants.JOB_OPENING_MAX_DEADLINE_DAYS - JobConstants.JOB_OPENING_MIN_DEADLINE_DAYS + 1;
        return JobConstants.JOB_OPENING_MIN_DEADLINE_DAYS + random().fork("job-opening:" + openingId).nextInt(span);
    }

    /**
     * Calculate job salary (v1.0: simple calculation)
     *
     * @param club The club
     * @param profession The profession
     * @return Calculated salary, in pounds and pence (J-01: it showed as 12.495)
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
        BigDecimal multiplier = BigDecimal.ONE;
        if (profession.is(Profession.MANAGER)) {
            multiplier = new BigDecimal("1.5");
        } else if (profession.is(Profession.DIRECTOR)) {
            multiplier = new BigDecimal("1.2");
        } else if (profession.is(Profession.SCOUT)) {
            multiplier = new BigDecimal("0.8");
        }

        BigDecimal result = baseSalary.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);

        // Ensure minimum salary
        BigDecimal minimum = new BigDecimal("5.00");
        return result.compareTo(minimum) < 0 ? minimum : result;
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
        if (currentGame == null || currentGame.getAllClubs() == null || clubStaffManager == null) {
            Log.error(Log.ENGINE_JOBS, "JobManager", "Cannot initialize job openings: no game or staff manager");
            return;
        }
        clubStaffManager.ensureHoldsPost(currentGame.getOwner());
        int created = advertiseVacancies(clubsById());
        Log.info(Log.ENGINE_JOBS, "JobManager", "Initialized " + created + " job openings for "
                + clubsById().size() + " clubs");
    }

    /**
     * Update job openings (called daily by game engine): offers lapse, filled posts leave the board, clubs appoint
     * at the deadline, AI staff leave now and then, every vacant post is advertised (once).
     */
    public void updateJobOpenings() {
        if (currentGame == null || currentGame.getGameDate() == null || clubStaffManager == null) {
            return;
        }
        LocalDateTime today = currentGame.getGameDate();

        clubStaffManager.ensureHoldsPost(currentGame.getOwner());
        expireOffers(today);
        processPendingApplications();

        Map<Long, Club> clubs = clubsById();
        closeFilledOpenings(clubs);
        int appointed = appointAtDeadline(today, clubs);
        int left = staffDepartures(today, clubs);
        int advertised = advertiseVacancies(clubs);
        if (appointed + left + advertised > 0) {
            Log.info(Log.ENGINE_JOBS, "JobManager", today.toLocalDate() + ": " + appointed + " appointed, " + left
                    + " left their posts, " + advertised + " posts advertised");
        }
    }

    /** Applications still pending (none while processing is instant; kept for a processing delay). */
    private void processPendingApplications() {
        for (JobApplication application : new ArrayList<>(currentGame.getAllApplications())) {
            if (application != null && application.getStatus() == ApplicationStatus.PENDING) {
                processApplication(application);
            }
        }
    }

    /** Advertises every vacant post that has no open opening. @return openings created */
    private int advertiseVacancies(Map<Long, Club> clubs) {
        Set<String> open = new HashSet<>();
        for (JobOpening job : currentGame.getActiveJobOpenings()) {
            if (job != null && job.getStatus() == JobStatus.OPEN) {
                open.add(postKey(job.getClubId(), job.getProfessionId()));
            }
        }
        int created = 0;
        for (Club club : clubs.values()) {
            for (Profession profession : clubStaffManager.getVacantPositions(club)) {
                if (open.add(postKey(club.getId(), profession.getId())) && createJobOpening(club, profession) != null) {
                    created++;
                }
            }
        }
        return created;
    }

    /**
     * Openings of posts filled meanwhile (another opening, a hire elsewhere) are closed; a post advertised twice
     * (saves from before J-01 re-posted an advert on its deadline day) keeps one opening.
     */
    private void closeFilledOpenings(Map<Long, Club> clubs) {
        Map<String, JobOpening> kept = new LinkedHashMap<>();
        for (JobOpening job : currentGame.getActiveJobOpenings()) {
            if (job == null || job.getStatus() != JobStatus.OPEN) {
                continue;
            }
            Club club = clubs.get(job.getClubId());
            if (club == null) {
                continue;
            }
            if (!ClubStaffManager.isStaffPosition(DatabaseLoader.getProfessionById(job.getProfessionId()))) {
                // saves from before J-01 advertised agents, investors and owners as club posts
                job.setStatus(JobStatus.EXPIRED);
                continue;
            }
            if (!club.isPositionVacant(job.getProfessionId())) {
                closeOpening(job, club.getStaffId(job.getProfessionId()));
                continue;
            }
            String key = postKey(job.getClubId(), job.getProfessionId());
            JobOpening first = kept.putIfAbsent(key, job);
            if (first != null && !hasLiveOffer(job)) {
                job.setStatus(JobStatus.EXPIRED);
            }
        }
    }

    /** At its deadline a club appoints a candidate, unless an offer to the player is still open. @return how many */
    private int appointAtDeadline(LocalDateTime today, Map<Long, Club> clubs) {
        int appointed = 0;
        for (JobOpening job : new ArrayList<>(currentGame.getActiveJobOpenings())) {
            if (job == null || job.getStatus() != JobStatus.OPEN || job.isAcceptingApplications(today)
                    || hasLiveOffer(job)) {
                continue;
            }
            Club club = clubs.get(job.getClubId());
            Profession profession = DatabaseLoader.getProfessionById(job.getProfessionId());
            if (club == null || !ClubStaffManager.isStaffPosition(profession) || !club.isPositionVacant(profession.getId())) {
                continue;
            }
            Person candidate = clubStaffManager.appointCandidate(club, profession,
                    random().fork("staff-appointment:" + job.getId()));
            if (candidate != null) {
                fillOpening(job, club, profession, candidate);
                appointed++;
            }
        }
        return appointed;
    }

    /** AI staff leave their posts now and then (never the player). @return how many left */
    private int staffDepartures(LocalDateTime today, Map<Long, Club> clubs) {
        if (staffDeparturePerDay <= 0) {
            return 0;
        }
        Person owner = currentGame.getOwner();
        Long ownerId = owner != null ? owner.getId() : null;
        int left = 0;
        for (Club club : clubs.values()) {
            for (Profession profession : clubStaffManager.getStaffPositions()) {
                Long holder = club.getStaffId(profession.getId());
                if (holder == null || holder.equals(ownerId)) {
                    continue;
                }
                RandomSource rng = random().fork("staff-departure:" + club.getId() + ":" + profession.getId() + ":"
                        + today.toLocalDate());
                if (rng.nextDouble() < staffDeparturePerDay && clubStaffManager.leavePost(club, profession) != null) {
                    left++;
                }
            }
        }
        return left;
    }

    /**
     * {@code person} takes the post: hired (leaving any post at another club), the opening filled, the other
     * applications for it turned down and other people's offers for it withdrawn.
     */
    private void fillOpening(JobOpening opening, Club club, Profession profession, Person person) {
        clubStaffManager.hireStaff(club, profession, person);
        closeOpening(opening, person.getId());
        for (JobApplication app : currentGame.getAllApplications()) {
            if (app != null && opening.getId().equals(app.getJobOpeningId()) && person.getId().equals(app.getApplicantId())
                    && app.isActive()) {
                app.setStatus(ApplicationStatus.ACCEPTED);
                Person applicant = getPersonById(app.getApplicantId());
                if (applicant != null) {
                    applicant.getActiveApplicationIds().remove(app.getId());
                }
            }
        }
    }

    /** The opening is filled by {@code holderId}: other applications turned down, other offers for it withdrawn. */
    private void closeOpening(JobOpening opening, Long holderId) {
        opening.setStatus(JobStatus.FILLED);
        if (opening.getSelectedCandidateId() == null) {
            opening.setSelectedCandidateId(holderId);
        }
        for (JobApplication app : currentGame.getAllApplications()) {
            if (app != null && opening.getId().equals(app.getJobOpeningId()) && app.isActive()
                    && !Objects.equals(app.getApplicantId(), holderId)) {
                app.setStatus(ApplicationStatus.REJECTED);
                Person applicant = getPersonById(app.getApplicantId());
                if (applicant != null) {
                    applicant.getActiveApplicationIds().remove(app.getId());
                }
            }
        }
        for (JobOffer offer : currentGame.getPendingOffers()) {
            if (offer != null && opening.getId().equals(offer.getJobOpeningId()) && isLive(offer)
                    && !Objects.equals(offer.getRecipientId(), holderId)) {
                offer.setStatus(OfferStatus.EXPIRED);
                Person recipient = getPersonById(offer.getRecipientId());
                if (recipient != null) {
                    recipient.getPendingOfferIds().remove(offer.getId());
                }
            }
        }
    }

    private static String postKey(Long clubId, Long professionId) {
        return clubId + ":" + professionId;
    }

    /**
     * A post's opening counts while the post is vacant (a club without the opening's club id counts as vacant)
     * and it is a club post at all (saves from before J-01 advertised agents, investors and owners).
     */
    private static boolean isPostVacant(JobOpening job, Map<Long, Club> clubs) {
        if (!ClubStaffManager.isStaffPosition(DatabaseLoader.getProfessionById(job.getProfessionId()))) {
            return false;
        }
        Club club = clubs.get(job.getClubId());
        return club == null || club.isPositionVacant(job.getProfessionId());
    }

    /** Each club once, by id (the first entry, as {@link SaveGame#getClubById} finds it). */
    private Map<Long, Club> clubsById() {
        Map<Long, Club> clubs = new LinkedHashMap<>();
        if (currentGame != null && currentGame.getAllClubs() != null) {
            for (Club club : currentGame.getAllClubs()) {
                if (club != null && club.getId() != null) {
                    clubs.putIfAbsent(club.getId(), club);
                }
            }
        }
        return clubs;
    }

    // ========== Applications ==========

    /**
     * Apply for a job opening
     *
     * @param applicant The applicant
     * @param jobOpening The job opening
     * @return the application (the open one if he applied already), or null if he cannot apply: the advert is
     *         closed, the post filled, or he applied before and was turned down, declined or let the offer lapse
     */
    public JobApplication applyForJob(Person applicant, JobOpening jobOpening) {
        if (applicant == null || applicant.getId() == null || jobOpening == null || jobOpening.getId() == null
                || currentGame == null || currentGame.getGameDate() == null) {
            Log.error(Log.ENGINE_JOBS, "JobManager", "applyForJob: missing applicant, opening, ids or game date");
            return null;
        }
        LocalDateTime gameDate = currentGame.getGameDate();

        if (!applicant.canApplyForJob(gameDate)) {
            Log.info(Log.ENGINE_JOBS, "JobManager", "Application limit reached for " + applicant.getName());
            return null;
        }
        Map<Long, Club> clubs = clubsById();
        if (!jobOpening.isAcceptingApplications(gameDate) || !isPostVacant(jobOpening, clubs)) {
            Log.info(Log.ENGINE_JOBS, "JobManager", "Job opening not accepting applications: " + jobOpening.getId());
            return null;
        }
        JobApplication existing = getApplicationFor(applicant, jobOpening);
        if (existing != null) {
            // one application per post: the open one, or none after a refusal, a declined or a lapsed offer
            return existing.isActive() ? existing : null;
        }

        JobApplication application = new JobApplication(applicant, jobOpening);
        application.setId(nextId());
        application.setApplicationDate(gameDate);
        application.setMatchPercentage(calculateMatchPercentage(applicant, jobOpening));
        currentGame.getAllApplications().add(application);
        jobOpening.getApplicationIds().add(application.getId());
        applicant.getActiveApplicationIds().add(application.getId());
        applicant.incrementApplicationCount(gameDate); // Use game date, not real-world time

        Log.info(Log.ENGINE_JOBS, "JobManager", "Application: " + applicant.getName() + " for job " + jobOpening.getId());

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

        if (minRep == null || minRep <= 0 || applicantRep >= minRep) {
            // No requirement, or meets it - 100% match
            return 100;
        }
        return Math.max(0, (applicantRep * 100) / minRep);
    }

    /**
     * Get all applications for a player
     *
     * @param player The player
     * @return List of applications
     */
    public List<JobApplication> getPlayerApplications(Person player) {
        if (player == null || player.getId() == null || currentGame == null || currentGame.getAllApplications() == null) {
            return new ArrayList<>();
        }

        Long playerId = player.getId();
        return currentGame.getAllApplications().stream()
            .filter(app -> app != null && playerId.equals(app.getApplicantId()))
            .collect(Collectors.toList());
    }

    /**
     * @return {@code person}'s latest application for {@code opening} that he did not withdraw (its status is what
     *         the job screens show for the post), or null
     */
    public JobApplication getApplicationFor(Person person, JobOpening opening) {
        if (person == null || opening == null || opening.getId() == null) {
            return null;
        }
        JobApplication latest = null;
        for (JobApplication app : getPlayerApplications(person)) {
            if (opening.getId().equals(app.getJobOpeningId()) && app.getStatus() != ApplicationStatus.WITHDRAWN) {
                latest = app;
            }
        }
        return latest;
    }

    /**
     * Process an application (v1.0: simplified - instant offer if meets requirements)
     *
     * @param application The application
     */
    private void processApplication(JobApplication application) {
        if (application == null || application.getStatus() != ApplicationStatus.PENDING) {
            return;
        }
        JobOpening jobOpening = getJobOpeningById(application.getJobOpeningId());
        Person applicant = getPersonById(application.getApplicantId());
        if (jobOpening == null || applicant == null) {
            Log.error(Log.ENGINE_JOBS, "JobManager", "processApplication: opening or applicant missing for "
                    + application.getId());
            return;
        }
        Integer matchPercentage = application.getMatchPercentage();
        // v1.0: Simple logic - make offer if match percentage >= 70%
        if (matchPercentage != null && matchPercentage >= 70 && makeOffer(jobOpening, applicant) != null) {
            application.setStatus(ApplicationStatus.OFFER_RECEIVED);
        } else {
            application.setStatus(ApplicationStatus.REJECTED);
            applicant.getActiveApplicationIds().remove(application.getId());
            Log.info(Log.ENGINE_JOBS, "JobManager", "Application rejected: " + application.getId() + " (match: "
                    + matchPercentage + "%)");
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
     * @return the offer: the one already open for this post and candidate (J-01: a second one showed as a second
     *         row), else a new one with its letter in the inbox
     */
    public JobOffer makeOffer(JobOpening jobOpening, Person candidate) {
        if (jobOpening == null || candidate == null || currentGame == null) {
            return null;
        }
        JobOffer live = liveOffer(jobOpening, candidate);
        if (live != null) {
            return live;
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

    /** @return true for an offer still to be answered (pending or being negotiated, not lapsed) */
    private boolean isLive(JobOffer offer) {
        return offer != null && !offer.isCounterOffer()
                && (offer.getStatus() == OfferStatus.PENDING || offer.getStatus() == OfferStatus.NEGOTIATING)
                && !offer.isExpired(today());
    }

    /** @return the live offer of {@code opening} to {@code person}, or null */
    private JobOffer liveOffer(JobOpening opening, Person person) {
        for (JobOffer offer : currentGame.getPendingOffers()) {
            if (isLive(offer) && opening.getId().equals(offer.getJobOpeningId())
                    && person.getId() != null && person.getId().equals(offer.getRecipientId())) {
                return offer;
            }
        }
        return null;
    }

    private boolean hasLiveOffer(JobOpening opening) {
        for (JobOffer offer : currentGame.getPendingOffers()) {
            if (isLive(offer) && opening.getId().equals(offer.getJobOpeningId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all offers for a player
     *
     * @param player The player
     * @return the offers he can still answer (one per post)
     */
    public List<JobOffer> getPlayerOffers(Person player) {
        if (player == null || player.getId() == null || currentGame == null || currentGame.getPendingOffers() == null) {
            return new ArrayList<>();
        }

        Long playerId = player.getId();
        return currentGame.getPendingOffers().stream()
            .filter(offer -> isLive(offer) && playerId.equals(offer.getRecipientId()))
            .collect(Collectors.toList());
    }

    /**
     * Offers left unanswered past their date lapse; the application shows the offer as expired. An application
     * still "offer received" without an offer to answer (saves from before J-01: a declined offer left it so) is
     * closed as declined.
     */
    private void expireOffers(LocalDateTime today) {
        Set<String> live = new HashSet<>();
        for (JobOffer offer : currentGame.getPendingOffers()) {
            if (offer != null && !offer.isCounterOffer() && offer.isExpired(today)) {
                expireOffer(offer);
            }
            if (isLive(offer)) {
                live.add(offer.getRecipientId() + ":" + offer.getJobOpeningId());
            }
        }
        for (JobApplication app : currentGame.getAllApplications()) {
            if (app != null && app.getStatus() == ApplicationStatus.OFFER_RECEIVED
                    && !live.contains(app.getApplicantId() + ":" + app.getJobOpeningId())) {
                app.setStatus(ApplicationStatus.DECLINED);
                Person applicant = getPersonById(app.getApplicantId());
                if (applicant != null) {
                    applicant.getActiveApplicationIds().remove(app.getId());
                }
            }
        }
    }

    private void expireOffer(JobOffer offer) {
        offer.setStatus(OfferStatus.EXPIRED);
        closeApplication(offer, ApplicationStatus.EXPIRED);
        Log.info(Log.ENGINE_JOBS, "JobManager", "Offer lapsed: " + offer.getId());
    }

    /** The recipient's application behind {@code offer} gets {@code status}; the offer leaves his pending list. */
    private void closeApplication(JobOffer offer, ApplicationStatus status) {
        Person recipient = getPersonById(offer.getRecipientId());
        if (recipient != null) {
            recipient.getPendingOfferIds().remove(offer.getId());
        }
        for (JobApplication app : currentGame.getAllApplications()) {
            if (app != null && app.getStatus() == ApplicationStatus.OFFER_RECEIVED
                    && offer.getJobOpeningId().equals(app.getJobOpeningId())
                    && offer.getRecipientId().equals(app.getApplicantId())) {
                app.setStatus(status);
                if (recipient != null) {
                    recipient.getActiveApplicationIds().remove(app.getId());
                }
            }
        }
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
     * @return true if accepted, false if it cannot be (answered already, lapsed, the post filled meanwhile)
     */
    public boolean acceptOffer(JobOffer offer) {
        if (offer == null || offer.getStatus() != OfferStatus.PENDING || currentGame == null) {
            Log.error(Log.ENGINE_JOBS, "JobManager", "acceptOffer: no offer, or not pending");
            return false;
        }
        if (offer.isExpired(today())) {
            expireOffer(offer);
            return false;
        }
        JobOpening jobOpening = getJobOpeningById(offer.getJobOpeningId());
        Person recipient = getPersonById(offer.getRecipientId());
        Club club = jobOpening != null ? currentGame.getClubById(jobOpening.getClubId()) : null;
        Profession profession = jobOpening != null ? DatabaseLoader.getProfessionById(jobOpening.getProfessionId()) : null;
        if (jobOpening == null || recipient == null || club == null || profession == null) {
            Log.error(Log.ENGINE_JOBS, "JobManager", "acceptOffer: opening, recipient, club or profession missing");
            return false;
        }
        Long holder = club.getStaffId(profession.getId());
        if (holder != null && !holder.equals(recipient.getId())) {
            // the post was filled meanwhile: the offer is void
            expireOffer(offer);
            return false;
        }

        // one job at a time: his other offers and applications end
        for (JobOffer other : currentGame.getPendingOffers()) {
            if (other != offer && isLive(other) && recipient.getId().equals(other.getRecipientId())) {
                other.setStatus(OfferStatus.REJECTED);
                closeApplication(other, ApplicationStatus.DECLINED);
            }
        }
        for (JobApplication app : currentGame.getAllApplications()) {
            if (app != null && recipient.getId().equals(app.getApplicantId())
                    && !jobOpening.getId().equals(app.getJobOpeningId()) && app.getStatus() == ApplicationStatus.PENDING) {
                app.setStatus(ApplicationStatus.WITHDRAWN);
                recipient.getActiveApplicationIds().remove(app.getId());
            }
        }

        // hired (leaving any post at his old club), the opening filled
        fillOpening(jobOpening, club, profession, recipient);
        offer.setStatus(OfferStatus.ACCEPTED);
        recipient.getPendingOfferIds().remove(offer.getId());

        Log.info(Log.ENGINE_JOBS, "JobManager", "Offer accepted: " + recipient.getName() + " is " + profession.getName()
                + " of " + club.getName());
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
        closeApplication(offer, ApplicationStatus.DECLINED);
        Log.info(Log.ENGINE_JOBS, "JobManager", "Offer declined: " + offer.getId());
    }

    // ========== Negotiation ==========

    /**
     * Submit a counter-offer: one negotiation round. The club accepts terms within
     * {@link JobConstants#AI_NEGOTIATION_TOLERANCE} of the salary it advertised and 3 months of its contract
     * length; then the offer carries the new terms, else its terms stand. Either way the offer is still open.
     *
     * @param originalOffer The original offer
     * @param newSalary The new salary (rounded to the penny)
     * @param newContractLength The new contract length (months)
     * @return the counter-offer, {@link OfferStatus#ACCEPTED} or {@link OfferStatus#REJECTED}; null if it could
     *         not be made (no rounds left, the offer not open, a salary or length of nothing)
     */
    public JobOffer submitCounterOffer(JobOffer originalOffer, BigDecimal newSalary, Integer newContractLength) {
        LocalDateTime gameDate = today();
        if (originalOffer == null || !originalOffer.canNegotiate(gameDate) || newSalary == null
                || newContractLength == null || newContractLength < 1) {
            return null;
        }
        BigDecimal salary = newSalary.setScale(2, RoundingMode.HALF_UP);
        if (salary.signum() <= 0) {
            return null;
        }
        JobOpening jobOpening = getJobOpeningById(originalOffer.getJobOpeningId());

        // the counter-offer is the round's record; its answer goes onto the original offer
        int round = originalOffer.getNegotiationRound() + 1;
        JobOffer counterOffer = new JobOffer();
        counterOffer.setId(nextId());
        counterOffer.setJobOpeningId(originalOffer.getJobOpeningId());
        counterOffer.setRecipientId(originalOffer.getRecipientId());
        counterOffer.setSalary(salary);
        counterOffer.setContractLengthMonths(newContractLength);
        counterOffer.setCounterOffer(true);
        counterOffer.setOriginalOfferId(originalOffer.getId());
        counterOffer.setNegotiationRound(round);
        counterOffer.setOfferDate(gameDate);
        counterOffer.setExpirationDate(originalOffer.getExpirationDate());
        originalOffer.setNegotiationRound(round);
        originalOffer.setCounterOfferId(null);

        BigDecimal advertised = jobOpening != null && jobOpening.getSalary() != null ? jobOpening.getSalary()
                : originalOffer.getSalary();
        Integer advertisedLength = jobOpening != null && jobOpening.getContractLengthMonths() != null
                ? jobOpening.getContractLengthMonths() : originalOffer.getContractLengthMonths();
        BigDecimal margin = advertised.multiply(BigDecimal.valueOf(JobConstants.AI_NEGOTIATION_TOLERANCE));
        boolean salaryAcceptable = salary.compareTo(advertised.add(margin)) <= 0;
        boolean lengthAcceptable = advertisedLength == null || Math.abs(newContractLength - advertisedLength) <= 3;

        if (salaryAcceptable && lengthAcceptable) {
            originalOffer.setSalary(salary);
            originalOffer.setContractLengthMonths(newContractLength);
            counterOffer.setStatus(OfferStatus.ACCEPTED);
        } else {
            counterOffer.setStatus(OfferStatus.REJECTED);
        }
        originalOffer.setStatus(OfferStatus.PENDING);
        Log.info(Log.ENGINE_JOBS, "JobManager", "Counter-offer " + counterOffer.getStatus() + " (round " + round + "): "
                + Money.format(salary) + ", " + newContractLength + " months");
        return counterOffer;
    }

    // ========== Utility Methods ==========

    /**
     * Get job opening by ID
     */
    public JobOpening getJobOpeningById(Long id) {
        if (id == null || currentGame == null || currentGame.getActiveJobOpenings() == null) {
            return null;
        }

        return currentGame.getActiveJobOpenings().stream()
            .filter(job -> job != null && id.equals(job.getId()))
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

        // Create message (J-01: in the JOB category, which the inbox shows a job seeker; it had none)
        Message message = new Message();
        message.setId(nextId());
        message.setRemitent(null); // System message
        message.setMessageTime(currentGame.getGameDate());
        message.setCategory(MessageCategory.JOB);
        message.setPriority(MessagePriority.NORMAL);
        message.setMessageType("JOB_OFFER");
        message.setTitle(LanguageModLoader.getValue("jobs_msg_offer_title"));
        message.setPlainTextMessage(LanguageModLoader.format("msg_job_offer_text",
                club.getName(), Money.format(offer.getSalary()), offer.getContractLengthMonths()));
        message.setIsRead(false);
        message.setIsDeleted(false);

        // Add to inbox
        if (currentGame.getAllMessages() == null) {
            currentGame.setAllMessages(new ArrayList<>());
        }
        currentGame.getAllMessages().add(message);
    }
}
