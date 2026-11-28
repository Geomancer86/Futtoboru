package com.rndmodgames.futtoboru.engine.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.jobs.ApplicationStatus;
import com.rndmodgames.futtoboru.data.jobs.JobApplication;
import com.rndmodgames.futtoboru.data.jobs.JobConstants;
import com.rndmodgames.futtoboru.data.jobs.JobOffer;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;
import com.rndmodgames.futtoboru.data.jobs.JobStatus;
import com.rndmodgames.futtoboru.data.jobs.OfferStatus;
import com.rndmodgames.futtoboru.menu.MenuEntries;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/** J-01: the job seeker's offers headless: once per post, lapsing, declined, accepted (the menu switches), negotiated. */
class JobOffersTests extends JobGameTestBase {

    /** V-01: applying and then the club offering again gave two offer rows and two letters. */
    @Test
    void anOfferAppearsOnceAndItsLetterIsAJobMessage() {
        becomeJobSeeker();
        JobOpening job = opening("bolton-wanderers", manager);
        assertNotNull(jobs.applyForJob(owner, job));
        JobOffer first = jobs.getPlayerOffers(owner).get(0);
        JobOffer again = jobs.makeOffer(job, owner);
        assertSame(first, again, "a second offer for the same post is the same offer");
        assertEquals(1, jobs.getPlayerOffers(owner).size());
        assertEquals(1, owner.getPendingOfferIds().size());
        assertSame(jobs.applyForJob(owner, job), applicationFor(job), "applying again is the same application");
        long letters = game.getAllMessages().stream().filter(m -> "JOB_OFFER".equals(m.getMessageType())).count();
        assertEquals(1, letters, "one offer letter");
        Message letter = game.getAllMessages().stream().filter(m -> "JOB_OFFER".equals(m.getMessageType()))
                .findFirst().orElseThrow();
        assertEquals(MessageCategory.JOB, letter.getCategory(), "the inbox shows job letters to a job seeker");
        assertTrue(letter.getPlainTextMessage().contains("£"), letter.getPlainTextMessage());
    }

    /** Offers never lapsed: they stayed on the offers screen and could be taken months later. */
    @Test
    void anOfferLapsesAfterAWeek() {
        becomeJobSeeker();
        JobOpening job = opening("bolton-wanderers", manager);
        jobs.applyForJob(owner, job);
        JobOffer offer = jobs.getPlayerOffers(owner).get(0);
        passDays(JobConstants.OFFER_EXPIRATION_DAYS);
        assertEquals(1, jobs.getPlayerOffers(owner).size(), "still open on its last day");
        passDays(1);
        assertTrue(jobs.getPlayerOffers(owner).isEmpty(), "a lapsed offer leaves the offers list");
        assertEquals(OfferStatus.EXPIRED, offer.getStatus());
        assertTrue(owner.getPendingOfferIds().isEmpty());
        assertEquals(ApplicationStatus.EXPIRED, applicationFor(job).getStatus());
        assertFalse(jobs.acceptOffer(offer), "a lapsed offer cannot be taken");
        assertNull(owner.getCurrentClubId());
    }

    /** A declined offer left the application "Offer Received" for good, with a View Offer button to nothing. */
    @Test
    void decliningAnOfferClosesTheApplication() {
        becomeJobSeeker();
        JobOpening job = opening("bolton-wanderers", manager);
        jobs.applyForJob(owner, job);
        jobs.rejectOffer(jobs.getPlayerOffers(owner).get(0));
        assertTrue(jobs.getPlayerOffers(owner).isEmpty());
        JobApplication app = applicationFor(job);
        assertEquals(ApplicationStatus.DECLINED, app.getStatus());
        assertFalse(owner.getActiveApplicationIds().contains(app.getId()));
        assertNull(jobs.applyForJob(owner, job), "no second application for a post the seeker turned down");
        assertEquals(1, jobs.getPlayerApplications(owner).size(), "one row in My Applications");
        assertSame(app, jobs.getApplicationFor(owner, job));
    }

    @Test
    void acceptingAManagersPostMakesTheSeekerTheClubsManager() {
        becomeJobSeeker();
        assertFalse(MenuEntries.hasClub(game), "the job seeker sees the job-seeker menu");
        JobOpening boltonJob = opening("bolton-wanderers", manager);
        JobOpening derbyJob = opening("derby-county", manager);
        jobs.applyForJob(owner, boltonJob);
        jobs.applyForJob(owner, derbyJob);
        assertEquals(2, jobs.getPlayerOffers(owner).size());
        JobOffer bolton = jobs.getPlayerOffers(owner).stream()
                .filter(o -> o.getJobOpeningId().equals(boltonJob.getId())).findFirst().orElseThrow();

        assertTrue(jobs.acceptOffer(bolton));
        Club club = DatabaseLoader.getClubByDataId("bolton-wanderers");
        assertSame(club, game.getCurrentClub());
        assertEquals(owner.getId(), club.getStaffId(manager.getId()));
        assertTrue(owner.getPrimaryProfession().is(Profession.MANAGER));
        assertTrue(MenuEntries.hasClub(game), "the menu switches to the club menu");
        assertEquals(JobStatus.FILLED, boltonJob.getStatus());
        assertEquals(ApplicationStatus.ACCEPTED, applicationFor(boltonJob).getStatus());
        assertEquals(ApplicationStatus.DECLINED, applicationFor(derbyJob).getStatus());
        assertTrue(jobs.getPlayerOffers(owner).isEmpty(), "the other offer is withdrawn");
        assertTrue(owner.getPendingOfferIds().isEmpty());
        assertTrue(owner.getActiveApplicationIds().isEmpty());
        assertTrue(jobs.getAvailableJobs(club).stream().noneMatch(j -> j.getProfessionId().equals(manager.getId())),
                "the filled post leaves the board");
        passDays(1);
        assertEquals(owner.getId(), club.getStaffId(manager.getId()), "still the manager the next day");
        assertTrue(openOpenings(club, manager).isEmpty(), "the post is not advertised again");
    }

