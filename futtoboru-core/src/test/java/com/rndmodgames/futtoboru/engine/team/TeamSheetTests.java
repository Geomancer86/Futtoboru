package com.rndmodgames.futtoboru.engine.team;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubStyle;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.data.TeamShape;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.body.Shape;

/**
 * G-01 team sheet logic on made-up squads (no game data): places, auto-pick, strength from the eleven, scorers by
 * position, playing short, and the shape contract with the ECS engine.
 */
class TeamSheetTests {

    /** A 2-3-5 squad of 16: one man per place plus a reserve keeper, back, half and two forwards; all rated 12. */
    static Club squad(long clubId) {
        Club club = new Club("Test " + clubId);
        club.setId(clubId);
        Position[] positions = { Position.GOALKEEPER, Position.FULL_BACK, Position.FULL_BACK, Position.HALF_BACK,
                Position.HALF_BACK, Position.HALF_BACK, Position.RIGHT_WING, Position.INSIDE_RIGHT,
                Position.CENTRE_FORWARD, Position.INSIDE_LEFT, Position.LEFT_WING, Position.GOALKEEPER,
                Position.FULL_BACK, Position.HALF_BACK, Position.CENTRE_FORWARD, Position.LEFT_WING };
        for (int i = 0; i < positions.length; i++) {
            club.getPlayers().add(player(clubId * 100 + i, positions[i], i < 11 ? 12f : 9f));
        }
        return club;
    }

    static Player player(long id, Position position, float rating) {
        Person person = new Person();
        person.setId(id);
        person.setName("P" + id);
        person.setLastname("Test");
        Player p = new Player();
        p.setId(id);
        p.setPerson(person);
        p.setPosition(position);
        Float r = rating;
        p.setShotStopping(r); p.setAreaControl(r); p.setHandToHand(r); p.setRushingOut(r); p.setPunching(r);
        p.setAreaPositioning(r); p.setTackling(r); p.setMarking(r); p.setHeading(r); p.setStrength(r);
        p.setPositioning(r); p.setKicking(r); p.setPassing(r); p.setTeamwork(r); p.setEndurance(r); p.setStamina(r);
        p.setDribbling(r); p.setSpeed(r); p.setAcceleration(r); p.setTrickShots(r); p.setOneTwos(r);
        p.setLongShots(r); p.setDexterity(r); p.setLeadership(r); p.setDetermination(r);
        return p;
    }

    @Test
    void theShapesHoldTheEcsEnginesSlotsInTheSameOrder() {
        for (TeamShape shape : TeamShape.values()) {
            Shape ecs = null;
            for (Shape s : Shape.values()) {
                if (s.label().equals(shape.label())) {
                    ecs = s;
                }
            }
            assertNotNull(ecs, "the ECS engine has the shape " + shape.label());
            for (int i = 0; i < TeamShape.SLOTS; i++) {
                assertEquals(ecs.slot(i).position(), shape.slot(i), shape.label() + " slot " + i);
            }
            assertEquals(shape, TeamShape.fromLabel(shape.label()));
        }
        assertEquals(1, TeamShape.TWO_THREE_FIVE.count(Position.Line.GOAL));
        assertEquals(5, TeamShape.TWO_THREE_FIVE.count(Position.Line.FORWARD));
        assertEquals(7, TeamShape.ONE_TWO_SEVEN.count(Position.Line.FORWARD));
    }

    @Test
    void movingAManSwapsHimWithTheOneInHisNewPlace() {
        TeamSheet sheet = new TeamSheet(1L, TeamShape.TWO_THREE_FIVE, ClubStyle.COMBINATION);
        sheet.setPlayerId(0, 10L);
        sheet.setPlayerId(8, 18L);
        sheet.setCaptainId(18L);
        sheet.setCaptainId(99L); // not on the sheet: refused
        assertEquals(18L, sheet.getCaptainId());

        sheet.setPlayerId(8, 10L); // the keeper moves to centre forward, the centre forward goes in goal
        assertEquals(10L, sheet.getPlayerId(8));
        assertEquals(18L, sheet.getPlayerId(0));
        assertEquals(2, sheet.selectedCount());

        sheet.setPlayerId(0, 77L); // a new man in goal: the captain leaves the side
        assertNull(sheet.getCaptainId());
        assertEquals(-1, sheet.slotOf(18L));

        TeamSheet copy = sheet.copy();
        sheet.setPlayerId(8, null);
        assertEquals(10L, copy.getPlayerId(8), "a copy does not follow later changes");
        assertEquals(1, sheet.selectedCount());
    }

