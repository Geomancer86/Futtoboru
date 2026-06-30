package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.simulation.AttendanceCalculator.AttendanceInputs;

class AttendanceCalculatorTest {

    private static final int CAPACITY = 10_000;

    @Test
    void leagueBaseFillHigherThanFriendly() {
        AttendanceInputs league = new AttendanceInputs(CAPACITY, 0.55, 1.0, 1.0, 1.0, 1.0);
        AttendanceInputs friendly = new AttendanceInputs(CAPACITY, 0.12, 1.0, 1.0, 1.0, 1.0);
        assertTrue(AttendanceCalculator.calculateFromInputs(league)
                > AttendanceCalculator.calculateFromInputs(friendly));
    }

    @Test
    void firstPlaceDrawsMoreThanLastPlace() {
        AttendanceInputs leader = new AttendanceInputs(CAPACITY, 0.55, 1.12, 1.0, 1.0, 1.0);
        AttendanceInputs last = new AttendanceInputs(CAPACITY, 0.55, 0.92, 1.0, 1.0, 1.0);
        assertTrue(AttendanceCalculator.calculateFromInputs(leader)
                > AttendanceCalculator.calculateFromInputs(last));
    }

    @Test
    void topThreeOpponentBoostsAttendance() {
        AttendanceInputs vsTop = new AttendanceInputs(CAPACITY, 0.55, 1.0, 1.10, 1.0, 1.0);
        AttendanceInputs vsMid = new AttendanceInputs(CAPACITY, 0.55, 1.0, 1.0, 1.0, 1.0);
        assertTrue(AttendanceCalculator.calculateFromInputs(vsTop)
                > AttendanceCalculator.calculateFromInputs(vsMid));
    }

    @Test
    void hotFormBoostsColdFormReduces() {
        AttendanceInputs hot = new AttendanceInputs(CAPACITY, 0.55, 1.0, 1.0, 1.08, 1.0);
        AttendanceInputs cold = new AttendanceInputs(CAPACITY, 0.55, 1.0, 1.0, 0.90, 1.0);
        assertTrue(AttendanceCalculator.calculateFromInputs(hot)
                > AttendanceCalculator.calculateFromInputs(cold));
    }

    @Test
    void derbyFactorIncreasesCrowd() {
        AttendanceInputs derby = new AttendanceInputs(CAPACITY, 0.55, 1.0, 1.0, 1.0, 1.18);
        AttendanceInputs normal = new AttendanceInputs(CAPACITY, 0.55, 1.0, 1.0, 1.0, 1.0);
        assertTrue(AttendanceCalculator.calculateFromInputs(derby)
                > AttendanceCalculator.calculateFromInputs(normal));
    }

    @Test
    void fillIsClampedToCapacityBand() {
        AttendanceInputs tiny = new AttendanceInputs(CAPACITY, 0.01, 0.5, 0.5, 0.5, 1.0);
        AttendanceInputs huge = new AttendanceInputs(CAPACITY, 2.0, 1.5, 1.5, 1.5, 1.5);
        assertEquals((int) Math.round(CAPACITY * AttendanceCalculator.MIN_FILL),
                AttendanceCalculator.calculateFromInputs(tiny));
        assertEquals((int) Math.round(CAPACITY * AttendanceCalculator.MAX_FILL),
                AttendanceCalculator.calculateFromInputs(huge));
    }

    @Test
    void baseFillByMatchType() {
        assertEquals(0.55, AttendanceCalculator.baseFill(Match.LEAGUE_MATCH), 0.001);
        assertEquals(0.65, AttendanceCalculator.baseFill(Match.CUP_MATCH), 0.001);
        assertEquals(0.12, AttendanceCalculator.baseFill(Match.FRIENDLY_MATCH), 0.001);
    }

    @Test
    void positionFactorScalesWithTableSize() {
        assertTrue(AttendanceCalculator.positionFactor(1, 12) > AttendanceCalculator.positionFactor(12, 12));
    }
}
