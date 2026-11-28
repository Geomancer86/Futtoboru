package com.rndmodgames.futtoboru.engine.contracts;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ContractBand;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerContract;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.engine.random.RandomSource;

/**
 * G-04: what a man is worth and what he costs, with no state: his ability, the wage he asks within his club's band,
 * a week's pay of an engagement (wage in the playing season, summer pay in the close season, expenses for an
 * amateur), and whether he accepts the terms offered (a seeded draw: wage against his value and age, a loyalty
 * placeholder).
 *
 * <p>Placeholders (D30 A3, listed in docs/STATUS.md for review): the ability scale, the age factors, the answer
 * curve, {@link #LOYALTY}, {@link #VETERAN_WILLING}, {@link #AMATEUR_ACCEPTS}.
 */
public final class ContractTerms {

    /** A man re-engaged by the club he played for is a little more willing (placeholder) */
    public static final double LOYALTY = 0.05;
    /** From this age a man has fewer clubs after him and is more willing (placeholder) */
    public static final int VETERAN_AGE = 30;
    public static final double VETERAN_WILLING = 0.05;
    /** An amateur asked to stay on his old terms (expenses only) (placeholder) */
    public static final double AMATEUR_ACCEPTS = 0.85;
    /** A fair offer (his asking wage) is accepted this often before loyalty (placeholder) */
    static final double FAIR_OFFER = 0.85;
    static final double MIN_CHANCE = 0.02;
    static final double MAX_CHANCE = 0.97;

    /** Ability from which the band's wage rises above its minimum, and the span to its maximum (placeholder) */
    static final double ABILITY_FLOOR = 0.25;
    static final double ABILITY_SPAN = 0.35;

    private ContractTerms() {
    }

    // ------------------------------------------------------------------------------------------------ value

    /**
     * A man's ability 0-1: the mean of the attributes his position uses most, on the 3-23 scale. Goalkeepers: shot
     * stopping, area control, punching, hand to hand, rushing out, area positioning, concentration, courage; everyone
     * else: acceleration, speed, stamina, strength, passing, kicking, dribbling, tackling, heading, positioning,
     * teamwork, determination. A missing attribute counts as the 3d6 mean.
     */
    public static double ability(Player p) {
        if (p == null) {
            return 0.375;
        }
        Float[] values;
        if (p.getPosition() == Position.GOALKEEPER) {
            values = new Float[] { p.getShotStopping(), p.getAreaControl(), p.getPunching(), p.getHandToHand(),
                    p.getRushingOut(), p.getAreaPositioning(), p.getConcentration(), p.getCourage() };
        } else {
            values = new Float[] { p.getAcceleration(), p.getSpeed(), p.getStamina(), p.getStrength(), p.getPassing(),
                    p.getKicking(), p.getDribbling(), p.getTackling(), p.getHeading(), p.getPositioning(),
                    p.getTeamwork(), p.getDetermination() };
        }
        double sum = 0;
        for (Float v : values) {
            sum += v == null ? 10.5 : v;
        }
        double mean = sum / values.length;
        return Math.max(0, Math.min(1, (mean - 3) / 20.0));
    }

    /** @return his age in whole years on {@code day} (25 when the birth date is unknown) */
    public static int age(Player p, LocalDate day) {
        if (p == null || p.getPerson() == null || p.getPerson().getBirthDate() == null || day == null) {
            return 25;
        }
        return (int) ChronoUnit.YEARS.between(p.getPerson().getBirthDate().toLocalDate(), day);
    }

    /** What age does to the wage he can ask (placeholder): youth and veterans ask less */
    public static double ageFactor(int age) {
        if (age <= 19) {
            return 0.85;
        }
        if (age <= 21) {
            return 0.95;
        }
        if (age <= 29) {
            return 1.0;
        }
        if (age <= 31) {
            return 0.95;
        }
        if (age <= 33) {
            return 0.85;
        }
        return 0.75;
    }

    /**
     * The weekly wage he asks for the playing season, in whole shillings inside his band: the band's minimum plus
     * the part of the band his ability reaches, times his age factor. An amateur band (no wage) asks nothing.
     */
    public static BigDecimal askingWage(Player p, ContractBand band, LocalDate day) {
        int min = ContractBand.shillings(band.getSeasonWageMin());
        int max = ContractBand.shillings(band.getSeasonWageMax());
        if (max <= 0) {
            return BigDecimal.ZERO;
        }
        double rel = Math.max(0, Math.min(1, (ability(p) - ABILITY_FLOOR) / ABILITY_SPAN));
        double raw = (min + (max - min) * rel) * ageFactor(age(p, day));
        int s = (int) Math.round(raw);
        return ContractBand.ofShillings(Math.max(min, Math.min(max, s)));
    }

