package com.rndmodgames.futtoboru.engine.recruitment;

import java.time.LocalDateTime;

import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * R-09 + R-03: the recruitment day, run by the game engine after the contracts' day (so a 1 May rollover has settled
 * the squads before the new pools leave out the men in them). Every club, AI clubs too (D31). The engine then runs
 * R-05's {@link CandidateSources#daily} (how men reach the clubs), which is not part of this call: the Candidates
 * screen and a save's load also call {@link #afterDay}.
 */
public final class Recruitment {

    private Recruitment() {
    }

    /**
     * Staff posts first (a trainer and a secretary per club), then the pools (a new season's at the close season or
     * the game's first day, the month's arrivals in season). Also called by the Candidates screen before it shows a
     * new game's pools.
     */
    public static void afterDay(SaveGame game, LocalDateTime now, RandomSource root) {
        if (game == null || now == null || root == null) {
            return;
        }
        StaffRoles.ensure(game, now, root);
        CandidatePools.afterDay(game, now, root);
    }
}
