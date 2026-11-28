package com.rndmodgames.futtoboru.engine.injuries;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.InjuryKind;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.MatchInjury;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.data.MessagePriority;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerInjury;
import com.rndmodgames.futtoboru.engine.messages.MessageManager;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * G-02: injuries outlast the match. After a recorded match the hurt men get their days out ({@link PlayerInjury});
 * every day the recovery clears the ones that are over. Every club alike (D31); only the player's club gets letters.
 *
 * <p>Trainer: 1888 clubs had a trainer (fitness and rubbing down, RECRUITMENT_1888-1900.md 6.3), but the game has no
 * trainer staff post yet, so nothing shortens a recovery (G-02 brief: "otherwise none").
 */
public final class InjuryManager {

    public static final String MSG_INJURY = "INJURY";
    public static final String MSG_FIT_AGAIN = "INJURY_FIT";

    private InjuryManager() {
    }

    /** The day a match counts from: its own date, else the game's. */
    static LocalDateTime dayOf(Match match, SaveGame game) {
        return match.getMatchDateTime() != null ? match.getMatchDateTime() : game.getGameDate();
    }

    /**
     * Gives the men hurt in the match their days out (a knock of 0 days: none) and writes the player's club its
     * injury letter. Called once per match by the recorder.
     */
    public static void applyAfterMatch(SaveGame game, Match match, MessageManager messages) {
        if (game == null || match == null) {
            return;
        }
        LocalDateTime day = dayOf(match, game);
        side(game, match, game.getClubById(match.getHomeClubId()), match.getHomeInjuries(), day, messages);
        side(game, match, game.getClubById(match.getAwayClubId()), match.getAwayInjuries(), day, messages);
    }

    private static void side(SaveGame game, Match match, Club club, List<MatchInjury> injuries, LocalDateTime day,
            MessageManager messages) {
        if (club == null || injuries == null || injuries.isEmpty()) {
            return;
        }
        List<String> lines = new ArrayList<>();
        for (MatchInjury i : injuries) {
            Player p = playerOf(club, i.getPersonId());
            InjuryKind kind = i.getKindEnum();
            if (p == null || kind == null) {
                continue;
            }
            int days = i.getDaysOut() != null ? i.getDaysOut() : 0;
            if (days > 0) {
                p.setInjury(new PlayerInjury(kind, day.toLocalDate().atStartOfDay(), days, match.getId()));
            }
            lines.add(line(p, i, days, day));
        }
        if (messages != null && club == game.getCurrentClub() && !lines.isEmpty()) {
            messages.deliverMessage(injuryLetter(game, match, club, lines, day));
        }
    }

    private static String line(Player p, MatchInjury i, int days, LocalDateTime day) {
        String name = nameOf(p);
        String kind = LanguageModLoader.getValue(i.getKindEnum().labelKey());
        if (days <= 0) {
            return LanguageModLoader.format("msg_injury_line_fit", name, kind, i.getMinute());
        }
        return LanguageModLoader.format("msg_injury_line", name, kind, i.getMinute(), days,
                GameFormat.mediumDate(day.toLocalDate().plusDays(days)));
    }

    private static Message injuryLetter(SaveGame game, Match match, Club club, List<String> lines,
            LocalDateTime day) {
        Long opponentId = club.getId().equals(match.getHomeClubId()) ? match.getAwayClubId() : match.getHomeClubId();
        Club opponent = game.getClubById(opponentId);
        String against = opponent != null ? opponent.getName() : "";
        StringBuilder text = new StringBuilder(
                LanguageModLoader.format("msg_injury_text", against, GameFormat.mediumDate(day.toLocalDate())));
        text.append("\n\n");
        for (String l : lines) {
            text.append(l).append("\n");
        }
        text.append("\n").append(LanguageModLoader.getValue("msg_injury_no_substitutes"));
        Message m = letter(MSG_INJURY, LanguageModLoader.format("msg_injury_title", against,
                GameFormat.mediumDate(day.toLocalDate())), text.toString());
        m.setPriority(MessagePriority.NORMAL);
        return m;
    }

    /**
     * The day's recovery for every club: an injury whose last day is over is cleared (the player's club gets a
     * "fit again" letter). A man who is no longer at any club keeps his record until he joins one; it is over by date.
     */
    public static void recoverDaily(SaveGame game, LocalDateTime today, MessageManager messages) {
        if (game == null || today == null || game.getAllClubs() == null) {
            return;
        }
        for (Club club : game.getAllClubs()) {
            if (club == null || club.getPlayers() == null) {
                continue;
            }
            for (Player p : club.getPlayers()) {
                PlayerInjury injury = p != null ? p.getInjury() : null;
                if (injury == null || !Availability.isAvailable(p, today)) {
                    continue;
                }
                p.setInjury(null);
                if (messages != null && club == game.getCurrentClub()) {
                    messages.deliverMessage(letter(MSG_FIT_AGAIN,
                            LanguageModLoader.format("msg_injury_fit_title", nameOf(p),
                                    GameFormat.mediumDate(today.toLocalDate())),
                            LanguageModLoader.format("msg_injury_fit_text", nameOf(p),
                                    injury.getDaysOut() != null ? injury.getDaysOut() : 0)));
                }
            }
        }
    }

    private static Message letter(String type, String title, String text) {
        Message m = new Message();
        m.setCategory(MessageCategory.MATCH);
        m.setMessageType(type);
        m.setPriority(MessagePriority.NORMAL);
        m.setTitle(title);
        m.setPlainTextMessage(text);
        m.setIsRead(false);
        m.setIsDeleted(false);
        return m;
    }

    static Player playerOf(Club club, Long personId) {
        if (personId == null) {
            return null;
        }
        for (Player p : club.getPlayers()) {
            if (p != null && p.getPerson() != null && personId.equals(p.getPerson().getId())) {
                return p;
            }
        }
        return null;
    }

    static String nameOf(Player p) {
        if (p.getPerson() == null) {
            return "?";
        }
        String first = p.getPerson().getName() != null ? p.getPerson().getName() : "";
        String last = p.getPerson().getLastname() != null ? p.getPerson().getLastname() : "";
        return (first + " " + last).trim();
    }
}
