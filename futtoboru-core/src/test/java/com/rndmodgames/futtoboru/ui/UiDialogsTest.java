package com.rndmodgames.futtoboru.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

/**
 * U-09 / U-12, headless: the one-modal-at-a-time bookkeeping and Esc (plain actors stand in for dialogs; no GL
 * needed).
 */
class UiDialogsTest {

    @AfterEach
    void forget() {
        UiDialogs.reset();
    }

    private static Actor shownOn(Group root) {
        Actor dialog = new Actor();
        root.addActor(dialog);
        return dialog;
    }

    @Test
    void openingASecondDialogClosesTheFirst() {
        Group root = new Group();
        Actor first = UiDialogs.track(shownOn(root));
        assertTrue(UiDialogs.isOpen());

        Actor second = UiDialogs.track(shownOn(root));

        assertFalse(first.hasParent(), "first dialog removed");
        assertTrue(second.hasParent());
        assertSame(second, root.getChildren().first());
        assertTrue(UiDialogs.isOpen());
    }

    @Test
    void showingTheSameDialogAgainKeepsIt() {
        Group root = new Group();
        Actor dialog = UiDialogs.track(shownOn(root));
        UiDialogs.track(dialog);
        assertTrue(dialog.hasParent());
    }

    @Test
    void aDialogClosedByItsOwnButtonIsNoLongerOpen() {
        Group root = new Group();
        Actor dialog = UiDialogs.track(shownOn(root));
        dialog.remove();
        assertFalse(UiDialogs.isOpen());
        assertFalse(UiDialogs.isOpenOn(null));
    }

    @Test
    void closeOpenRemovesIt() {
        Group root = new Group();
        Actor dialog = UiDialogs.track(shownOn(root));
        UiDialogs.closeOpen();
        assertFalse(dialog.hasParent());
        assertFalse(UiDialogs.isOpen());
        UiDialogs.closeOpen(); // nothing open: no-op
    }

    // ---- U-12: Esc is the dialog's Cancel / Close and stops there ----

    /** Fires a key-down on {@code target} the way the stage does (to the keyboard focus, bubbling to the dialog). */
    private static InputEvent key(Actor target, int keycode) {
        InputEvent event = new InputEvent();
        event.setType(InputEvent.Type.keyDown);
        event.setKeyCode(keycode);
        target.fire(event);
        return event;
    }

    @Test
    void escapeRunsTheCancelActionAndIsHandledByTheDialog() {
        Group root = new Group();
        Actor dialog = UiDialogs.track(shownOn(root));
        AtomicInteger cancelled = new AtomicInteger();
        UiDialogs.closeOnEscape(dialog, cancelled::incrementAndGet);

        InputEvent other = key(dialog, Keys.ENTER);
        assertEquals(0, cancelled.get(), "only Esc");
        assertFalse(other.isHandled());

        InputEvent esc = key(dialog, Keys.ESCAPE);
        assertEquals(1, cancelled.get());
        // handled: Stage.keyDown returns true, so the screen's own Esc (the top-bar Back) never runs
        assertTrue(esc.isHandled());
        assertTrue(esc.isStopped(), "does not reach the screen behind the dialog");
    }

    @Test
    void escapeInAWidgetInsideTheDialogStillCancels() {
        Group root = new Group();
        Group dialog = new Group();
        root.addActor(dialog);
        Actor nameField = new Actor(); // the save name field has the keyboard
        dialog.addActor(nameField);
        AtomicBoolean fieldSawEsc = new AtomicBoolean();
        nameField.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                fieldSawEsc.set(true);
                return true;
            }
        });
        AtomicInteger cancelled = new AtomicInteger();
        UiDialogs.closeOnEscape(dialog, cancelled::incrementAndGet);

        assertTrue(key(nameField, Keys.ESCAPE).isHandled());
        assertEquals(1, cancelled.get());
        assertFalse(fieldSawEsc.get(), "the dialog takes Esc before the widgets inside it");
    }

    @Test
    void settingTheEscapeActionAgainReplacesIt() {
        Group root = new Group();
        Actor dialog = shownOn(root);
        AtomicInteger first = new AtomicInteger();
        AtomicInteger second = new AtomicInteger();
        UiDialogs.closeOnEscape(dialog, first::incrementAndGet);
        UiDialogs.closeOnEscape(dialog, second::incrementAndGet);
        key(dialog, Keys.ESCAPE);
        assertEquals(0, first.get());
        assertEquals(1, second.get());
        assertEquals(1, dialog.getCaptureListeners().size, "one Esc listener per dialog");
    }

    @Test
    void escapeOnAQuestionAnswersCancelAndClosesIt() {
        Group root = new Group();
        Actor question = UiDialogs.track(shownOn(root));
        List<Integer> answers = new ArrayList<>();
        Integer saveFirst = 1;
        Integer discard = 2;
        Integer cancel = 3;
        UiDialogs.escapeAnswers(question, cancel, answers::add);

        key(question, Keys.ESCAPE);

        assertEquals(List.of(cancel), answers, "Esc is Cancel, never Save First or Do Not Save");
        assertFalse(answers.contains(saveFirst) || answers.contains(discard));
        assertFalse(question.hasParent(), "closed");
        assertFalse(UiDialogs.isOpen());
    }

    @Test
    void anAnswerThatOpensAnotherDialogKeepsIt() {
        Group root = new Group();
        Actor question = UiDialogs.track(shownOn(root));
        Actor[] reopened = new Actor[1];
        // Cancel on "Delete save?" brings the load list back (it replaces the question)
        UiDialogs.escapeAnswers(question, Boolean.FALSE, answer -> reopened[0] = UiDialogs.track(shownOn(root)));

        key(question, Keys.ESCAPE);

        assertFalse(question.hasParent());
        assertTrue(reopened[0].hasParent(), "the dialog the answer opened stays");
        assertTrue(UiDialogs.isOpen());
    }

    @Test
    void escapeOnAClosedDialogDoesNothing() {
        Group root = new Group();
        Actor dialog = shownOn(root);
        AtomicInteger cancelled = new AtomicInteger();
        UiDialogs.closeOnEscape(dialog, cancelled::incrementAndGet);
        dialog.remove();
        key(dialog, Keys.ESCAPE);
        assertEquals(0, cancelled.get());
    }
}
