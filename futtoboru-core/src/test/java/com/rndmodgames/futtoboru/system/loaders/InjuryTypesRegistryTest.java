package com.rndmodgames.futtoboru.system.loaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.InjurySeverity;

class InjuryTypesRegistryTest {

    @Test
    void parseLinesReadsModFormat() {
        List<String> lines = List.of(
                "# comment",
                "strain | Strain | 1 | 2 | MINOR | 40",
                "knee | Knee injury | 3 | 6 | MODERATE | 10");
        var parsed = InjuryTypesRegistry.parseLines(lines);
        assertEquals(2, parsed.size());
        assertEquals("strain", parsed.get(0).getId());
        assertEquals(InjurySeverity.MINOR, parsed.get(0).getSeverity());
        assertEquals("Knee injury", parsed.get(1).getDisplayName());
    }

    @Test
    void defaultTypesAvailableWithoutModFile() {
        assertFalse(InjuryTypesRegistry.getTypes().isEmpty());
        assertEquals("strain", InjuryTypesRegistry.getTypes().get(0).getId());
    }
}
