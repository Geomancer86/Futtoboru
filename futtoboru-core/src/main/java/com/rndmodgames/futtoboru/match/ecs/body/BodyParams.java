package com.rndmodgames.futtoboru.match.ecs.body;

import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.match.ecs.Surface;

/**
 * Parameters of player bodies (E-13, docs/design/MATCH_ENGINE_ECS.md §3.3): attribute-to-physics mapping, physique
 * by position (D22: defaults by position now, sourced height/weight later), grip and fatigue per surface and boot
 * sole, stamina, contacts, charges and injuries. Plain public fields with defaults (D14: later
 * {@code gamedata/core/engine/physics.json}), part of {@link com.rndmodgames.futtoboru.match.ecs.PhysicsParams#body}.
 *
 * Confidence as in BALL_AND_KIT_PHYSICS.md (BKP): almost everything here is D (design estimate, tune in calibration
 * E-20); there is no period measurement of footballers' speed or grip.
 */
public final class BodyParams {

    // ---- attributes -> limits (ratings on the game's 3..23 scale) --------------------------------------------------

    /** Lowest and highest rating of the people data (E-01). */
    public double ratingMin = 3, ratingMax = 23;
    /** Top speed of the slowest / fastest rating, m/s (D: modern elite sprint peaks ~9.5-10 m/s; amateurs of 1888 in heavy boots lower). */
    public double topSpeedMin = 6.5, topSpeedMax = 8.8;
    /** Acceleration at standstill, m/s^2, slowest / quickest rating (D: modern sprint starts 4-6 m/s^2). */
    public double accelMin = 3.0, accelMax = 5.0;
    /** Braking is this much harder than accelerating (D). */
    public double decelOverAccel = 1.6;
    /** Turn rate when standing, rad/s, least / most agile (dexterity) (D: a half turn in ~0.3-0.5 s). */
    public double turnStandingMin = 7.0, turnStandingMax = 11.0;
    /** Turn rate at top speed, rad/s, least / most agile (D: "a sprinting forward cannot cut back"). */
    public double turnAtTopMin = 1.2, turnAtTopMax = 2.2;

    // ---- physique by position (D22-8 a: defaults until sourced height/weight) --------------------------------------

    /**
     * Body mass in kg per {@link Position} ordinal (GK, FB, HB, RW, IR, CF, IL, LW). D: Victorian working men averaged
     * ~1.68 m and ~63 kg; footballers heavier, backs and keepers the big men ("burly backs").
     */
    public double[] massByPosition = {76, 76, 71, 64, 67, 70, 67, 64};
    /** Body circle radius in metres per {@link Position} ordinal (D, ~0.35 m in §3.3). */
    public double[] radiusByPosition = {0.37, 0.37, 0.35, 0.33, 0.34, 0.35, 0.34, 0.33};

    // ---- surface (grip, speed, fatigue) and boot soles -------------------------------------------------------------

    /** Firm turf: firm footing. */
    public SurfaceBody dry = new SurfaceBody(0.90, 1.00, 1.00, 0.90, 1.00, 1.00);
    /** Heavy soft pitch: slippery top, bars dig in. */
    public SurfaceBody wet = new SurfaceBody(0.60, 0.95, 1.15, 0.65, 1.00, 1.00);
    /** Mud / standing water: heavy legs, little grip without bars. */
    public SurfaceBody muddy = new SurfaceBody(0.45, 0.85, 1.35, 0.55, 1.00, 0.95);
    /** Hard / frozen ground: bars and studs skate (BKP §2 "slip chance on frozen ground high"). */
    public SurfaceBody frozen = new SurfaceBody(0.45, 1.00, 1.00, 1.00, 0.80, 0.75);

