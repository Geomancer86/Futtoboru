package com.rndmodgames.futtoboru.engine.committee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubCommittee;
import com.rndmodgames.futtoboru.data.CommitteeMember;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.jobs.ApplicationStatus;
import com.rndmodgames.futtoboru.data.jobs.JobApplication;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;
import com.rndmodgames.futtoboru.data.jobs.JobStatus;
import com.rndmodgames.futtoboru.engine.jobs.ClubStaffManager;
import com.rndmodgames.futtoboru.engine.jobs.JobManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MenuEntries;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.save.LoadResult;
import com.rndmodgames.futtoboru.system.save.SaveCodec;

/**
 * G-05: every club's committee sets the season's expectations, its confidence in the secretary moves, it warns and
 * then dismisses him (the player becomes a job seeker; an AI club appoints a replacement from the job market), the
 * same seed gives the same committees, and it is saved with the club. A new 1888-89 game, seed 1888, the manager at
 * Preston; days pass for the committees and the job market only (no matches).
 */
class CommitteeTests extends BaseTest {

    private SaveGame game;
    private JobManager jobs;
    private ClubStaffManager staff;
    private Person owner;
    private Profession manager;

    @BeforeEach
    void newGame() {
        application.setSeed(1888L);
        game = NewGameFixture.newGame(application);
        jobs = application.getJobManager();
        staff = application.getClubStaffManager();
        owner = game.getOwner();
        manager = DatabaseLoader.getInstance().getProfessionByDataId(Profession.MANAGER);
        jobs.setStaffDeparturePerDay(0.0);
    }

    /** The engine's daily order for these systems: scripts, committees, then the job market (no matches). */
    private void passDays(int days) {
        for (int i = 0; i < days; i++) {
            game.setGameDate(game.getGameDate().plusDays(1));
            application.getScriptsManager().checkGameScripts(); // the League is formed by the season's scripts
            CommitteeManager.daily(game, Futtoboru.randomOf(application), staff,
                    application.getGameEngine().getMessageManager());
            jobs.updateJobOpenings();
        }
    }

    private List<Message> letters(String type) {
        return game.getAllMessages().stream().filter(m -> type.equals(m.getMessageType())).collect(Collectors.toList());
    }

    private static Club club(String dataId) {
        return DatabaseLoader.getInstance().getClubByDataId(dataId);
    }

    /** Days pass until the League is formed and its clubs' committees have met for 1888-89. */
    private void untilTheCommitteesMeet() {
        for (int i = 0; i < 120 && (game.getCurrentClub().getCommittee() == null
                || game.getCurrentClub().getCommittee().getSeasonYear() == null); i++) {
            passDays(1);
        }
        assertNotNull(game.getCurrentClub().getCommittee().getSeasonYear(), "the committee met");
    }

