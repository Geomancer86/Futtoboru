package com.rndmodgames.futtoboru.engine.tactics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.tactics.FormationTemplate;

class FormationRegistryTest {

    @Test
    void defaultTemplateIsTwoThreeFive1888() {
        FormationTemplate template = FormationRegistry.getDefaultTemplate();
        assertNotNull(template);
        assertEquals("2-3-5-1888", template.getId());
        assertEquals(11, template.slotCount());
    }

    @Test
    void allTemplatesHaveElevenSlots() {
        assertEquals(3, FormationRegistry.getAllTemplates().size());
        for (FormationTemplate template : FormationRegistry.getAllTemplates()) {
            assertEquals(11, template.getSlots().size(), template.getId());
        }
    }

    @Test
    void era1888TemplatesLoadById() {
        assertNotNull(FormationRegistry.getTemplate("2-2-6-1888"));
        assertNotNull(FormationRegistry.getTemplate("3-2-5-1888"));
    }
}
