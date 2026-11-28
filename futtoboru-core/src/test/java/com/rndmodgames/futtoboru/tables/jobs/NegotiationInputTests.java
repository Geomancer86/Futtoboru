package com.rndmodgames.futtoboru.tables.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

/** J-01: the negotiation screen's salary and contract fields (V-01 saw the raw BigDecimal "12.495"). */
class NegotiationInputTests {

    @Test
    void theSalaryFieldStartsWithPoundsAndPence() {
        assertEquals("12.50", NegotiationInput.salaryText(new BigDecimal("12.495")));
        assertEquals("1234.00", NegotiationInput.salaryText(new BigDecimal("1234")));
        assertEquals("1.20", NegotiationInput.salaryText(new BigDecimal(1.2)));
        assertEquals("0.00", NegotiationInput.salaryText(null));
    }

    @Test
    void salariesAreReadAsMoneyAndRoundedToThePenny() {
        assertEquals(new BigDecimal("12.50"), NegotiationInput.parseSalary("12.50"));
        assertEquals(new BigDecimal("12.50"), NegotiationInput.parseSalary("12.495"));
        assertEquals(new BigDecimal("12.00"), NegotiationInput.parseSalary(" 12 "));
        assertEquals(new BigDecimal("1234.50"), NegotiationInput.parseSalary("£1,234.5"));
        assertEquals(new BigDecimal("12.50"), NegotiationInput.parseSalary("12,50"), "a decimal comma");
        assertEquals(new BigDecimal("1234.00"), NegotiationInput.parseSalary("1.234,00"));
    }

    @Test
    void nonsenseSalariesAreRefused() {
        assertNull(NegotiationInput.parseSalary(""));
        assertNull(NegotiationInput.parseSalary(null));
        assertNull(NegotiationInput.parseSalary("abc"));
        assertNull(NegotiationInput.parseSalary("0"));
        assertNull(NegotiationInput.parseSalary("0.004"), "rounds to nothing");
        assertNull(NegotiationInput.parseSalary("-5"));
        assertNull(NegotiationInput.parseSalary("1e9"));
        assertNull(NegotiationInput.parseSalary("12.5.0"));
        assertNull(NegotiationInput.parseSalary("1000000"), "above the most a club could pay");
    }

    @Test
    void contractLengthsAreWholeMonthsWithinFiveYears() {
        assertEquals(12, NegotiationInput.parseContractMonths("12"));
        assertEquals(1, NegotiationInput.parseContractMonths(" 1 "));
        assertEquals(60, NegotiationInput.parseContractMonths("60"));
        assertNull(NegotiationInput.parseContractMonths("0"));
        assertNull(NegotiationInput.parseContractMonths("61"));
        assertNull(NegotiationInput.parseContractMonths("12.5"));
        assertNull(NegotiationInput.parseContractMonths("twelve"));
        assertNull(NegotiationInput.parseContractMonths(""));
    }
}