    /** The menu went by the profession: a director hired from the board kept the job-seeker menu. */
    @Test
    void acceptingADirectorsPostAlsoGivesTheClubMenu() {
        becomeJobSeeker();
        jobs.applyForJob(owner, opening("bolton-wanderers", director));
        assertTrue(jobs.acceptOffer(jobs.getPlayerOffers(owner).get(0)));
        assertTrue(owner.getPrimaryProfession().is(Profession.DIRECTOR));
        assertTrue(MenuEntries.hasClub(game), "a director with a club sees the club menu");
    }

    /** A move vacated the new profession's post at the old club, not the post he held. */
    @Test
    void movingClubsVacatesTheOldPost() {
        becomeJobSeeker();
        jobs.applyForJob(owner, opening("bolton-wanderers", director));
        jobs.acceptOffer(jobs.getPlayerOffers(owner).get(0));
        Club bolton = game.getCurrentClub();
        // the director is offered Derby's manager's post (a club approaches him)
        JobOffer derby = jobs.makeOffer(opening("derby-county", manager), owner);
        assertTrue(jobs.acceptOffer(derby));
        assertNull(bolton.getStaffId(director.getId()), "the old post is vacant again");
        assertEquals(DatabaseLoader.getClubByDataId("derby-county").getId(), owner.getCurrentClubId());
        assertNull(bolton.getStaffId(manager.getId()), "nobody was taken off a post the director did not hold");
    }

    /** The round never advanced ("Round 0/3" for ever, no limit). */
    @Test
    void negotiationRoundsAdvanceAndStopAtTheLimit() {
        becomeJobSeeker();
        jobs.applyForJob(owner, opening("bolton-wanderers", manager));
        JobOffer offer = jobs.getPlayerOffers(owner).get(0);
        for (int round = 1; round <= JobConstants.MAX_NEGOTIATION_ROUNDS; round++) {
            assertTrue(offer.canNegotiate(game.getGameDate()), "round " + round);
            assertNotNull(jobs.submitCounterOffer(offer, offer.getSalary(), offer.getContractLengthMonths()));
            assertEquals(round, offer.getNegotiationRound());
        }
        assertFalse(offer.canNegotiate(game.getGameDate()), "no fourth round");
        assertNull(jobs.submitCounterOffer(offer, offer.getSalary(), offer.getContractLengthMonths()));
        assertEquals(1, jobs.getPlayerOffers(owner).size(), "still one offer after the rounds");
    }

    /** The answer was not told (the counter-offer stayed "negotiating") and refused ones piled up as offers. */
    @Test
    void aCounterOfferWithinTheClubsMarginIsAccepted() {
        becomeJobSeeker();
        jobs.applyForJob(owner, opening("bolton-wanderers", manager));
        JobOffer offer = jobs.getPlayerOffers(owner).get(0);
        BigDecimal original = offer.getSalary();
        BigDecimal asked = original.multiply(new BigDecimal("1.05"));
        JobOffer counter = jobs.submitCounterOffer(offer, asked, 12);
        assertEquals(OfferStatus.ACCEPTED, counter.getStatus());
        assertEquals(asked.setScale(2, RoundingMode.HALF_UP), offer.getSalary());
        assertEquals(2, offer.getSalary().scale());
        assertEquals(OfferStatus.PENDING, offer.getStatus());

        JobOffer greedy = jobs.submitCounterOffer(offer, offer.getSalary().multiply(new BigDecimal("2")), 12);
        assertEquals(OfferStatus.REJECTED, greedy.getStatus());
        assertEquals(asked.setScale(2, RoundingMode.HALF_UP), offer.getSalary(), "the terms stand");
        assertEquals(1, jobs.getPlayerOffers(owner).size(), "the counter-offers are not offers of their own");
        assertFalse(game.getPendingOffers().contains(counter));
        assertNull(jobs.submitCounterOffer(offer, BigDecimal.ZERO, 12), "no salary of nothing");
        assertNull(jobs.submitCounterOffer(offer, original, 0), "no contract of no months");
    }
}
