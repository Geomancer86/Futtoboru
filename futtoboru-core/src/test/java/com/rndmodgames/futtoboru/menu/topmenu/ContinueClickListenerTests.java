package com.rndmodgames.futtoboru.menu.topmenu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

/**
 * P2-1: pressing Continue must not advance two days on a double-click.
 * Drives the real Continue click listener with scene2d input events on a plain actor
 * (no GL, skin or Stage needed). Before the fix, any second click arriving after the advance had
 * finished started another one; doubleClickAdvancesOnceEvenAfterProcessingFinished covers that.
 */
class ContinueClickListenerTests {

    private Actor button;
    private AtomicInteger advances;
    private AtomicBoolean blocked;
    private MainGameMenuTable.ContinueClickListener listener;

    @BeforeEach
    void setUp() {
        button = new Actor();
        button.setSize(100, 30);
        advances = new AtomicInteger();
        blocked = new AtomicBoolean(false);
        listener = new MainGameMenuTable.ContinueClickListener(blocked::get, advances::incrementAndGet);
    }

    /**
     * Delivers a touch event straight to the listener, the way Stage does (touchDown on hit, touchUp
     * through touch focus), without a Stage (which would need GL for its SpriteBatch).
     */
    private void fire(InputEvent.Type type, float x, float y) {
        InputEvent event = new InputEvent();
        event.setType(type);
        event.setStageX(x);
        event.setStageY(y);
        event.setPointer(0);
        event.setButton(Input.Buttons.LEFT);
        event.setTarget(button);
        event.setListenerActor(button);
        if (type == InputEvent.Type.touchDown) {
            listener.touchDown(event, x, y, 0, Input.Buttons.LEFT);
        } else {
            listener.touchUp(event, x, y, 0, Input.Buttons.LEFT);
        }
    }

    private void click() {
        fire(InputEvent.Type.touchDown, 10, 10);
        fire(InputEvent.Type.touchUp, 10, 10);
    }

    @Test
    void singleClickAdvancesOnce() {
        click();
        assertEquals(1, advances.get());
    }

    @Test
    void doubleClickAdvancesOnceEvenAfterProcessingFinished() {
        // the advance finishes instantly here (blocked stays false), the worst case for the old cooldown
        click();
        click();
        assertEquals(1, advances.get());
    }

    @Test
    void thirdQuickClickStartsANewContinue() {
        click();
        click();
        click();
        assertEquals(2, advances.get());
    }

    @Test
    void clicksWhileProcessingAreIgnored() {
        blocked.set(true);
        click();
        listener.setTapCount(0);
        click();
        assertEquals(0, advances.get());
    }

    @Test
    void separateClicksEachAdvance() throws InterruptedException {
        listener.setTapCountInterval(0.02f);
        click();
        Thread.sleep(60);
        click();
        Thread.sleep(60);
        click();
        assertEquals(3, advances.get());
    }

    @Test
    void releaseOutsideTheButtonDoesNotAdvance() {
        fire(InputEvent.Type.touchDown, 10, 10);
        fire(InputEvent.Type.touchUp, 500, 500);
        assertEquals(0, advances.get());
    }
}
