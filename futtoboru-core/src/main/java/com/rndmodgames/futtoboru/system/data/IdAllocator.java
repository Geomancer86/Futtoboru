package com.rndmodgames.futtoboru.system.data;

import java.io.Serializable;

/**
 * Hands out runtime ids (P2-4, decision D11).
 *
 * <ul>
 *   <li>Data records get theirs at load time from a data allocator that starts at {@link #DATA_START}; they stay
 *       below {@link #GAME_START} and are the same on every run for the same packs.</li>
 *   <li>Every object made during play (generated people, the manager, matches, editions, messages, jobs...) gets
 *       its id from the game's allocator, which lives in the {@code SaveGame}, starts at {@link #GAME_START} and is
 *       saved with the game, so an id is never handed out twice.</li>
 * </ul>
 */
public class IdAllocator implements Serializable {

    private static final long serialVersionUID = 1L;

    /** First runtime id of a data record. */
    public static final long DATA_START = 1L;

    /** First runtime id of an object created during play; every data id is below it. */
    public static final long GAME_START = 1_000_000L;

    /** The next id to hand out (the only state, so it serializes as one number). */
    private long next = GAME_START;

    /** Required for de-serializing; starts at {@link #GAME_START}. */
    public IdAllocator() {
    }

    public IdAllocator(long start) {
        this.next = start;
    }

    /** @return a new id, never handed out before by this allocator */
    public long nextId() {
        return next++;
    }

    /** @return the id the next call to {@link #nextId()} will return */
    public long peek() {
        return next;
    }

    /** Makes sure the next id is above {@code id} (used when an object with a known id joins the game). */
    public void reserveUpTo(long id) {
        if (id >= next) {
            next = id + 1;
        }
    }

    @Override
    public String toString() {
        return "IdAllocator[next=" + next + "]";
    }
}