    @Test
    void anotherShapeKeepsEveryManAndHisLineWhereItCan() {
        Club club = squad(1);
        TeamSheet sheet = TeamSelector.pick(1L, TeamShape.TWO_THREE_FIVE, ClubStyle.COMBINATION,
                TeamSheets.available(club), null);
        List<Long> before = sheet.selectedIds();
        sheet.reshape(TeamShape.ONE_TWO_SEVEN);
        assertEquals(TeamShape.ONE_TWO_SEVEN, sheet.getShape());
        assertEquals(new HashSet<>(before), new HashSet<>(sheet.selectedIds()), "nobody dropped");
        Map<Long, Player> squad = TeamSheets.squadById(club);
        assertEquals(Position.GOALKEEPER, squad.get(sheet.getPlayerId(0)).getPosition());
        // 1-2-7 has one back and two halves: one back and one half now play up front
        assertEquals(2, TeamSheets.check(sheet, club).outOfPosition().size());
    }

    @Test
    void autoPickFillsElevenValidPlacesWithTheRightMen() {
        Club club = squad(2);
        TeamSheet sheet = TeamSheets.standing(club);
        assertEquals(TeamShape.SLOTS, sheet.selectedCount());
        Set<Long> ids = new HashSet<>(sheet.selectedIds());
        assertEquals(TeamShape.SLOTS, ids.size(), "eleven different men");
        Map<Long, Player> squad = TeamSheets.squadById(club);
        for (int s = 0; s < TeamShape.SLOTS; s++) {
            Player p = squad.get(sheet.getPlayerId(s));
            assertNotNull(p, "place " + s + " holds a squad man");
            assertEquals(sheet.position(s), p.getPosition(), "place " + s + ": his own position");
            assertTrue(p.getId() % 100 < 11, "the first eleven (rated 12) beat the reserves (9)");
        }
        assertEquals(TeamShape.TWO_THREE_FIVE, sheet.getShape(), "a 2-3-5 squad plays 2-3-5");
        assertTrue(sheet.isSelected(sheet.getCaptainId()), "the captain is one of the eleven");
        TeamSheets.Check check = TeamSheets.check(sheet, club);
        assertEquals(List.of(), check.outOfPosition());
        assertTrue(!check.playsShort() && !check.hasEmptyPlaces() && !check.noCaptain());
    }

    @Test
    void theStrengthComesFromTheElevenOnly() {
        Club club = squad(3);
        TeamSheet sheet = TeamSheets.standing(club);
        Map<Long, Player> squad = TeamSheets.squadById(club);
        TeamStrength base = TeamStrength.of(sheet, squad);
        assertEquals(TeamShape.SLOTS, base.men());
        assertEquals(0, base.outOfPosition());

        // a reserve's attributes change nothing
        Player reserve = squad.get(3L * 100 + 13);
        assertTrue(!sheet.isSelected(reserve.getId()));
        reserve.setTackling(23f);
        reserve.setPassing(23f);
        assertEquals(base, TeamStrength.of(sheet, squad));

        // a better centre forward raises the attack, not the defence
        Player cf = squad.get(sheet.getPlayerId(8));
        cf.setKicking(20f);
        cf.setDribbling(20f);
        TeamStrength better = TeamStrength.of(sheet, squad);
        assertTrue(better.attack() > base.attack());
        assertEquals(base.defence(), better.defence(), 1e-9);

        // the centre forward in goal: out of position, a weaker defence
        TeamSheet swapped = sheet.copy();
        swapped.setPlayerId(0, cf.getId());
        TeamStrength wrong = TeamStrength.of(swapped, squad);
        assertEquals(2, wrong.outOfPosition());
        assertTrue(wrong.defence() < better.defence(), wrong + " vs " + better);
    }

    @Test
    void theShapeAndTheStyleAreSmallModifiers() {
        Club club = squad(4);
        Map<Long, Player> squad = TeamSheets.squadById(club);
        TeamSheet twoThreeFive = TeamSheets.standing(club);
        TeamSheet oneTwoSeven = twoThreeFive.copy();
        oneTwoSeven.setShape(TeamShape.ONE_TWO_SEVEN); // same men, 1-2-7 places
        TeamStrength a = TeamStrength.of(twoThreeFive, squad);
        TeamStrength b = TeamStrength.of(oneTwoSeven, squad);
        assertTrue(b.attack() > a.attack() * 0.95 && b.defence() < a.defence(), a + " vs " + b);

        for (Player p : club.getPlayers()) {
            p.setDribbling(18f);
            p.setDexterity(18f);
            p.setTrickShots(18f);
        }
        TeamSheet dribbling = twoThreeFive.copy();
        dribbling.setStyle(ClubStyle.DRIBBLING);
        TeamStrength d = TeamStrength.of(dribbling, squad);
        TeamStrength c = TeamStrength.of(twoThreeFive, squad);
        assertTrue(d.attack() > c.attack(), "dribblers do better at the dribbling game");
        assertTrue(d.attack() < c.attack() * 1.1, "a small modifier");
        assertEquals(ClubStyle.DRIBBLING, TeamStrength.bestStyle(dribbling, squad));
    }

