package com.rndmodgames.futtoboru.engine.season;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.ReElection;
import com.rndmodgames.futtoboru.engine.EngineTestWorld;
import com.rndmodgames.futtoboru.system.save.SaveCodec;

/**
 * G-08: the re-election vote on a hand-built 12-club League with 4 clubs outside it (headless, seeded).
 */
public class ReElectionTests extends BaseTest {

    private static final LocalDateTime LAST_DAY = LocalDateTime.of(1889, 4, 21, 12, 0);

    private EngineTestWorld world;
    private CompetitionEdition season;

    /** 12 League clubs (A..L, best first at the end of the season) and 4 outsiders (M..P). */
    private void build(long seed, Integer ownClub) {
        // an application of its own: the seed of the shared one belongs to the season memo (SaveGameTests)
        world = new EngineTestWorld(new com.rndmodgames.futtoboru.game.Futtoboru(seed), 16);
        world.league.getLeagueClubs().removeAll(world.clubs.subList(12, 16));
        world.authorityManager.getReElectionManager().setRules(l -> new ReElectionRule(4, 1888, 1891, 7));
        season = new CompetitionEdition();
        season.setId(world.game.getIdAllocator().nextId());
        season.setName("Test League 1888-89");
        season.setStartDate(LocalDateTime.of(1888, 9, 8, 0, 0));
        season.setEndDate(LocalDateTime.of(1889, 4, 20, 0, 0));
        for (int i = 0; i < 12; i++) {
            season.getParticipantClubsIds().add(world.club(i).getId());
        }
        world.league.getEditions().add(season);
        for (int i = 0; i < 12; i++) {
            Club c = world.club(i);
            int points = 34 - 3 * i;
            c.setMatchesPlayed(22);
            c.setMatchesWon(points / 2);
            c.setMatchesDrawn(points % 2);
            c.setMatchesLost(22 - points / 2 - points % 2);
            c.setGoalsScored(40 - i);
            c.setGoalsConceded(20 + i);
            c.setPoints(points);
        }
        for (int i = 12; i < 16; i++) { // keen outsiders: big grounds, money in the bank
            world.club(i).getStadium().setCapacity(20000);
            world.club(i).setClubBalance(new BigDecimal("1500"));
        }
        if (ownClub != null) {
            Person owner = new Person();
            owner.setId(world.game.getIdAllocator().nextId());
            owner.setCurrentClubId(world.club(ownClub).getId());
            world.game.setOwner(owner);
        }
        world.game.setGameDate(LAST_DAY);
    }

    private ReElection runToMeeting() {
        world.authorityManager.checkCompetitionsSchedule();
        ReElection vote = season.getReElection();
        assertNotNull(vote, "opened at the end of the season");
        world.game.setGameDate(vote.getMeeting().withHour(12));
        world.authorityManager.checkCompetitionsSchedule();
        return vote;
    }

    @Test
    void theBottomFourGoToTheVoteAndTheNextSeasonWaitsForTheMeeting() {
        build(1888, null);
        world.authorityManager.checkCompetitionsSchedule();
        ReElection vote = season.getReElection();
        assertNotNull(vote);
        List<Long> bottom = new ArrayList<>();
        for (int i = 8; i < 12; i++) {
            bottom.add(world.club(i).getId());
        }
        assertEquals(bottom, vote.getRetiringIds());
        assertEquals(4, vote.getPlaces());
        assertFalse(vote.isHeld());
        assertEquals(LocalDateTime.of(1889, 4, 28, 0, 0), vote.getMeeting());
        for (Long id : vote.getApplicantIds()) {
            assertTrue(world.club(12).getId() <= id && id <= world.club(15).getId(), "applicants come from outside");
        }
        assertEquals(1, world.league.getEditions().size(), "no next season before the meeting");
        assertTrue(world.authorityManager.getReElectionManager().isPending(world.league));

        world.game.setGameDate(LocalDateTime.of(1889, 4, 25, 12, 0));
        world.authorityManager.checkCompetitionsSchedule();
        assertFalse(vote.isHeld(), "not before the meeting");
        assertEquals(1, world.league.getEditions().size());
    }

    @Test
    void exactlyThePlacesAreFilledAndTheNextLeagueHasTheRightClubs() {
        for (long seed = 1; seed <= 12; seed++) {
            build(seed, null);
            ReElection vote = runToMeeting();
            assertTrue(vote.isHeld(), "seed " + seed);
            assertEquals(4, vote.getElectedIds().size(), "seed " + seed);
            assertEquals(4, new HashSet<>(vote.getElectedIds()).size(), "seed " + seed);
            int total = 0;
            for (int v : vote.getVotes()) {
                total += v;
            }
            assertEquals(8 * 4, total, "8 voters, 4 votes each (seed " + seed + ")");

            Set<Long> expected = new HashSet<>();
            for (int i = 0; i < 8; i++) {
                expected.add(world.club(i).getId());
            }
            expected.addAll(vote.getElectedIds());
            Set<Long> members = new HashSet<>();
            for (Club c : world.league.getLeagueClubs()) {
                members.add(c.getId());
            }
            assertEquals(12, world.league.getLeagueClubs().size(), "seed " + seed);
            assertEquals(expected, members, "seed " + seed);
            for (Long id : vote.getRetiringIds()) {
                if (!vote.getElectedIds().contains(id)) {
                    assertNull(world.game.getClubById(id).getCurrentLeague(), "a loser leaves the League");
                }
            }
            assertEquals(2, world.league.getEditions().size(), "next season generated after the meeting");
            CompetitionEdition next = world.league.getEditions().get(1);
            assertTrue(next.getName().contains("1889-90"), next.getName());
            assertEquals(expected, new HashSet<>(next.getParticipantClubsIds()), "seed " + seed);
            assertFalse(world.authorityManager.getReElectionManager().isPending(world.league));

            // another day: nothing is voted again
            world.authorityManager.checkCompetitionsSchedule();
            assertEquals(2, world.league.getEditions().size());
            assertEquals(1, count(ReElectionManager.MSG_MEETING), "seed " + seed);
        }
    }

