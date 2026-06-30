package com.rndmodgames.futtoboru.engine.economy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Early-warning scan for club finances (no bankruptcy simulation yet).
 */
public final class EconomyHealthMonitor {

    private EconomyHealthMonitor() {
    }

    public static List<ClubBalanceAlert> findNegativeBalances(SaveGame save) {
        if (save == null || save.getAllClubs() == null) {
            return Collections.emptyList();
        }
        List<ClubBalanceAlert> alerts = new ArrayList<>();
        for (Club club : save.getAllClubs()) {
            if (club == null || club.getClubBalance() == null) {
                continue;
            }
            if (club.getClubBalance().compareTo(BigDecimal.ZERO) < 0) {
                alerts.add(new ClubBalanceAlert(
                        club.getId(),
                        club.getName(),
                        club.getClubBalance()));
            }
        }
        return alerts;
    }

    /**
     * Scans only clubs in the given league roster (1888 FL members during sim).
     */
    public static List<ClubBalanceAlert> findNegativeBalancesInLeague(League league) {
        if (league == null || league.getLeagueClubs() == null) {
            return Collections.emptyList();
        }
        List<ClubBalanceAlert> alerts = new ArrayList<>();
        for (Club club : league.getLeagueClubs()) {
            if (club == null || club.getClubBalance() == null) {
                continue;
            }
            if (club.getClubBalance().compareTo(BigDecimal.ZERO) < 0) {
                alerts.add(new ClubBalanceAlert(club.getId(), club.getName(), club.getClubBalance()));
            }
        }
        return alerts;
    }

    public static BigDecimal minimumBalanceInLeague(League league) {
        if (league == null || league.getLeagueClubs() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal min = null;
        for (Club club : league.getLeagueClubs()) {
            if (club == null || club.getClubBalance() == null) {
                continue;
            }
            if (min == null || club.getClubBalance().compareTo(min) < 0) {
                min = club.getClubBalance();
            }
        }
        return min != null ? min : BigDecimal.ZERO;
    }

    public static BigDecimal minimumBalance(SaveGame save) {
        if (save == null || save.getAllClubs() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal min = null;
        for (Club club : save.getAllClubs()) {
            if (club == null || club.getClubBalance() == null) {
                continue;
            }
            if (min == null || club.getClubBalance().compareTo(min) < 0) {
                min = club.getClubBalance();
            }
        }
        return min != null ? min : BigDecimal.ZERO;
    }

    public static final class ClubBalanceAlert {
        private final Long clubId;
        private final String clubName;
        private final BigDecimal balance;

        public ClubBalanceAlert(Long clubId, String clubName, BigDecimal balance) {
            this.clubId = clubId;
            this.clubName = clubName;
            this.balance = balance;
        }

        public Long getClubId() {
            return clubId;
        }

        public String getClubName() {
            return clubName;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        @Override
        public String toString() {
            return clubName + " (id=" + clubId + ") balance=" + balance;
        }
    }
}