    @Test
    void aClubWithFewerThanElevenFitMenPlaysShort() {
        Club club = squad(5);
        TeamStrength full = TeamStrength.of(TeamSheets.standing(club), TeamSheets.squadById(club));
        club.getPlayers().subList(8, club.getPlayers().size()).clear(); // eight men left
        club.setTeamSheet(null);
        TeamSheet sheet = TeamSheets.standing(club);
        assertEquals(8, sheet.selectedCount());
        TeamSheets.Check check = TeamSheets.check(sheet, club);
        assertTrue(check.playsShort());
        assertTrue(!check.hasEmptyPlaces(), "every fit man is on the sheet");
        TeamStrength shortSide = TeamStrength.of(sheet, TeamSheets.squadById(club));
        assertEquals(8, shortSide.men());
        assertTrue(shortSide.overall() < full.overall() * 0.85, shortSide + " vs " + full);
        // the keeper is still in goal: places fill best first, short where the men run out
        assertEquals(Position.GOALKEEPER, TeamSheets.squadById(club).get(sheet.getPlayerId(0)).getPosition());
    }

    @Test
    void scorersAreWeightedByThePositionOfTheirPlace() {
        Club club = squad(6);
        TeamSheet sheet = TeamSheets.standing(club);
        Map<Long, Player> squad = TeamSheets.squadById(club);
        Map<Position.Line, Integer> goals = new EnumMap<>(Position.Line.class);
        RandomSource random = RandomSource.seeded(1888);
        int n = 20_000;
        for (int i = 0; i < n; i++) {
            Player scorer = TeamStrength.pickScorer(sheet, squad, random);
            assertTrue(sheet.isSelected(scorer.getId()), "only the eleven score");
            goals.merge(sheet.position(sheet.slotOf(scorer.getId())).getLine(), 1, Integer::sum);
        }
        double forwards = goals.getOrDefault(Position.Line.FORWARD, 0) / (double) n;
        double halves = goals.getOrDefault(Position.Line.HALF, 0) / (double) n;
        double backs = goals.getOrDefault(Position.Line.BACK, 0) / (double) n;
        double keeper = goals.getOrDefault(Position.Line.GOAL, 0) / (double) n;
        assertTrue(forwards > 0.8, "forwards score most: " + goals);
        assertTrue(halves > backs && backs > keeper, goals.toString());
        assertTrue(keeper < 0.005, "the keeper almost never: " + goals);
        // the centre forward outscores each winger
        int cf = 0, rw = 0;
        RandomSource again = RandomSource.seeded(1888);
        for (int i = 0; i < n; i++) {
            Position p = sheet.position(sheet.slotOf(TeamStrength.pickScorer(sheet, squad, again).getId()));
            cf += p == Position.CENTRE_FORWARD ? 1 : 0;
            rw += p == Position.RIGHT_WING ? 1 : 0;
        }
        assertTrue(cf > rw * 1.3, cf + " vs " + rw);
        assertNull(TeamStrength.pickScorer(new TeamSheet(), squad, random), "an empty sheet has no scorer");
    }

    @Test
    void theCommitteeFormIsSeededPerManAndSmall() {
        Club club = squad(7);
        List<Player> men = TeamSheets.available(club);
        Map<Long, Double> a = TeamSelector.form(men, RandomSource.seeded(5).fork("teamsheet:1:7"));
        List<Player> reversed = new ArrayList<>(men);
        java.util.Collections.reverse(reversed);
        Map<Long, Double> b = TeamSelector.form(reversed, RandomSource.seeded(5).fork("teamsheet:1:7"));
        Map<Long, Double> c = TeamSelector.form(men, RandomSource.seeded(5).fork("teamsheet:2:7"));
        assertEquals(a, b, "squad order does not matter");
        assertNotEquals(a, c, "another match, another form");
        for (double f : a.values()) {
            assertTrue(f >= 1 - 2 * TeamSelector.FORM_SPREAD && f <= 1 + 2 * TeamSelector.FORM_SPREAD, "" + f);
        }
    }
}