    /** Summer pay for a season wage: the band's summer share of it, whole shillings inside the summer band */
    public static BigDecimal summerFor(BigDecimal wage, ContractBand band) {
        int min = ContractBand.shillings(band.getSummerWageMin());
        int max = ContractBand.shillings(band.getSummerWageMax());
        if (max <= 0 || wage == null || wage.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        int s = (int) Math.round(ContractBand.shillings(wage) * band.summerRatio());
        return ContractBand.ofShillings(Math.max(min, Math.min(max, s)));
    }

    // ------------------------------------------------------------------------------------------------- cost

    /**
     * What one week from {@code day} costs the club for this engagement: nothing outside its dates or when inactive;
     * in the close season (May-August) the summer pay; in the playing season the weekly wage plus an amateur's
     * expenses.
     */
    public static BigDecimal weeklyPay(PlayerContract c, LocalDate day) {
        if (c == null || day == null || !Boolean.TRUE.equals(c.getIsActive())) {
            return BigDecimal.ZERO;
        }
        if (c.getStartDate() != null && day.isBefore(c.getStartDate().toLocalDate())) {
            return BigDecimal.ZERO;
        }
        if (c.getEndDate() != null && day.isAfter(c.getEndDate().toLocalDate())) {
            return BigDecimal.ZERO;
        }
        if (ContractCalendar.isCloseSeason(day)) {
            return c.getSummerWage();
        }
        return c.getWeeklyWage().add(c.getWeeklyExpenses());
    }

    /** The club's weekly bill for its engagements, in the playing season ({@code summer} false) or the close season */
    public static BigDecimal wageBill(Club club, boolean summer) {
        BigDecimal total = BigDecimal.ZERO;
        if (club == null) {
            return total;
        }
        for (PlayerContract c : club.getPlayerContracts()) {
            if (c == null || !Boolean.TRUE.equals(c.getIsActive())) {
                continue;
            }
            total = total.add(summer ? c.getSummerWage() : c.getWeeklyWage().add(c.getWeeklyExpenses()));
        }
        return total;
    }

    // ----------------------------------------------------------------------------------------------- answer

    /**
     * The chance he accepts. An amateur: {@link #AMATEUR_ACCEPTS}. A professional compares the year's pay offered
     * (35 season weeks at the wage, 17 close-season weeks at the summer pay) with what he asks (his asking wage and
     * the summer pay he has now): a fair offer is accepted {@link #FAIR_OFFER} of the time, more for a better one, and
     * the chance falls fast below it. Loyalty and age add a little.
     */
    public static double acceptChance(PlayerContract current, BigDecimal offeredWage, BigDecimal offeredSummer,
            BigDecimal asking, int age) {
        double p;
        if (current != null && current.isAmateur()) {
            p = AMATEUR_ACCEPTS;
        } else {
            double askAnnual = value(asking) * ContractCalendar.SEASON_WEEKS
                    + value(current != null ? current.getSummerWage() : null) * ContractCalendar.SUMMER_WEEKS;
            double offerAnnual = value(offeredWage) * ContractCalendar.SEASON_WEEKS
                    + value(offeredSummer) * ContractCalendar.SUMMER_WEEKS;
            double r = askAnnual <= 0 ? 2 : offerAnnual / askAnnual;
            p = r >= 1 ? FAIR_OFFER + Math.min(0.1, (r - 1) * 0.5) : FAIR_OFFER - (1 - r) * 2.0;
        }
        p += LOYALTY;
        if (age >= VETERAN_AGE) {
            p += VETERAN_WILLING;
        }
        return Math.max(MIN_CHANCE, Math.min(MAX_CHANCE, p));
    }

    /** One seeded answer: true when he accepts ({@code rng} is the fork of this offer) */
    public static boolean accepts(double chance, RandomSource rng) {
        return rng.nextDouble() < chance;
    }

    private static double value(BigDecimal n) {
        return n == null ? 0 : n.doubleValue();
    }
}