    @Test
    void everyClubGetsACommitteeAndTheSeasonsExpectations() {
        passDays(1);
        assertNotNull(game.getCurrentClub().getCommittee(), "a committee from the first day");
        assertNull(game.getCurrentClub().getCommittee().getSeasonYear(), "no League yet: it waits");
        untilTheCommitteesMeet();
        game.setGameDate(LocalDateTime.of(1888, 7, 31, 12, 0));
        passDays(1); // 1 August: the clubs outside the League meet too
        for (Club club : game.getAllClubs()) {
            ClubCommittee c = club.getCommittee();
            assertNotNull(c, club.getName() + " has a committee");
            assertEquals(1888, c.getSeasonYear());
            assertTrue(c.getMembers().size() >= 4 && c.getMembers().size() <= 6, club.getName());
            assertNotNull(c.member(CommitteeMember.PRESIDENT));
            assertNotNull(c.member(CommitteeMember.TREASURER));
            for (CommitteeMember m : c.getMembers()) {
                assertNotNull(m.getName());
                assertNotNull(m.getLastname());
                assertNotNull(m.getOccupation());
                assertTrue(m.getBirthYear() >= 1822 && m.getBirthYear() <= 1856, "born " + m.getBirthYear());
            }
            assertNotNull(c.getWageBandPence());
        }
        Club preston = game.getCurrentClub();
        ClubCommittee c = preston.getCommittee();
        assertEquals(12, c.getLeagueSize());
        assertTrue(c.getTargetPosition() >= 1 && c.getTargetPosition() <= 12);
        assertEquals(owner.getId(), c.getSecretaryId());
        assertTrue(Math.abs(c.getConfidence() - CommitteeManager.START_CONFIDENCE) <= 6,
                "no matches yet: only the weekly mood moved it, " + c.getConfidence());
        // the biggest clubs are expected higher up than the smallest
        int best = Integer.MAX_VALUE;
        int worst = 0;
        for (Club club : CommitteeManager.leagueOf(game, preston).getLeagueClubs()) {
            best = Math.min(best, club.getCommittee().getTargetPosition());
            worst = Math.max(worst, club.getCommittee().getTargetPosition());
        }
        assertTrue(best < worst, "targets differ by size: " + best + ".." + worst);
        assertTrue(game.getAllClubs().stream().anyMatch(cl -> CommitteeManager.leagueOf(game, cl) == null
                && cl.getCommittee().getTargetPosition() == null), "a club outside the League has no League target");

        List<Message> letters = letters(CommitteeManager.MSG_EXPECTATIONS);
        assertEquals(1, letters.size(), "only the player's club gets the expectations");
        assertEquals(MessageCategory.JOB, letters.get(0).getCategory());
        assertTrue(letters.get(0).getPlainTextMessage().contains(preston.getName()));
        passDays(10);
        assertEquals(1, letters(CommitteeManager.MSG_EXPECTATIONS).size(), "once a season");
        assertTrue(letters.get(0).getPlainTextMessage().contains("1888-89"));
    }

    @Test
    void confidenceMovesWithResultsAndTheBalance() {
        untilTheCommitteesMeet();
        Club preston = game.getCurrentClub();
        ClubCommittee c = preston.getCommittee();
        LocalDateTime from = game.getGameDate();
        Club other = club("bolton-wanderers");
        preston.getPlayedMatches().add(match(preston, other, 5, 0, from.plusDays(2)));
        preston.getPlayedMatches().add(match(other, preston, 0, 2, from.plusDays(4)));
        int good = CommitteeManager.weeklyDelta(preston, CommitteeManager.leagueOf(game, preston), c, from, from.plusDays(7), new java.util.HashMap<>());
        assertTrue(good >= 6, "two wins: +" + good);

        preston.getPlayedMatches().clear();
        preston.getPlayedMatches().add(match(preston, other, 0, 5, from.plusDays(2)));
        int bad = CommitteeManager.weeklyDelta(preston, CommitteeManager.leagueOf(game, preston), c, from, from.plusDays(7), new java.util.HashMap<>());
        assertTrue(bad <= -3, "a heavy home defeat: " + bad);

        preston.getPlayedMatches().clear();
        BigDecimal kept = preston.getClubBalance();
        preston.setClubBalance(BigDecimal.valueOf(-50));
        assertTrue(CommitteeManager.weeklyDelta(preston, CommitteeManager.leagueOf(game, preston), c, from, from.plusDays(7), new java.util.HashMap<>()) < 0,
                "a balance in debt costs confidence");
        preston.setClubBalance(kept);

        // through the daily path: a week with the heavy defeat
        preston.getPlayedMatches().add(match(preston, other, 0, 5, from.plusDays(2)));
        passDays(7);
        assertTrue(c.getConfidence() < CommitteeManager.START_CONFIDENCE, "confidence " + c.getConfidence());
    }

