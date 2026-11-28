package com.rndmodgames.futtoboru.ui;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kotcrab.vis.ui.util.dialog.ConfirmDialogListener;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.VisDialog;

/**
 * One modal at a time (U-09). Every dialog goes through here: opening a dialog closes the one that is open (at once,
 * no fade), so two modals never stack. A dialog that opens another from its own button (Save First, an error after
 * a failed load) simply replaces itself.
 * <p>
 * <b>Esc</b> (U-12) is the dialog's Cancel / Close / OK, never a destructive choice: {@link #confirm} takes the value
 * Esc answers with, {@link #info} and {@link #show} close. The key stops at the dialog, so the screen behind it (the
 * top-bar Back) does not see it.
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

    /** Shows {@code dialog} as the only modal; Esc closes it unless the dialog set its own Esc action. */
    public static <T extends VisDialog> T show(Stage stage, T dialog) {
        if (escapeAction(dialog) == null) {
            closeOnEscape(dialog, () -> close(dialog));
        }
        track(dialog);
        dialog.show(stage);
        return dialog;
    }

    /** Message with an OK button; Esc is OK. */
    public static VisDialog info(Stage stage, String title, String text) {
        closeOpen();
        VisDialog dialog = Dialogs.showOKDialog(stage, title, text);
        closeOnEscape(dialog, () -> close(dialog));
        focus(stage, dialog);
        return track(dialog);
    }

    /**
     * Question with one button per {@code buttons} entry; {@code listener} gets the matching {@code returns} value.
     * Esc answers {@code cancel} (the Cancel button's value: never a destructive one) and closes the question.
     */
    public static <T> VisDialog confirm(Stage stage, String title, String text, String[] buttons, T[] returns,
            T cancel, ConfirmDialogListener<T> listener) {
        closeOpen();
        VisDialog dialog = Dialogs.showConfirmDialog(stage, title, text, buttons, returns, listener);
        escapeAnswers(dialog, cancel, listener);
        focus(stage, dialog);
        return track(dialog);
    }

    /** Esc on {@code dialog} gives {@code listener} the {@code cancel} answer, then closes the dialog. */
    static <T> void escapeAnswers(Actor dialog, T cancel, ConfirmDialogListener<T> listener) {
        closeOnEscape(dialog, () -> {
            listener.result(cancel);
            close(dialog); // nothing to do when the answer already replaced it with another dialog
        });
    }

    /**
     * Esc on {@code dialog} (or on any widget inside it, such as a text field) runs {@code action}: the dialog's
     * Cancel / Close. The key is handled there, so it never reaches the screen behind the dialog. Calling it again
     * replaces the action.
     */
    public static <T extends Actor> T closeOnEscape(T dialog, Runnable action) {
        EscapeListener existing = escapeAction(dialog);
        if (existing != null) {
            existing.action = action;
        } else {
            // a capture listener: it runs before the widgets inside (a text field) and before VisUI's own Esc
            dialog.addCaptureListener(new EscapeListener(action));
        }
        return dialog;
    }

    /** @return the Esc listener on {@code dialog}, or null */
    static EscapeListener escapeAction(Actor dialog) {
        for (EventListener listener : dialog.getCaptureListeners()) {
            if (listener instanceof EscapeListener) {
                return (EscapeListener) listener;
            }
        }
        return null;
    }

    /** VisUI's ready-made dialogs are added without the keyboard: give it to them so Esc reaches them. */
    private static void focus(Stage stage, Actor dialog) {
        if (stage != null) {
            stage.setKeyboardFocus(dialog);
        }
    }

    /** Hides a dialog (fade) or removes any other actor; nothing when it is no longer on a stage. */
    static void close(Actor dialog) {
        if (!dialog.hasParent()) {
            return;
        }
        if (dialog instanceof VisDialog) {
            ((VisDialog) dialog).hide();
        } else {
            dialog.remove();
        }
    }

    /** Runs the dialog's Esc action and stops the key there (U-12). */
    static final class EscapeListener extends InputListener {

        Runnable action;

        EscapeListener(Runnable action) {
            this.action = action;
        }

        @Override
        public boolean keyDown(InputEvent event, int keycode) {
            if (keycode != Keys.ESCAPE) {
                return false;
            }
            event.stop(); // not the top-bar Back, nor a parent's Esc
            if (event.getListenerActor() != null && event.getListenerActor().hasParent() && action != null) {
                action.run();
            }
            return true;
        }
    }

    /** Forgets the open dialog without touching it (tests, screen changes). */
    static synchronized void reset() {
        current = null;
    }
}