    /** Footing of one surface for players. All D. */
    public static final class SurfaceBody {
        /** Friction coefficient of a barred boot: horizontal acceleration (forward and sideways) is capped at grip x g. */
        public double grip;
        /** Multiplier on top speed. */
        public double speedFactor;
        /** Multiplier on stamina drain ({@code fatigueFactor} in §2.2). */
        public double fatigueFactor;
        /** Multiplier on {@link #grip} per {@link Studs} ordinal: flat sole, leather bars, studs. */
        public double[] soleGrip;

        public SurfaceBody() {
        }

        public SurfaceBody(double grip, double speedFactor, double fatigueFactor, double flat, double bars,
                double studs) {
            this.grip = grip;
            this.speedFactor = speedFactor;
            this.fatigueFactor = fatigueFactor;
            this.soleGrip = new double[] {flat, bars, studs};
        }

        /** Effective friction coefficient with this sole. */
        public double grip(Studs sole) {
            return grip * soleGrip[sole.ordinal()];
        }
    }

    public SurfaceBody surface(Surface s) {
        switch (s) {
            case DRY:
                return dry;
            case WET:
                return wet;
            case MUDDY:
                return muddy;
            case FROZEN:
                return frozen;
            default:
                throw new IllegalArgumentException(String.valueOf(s));
        }
    }

    // ---- boots (BKP §2: ~500 g dry per boot; top speed -3 %, fatigue +5 % vs a 1930s boot of a third the weight) ----

    /** Boot mass per boot at which kit effects are zero, kg: the dry 1888 boot (BKP §2, M). */
    public double bootReferenceKg = 0.50;
    /** Top speed lost per kg of boot pair above the reference (D: BKP's -3 % for 0.67 kg pair difference). */
    public double topSpeedPerBootKg = 0.045;
    /** Acceleration lost per kg of boot pair above the reference (D: twice the speed effect; §3.3 "-acceleration"). */
    public double accelPerBootKg = 0.09;
    /** Extra stamina drain per kg of boot pair above the reference (D: BKP's +5 % for 0.67 kg). */
    public double fatiguePerBootKg = 0.075;

    // ---- stamina (§3.3: drains with speed^2 and surface, recovers walking) ----------------------------------------

    /** Below this share of top speed squared a player recovers short-term stamina (D: ~55 % of top speed). */
    public double aerobicPowerShare = 0.30;
    /** Short-term stamina used per second at full sprint, average capacity (D: ~25 s of flat-out running). */
    public double sprintDrainPerS = 0.04;
    /** Short-term stamina recovered per second standing, average endurance (D: ~20 s from empty). */
    public double recoverPerS = 0.05;
    /** Match reserve used per second per unit (v/top)^2 (D: average play ~0.1 drains ~0.3 over 90 min). */
    public double reserveDrainPerS = 5.5e-4;
    /** Top speed kept with empty short-term stamina / empty reserve (D). */
    public double speedAtEmptyStamina = 0.70, speedAtEmptyReserve = 0.85;
    /** Acceleration kept with empty short-term stamina (D). */
    public double accelAtEmptyStamina = 0.60;
    /** Reserve recovered at half-time, share of what was used (D). */
    public double halfTimeReserveRecovery = 0.25;

    // ---- contacts and charges (§3.3) --------------------------------------------------------------------------------

    /** Restitution of a body-body contact (D: bodies barely bounce). */
    public double bodyRestitution = 0.10;
    /** Extra impulse of a deliberate shoulder charge, N·s, scaled by 0.5 + strength (D). */
    public double chargeDriveNs = 40;
    /** Speed change (m/s) that makes an average-balance player stumble / go down (D). */
    public double stumbleDeltaV = 1.2, knockDownDeltaV = 2.4;
    /** Balance scales both thresholds by (1 - spread/2 .. 1 + spread/2) from worst to best balance (D). */
    public double balanceSpread = 0.8;
    /** Ticks without full control after a stumble (D: 1/3 s at 30 Hz). */
    public int stumbleTicks = 10;
    /** Ticks on the ground after a knock-down, plus extra per m/s of speed change above the threshold (D). */
    public int knockDownTicks = 45;
    public double knockDownTicksPerDeltaV = 15;
    /** Speed kept per second while on the ground (sliding to a stop) (D). */
    public double groundFrictionPerS = 6.0;
    /** A charge is "from behind" when the contact comes within this angle of the victim's back, rad (D, 1869 law). */
    public double fromBehindHalfAngle = StrictMath.toRadians(60);

