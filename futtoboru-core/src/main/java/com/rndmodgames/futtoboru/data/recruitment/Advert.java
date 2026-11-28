package com.rndmodgames.futtoboru.data.recruitment;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * R-05 (docs/design/RECRUITMENT_AND_TRIALS.md §3.3): an advert a club placed in a paper ("WANTED, a first-class
 * centre..."). Its cost is booked once, when placed; the replies are {@link ShortlistEntry} records of source
 * {@code advert} that arrive by letter over the next days. Saved in {@code SaveGame.adverts} (optional, saveVersion 3).
 */
public class Advert implements Serializable {

    private static final long serialVersionUID = 1L;

    /** the wanted men: any line */
    public static final String ANY = "any";

    private Long club;
    /** the paper's data id ({@code recruitment/newspapers.json}) */
    private String newspaper;
    /** the line wanted: {@code Position.Line} name, or {@link #ANY} */
    private String wanted;
    private LocalDateTime placed;
    private Integer costPence;
    /** how many replies it will bring */
    private Integer replies;

    public Advert() {
    }

    public Advert(Long club, String newspaper, String wanted, LocalDateTime placed, int costPence, int replies) {
        this.club = club;
        this.newspaper = newspaper;
        this.wanted = wanted;
        this.placed = placed;
        this.costPence = costPence;
        this.replies = replies;
    }

    public Long getClub() {
        return club;
    }

    public String getNewspaper() {
        return newspaper;
    }

    public String getWanted() {
        return wanted != null ? wanted : ANY;
    }

    public LocalDateTime getPlaced() {
        return placed;
    }

    public int getCostPence() {
        return costPence != null ? costPence : 0;
    }

    public int getReplies() {
        return replies != null ? replies : 0;
    }
}
