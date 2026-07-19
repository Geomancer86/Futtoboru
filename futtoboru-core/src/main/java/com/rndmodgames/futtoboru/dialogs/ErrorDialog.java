package com.rndmodgames.futtoboru.dialogs;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Small reusable OK-only error/failure dialog.
 *
 * <p>Added 2026-07-19 per the pre-game menus UX audit: the highest-value single finding was that
 * {@code NewGameOverviewScreen}'s Start Game failure path only logged to console and otherwise did
 * nothing visible — the player just sat looking at an unresponsive button with no explanation. This
 * widget, and the corresponding failed-load path in {@code LoadGameDialog}, replace that silence.
 *
 * <p>{@code title} and {@code message} are already-resolved display strings; callers own any
 * {@link LanguageModLoader} lookups.
 */
public class ErrorDialog extends VisDialog {

    public ErrorDialog(String title, String message) {
        super(title);

        VisLabel messageLabel = new VisLabel(message);
        messageLabel.setWrap(true);

        add(messageLabel).width(360).pad(10);

        VisTextButton okButton = new VisTextButton(LanguageModLoader.getValue("confirm_ok"));
        okButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                hide();
            }
        });

        row();
        add(okButton).pad(5);

        closeOnEscape();
    }
}
