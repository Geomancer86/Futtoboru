package com.rndmodgames.futtoboru.ui;

import com.kotcrab.vis.ui.widget.VisLabel;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * "Step X of N" indicator shared by the three New Game wizard screens (Season/Manager, Countries,
 * Overview). Added 2026-07-19 per the pre-game menus UX audit — the wizard previously gave the player
 * no indication of how many steps remained.
 */
public final class WizardStepLabel {

    private WizardStepLabel() {
    }

    public static VisLabel of(int step, int totalSteps) {
        String text = LanguageModLoader.getValue("wizard_step_of")
                .replace("{step}", String.valueOf(step))
                .replace("{total}", String.valueOf(totalSteps));
        VisLabel label = new VisLabel(text);
        label.setColor(0.7f, 0.7f, 0.7f, 1f);
        return label;
    }
}
