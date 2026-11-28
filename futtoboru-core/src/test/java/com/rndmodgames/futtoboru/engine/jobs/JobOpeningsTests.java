package com.rndmodgames.futtoboru.engine.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;
import com.rndmodgames.futtoboru.data.jobs.JobStatus;
import com.rndmodgames.futtoboru.menu.MenuEntries;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/** J-01: the posts a club advertises, one opening per vacant post, and filled posts leaving the board. */
class JobOpeningsTests extends JobGameTestBase {

    private static Set<Long> ids(List<Profession> professions) {
        Set<Long> out = new HashSet<>();
        for (Profession p : professions) {
            out.add(p.getId());
        }
        return out;
    }

    /** It advertised agents, investors and owners at every club, and salaries such as 12.495. */
    @Test
    void clubsAdvertiseTheirManagerDirectorAndScoutInPoundsAndPence() {
        Set<Long> posts = Set.of(manager.getId(), director.getId(),
                DatabaseLoader.getProfessionByDataId(Profession.SCOUT).getId());
        assertEquals(posts, ids(staff.getStaffPositions()));
        Club bolton = DatabaseLoader.getClubByDataId("bolton-wanderers");
        assertEquals(posts, ids(staff.getVacantPositions(bolton)), "agents, investors and owners are not club staff");
        assertFalse(game.getActiveJobOpenings().isEmpty());
        for (JobOpening job : game.getActiveJobOpenings()) {
            assertTrue(posts.contains(job.getProfessionId()), "an opening for a non-staff profession");
            BigDecimal salary = job.getSalary();
            assertEquals(2, salary.scale(), "salary " + salary.toPlainString() + " should be in pounds and pence");
        }
    }

    /** A new game only set the manager's club: his own post was vacant and advertised. */
    @Test
    void theManagerHoldsHisOwnPost() {
        Club preston = game.getCurrentClub();
        assertEquals(owner.getId(), preston.getStaffId(manager.getId()), "the owner is Preston's manager");
        assertFalse(staff.getVacantPositions(preston).contains(manager));
        assertNull(opening("preston-north-end", manager), "no advert for the owner's own post");
        assertTrue(MenuEntries.hasClub(game), "the manager sees the club menu");
    }

    /** On an advert's deadline day a second advert for the same post was posted (two rows, two offers). */
    @Test
    void onePostHasOneOpening() {
        becomeJobSeeker();
        JobOpening job = opening("bolton-wanderers", manager);
        Club bolton = DatabaseLoader.getClubByDataId("bolton-wanderers");
        game.setGameDate(job.getApplicationDeadline().minusDays(1));
        jobs.applyForJob(owner, job); // an offer the seeker sits on keeps the post open past its deadline
        game.setGameDate(job.getApplicationDeadline());
        jobs.updateJobOpenings();
        assertEquals(1, openOpenings(bolton, manager).size(), "the deadline day posts no second advert");
        assertSame(job, jobs.createJobOpening(bolton, manager), "the open advert is the post's advert");
        assertEquals(1, openOpenings(bolton, manager).size());
        assertTrue(jobs.getAvailableJobs(bolton).stream().noneMatch(j -> j == job), "past its deadline");
    }

    /** A save from before J-01: an agent's "post", a post advertised twice, an application left at offer received. */
    @Test
    void anOlderSaveIsTidiedOnTheNextDay() {
        becomeJobSeeker();
        Club bolton = DatabaseLoader.getClubByDataId("bolton-wanderers");
        Club derby = DatabaseLoader.getClubByDataId("derby-county");
        JobOpening job = opening("bolton-wanderers", manager);
        jobs.applyForJob(owner, job);
        jobs.getPlayerOffers(owner).get(0).setStatus(com.rndmodgames.futtoboru.data.jobs.OfferStatus.REJECTED);
        JobOpening agent = new JobOpening(bolton, DatabaseLoader.getProfessionByDataId(Profession.AGENT), game.getGameDate());
        agent.setId(game.getIdAllocator().nextId());
        JobOpening twice = new JobOpening(derby, manager, game.getGameDate());
        twice.setId(game.getIdAllocator().nextId());
        game.getActiveJobOpenings().add(agent);
        game.getActiveJobOpenings().add(twice);

        passDays(1);
        assertEquals(com.rndmodgames.futtoboru.data.jobs.ApplicationStatus.DECLINED, applicationFor(job).getStatus());
        assertTrue(owner.getActiveApplicationIds().isEmpty());
        assertEquals(JobStatus.EXPIRED, agent.getStatus());
        assertTrue(jobs.getAvailableJobs().stream().noneMatch(j -> j == agent));
        assertEquals(1, openOpenings(derby, manager).size(), "one advert per post");
        assertEquals(JobStatus.EXPIRED, twice.getStatus(), "the later copy goes");
    }

    @Test
    void aPostFilledElsewhereLeavesTheBoardAtOnce() {
        becomeJobSeeker();
        JobOpening job = opening("bolton-wanderers", manager);
        Club bolton = DatabaseLoader.getClubByDataId("bolton-wanderers");
        Person other = new Person();
        game.assignId(other);
        game.getAllPersons().add(other);
        staff.hireStaff(bolton, manager, other);
        assertTrue(jobs.getAvailableJobs(bolton).stream().noneMatch(j -> j == job), "a filled post is not on the board");
        passDays(1);
        assertEquals(JobStatus.FILLED, job.getStatus());
        assertTrue(openOpenings(bolton, manager).isEmpty());
    }
}
