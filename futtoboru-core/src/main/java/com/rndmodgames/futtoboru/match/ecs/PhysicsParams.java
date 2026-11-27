package com.rndmodgames.futtoboru.match.ecs;

/**
 * All physics parameters of the match engine in one data class (D14: data, later
 * {@code gamedata/core/engine/physics.json}). Plain public fields with defaults and a no-arg constructor so a JSON
 * reader can fill it; treat an instance as immutable once a {@link MatchWorld} uses it.
 *
 * Defaults are for 1888-89 from docs/data/history/BALL_AND_KIT_PHYSICS.md (BKP). Confidence as in that doc:
 * H measured/primary, M one good secondary source, D design estimate (tune in calibration E-20).
 */
public final class PhysicsParams {

    // ---- clock (D22: 30 Hz bodies, 60 Hz ball, 10 Hz AI) ----------------------------------------------------------

    /** Simulation ticks per second (D22). */
    public int tickHz = 30;
    /** Ball integration substeps per tick: 2 = 60 Hz (D22). */
    public int ballSubsteps = 2;
    /** AI thinks every N ticks: 3 = 10 Hz (D22). */
    public int thinkEveryTicks = 3;

    // ---- environment -----------------------------------------------------------------------------------------------

    /** Standard gravity, m/s^2 (H). */
    public double gravity = 9.81;
    /** Air density, kg/m^3: ISA sea level, 15 C (H). A cold winter day is ~1.27; from temperature later. */
    public double airDensity = 1.225;

    // ---- ball (BKP §1) -------------------------------------------------------------------------------------------

    /** Radius, m: 27-28 in circumference = 10.9-11.3 cm (H, Laws 1872/1883); mid value (D). BKP §1.2. */
    public double ballRadiusM = 0.111;
    /** Dry mass, kg: no rule in 1888-89, 13-15 oz = 369-425 g from 1889 (H); mid value 400 g (D). BKP §1.2. */
    public double ballDryMassKg = 0.400;
    /**
     * Drag coefficient (D: no period measurement; a modern ball is ~0.45 below and ~0.2 above the drag crisis; a
     * stitched leather case with a lace sits between).
     */
    public double ballDragCoefficient = 0.30;
    /**
     * Magnus lift slope: lift coefficient per unit spin parameter (r*omega/v), so the curl force is
     * {@code 0.5 * rho * A * r * slope * omega * v} (D: wind-tunnel studies of modern balls give ~0.8-1.2 at low spin).
     */
    public double ballMagnusLiftSlope = 1.0;
    /** Spin lost per second in the air, share (D). */
    public double ballSpinDecayAirPerS = 0.05;
    /** Spin lost per second while rolling, share (D). */
    public double ballSpinDecayGroundPerS = 2.0;
    /** Water uptake ceiling, share of dry mass: +55.8 % for a laced uncoated leather ball (H, PH-25). BKP §1.3. */
    public double ballWaterUptakeMax = 0.56;
    /** Soak time constant at full submersion, minutes: 90 % of the ceiling in 90 min (D fit to PH-25). BKP §1.3. */
    public double ballUptakeTauMin = 39.0;
    /** Restitution multiplier of a saturated ball vs a dry one (D). BKP §1.4. */
    public double ballWetRestitutionScale = 0.875;
    /** Restitution of the ball on a rigid surface (steel, posts, bar), dry (D: 0.75-0.85). BKP §1.4. For E-12. */
    public double ballRestitutionRigid = 0.80;
    /** Below this downward speed (m/s) a bounce becomes a roll (D). */
    public double ballRollThresholdMps = 0.5;
    /**
     * Base rolling resistance on firm turf: deceleration = coefficient * g * surface factor (D: no historical data,
     * BKP §1.4 "tune in calibration"; 0.1 rolls a 10 m/s ball ~51 m).
     */
    public double ballRollingResistance = 0.10;

    // ---- kick (BKP §1.5) ----------------------------------------------------------------------------------------

    /** Effective foot + boot mass in a kick, kg (D, ~1.2 kg). BKP §1.5. */
    public double footEffectiveMassKg = 1.2;
    /** Foot-ball restitution in a kick (D: not in BKP; ~0.5 in modern kick studies, untested for leather). */
    public double footKickRestitution = 0.5;

    // ---- surfaces (BKP §1.4 and §3: pitch.restitution, pitch.rollDrag) ---------------------------------------------

    /** Firm turf: e 0.63 (BKP §3, FIFA turf band 0.55-0.71, M), roll drag x1. */
    public SurfaceParams dry = new SurfaceParams(0.63, 0.80, 1.0);
    /** Heavy soft pitch: e 0.45 (D, 0.40-0.50), roll drag x2 (D). */
    public SurfaceParams wet = new SurfaceParams(0.45, 0.70, 2.0);
    /** Mud / standing water: e 0.30 (D, 0.25-0.35), roll drag x3.5 (D, x2-x4); the ball "holds up". */
    public SurfaceParams muddy = new SurfaceParams(0.30, 0.55, 3.5);
    /** Hard / frozen ground: e 0.78 (D, 0.75-0.80), roll drag x0.5 (D: runs on). */
    public SurfaceParams frozen = new SurfaceParams(0.78, 0.92, 0.5);

    /** Bounce and roll of one surface. */
    public static final class SurfaceParams {
        /** Coefficient of restitution of a dry ball on this ground (vertical speed kept by a bounce). */
        public double restitution;
        /** Share of horizontal speed (and spin) kept by a bounce (D: no data; tune). */
        public double bounceKeep;
        /** Multiplier on {@link PhysicsParams#ballRollingResistance}. */
        public double rollDrag;

        public SurfaceParams() {
        }

        public SurfaceParams(double restitution, double bounceKeep, double rollDrag) {
            this.restitution = restitution;
            this.bounceKeep = bounceKeep;
            this.rollDrag = rollDrag;
        }
    }

    /** The parameters of a surface. */
    public SurfaceParams surface(Surface s) {
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

    /** Seconds per simulation tick. */
    public double tickSeconds() {
        return 1.0 / tickHz;
    }

    /** Throws on values the engine cannot run with. Returns this. */
    public PhysicsParams validate() {
        require(tickHz > 0 && ballSubsteps > 0 && thinkEveryTicks > 0, "tick rates must be positive");
        require(gravity > 0 && airDensity >= 0, "gravity/air");
        require(ballRadiusM > 0 && ballDryMassKg > 0, "ball size/mass");
        require(ballDragCoefficient >= 0 && ballMagnusLiftSlope >= 0, "ball aero");
        require(ballWaterUptakeMax >= 0 && ballUptakeTauMin > 0, "ball soak");
        require(ballWetRestitutionScale > 0 && ballWetRestitutionScale <= 1, "wet restitution scale");
        require(ballRollThresholdMps >= 0 && ballRollingResistance >= 0, "roll");
        require(footEffectiveMassKg > 0 && footKickRestitution >= 0 && footKickRestitution <= 1, "kick");
        for (Surface s : Surface.values()) {
            SurfaceParams p = surface(s);
            require(p != null && p.restitution >= 0 && p.restitution < 1 && p.bounceKeep >= 0 && p.bounceKeep <= 1
                    && p.rollDrag >= 0, "surface " + s);
        }
        return this;
    }

    private static void require(boolean ok, String what) {
        if (!ok) {
            throw new IllegalArgumentException("invalid physics params: " + what);
        }
    }
}
