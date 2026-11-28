package com.rndmodgames.futtoboru.engine.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.jobs.ApplicationStatus;
import com.rndmodgames.futtoboru.data.jobs.JobConstants;
import com.rndmodgames.futtoboru.data.jobs.JobOffer;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;
import com.rndmodgames.futtoboru.data.jobs.JobStatus;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.save.LoadResult;
import com.rndmodgames.futtoboru.system.save.SaveCodec;

/**
 * J-01 (D31): AI clubs fill their posts too. Nothing filled them: every club's posts stayed vacant for ever.
 * Now a club appoints at its advert's deadline (waiting while an offer to the player is open) and AI staff leave
 * now and then, so the posts come up again.
 */
class AiStaffMarketTests extends JobGameTestBase {

    @Test
    void aiClubsAppointStaffAtTheDeadline() {
        becomeJobSeeker();
        Club bolton = DatabaseLoader.getClubByDataId("bolton-wanderers");
        JobOpening job = opening("bolton-wanderers", manager);
        long days = ChronoUnit.DAYS.between(game.getGameDate(), job.getApplicationDeadline());
        assertTrue(days >= JobConstants.JOB_OPENING_MIN_DEADLINE_DAYS && days <= JobConstants.JOB_OPENING_MAX_DEADLINE_DAYS,
                "deadline in " + days + " days");
        passDays((int) days - 1);
        assertNull(bolton.getStaffId(manager.getId()), "nobody appointed before the deadline");
        passDays(1);
        Long appointed = bolton.getStaffId(manager.getId());
        assertNotNull(appointed, "the club appoints a candidate at the deadline");
        assertEquals(JobStatus.FILLED, job.getStatus());
        assertEquals(appointed, job.getSelectedCandidateId());
        Person person = game.getAllPersons().stream().filter(p -> appointed.equals(p.getId())).findFirst().orElseThrow();
        assertEquals(bolton.getId(), person.getCurrentClubId());
        assertTrue(person.getPrimaryProfession().is(Profession.MANAGER));
        assertNotNull(person.getName());
        assertNotNull(person.getBirthDate());
        assertTrue(openOpenings(bolton, manager).isEmpty());
    }

    @Test
    void aClubWaitsForTheSeekersAnswerBeforeAppointingAnother() {
        becomeJobSeeker();
        Club bolton = DatabaseLoader.getClubByDataId("bolton-wanderers");
        JobOpening job = opening("bolton-wanderers", manager);
        game.setGameDate(job.getApplicationDeadline().minusDays(1));
        jobs.applyForJob(owner, job);
        JobOffer offer = jobs.getPlayerOffers(owner).get(0);
        passDays(3);
        assertNull(bolton.getStaffId(manager.getId()), "the offer to the seeker stands");
        assertTrue(jobs.acceptOffer(offer), "and can still be taken after the advert's deadline");
        assertEquals(owner.getId(), bolton.getStaffId(manager.getId()));
    }

    @Test
    void aiStaffLeaveAndThePostIsAdvertisedAgain() {
        Club bolton = DatabaseLoader.getClubByDataId("bolton-wanderers");
        passDays(JobConstants.JOB_OPENING_MAX_DEADLINE_DAYS + 1);
        Long first = bolton.getStaffId(manager.getId());
        assertNotNull(first, "every advert is filled after the longest deadline");
        Club preston = game.getCurrentClub();
        jobs.setStaffDeparturePerDay(1.0);
        passDays(1);
        assertEquals(owner.getId(), preston.getStaffId(manager.getId()), "the player never leaves on his own");
        assertNull(bolton.getStaffId(manager.getId()), "the AI manager left");
        Person left = game.getAllPersons().stream().filter(p -> first.equals(p.getId())).findFirst().orElseThrow();
        assertNull(left.getCurrentClubId());
        assertEquals(1, openOpenings(bolton, manager).size(), "the post is advertised again");
    }

