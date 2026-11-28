package com.rndmodgames.futtoboru.engine.season;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.MatchIncome;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.data.MessagePriority;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.engine.finances.Money;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * G-08: the committee's letter on the season just completed, for the player's club in that League: final position,
 * points, the club's top League scorer, home League gates (total and average) and the balance.
 */
public final class SeasonSummaryLetter {

    public static final String MSG_TYPE = "SEASON_SUMMARY";

    private SeasonSummaryLetter() {
    }

    /**
     * @param standings the final table, champion first
     * @return the letter, or null when the club is not in the table
     */
    public static Message build(Club club, League league, CompetitionEdition season, List<Club> standings,
            LocalDateTime now) {
        if (club == null || standings == null) {
            return null;
        }
        int position = -1;
        for (int i = 0; i < standings.size(); i++) {
            if (standings.get(i) != null && club.getId().equals(standings.get(i).getId())) {
                position = i + 1;
            }
        }
        if (position < 0) {
            return null;
        }
        String seasonName = season != null && season.getName() != null ? season.getName() : league.getName();
        LocalDateTime from = season != null ? season.getStartDate() : null;

        StringBuilder text = new StringBuilder();
        text.append(LanguageModLoader.format("msg_season_summary_position", club.getName(), position, standings.size(),
                league.getName(), club.getPoints())).append("\n\n");

        // top League scorer of the club this season
        Map<Long, Integer> goals = new LinkedHashMap<>();
        for (Match m : club.getPlayedMatches()) {
            if (m == null || m.getMatchType() == null || m.getMatchType() != Match.LEAGUE_MATCH
                    || !within(m.getMatchDateTime(), from, now)) {
                continue;
            }
            List<Long> ours = club.getId().equals(m.getHomeClubId()) ? m.getHomeScorerIds() : m.getAwayScorerIds();
            for (Long id : ours) {
                goals.merge(id, 1, Integer::sum);
            }
        }
        Long best = null;
        for (Map.Entry<Long, Integer> e : goals.entrySet()) {
            if (best == null || e.getValue() > goals.get(best)) {
                best = e.getKey();
            }
        }
        String scorer = best == null ? null : scorerName(club, best);
        text.append(scorer == null ? LanguageModLoader.getValue("msg_season_summary_no_scorer")
                : LanguageModLoader.format("msg_season_summary_scorer", scorer, goals.get(best))).append("\n");

        // home League gates
        long total = 0;
        int games = 0;
        for (MatchIncome income : club.getMatchIncomes()) {
            if (income != null && income.getAttendance() != null && club.getId().equals(income.getClubId())
                    && income.getMatchType() != null && income.getMatchType() == Match.LEAGUE_MATCH
                    && within(income.getMatchDate(), from, now)) {
                total += income.getAttendance();
                games++;
            }
        }
        text.append(games == 0 ? LanguageModLoader.getValue("msg_season_summary_no_gates")
                : LanguageModLoader.format("msg_season_summary_gates", total, games, total / games)).append("\n");
        text.append(LanguageModLoader.format("msg_season_summary_balance",
                Money.format(club.getClubBalance() != null ? club.getClubBalance() : java.math.BigDecimal.ZERO)));

        Message m = new Message();
        m.setCategory(MessageCategory.LEAGUE);
        m.setMessageType(MSG_TYPE);
        m.setPriority(MessagePriority.NORMAL);
        m.setTitle(LanguageModLoader.format("msg_season_summary_title", club.getName(), seasonName));
        m.setPlainTextMessage(text.toString());
        m.setIsRead(false);
        m.setIsDeleted(false);
        return m;
    }

    private static boolean within(LocalDateTime date, LocalDateTime from, LocalDateTime to) {
        return date != null && (from == null || !date.isBefore(from)) && (to == null || !date.isAfter(to));
    }

    private static String scorerName(Club club, Long personId) {
        for (Player p : club.getPlayers()) {
            if (p != null && p.getPerson() != null && personId.equals(p.getPerson().getId())) {
                return p.getPerson().getName() + " " + p.getPerson().getLastname();
            }
        }
        return null;
    }
}
