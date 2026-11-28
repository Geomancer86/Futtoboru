package com.rndmodgames.futtoboru.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;

/** U-09, headless: the one-modal-at-a-time bookkeeping (plain actors stand in for dialogs; no GL needed). */
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
}