    /** 100 days of churn (slow tier, T-01: a class over 2 s). */
    @Tag("slow")
    @Test
    void everyStaffMemberIsAPersonOfTheGameAndHoldsOnePost() {
        passDays(JobConstants.JOB_OPENING_MAX_DEADLINE_DAYS + 1);
        jobs.setStaffDeparturePerDay(0.05);
        passDays(60);
        Map<Long, Person> persons = new HashMap<>();
        for (Person p : game.getAllPersons()) {
            persons.put(p.getId(), p);
        }
        Map<Long, String> posts = new HashMap<>();
        for (Club club : new HashSet<>(game.getAllClubs())) {
            for (Map.Entry<Long, Long> post : club.getStaff().entrySet()) {
                Person p = persons.get(post.getValue());
                assertNotNull(p, "staff " + post.getValue() + " at " + club.getName() + " is in the game");
                assertEquals(club.getId(), p.getCurrentClubId(), p.getName() + " works where the club says");
                String where = club.getId() + ":" + post.getKey();
                String before = posts.put(p.getId(), where);
                assertTrue(before == null || before.equals(where), p.getName() + " holds two posts");
            }
            for (Profession post : staff.getStaffPositions()) {
                assertTrue(openOpenings(club, post).size() <= 1, "two adverts for one post at " + club.getName());
            }
        }
    }

    /** Two new games, 43 days each (slow tier). */
    @Tag("slow")
    @Test
    void theSameSeedAppointsTheSamePeople() {
        passDays(JobConstants.JOB_OPENING_MAX_DEADLINE_DAYS + 1);
        String first = staffFingerprint();
        newJobGame();
        passDays(JobConstants.JOB_OPENING_MAX_DEADLINE_DAYS + 1);
        assertEquals(first, staffFingerprint());
    }

    private String staffFingerprint() {
        StringBuilder sb = new StringBuilder();
        for (Club club : game.getAllClubs()) {
            for (Profession post : staff.getStaffPositions()) {
                Person p = staff.getClubStaff(club, post);
                sb.append(club.getDataId()).append('/').append(post.getDataId()).append('=')
                        .append(p != null ? p.getName() + " " + p.getLastname() : "-").append(';');
            }
        }
        return sb.toString();
    }

    /** The people appointed are saved with the game: a load finds every club's staff (SaveLinks reports none missing). */
    @Tag("slow")
    @Test
    void appointedStaffSurviveASaveAndLoad() {
        passDays(JobConstants.JOB_OPENING_MAX_DEADLINE_DAYS + 1);
        Club bolton = DatabaseLoader.getClubByDataId("bolton-wanderers");
        Person held = staff.getClubStaff(bolton, manager);
        assertNotNull(held);
        String text = SaveCodec.encode(game, "Staff", LocalDateTime.of(2000, 1, 1, 0, 0));
        LoadResult result = SaveCodec.decode(text);
        assertEquals(List.of(), result.getProblems());
        SaveGame loaded = result.getGame();
        Long heldId = loaded.getClubById(bolton.getId()).getStaffId(manager.getId());
        assertEquals(held.getId(), heldId);
        Person again = loaded.getAllPersons().stream().filter(p -> heldId.equals(p.getId())).findFirst().orElseThrow();
        assertEquals(held.getName() + " " + held.getLastname(), again.getName() + " " + again.getLastname());
        assertEquals(bolton.getId(), again.getCurrentClubId());
        assertTrue(again.getPrimaryProfession().is(Profession.MANAGER));
    }

    /** Days of real play (the engine's daily loop): the board lists only open adverts for vacant posts. */
    @Test
    void daysOfPlayKeepTheBoardTidy() {
        becomeJobSeeker();
        JobOpening job = opening("bolton-wanderers", manager);
        jobs.applyForJob(owner, job);
        jobs.rejectOffer(jobs.getPlayerOffers(owner).get(0));
        NewGameFixture.play(application, game, 10, true);
        LocalDateTime today = game.getGameDate();
        for (JobOpening open : jobs.getAvailableJobs()) {
            assertTrue(open.isAcceptingApplications(today));
            Club club = game.getClubById(open.getClubId());
            assertTrue(club.isPositionVacant(open.getProfessionId()), "a filled post on the board");
        }
        assertEquals(ApplicationStatus.DECLINED, applicationFor(job).getStatus());
    }
}
