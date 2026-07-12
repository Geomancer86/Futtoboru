package com.rndmodgames.futtoboru.system.loaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.harness.HeadlessGdxTestSupport;

class MedicalEraRegistryTest {

    @BeforeEach
    void setUp() {
        HeadlessGdxTestSupport.ensureInitialized();
        MedicalEraRegistry.reloadFromMod();
    }

    @Test
    void resolves1888ToMe1() {
        assertEquals("ME-1", MedicalEraRegistry.resolve(LocalDate.of(1888, 9, 8)).getId());
    }

    @Test
    void calendar1993ResolvesToMe6ButReleaseCeilingClampsToMe1() {
        assertEquals("ME-6", MedicalEraRegistry.resolveCalendarEra(LocalDate.of(1993, 8, 1)).getId());
        assertEquals("ME-1", MedicalEraRegistry.resolve(LocalDate.of(1993, 8, 1)).getId());
    }

    @Test
    void resolves1906CalendarToMe2ButClampedForR1888Release() {
        assertEquals("ME-2", MedicalEraRegistry.resolveCalendarEra(LocalDate.of(1906, 1, 1)).getId());
        assertEquals("ME-1", MedicalEraRegistry.resolve(LocalDate.of(1906, 1, 1)).getId());
    }
}
