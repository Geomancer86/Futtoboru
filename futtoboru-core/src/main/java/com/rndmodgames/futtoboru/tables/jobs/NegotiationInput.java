package com.rndmodgames.futtoboru.tables.jobs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

/**
 * J-01: the negotiation screen's two fields, without UI (tested headless). The salary field showed the raw
 * BigDecimal ("12.495") and any text went to {@code new BigDecimal}; now it starts with pounds and pence and the
 * input is read as money: an optional £, grouping commas or a decimal comma, rounded to the penny.
 */
public final class NegotiationInput {

    /** The most a counter-offer may ask (a typing slip, not a wage). */
    public static final BigDecimal MAX_SALARY = new BigDecimal("99999.99");

    /** Contract lengths a counter-offer may ask, in months. */
    public static final int MIN_CONTRACT_MONTHS = 1;
    public static final int MAX_CONTRACT_MONTHS = 60;

    private static final Pattern AMOUNT = Pattern.compile("[0-9][0-9.,]*");
    private static final Pattern MONTHS = Pattern.compile("[0-9]{1,3}");

    private NegotiationInput() {
    }

    /** @return the text the salary field starts with: pounds and pence, no symbol ("12.50") */
    public static String salaryText(BigDecimal salary) {
        return (salary != null ? salary : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * @return the salary typed, rounded to the penny, or null when it is not an amount of money above nothing and
     *         up to {@link #MAX_SALARY}
     */
    public static BigDecimal parseSalary(String text) {
        if (text == null) {
            return null;
        }
        String s = text.replace("£", "").replace(" ", "").replace(" ", "").trim();
        if (!AMOUNT.matcher(s).matches()) {
            return null;
        }
        String digits = normalise(s);
        if (digits == null) {
            return null;
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(digits).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
        if (amount.signum() <= 0 || amount.compareTo(MAX_SALARY) > 0) {
            return null;
        }
        return amount;
    }

    /**
     * Grouping and decimal marks to a plain number: with both marks the last one is the decimal mark; a lone comma
     * followed by one or two digits is a decimal comma, else a thousands comma; a lone point is the decimal point.
     */
    private static String normalise(String s) {
        int lastComma = s.lastIndexOf(',');
        int lastPoint = s.lastIndexOf('.');
        if (lastComma >= 0 && lastPoint >= 0) {
            char decimal = lastComma > lastPoint ? ',' : '.';
            char grouping = decimal == ',' ? '.' : ',';
            String whole = s.substring(0, Math.max(lastComma, lastPoint)).replace(String.valueOf(grouping), "");
            String fraction = s.substring(Math.max(lastComma, lastPoint) + 1);
            return whole.indexOf(decimal) >= 0 || fraction.isEmpty() ? null : whole + "." + fraction;
        }
        if (lastComma >= 0) {
            int digitsAfter = s.length() - lastComma - 1;
            if (s.indexOf(',') == lastComma && digitsAfter >= 1 && digitsAfter <= 2) {
                return s.replace(',', '.');
            }
            return s.replace(",", "");
        }
        if (lastPoint >= 0 && s.indexOf('.') != lastPoint) {
            return null; // "12.5.0"
        }
        return s;
    }

    /** @return the whole months typed, or null outside {@link #MIN_CONTRACT_MONTHS}..{@link #MAX_CONTRACT_MONTHS} */
    public static Integer parseContractMonths(String text) {
        if (text == null) {
            return null;
        }
        String s = text.trim();
        if (!MONTHS.matcher(s).matches()) {
            return null;
        }
        int months = Integer.parseInt(s);
        return months >= MIN_CONTRACT_MONTHS && months <= MAX_CONTRACT_MONTHS ? months : null;
    }
}