    @Test
    void theVoteIsNotScriptedApplicantsWinSometimes() {
        int applicantsElected = 0;
        int applied = 0;
        for (long seed = 1; seed <= 30; seed++) {
            build(seed, null);
            ReElection vote = runToMeeting();
            applied += vote.getApplicantIds().size();
            for (Long id : vote.getElectedIds()) {
                if (vote.getApplicantIds().contains(id)) {
                    applicantsElected++;
                }
            }
        }
        assertTrue(applied > 0, "some clubs apply");
        assertTrue(applicantsElected > 0, "an applicant wins a place on some seed");
        assertTrue(applicantsElected < 30 * 4, "members keep places too");
    }

    @Test
    void sameSeedSameVote() {
        build(77, null);
        ReElection a = runToMeeting();
        List<Long> members = new ArrayList<>();
        for (Club c : world.league.getLeagueClubs()) {
            members.add(c.getId());
        }
        build(77, null);
        ReElection b = runToMeeting();
        List<Long> members2 = new ArrayList<>();
        for (Club c : world.league.getLeagueClubs()) {
            members2.add(c.getId());
        }
        assertEquals(a.getApplicantIds(), b.getApplicantIds());
        assertEquals(a.getVotes(), b.getVotes());
        assertEquals(a.getElectedIds(), b.getElectedIds());
        assertEquals(members, members2);
    }

    @Test
    void thePlayersClubGetsTheLettersAndTheResult() {
        build(5, 11); // the player's club finished bottom
        world.authorityManager.checkCompetitionsSchedule();
        assertEquals(1, count(ReElectionManager.MSG_CALL), "the League's notice to all");
        assertEquals(1, count(ReElectionManager.MSG_NOTICE), "our application letter");
        assertEquals(0, count(ReElectionManager.MSG_CANVASS), "canvass report comes later");
        assertEquals(1, count(SeasonSummaryLetter.MSG_TYPE), "season review letter");
        Message review = find(SeasonSummaryLetter.MSG_TYPE);
        assertTrue(review.getPlainTextMessage().contains("12"), review.getPlainTextMessage());

        world.game.setGameDate(LocalDateTime.of(1889, 4, 25, 12, 0));
        world.engine.getMessageManager().deliverScheduledMessages(world.game.getGameDate());
        assertEquals(1, count(ReElectionManager.MSG_CANVASS), "canvass report before the meeting");

        world.game.setGameDate(season.getReElection().getMeeting().withHour(12));
        world.authorityManager.checkCompetitionsSchedule();
        assertEquals(1, count(ReElectionManager.MSG_RESULT), "our result");
        assertEquals(1, count(ReElectionManager.MSG_MEETING));
    }

    @Test
    void noLettersForAClubNotInTheVote() {
        build(5, 0); // the champions
        runToMeeting();
        assertEquals(0, count(ReElectionManager.MSG_NOTICE));
        assertEquals(0, count(ReElectionManager.MSG_RESULT));
        assertEquals(1, count(SeasonSummaryLetter.MSG_TYPE));
    }

    @Test
    void noRuleNoVote() {
        build(5, null);
        world.authorityManager.getReElectionManager().setRules(l -> new ReElectionRule(4, 1890, 1891, 7));
        world.authorityManager.checkCompetitionsSchedule();
        assertNull(season.getReElection());
        assertEquals(2, world.league.getEditions().size(), "next season at once, as before G-08");
    }

    @Test
    void aPendingVoteSurvivesASaveRoundTrip() {
        build(9, null);
        world.authorityManager.checkCompetitionsSchedule();
        ReElection vote = season.getReElection();
        String json = SaveCodec.encode(world.game, "g08", LAST_DAY);
        assertTrue(json.contains("retiringIds"), "saved with the season");
        assertTrue(json.contains(vote.getMeeting().toLocalDate().toString()), "meeting date saved");
        com.rndmodgames.futtoboru.system.save.LoadResult loaded = SaveCodec.decode(json);
        assertTrue(loaded.isLoaded(), loaded.toString());
        ReElection back = null;
        for (com.rndmodgames.futtoboru.data.League l : loaded.getGame().getMainAuthority().getLeagues()) {
            for (CompetitionEdition e : l.getEditions()) {
                if (e.getReElection() != null) {
                    back = e.getReElection();
                }
            }
        }
        assertNotNull(back, "re-election read back");
        assertEquals(vote.getRetiringIds(), back.getRetiringIds());
        assertEquals(vote.getApplicantIds(), back.getApplicantIds());
        assertEquals(vote.getMeeting(), back.getMeeting());
        assertFalse(back.isHeld());
    }

    private Message find(String type) {
        for (Message m : world.game.getAllMessages()) {
            if (type.equals(m.getMessageType())) {
                return m;
            }
        }
        return null;
    }

    private int count(String type) {
        int n = 0;
        for (Message m : world.game.getAllMessages()) {
            if (type.equals(m.getMessageType())) {
                n++;
            }
        }
        return n;
    }
}
