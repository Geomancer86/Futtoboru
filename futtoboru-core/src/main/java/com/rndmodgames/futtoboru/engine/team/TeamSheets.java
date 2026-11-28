package com.rndmodgames.futtoboru.engine.team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.TeamShape;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * G-01: the team sheets of the game. Every club has a standing sheet ({@link #standing}); every match gets a sheet
 * per side at kick-off ({@link #kickOff}), frozen on the {@link Match} and saved with it.
 * <ul>
 *   <li><b>The player's club</b> plays its standing sheet as he left it: men who left the club or are not fit drop
 *       out, and the committee fills empty places from the fit men left (the auto-pick, no dice), then names a
 *       captain if there is none. The standing sheet itself is not changed.</li>
 *   <li><b>An AI club</b> picks again for every match ({@link TeamSelector#bestSheet}: XI, shape, style, captain),
 *       with a small seeded form per man from {@code fork("teamsheet:" + match + ":" + club)}; the pick becomes its
 *       standing sheet. The same seed and squads give the same sheets, in any order of play.</li>
 *   <li>A club with fewer than eleven fit men plays short: the places it cannot fill stay empty (D7: no
 *       substitutes).</li>
 * </ul>
 */
public final class TeamSheets {

    private TeamSheets() {
    }

    /**
     * Whether a man can play today. Every squad man for now; G-02 (injuries and availability) narrows it.
     */
    public static boolean isAvailable(Player p) {
        return p != null && p.getId() != null && p.getPerson() != null;
    }

    /** The club's fit men, in squad order. */
    public static List<Player> available(Club club) {
        List<Player> out = new ArrayList<>();
        if (club != null && club.getPlayers() != null) {
            for (Player p : club.getPlayers()) {
                if (isAvailable(p)) {
                    out.add(p);
                }
            }
        }
        return out;
    }

    /** The club's squad by player id. */
    public static Map<Long, Player> squadById(Club club) {
        Map<Long, Player> out = new LinkedHashMap<>();
        if (club != null && club.getPlayers() != null) {
            for (Player p : club.getPlayers()) {
                if (p != null && p.getId() != null) {
                    out.put(p.getId(), p);
                }
            }
        }
        return out;
    }

    /** @return true when {@code club} is the one the player manages in {@code game} */
    public static boolean isManaged(Club club, SaveGame game) {
        Club current = game != null ? game.getCurrentClub() : null;
        return club != null && current != null && club.getId() != null && club.getId().equals(current.getId());
    }

    /**
     * The club's standing sheet, made on first use (the committee's best sheet, no dice) and pruned of men who are
     * no longer in the squad.
     */
    public static TeamSheet standing(Club club) {
        TeamSheet sheet = club.getTeamSheet();
        if (sheet == null) {
            sheet = TeamSelector.bestSheet(club.getId(), available(club), null);
            club.setTeamSheet(sheet);
            return sheet;
        }
        if (sheet.getClubId() == null) {
            sheet.setClubId(club.getId());
        }
        Map<Long, Player> squad = squadById(club);
        for (int s = 0; s < TeamShape.SLOTS; s++) {
            Long id = sheet.getPlayerId(s);
            if (id != null && !squad.containsKey(id)) {
                sheet.setPlayerId(s, null);
            }
        }
        return sheet;
    }

    /** Re-picks the standing sheet's eleven for its shape (the editor's Auto-pick); style and shape stay. */
    public static void autoPick(Club club) {
        TeamSheet sheet = standing(club);
        sheet.clearPlayers();
        TeamSelector.fillEmpty(sheet, available(club), null);
    }

    /**
     * The sheet a side will play {@code match} with, as it stands now (the Match Preview): the frozen one once
     * played; for the player's club its standing sheet as it is (still editable); for an AI club the pick it makes
     * at kick-off (same seed, same pick while the squad does not change).
     */
    public static TeamSheet preview(Match match, Club club, boolean home, SaveGame game, RandomSource root) {
        TeamSheet frozen = home ? match.getHomeSheet() : match.getAwaySheet();
        if (frozen != null) {
            return frozen;
        }
        if (isManaged(club, game)) {
            return standing(club);
        }
        return aiPick(match, club, root);
    }

    /**
     * The side's sheet at kick-off, frozen on the match (an already frozen sheet is kept). The player's club: its
     * standing sheet completed by the committee; an AI club: its pick for this match, which becomes its standing
     * sheet.
     */
    public static TeamSheet kickOff(Match match, Club club, boolean home, SaveGame game, RandomSource root) {
        TeamSheet frozen = home ? match.getHomeSheet() : match.getAwaySheet();
        if (frozen != null) {
            return frozen;
        }
        if (isManaged(club, game)) {
            frozen = standing(club).copy();
            List<Player> fit = available(club);
            Map<Long, Player> fitById = new LinkedHashMap<>();
            for (Player p : fit) {
                fitById.put(p.getId(), p);
            }
            for (int s = 0; s < TeamShape.SLOTS; s++) {
                Long id = frozen.getPlayerId(s);
                if (id != null && !fitById.containsKey(id)) {
                    frozen.setPlayerId(s, null);
                }
            }
            TeamSelector.fillEmpty(frozen, fit, null);
        } else {
            frozen = aiPick(match, club, root);
            club.setTeamSheet(frozen.copy());
        }
        frozen.setClubId(club.getId());
        if (home) {
            match.setHomeSheet(frozen);
        } else {
            match.setAwaySheet(frozen);
        }
        return frozen;
    }

    /** An AI club's pick for {@code match}: seeded per match and club, independent of the order of play. */
    static TeamSheet aiPick(Match match, Club club, RandomSource root) {
        List<Player> fit = available(club);
        RandomSource rng = root != null ? root.fork("teamsheet:" + matchKey(match) + ":" + club.getId()) : null;
        return TeamSelector.bestSheet(club.getId(), fit, TeamSelector.form(fit, rng));
    }

    /** The match's stream key, as {@code MatchSimulator} builds it. */
    static String matchKey(Match match) {
        return match.getId() != null ? String.valueOf(match.getId())
                : match.getHomeClubId() + "-" + match.getAwayClubId() + "@" + match.getMatchDateTime();
    }

    // ------------------------------------------------------------------------------------------------ warnings

    /**
     * What the editor warns about.
     *
     * @param filled        places with a man
     * @param fitMen        fit men in the squad
     * @param outOfPosition places whose man is not in his own position
     * @param noCaptain     nobody named captain
     */
    public record Check(int filled, int fitMen, List<Integer> outOfPosition, boolean noCaptain) {

        /** @return true when the club has fewer fit men than places: it will play short */
        public boolean playsShort() {
            return fitMen < TeamShape.SLOTS;
        }

        /** @return true when places are empty although fit men are left (the committee fills them at kick-off) */
        public boolean hasEmptyPlaces() {
            return filled < Math.min(TeamShape.SLOTS, fitMen);
        }
    }

    /** Checks {@code sheet} against the club's squad. */
    public static Check check(TeamSheet sheet, Club club) {
        Map<Long, Player> squad = squadById(club);
        List<Integer> out = new ArrayList<>();
        int filled = 0;
        for (int s = 0; s < TeamShape.SLOTS; s++) {
            Player p = sheet.getPlayerId(s) != null ? squad.get(sheet.getPlayerId(s)) : null;
            if (p == null) {
                continue;
            }
            filled++;
            if (TeamRatings.outOfPosition(p, sheet.position(s))) {
                out.add(s);
            }
        }
        boolean noCaptain = sheet.getCaptainId() == null || !sheet.isSelected(sheet.getCaptainId());
        return new Check(filled, available(club).size(), Collections.unmodifiableList(out), noCaptain);
    }
}
