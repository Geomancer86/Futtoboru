package com.rndmodgames.futtoboru.engine.report;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEvent;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEventLog;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * The stored events of a played match (E-17, D19-9) for the report and the minute ticker: reads
 * {@link Match#getEventLog()} back and names the men in it. A player ref names a slot of the side's team sheet frozen
 * at kick-off (G-01), whose player id leads to the man.
 */
public final class MatchEvents {

    private MatchEvents() {
    }

    /** @return the match's events, or null when it has none (instant engine, older save, unreadable log) */
    public static MatchEventLog of(Match match) {
        return match == null ? null : MatchEventLog.decode(match.getEventLog());
    }

    /** @return the player a ref names (the man in that slot of the side's frozen sheet), or null when unknown */
    public static Player player(SaveGame game, Match match, int ref) {
        Team team = MatchEvent.refTeam(ref);
        if (team == null || game == null || match == null) {
            return null;
        }
        boolean home = team == Team.HOME;
        TeamSheet sheet = home ? match.getHomeSheet() : match.getAwaySheet();
        int slot = MatchEvent.refSlot(ref);
        if (sheet == null || slot < 0 || slot >= com.rndmodgames.futtoboru.data.TeamShape.SLOTS) {
            return null;
        }
        Long playerId = sheet.getPlayerId(slot);
        Club club = game.getClubById(home ? match.getHomeClubId() : match.getAwayClubId());
        if (playerId == null || club == null || club.getPlayers() == null) {
            return null;
        }
        for (Player p : club.getPlayers()) {
            if (p != null && playerId.equals(p.getId())) {
                return p;
            }
        }
        return null;
    }

    /** @return the surname of the man a ref names (his first name when he has no surname), or null when unknown */
    public static String surname(SaveGame game, Match match, int ref) {
        Player p = player(game, match, ref);
        if (p == null || p.getPerson() == null) {
            return null;
        }
        String last = p.getPerson().getLastname();
        if (last != null && !last.isBlank()) {
            return last.trim();
        }
        String first = p.getPerson().getName();
        return first != null && !first.isBlank() ? first.trim() : null;
    }

    /** @return the person id of the man a ref names, or null when unknown */
    public static Long personId(SaveGame game, Match match, int ref) {
        Player p = player(game, match, ref);
        return p != null && p.getPerson() != null ? p.getPerson().getId() : null;
    }
}
