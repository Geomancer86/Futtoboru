package com.rndmodgames.futtoboru.match.watch;

import java.util.function.IntFunction;

import com.rndmodgames.futtoboru.match.ecs.events.MatchEvent;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEventLevel;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * The words of the match screen's newspaper ticker (E-21f, H2): one short line for each event shown at the KEY and
 * EXTENDED levels (D19-9), from the language bundles. Pure: players are named through a lookup of the surname of a
 * player ref, so the text is the same however the match is shown.
 */
public final class MatchTickerText {

    /** The ticker shows these levels: key events and the extended ones (shots, saves, fouls given, knocks). */
    public static final MatchEventLevel LEVEL = MatchEventLevel.EXTENDED;

    private final String homeName;
    private final String awayName;
    private final IntFunction<String> surname;

    /**
     * @param surname the surname of the man a player ref names, null when unknown
     */
    public MatchTickerText(String homeName, String awayName, IntFunction<String> surname) {
        this.homeName = homeName;
        this.awayName = awayName;
        this.surname = surname;
    }

    /** True when the ticker prints this event. */
    public boolean shows(MatchEvent e) {
        return e.shownAt(LEVEL) && line(e) != null;
    }

    /** The line for an event, or null when the ticker has nothing to say of it. */
    public String line(MatchEvent e) {
        String side = clubOf(e.side());
        switch (e.type()) {
            case KICK_OFF:
                return LanguageModLoader.format(e.minute() > 45 ? "watch_ev_kickoff_second" : "watch_ev_kickoff", side);
            case SHOT:
                return LanguageModLoader.format(e.header() ? "watch_ev_header" : "watch_ev_shot", name(e.actor()),
                        side);
            case SAVE:
                return LanguageModLoader.format("watch_ev_save", name(e.actor()), name(e.other()));
            case GOAL:
                if (e.ownGoal()) {
                    return LanguageModLoader.format("watch_ev_goal_own", name(e.actor()), side);
                }
                return LanguageModLoader.format("watch_ev_goal", name(e.actor()), side);
            case DISPUTED_GOAL:
                return LanguageModLoader.getValue(e.goalStood() ? "watch_ev_disputed_stood" : "watch_ev_disputed_off");
            case OFFSIDE_APPEAL:
                return e.given() ? LanguageModLoader.format("watch_ev_offside", name(e.actor())) : null;
            case FOUL:
                return e.given() ? LanguageModLoader.format("watch_ev_foul", name(e.actor())) : null;
            case INJURY:
                return LanguageModLoader.format(e.severity() >= MatchEvent.CARRIED_OFF ? "watch_ev_injury_off"
                        : e.severity() == MatchEvent.PASSENGER ? "watch_ev_injury_passenger" : "watch_ev_injury_knock",
                        name(e.actor()));
            case LACE_CUT:
                return LanguageModLoader.format("watch_ev_lace", name(e.actor()));
            case HEAVY_BALL:
                return LanguageModLoader.getValue("watch_ev_heavy");
            case HALF_TIME:
                return LanguageModLoader.format("watch_ev_half_time", homeName, e.homeScore(), e.awayScore(),
                        awayName);
            case FULL_TIME:
                return LanguageModLoader.format("watch_ev_full_time", homeName, e.homeScore(), e.awayScore(),
                        awayName);
            case SENDING_OFF:
                return LanguageModLoader.format("watch_ev_sent_off", name(e.actor()));
            default:
                return null;
        }
    }

    /** True for the lines the ticker sets in heavier type (goals, the whistles). */
    public static boolean emphasised(MatchEvent e) {
        switch (e.type()) {
            case GOAL:
            case HALF_TIME:
            case FULL_TIME:
            case SENDING_OFF:
                return true;
            default:
                return false;
        }
    }

    private String clubOf(Team side) {
        return side == Team.HOME ? homeName : side == Team.AWAY ? awayName : "";
    }

    private String name(int ref) {
        String n = ref < 0 ? null : surname.apply(ref);
        return n != null ? n : LanguageModLoader.getValue("watch_unknown_player");
    }
}
