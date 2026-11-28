package com.rndmodgames.futtoboru.data.language;

import java.io.Serializable;

/**
 * R-11a (design §10.1): how well a person handles one language. Saved with the person (saves v3).
 * <ul>
 * <li>{@code speak} and {@code understand}: 0 none, 1 a few words, 2 basic, 3 good, 4 native.</li>
 * <li>{@code readWrite}: 0 none, 1 signs his name, 2 reads slowly, 3 reads well, 4 writes well.</li>
 * </ul>
 * Comes only from his upbringing (birth area, birth year, parents, schooling), never from football ability.
 */
public class LanguageSkill implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int NONE = 0;
    public static final int MAX = 4;

    /** Language data id ({@code languages.json}). */
    private String language;
    private int speak;
    private int understand;
    private int readWrite;

    public LanguageSkill() {
        // for the save reader
    }

    public LanguageSkill(String language, int speak, int understand, int readWrite) {
        this.language = language;
        this.speak = clamp(speak);
        this.understand = clamp(understand);
        this.readWrite = clamp(readWrite);
    }

    static int clamp(int level) {
        return Math.max(NONE, Math.min(MAX, level));
    }

    public String getLanguage() {
        return language;
    }

    public int getSpeak() {
        return speak;
    }

    public int getUnderstand() {
        return understand;
    }

    public int getReadWrite() {
        return readWrite;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LanguageSkill)) {
            return false;
        }
        LanguageSkill s = (LanguageSkill) o;
        return java.util.Objects.equals(language, s.language) && speak == s.speak && understand == s.understand
                && readWrite == s.readWrite;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(language, speak, understand, readWrite);
    }

    @Override
    public String toString() {
        return language + " " + speak + "/" + understand + "/" + readWrite;
    }
}