    // ---- injuries (§3.3, D19-3: knock / passenger / off) -------------------------------------------------------------

    /** Contact impulse below which there is no injury roll, N·s (D: a firm fair charge ~150 N·s). */
    public double injuryThresholdNs = 120;
    /** Injury chance at twice the threshold for an average player without shin pads (D). */
    public double injuryRiskAtDouble = 0.02;
    /** Upper bound of any single injury chance (D). */
    public double injuryRiskMax = 0.25;
    /** Shin pads multiply the contact injury risk (BKP §2 kit.shinPadInjuryScale 0.6, D). */
    public double shinPadInjuryScale = 0.6;
    /** Severity split of an injury at the threshold: knock, passenger; rest is off (D). Harder hits shift severity up. */
    public double knockShare = 0.70, passengerShare = 0.22;
    /** Severity shift per unit of (impulse / threshold - 1) (D); the knock share never falls below the minimum. */
    public double severityShiftPerExcess = 0.15, knockShareMin = 0.25;
    /** Ticks a knock lasts (D: two minutes of reduced limits). */
    public int knockTicks = 30 * 120;
    /** Limits kept while knocked / as a passenger (D19-3: "plays with reduced limits"). */
    public double knockedLimitFactor = 0.85, passengerLimitFactor = 0.60;

    /** Fragility = base x age factor x condition factor (D). */
    public double fragilityAgeFrom = 30, fragilityPerYear = 0.06, fragilityAtZeroCondition = 2.0;

    /** Throws on values the engine cannot run with. Returns this. */
    public BodyParams validate() {
        req(ratingMax > ratingMin, "rating range");
        req(topSpeedMin > 0 && topSpeedMax >= topSpeedMin && accelMin > 0 && accelMax >= accelMin, "speed/accel");
        req(decelOverAccel > 0 && turnStandingMin > 0 && turnAtTopMin > 0, "turn/decel");
        req(massByPosition.length == Position.values().length && radiusByPosition.length == massByPosition.length,
                "physique table");
        for (int i = 0; i < massByPosition.length; i++) {
            req(massByPosition[i] > 0 && radiusByPosition[i] > 0, "physique " + i);
        }
        for (Surface s : Surface.values()) {
            SurfaceBody b = surface(s);
            req(b != null && b.grip > 0 && b.speedFactor > 0 && b.fatigueFactor > 0 && b.soleGrip != null
                    && b.soleGrip.length == Studs.values().length, "surface " + s);
        }
        req(bootReferenceKg > 0, "boot reference");
        req(aerobicPowerShare > 0 && aerobicPowerShare < 1 && sprintDrainPerS >= 0 && recoverPerS >= 0, "stamina");
        req(bodyRestitution >= 0 && bodyRestitution <= 1, "restitution");
        req(knockDownDeltaV > stumbleDeltaV && stumbleDeltaV > 0 && balanceSpread >= 0 && balanceSpread < 2, "falls");
        req(injuryThresholdNs > 0 && injuryRiskAtDouble >= 0 && injuryRiskMax <= 1, "injury risk");
        req(knockShare >= 0 && passengerShare >= 0 && knockShare + passengerShare <= 1
                && knockShareMin >= 0 && knockShareMin <= knockShare, "severity split");
        return this;
    }

    private static void req(boolean ok, String what) {
        if (!ok) {
            throw new IllegalArgumentException("invalid body params: " + what);
        }
    }
}
