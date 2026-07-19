package com.rndmodgames.futtoboru.dialogs;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Small reusable Yes/Cancel confirmation dialog.
 *
 * <p>Added 2026-07-19 per the pre-game menus UX audit
 * ({@code docs/planning/ANALYSIS_PREGAME_MENUS_UX_AUDIT_V1.md}): Quit, manager/save overwrite, and
 * save/manager delete all previously fired with zero confirmation. This is the single shared widget
 * every one of those call sites should use instead of re-implementing a Yes/Cancel dialog each time.
 *
 * <p>{@code title} and {@code message} are the already-resolved display strings — callers own any
 * {@link LanguageModLoader} lookups and string interpolation (e.g. "Overwrite manager 'Smith'?"), this
 * class only owns the button plumbing.
 */
public class ConfirmDialog extends VisDialog {

    public ConfirmDialog(String title, String message, Runnable onConfirm) {
        this(title, message, onConfirm, null);
    }

    public ConfirmDialog(String title, String message, Runnable onConfirm, Runnable onCancel) {
        super(title);

        VisLabel messageLabel = new VisLabel(message);
        messageLabel.setWrap(true);

        add(messageLabel).width(360).pad(10).colspan(2);

        VisTextButton cancelButton = new VisTextButton(LanguageModLoader.getValue("cancel"));
        VisTextButton confirmButton = new VisTextButton(LanguageModLoader.getValue("confirm_yes"));

        cancelButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                hide();
                if (onCancel != null) {
                    onCancel.run();
                }
            }
        });

        confirmButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                hide();
                if (onConfirm != null) {
                    onConfirm.run();
                }
            }
        });

        row();
        add(cancelButton).pad(5).align(Align.right);
        add(confirmButton).pad(5).align(Align.left);

        closeOnEscape();
    }
}