    @Test
    void warningsThenDismissalMakeThePlayerAJobSeeker() {
        untilTheCommitteesMeet();
        Club preston = game.getCurrentClub();
        ClubCommittee c = preston.getCommittee();
        c.setConfidence(12);

        passDays(7);
        assertEquals(1, c.getWarnings());
        assertEquals(1, letters(CommitteeManager.MSG_WARNING).size());
        assertEquals(preston.getId(), owner.getCurrentClubId(), "no dismissal on a first warning");

        c.setConfidence(12); // the results stay bad (no matches in this test: the weekly drift would mend it)
        passDays(CommitteeManager.FINAL_WARNING_AFTER_DAYS);
        assertEquals(2, c.getWarnings());
        assertEquals(1, letters(CommitteeManager.MSG_FINAL_WARNING).size());
        assertEquals(preston.getId(), owner.getCurrentClubId(), "no dismissal on the final warning");

        c.setConfidence(12);
        passDays(CommitteeManager.DISMISS_AFTER_DAYS);
        assertNull(owner.getCurrentClubId(), "dismissed");
        assertFalse(MenuEntries.hasClub(game), "the job-seeker menu");
        assertNull(preston.getStaffId(manager.getId()), "the post is vacant");
        assertTrue(c.getDismissedIds().contains(owner.getId()));
        List<Message> dismissal = letters(CommitteeManager.MSG_DISMISSAL);
        assertEquals(1, dismissal.size());
        assertEquals(MessageCategory.JOB, dismissal.get(0).getCategory(), "a job seeker's inbox shows it");

        // the J-01 job market advertises the post; the committee does not take him back
        JobOpening opening = jobs.getAvailableJobs(preston).stream()
                .filter(j -> j.getProfessionId().equals(manager.getId())).findFirst().orElse(null);
        assertNotNull(opening, "Preston's secretary post is on the Job Board");
        JobApplication application = jobs.applyForJob(owner, opening);
        assertNotNull(application);
        assertEquals(ApplicationStatus.REJECTED, application.getStatus());

        // and he may still find a post elsewhere
        assertFalse(jobs.getAvailableJobs().isEmpty());
    }

    @Test
    void anAiCommitteeDismissesItsSecretaryAndTheClubAppointsAnother() {
        passDays(43); // every advert of the first day filled (J-01: deadlines 21-42 days)
        Club bolton = club("bolton-wanderers");
        Long first = bolton.getStaffId(manager.getId());
        assertNotNull(first);
        ClubCommittee c = bolton.getCommittee();
        assertEquals(first, c.getSecretaryId());
        c.setConfidence(5);
        c.setWarnings(2);
        c.setLastWarning(game.getGameDate().minusDays(30));
        c.setLastReview(game.getGameDate().minusDays(7));

        passDays(1);
        assertNull(bolton.getStaffId(manager.getId()), "dismissed");
        Person gone = CommitteeManager.person(game, first);
        assertNull(gone.getCurrentClubId(), "out of work, a candidate for other clubs");
        assertTrue(c.getDismissedIds().contains(first));
        assertTrue(letters(CommitteeManager.MSG_DISMISSAL).isEmpty(), "no letter to the player about an AI club");
        assertTrue(jobs.getAvailableJobs(bolton).stream().anyMatch(j -> j.getProfessionId().equals(manager.getId())
                && j.getStatus() == JobStatus.OPEN), "the post is advertised");

        passDays(43);
        Long next = bolton.getStaffId(manager.getId());
        assertNotNull(next, "the club appointed a new secretary from the job market");
        assertNotEquals(first, next, "not the man it dismissed");
        assertEquals(next, c.getSecretaryId(), "the committee judges the new man");
        assertEquals(0, c.getWarnings(), "and starts afresh");
    }

    @Test
    void theSameSeedGivesTheSameCommittees() {
        untilTheCommitteesMeet();
        passDays(8);
        String first = fingerprint();
        newGame();
        untilTheCommitteesMeet();
        passDays(8);
        assertEquals(first, fingerprint());
    }

