package com.rndmodgames.futtoboru.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.harness.HeadlessGdxTestSupport;
import com.rndmodgames.futtoboru.system.loaders.MedicalEraRegistry;

/**
 * A-8 — production mod files load with squads and 1888 competition data.
 */
@Tag("r1888")
class ProductionModLoadIT {

    @BeforeEach
    void setUp() {
        HeadlessGdxTestSupport.ensureInitialized();
        DatabaseLoader.resetDatabaseLoaderInstance();
        DatabaseLoader.getInstance();
        MedicalEraRegistry.reloadFromMod();
    }

    @Test
    void databaseLoads1888ClubsWithPlayers() {
        var clubs = DatabaseLoader.getInstance().getClubsByCountry(DatabaseLoader.getCountryById(1000L));
        assertNotNull(clubs);
        assertFalse(clubs.isEmpty());
        long withPlayers = clubs.stream().filter(c -> c.getPlayers() != null && !c.getPlayers().isEmpty()).count();
        assertTrue(withPlayers >= 10, "Expected at least 10 clubs with squads, got " + withPlayers);
    }

    @Test
    void medicalEraModLoadsFor1888() {
        assertEquals("ME-1", MedicalEraRegistry.resolve(java.time.LocalDate.of(1888, 9, 1)).getId());
    }
}
