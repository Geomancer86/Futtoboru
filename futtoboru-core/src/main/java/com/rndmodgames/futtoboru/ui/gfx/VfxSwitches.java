// Adapted from DarkBlade (VfxSwitches), ported under D23/D24.
package com.rndmodgames.futtoboru.ui.gfx;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The player's switches for the viewer's visual modules (docs/design/MATCH_VIEWER_PLAN.md §2.4, rule 2).
 *
 * <ul>
 * <li>One switch per module (the module names of {@code gfx/quality.json}), a master switch over all of them, and
 * {@code -Dfuttoboru.gfx.off=true}, which turns all of them off whatever the player chose (the "safe" profile).</li>
 * <li>{@link GraphicsQuality#on(String)} asks here first, then the module's {@code on} knob of the file, so the file
 * turns a module off without recompiling and the player turns it off in the options.</li>
 * <li>Only drawing: nothing in the simulation reads a switch (the state hash is the same with every switch on or
 * off).</li>
 * <li>{@link #on(String)} runs every frame in the draw passes: a scan of about ten strings, no allocation.</li>
 * </ul>
 *
 * One instance per {@link GraphicsQuality}, no statics, so tests never leak switches into each other.
 */
public final class VfxSwitches {

    /** {@code -Dfuttoboru.gfx.off=true} turns every module off. */
    public static final String OFF_PROPERTY = "futtoboru.gfx.off";

    private final String[] names;
    private final boolean[] modules;
    private boolean forcedOff;
    private boolean master = true;

    /** Switches for these modules, all on; the kill flag read from the system property. */
    public VfxSwitches(Collection<String> moduleNames) {
        this(moduleNames, Boolean.getBoolean(OFF_PROPERTY));
    }

    /** Switches for these modules, all on, with the kill flag given (tests). */
    public VfxSwitches(Collection<String> moduleNames, boolean forcedOff) {
        this.names = moduleNames.toArray(new String[0]);
        this.modules = new boolean[names.length];
        Arrays.fill(modules, true);
        this.forcedOff = forcedOff;
    }

    private int index(String module) {
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(module)) {
                return i;
            }
        }
        return -1;
    }

    /** Whether the player and the command line let the module draw; true for a name that has no switch. */
    public boolean on(String module) {
        int index = index(module);
        return index < 0 || (!forcedOff && master && modules[index]);
    }

    /** The modules with a switch, in file order (the order of the options panel). */
    public List<String> modules() {
        return Collections.unmodifiableList(Arrays.asList(names));
    }

    public boolean hasSwitch(String module) {
        return index(module) >= 0;
    }

    /** The master switch, as the player left it. */
    public boolean master() {
        return master;
    }

    /** The module's own switch, as the player left it (the master switch and the command line aside). */
    public boolean module(String module) {
        int index = index(module);
        return index < 0 || modules[index];
    }

    /** Whether {@code -Dfuttoboru.gfx.off} turned every module off. */
    public boolean forcedOff() {
        return forcedOff;
    }

    public void setMaster(boolean on) {
        master = on;
    }

    /**
     * @throws IllegalArgumentException for a module without a switch
     */
    public void setModule(String module, boolean on) {
        int index = index(module);
        if (index < 0) {
            throw new IllegalArgumentException("no switch for " + module + ", the modules are " + modules());
        }
        modules[index] = on;
    }

    /** Every switch on and the command line read again (restore defaults). */
    public void reset() {
        forcedOff = Boolean.getBoolean(OFF_PROPERTY);
        master = true;
        Arrays.fill(modules, true);
    }
}