    private String fingerprint() {
        StringBuilder sb = new StringBuilder();
        for (Club club : game.getAllClubs()) {
            ClubCommittee c = club.getCommittee();
            sb.append(club.getDataId()).append(':').append(c.getTargetPosition()).append('/')
                    .append(c.getCupTiesExpected()).append('/').append(c.getStanding()).append('/')
                    .append(c.getConfidence());
            for (CommitteeMember m : c.getMembers()) {
                sb.append(',').append(m.getFullName()).append('|').append(m.getRole()).append('|')
                        .append(m.getOccupation()).append('|').append(m.getBirthYear()).append('|')
                        .append(m.getPatience());
            }
            sb.append(';');
        }
        return sb.toString();
    }

    @Test
    void theCommitteeIsSavedWithTheClub() {
        untilTheCommitteesMeet();
        Club preston = game.getCurrentClub();
        ClubCommittee c = preston.getCommittee();
        c.setWarnings(1);
        c.setLastWarning(game.getGameDate());
        c.getDismissedIds().add(12345L);

        LoadResult result = SaveCodec.decode(SaveCodec.encode(game, "Committee", LocalDateTime.of(2000, 1, 1, 0, 0)));
        assertEquals(List.of(), result.getProblems());
        ClubCommittee again = result.getGame().getClubById(preston.getId()).getCommittee();
        assertNotNull(again);
        assertEquals(c.getSeasonYear(), again.getSeasonYear());
        assertEquals(c.getTargetPosition(), again.getTargetPosition());
        assertEquals(c.getConfidence(), again.getConfidence());
        assertEquals(1, again.getWarnings());
        assertEquals(c.getLastWarning(), again.getLastWarning());
        assertEquals(c.getSecretaryId(), again.getSecretaryId());
        assertEquals(List.of(12345L), again.getDismissedIds());
        assertEquals(0, c.getStartBalance().compareTo(again.getStartBalance()));
        assertEquals(c.getMembers().size(), again.getMembers().size());
        for (int i = 0; i < c.getMembers().size(); i++) {
            assertEquals(c.getMembers().get(i).getFullName(), again.getMembers().get(i).getFullName());
            assertEquals(c.getMembers().get(i).getRole(), again.getMembers().get(i).getRole());
            assertEquals(c.getMembers().get(i).getGenerosity(), again.getMembers().get(i).getGenerosity());
        }
    }

    @Test
    void wagesAboveTheBandGoToAVote() {
        untilTheCommitteesMeet();
        Club preston = game.getCurrentClub();
        ClubCommittee c = preston.getCommittee();
        BigDecimal band = Committee.bandWeekly(preston);
        assertEquals(0, band.compareTo(BigDecimal.valueOf(c.getWageBandPence()).divide(BigDecimal.valueOf(240), 2,
                java.math.RoundingMode.HALF_UP)));

        Committee.WageDecision within = Committee.approveWage(preston, band);
        assertTrue(within.approved());
        assertFalse(within.voted(), "the secretary agrees a wage within the band alone");

        c.setConfidence(90);
        for (CommitteeMember m : c.getMembers()) {
            m.setGenerosity(0.5);
        }
        Committee.WageDecision little = Committee.approveWage(preston, band.add(new BigDecimal("0.25")));
        assertTrue(little.voted());
        assertTrue(little.approved(), "a confident committee allows a little more: " + little);
        assertEquals(little, Committee.approveWage(preston, band.add(new BigDecimal("0.25"))), "the same answer twice");

        c.setConfidence(20);
        Committee.WageDecision lot = Committee.approveWage(preston, band.multiply(BigDecimal.valueOf(2)));
        assertTrue(lot.voted());
        assertFalse(lot.approved(), "double the band from a doubtful committee: " + lot);
        assertEquals(c.getMembers().size(), lot.yes() + lot.no());
    }

    private static Match match(Club home, Club away, int homeGoals, int awayGoals, LocalDateTime when) {
        Match m = new Match();
        m.setHomeClubId(home.getId());
        m.setAwayClubId(away.getId());
        m.setHomeGoals(homeGoals);
        m.setAwayGoals(awayGoals);
        m.setMatchDateTime(when);
        m.setMatchType(Match.LEAGUE_MATCH);
        m.setIsPlayed(true);
        return m;
    }
}
