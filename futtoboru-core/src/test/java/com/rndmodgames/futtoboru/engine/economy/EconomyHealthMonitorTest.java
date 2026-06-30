package com.rndmodgames.futtoboru.engine.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.system.SaveGame;

class EconomyHealthMonitorTest {

    @Test
    void findsClubsWithNegativeBalance() {
        SaveGame save = new SaveGame();
        Club healthy = new Club("Healthy FC");
        healthy.setId(1L);
        healthy.setClubBalance(new BigDecimal("100.00"));
        Club broke = new Club("Broke FC");
        broke.setId(2L);
        broke.setClubBalance(new BigDecimal("-5.50"));
        save.getAllClubs().add(healthy);
        save.getAllClubs().add(broke);

        List<EconomyHealthMonitor.ClubBalanceAlert> alerts = EconomyHealthMonitor.findNegativeBalances(save);
        assertEquals(1, alerts.size());
        assertEquals(2L, alerts.get(0).getClubId());
        assertTrue(alerts.get(0).getBalance().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    void minimumBalanceAcrossSave() {
        SaveGame save = new SaveGame();
        Club a = new Club("A");
        a.setClubBalance(new BigDecimal("50"));
        Club b = new Club("B");
        b.setClubBalance(new BigDecimal("3"));
        save.getAllClubs().add(a);
        save.getAllClubs().add(b);
        assertEquals(new BigDecimal("3"), EconomyHealthMonitor.minimumBalance(save));
    }

    @Test
    void findsNegativeBalancesOnlyInLeagueRoster() {
        SaveGame save = new SaveGame();
        com.rndmodgames.futtoboru.data.League league = new com.rndmodgames.futtoboru.data.League();
        league.setLeagueClubs(new ArrayList<>());
        Club inLeague = new Club("FL Member");
        inLeague.setId(1L);
        inLeague.setClubBalance(new BigDecimal("-1.00"));
        Club outsider = new Club("Non-FL");
        outsider.setId(99L);
        outsider.setClubBalance(new BigDecimal("-100.00"));
        league.getLeagueClubs().add(inLeague);
        save.getAllClubs().add(inLeague);
        save.getAllClubs().add(outsider);

        List<EconomyHealthMonitor.ClubBalanceAlert> leagueAlerts =
                EconomyHealthMonitor.findNegativeBalancesInLeague(league);
        assertEquals(1, leagueAlerts.size());
        assertEquals(1L, leagueAlerts.get(0).getClubId());
        assertEquals(new BigDecimal("-1.00"), EconomyHealthMonitor.minimumBalanceInLeague(league));
    }
}
