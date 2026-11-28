package com.rndmodgames.futtoboru.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kotcrab.vis.ui.util.dialog.ConfirmDialogListener;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.VisDialog;

/**
 * One modal at a time (U-09). Every dialog goes through here: opening a dialog closes the one that is open (at once,
 * no fade), so two modals never stack. A dialog that opens another from its own button (Save First, an error after
 * a failed load) simply replaces itself.
 */
public final class UiDialogs {

    private static Actor current;

    private UiDialogs() {
    }

    /** @return true while the last dialog shown through here is still on a stage */
    public static synchronized boolean isOpen() {
        return current != null && current.hasParent();
    }

    /** @return true while a dialog shown through here is open on {@code stage} */
    public static synchronized boolean isOpenOn(Stage stage) {
        return isOpen() && stage != null && current.getStage() == stage;
    }

    /** Closes the open dialog, if any (no fade). */
    public static synchronized void closeOpen() {
        if (isOpen()) {
            current.remove();
        }
        current = null;
    }

    /**
     * Closes the open dialog unless it is {@code next}, and remembers {@code next} as the open one. A dialog class
     * that shows itself ({@code super.show(stage)}) calls this first.
     */
    public static synchronized <T extends Actor> T track(T next) {
        if (current != next) {
            closeOpen();
        }
        current = next;
        return next;
    }

    /** Shows {@code dialog} as the only modal. */
    public static <T extends VisDialog> T show(Stage stage, T dialog) {
        track(dialog);
        dialog.show(stage);
        return dialog;
    }

    /** Message with an OK button. */
    public static VisDialog info(Stage stage, String title, String text) {
        closeOpen();
        return track(Dialogs.showOKDialog(stage, title, text));
    }

    /** Question with one button per {@code buttons} entry; {@code listener} gets the matching {@code returns} value. */
    public static <T> VisDialog confirm(Stage stage, String title, String text, String[] buttons, T[] returns,
            ConfirmDialogListener<T> listener) {
        closeOpen();
        return track(Dialogs.showConfirmDialog(stage, title, text, buttons, returns, listener));
    }

    /** Forgets the open dialog without touching it (tests, screen changes). */
    static synchronized void reset() {
        current = null;
    }
}
