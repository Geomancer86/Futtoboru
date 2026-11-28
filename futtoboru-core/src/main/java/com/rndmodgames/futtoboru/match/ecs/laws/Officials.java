package com.rndmodgames.futtoboru.match.ecs.laws;

import com.badlogic.ashley.core.Entity;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.pitch.MatchEnds;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * Spawns the three officials of 1888-89 (E-16): the referee, then the home club's umpire, then the away club's, after
 * the players and the ball ({@link SpawnRole#OFFICIAL}). That order is the viewer's official order (0 = referee, 1
 * and 2 umpires). Partisanship and perception come from {@link LawsParams}.
 */
public final class Officials {

    private Officials() {
    }

    /** @return referee, home umpire, away umpire */
    public static Entity[] spawn(MatchWorld world, PitchGeometry pitch, MatchEnds ends) {
        LawsParams p = world.params().laws;
        double hw = pitch.halfWidth();
        double hl = pitch.halfLength();
        Entity[] out = new Entity[3];
        out[0] = world.spawn(SpawnRole.OFFICIAL);
        out[0].add(new OfficialComponent(OfficialComponent.REFEREE, null, p.refereePerception, 0, 0,
                world.params().rules.refereeOnField() ? -p.refereeOnFieldGapM : -(hw + p.offFieldOffsetM)));
        Team[] clubs = { Team.HOME, Team.AWAY };
        for (int k = 0; k < 2; k++) {
            Team club = clubs[k];
            double own = ends.endDefendedBy(club).sign();
            double flank = club == Team.HOME ? -1 : 1;
            double y = world.params().rules.refereeOnField() ? flank * (hw + p.offFieldOffsetM)
                    : flank * (hw - p.umpireInsetM);
            out[1 + k] = world.spawn(SpawnRole.OFFICIAL);
            out[1 + k].add(new OfficialComponent(OfficialComponent.UMPIRE, club, p.umpirePerception,
                    p.umpirePartisanship, own * hl * 0.5, y));
        }
        return out;
    }
}
