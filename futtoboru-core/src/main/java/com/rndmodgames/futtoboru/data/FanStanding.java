package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * G-10: what a club's fans think of one of its players: a points meter and the tier (0 unknown .. 5 legend) it shows.
 * Saved inside its {@link Player} (one per club-person pair, since a person listed at two clubs has a player per
 * squad). Optional: an older save has none, and the tier then starts from the season's {@code fanTiers} data.
 * Moved only by {@link com.rndmodgames.futtoboru.engine.fans.FanMeter}.
 */
public class FanStanding implements Serializable {

    private static final long serialVersionUID = 1L;

    private Double points;
    private Integer tier;

    public FanStanding() {
    }

    public FanStanding(double points, int tier) {
        this.points = points;
        this.tier = tier;
    }

    public double getPoints() {
        return points != null ? points : 0;
    }

    public void setPoints(Double points) {
        this.points = points;
    }

    public int getTier() {
        return tier != null ? tier : 0;
    }

    public void setTier(Integer tier) {
        this.tier = tier;
    }
}
