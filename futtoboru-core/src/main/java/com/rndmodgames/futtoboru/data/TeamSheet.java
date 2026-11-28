package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A club's team sheet (G-01, D19-6, D30 A1): the eleven men in their places, the shape, the club's style and the
 * captain. A club has a <b>standing</b> sheet ({@link Club#getTeamSheet()}: the player edits his on the Squad and
 * Match Preview screens; an AI club's is the last one it played), and every played match keeps the sheet each side
 * used, <b>frozen at kick-off</b> ({@link Match#getHomeSheet()} / {@link Match#getAwaySheet()}), so history shows who
 * played. 1888 rules (D7): no substitutes, so the eleven at kick-off are the side for the whole match; a club with
 * fewer than eleven fit men plays short (empty places stay {@code null}).
 *
 * <p><b>Contract for the match engines.</b> The instant result ({@code engine.simulation.MatchSimulator}) and the
 * ECS engine ({@code match.ecs.EcsMatchEngine.play}, when it is wired to real matches) read the frozen match sheet
 * as the line-up and nothing else about who plays:
 * <ul>
 *   <li>{@link #getPlayerId(int) slot i} (0..10, 0 = keeper) holds a {@link Player#getId() player id} of the club's
 *       squad, or {@code null} when the club plays a man short in that place;</li>
 *   <li>{@link #position(int) the place's position} is {@code getShape().slot(i)}, the same {@link Position} as slot i
 *       of the engine's {@code match.ecs.body.Shape} with the label {@code getShape().label()} (kick-off spots come
 *       from there); a man whose own {@link Player#getPosition()} differs plays out of position;</li>
 *   <li>{@link #getStyle()} is the club style (dribbling / combination) for the AI's intent weights;</li>
 *   <li>{@link #getCaptainId()} is one of the eleven (or {@code null} with nobody on the sheet).</li>
 * </ul>
 * The sheet holds ids only (no game objects), so it is plain state in the save (an optional field: saves without
 * it load, and the sheet is picked again).
 */
public class TeamSheet implements Serializable {

    private static final long serialVersionUID = 4471889361504217331L;

    /** The club whose sheet this is. */
    private Long clubId;
    private TeamShape shape = TeamShape.TWO_THREE_FIVE;
    private ClubStyle style = ClubStyle.COMBINATION;
    /** Player id per place, in slot order; {@code null} = empty. Always {@link TeamShape#SLOTS} entries. */
    private List<Long> playerIds = emptySlots();
    private Long captainId;

    public TeamSheet() {
    }

    public TeamSheet(Long clubId, TeamShape shape, ClubStyle style) {
        this.clubId = clubId;
        this.shape = shape != null ? shape : TeamShape.TWO_THREE_FIVE;
        this.style = style != null ? style : ClubStyle.COMBINATION;
    }

    private static List<Long> emptySlots() {
        List<Long> slots = new ArrayList<>(TeamShape.SLOTS);
        for (int i = 0; i < TeamShape.SLOTS; i++) {
            slots.add(null);
        }
        return slots;
    }

    /** @return an independent copy (what a match freezes) */
    public TeamSheet copy() {
        TeamSheet c = new TeamSheet(clubId, getShape(), getStyle());
        for (int i = 0; i < TeamShape.SLOTS; i++) {
            c.playerIds.set(i, getPlayerId(i));
        }
        c.captainId = captainId;
        return c;
    }

    // ------------------------------------------------------------------------------------------------- places

    /** @return the player id in place {@code slot} (0 = keeper), or null when the place is empty */
    public Long getPlayerId(int slot) {
        List<Long> ids = slotsList();
        return ids.get(slot);
    }

    /**
     * Puts {@code playerId} in place {@code slot}. A man already in another place swaps with the one who was here
     * (the editor's "move him there"); {@code null} empties the place (and clears the captaincy if it was his).
     */
    public void setPlayerId(int slot, Long playerId) {
        List<Long> ids = slotsList();
        Long previous = ids.get(slot);
        if (playerId == null) {
            ids.set(slot, null);
            if (previous != null && previous.equals(captainId)) {
                captainId = null;
            }
            return;
        }
        int other = slotOf(playerId);
        if (other == slot) {
            return;
        }
        if (other >= 0) {
            ids.set(other, previous);
        } else if (previous != null && previous.equals(captainId)) {
            captainId = null; // the captain left the side
        }
        ids.set(slot, playerId);
    }

    /** @return the place of {@code playerId}, or -1 when he is not on the sheet */
    public int slotOf(Long playerId) {
        if (playerId == null) {
            return -1;
        }
        List<Long> ids = slotsList();
        for (int i = 0; i < ids.size(); i++) {
            if (playerId.equals(ids.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public boolean isSelected(Long playerId) {
        return slotOf(playerId) >= 0;
    }

    /** @return how many places have a man */
    public int selectedCount() {
        int n = 0;
        for (Long id : slotsList()) {
            if (id != null) {
                n++;
            }
        }
        return n;
    }

    /** @return the men on the sheet, in slot order (empty places left out) */
    public List<Long> selectedIds() {
        List<Long> out = new ArrayList<>();
        for (Long id : slotsList()) {
            if (id != null) {
                out.add(id);
            }
        }
        return out;
    }

    /** Empties every place and the captaincy (shape and style stay). */
    public void clearPlayers() {
        playerIds = emptySlots();
        captainId = null;
    }

    /** @return the position of place {@code slot} in this sheet's shape */
    public Position position(int slot) {
        return getShape().slot(slot);
    }

    /**
     * Changes the shape and moves each man to a place of the new shape: first one with the position he had, then one
     * in the same line, then any free place (he then plays out of position and the editor says so). Nobody is
     * dropped.
     */
    public void reshape(TeamShape newShape) {
        if (newShape == null || newShape == getShape()) {
            return;
        }
        TeamShape old = getShape();
        Long[] men = new Long[TeamShape.SLOTS];
        Position[] had = new Position[TeamShape.SLOTS];
        for (int i = 0; i < TeamShape.SLOTS; i++) {
            men[i] = getPlayerId(i);
            had[i] = old.slot(i);
        }
        Long[] placed = new Long[TeamShape.SLOTS];
        boolean[] done = new boolean[TeamShape.SLOTS];
        for (int pass = 0; pass < 3; pass++) {
            for (int i = 0; i < TeamShape.SLOTS; i++) {
                if (men[i] == null || done[i]) {
                    continue;
                }
                for (int s = 0; s < TeamShape.SLOTS; s++) {
                    Position p = newShape.slot(s);
                    boolean fits = pass == 0 ? p == had[i] : pass == 1 ? p.getLine() == had[i].getLine() : true;
                    if (placed[s] == null && fits) {
                        placed[s] = men[i];
                        done[i] = true;
                        break;
                    }
                }
            }
        }
        List<Long> ids = emptySlots();
        for (int s = 0; s < TeamShape.SLOTS; s++) {
            ids.set(s, placed[s]);
        }
        playerIds = ids;
        shape = newShape;
    }

    /** The stored list, repaired to exactly {@link TeamShape#SLOTS} entries (a damaged save). */
    private List<Long> slotsList() {
        if (playerIds == null || playerIds.size() != TeamShape.SLOTS) {
            List<Long> fixed = emptySlots();
            if (playerIds != null) {
                for (int i = 0; i < Math.min(playerIds.size(), TeamShape.SLOTS); i++) {
                    fixed.set(i, playerIds.get(i));
                }
            }
            playerIds = fixed;
        }
        return playerIds;
    }

    // ------------------------------------------------------------------------------------------- the rest

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }

    public TeamShape getShape() {
        return shape != null ? shape : TeamShape.TWO_THREE_FIVE;
    }

    /** Sets the shape without moving anybody; the editor uses {@link #reshape(TeamShape)}. */
    public void setShape(TeamShape shape) {
        this.shape = shape;
    }

    public ClubStyle getStyle() {
        return style != null ? style : ClubStyle.COMBINATION;
    }

    public void setStyle(ClubStyle style) {
        this.style = style;
    }

    public Long getCaptainId() {
        return captainId;
    }

    /** The captain must be one of the eleven; anybody else is refused (the captaincy is left as it was). */
    public void setCaptainId(Long captainId) {
        if (captainId == null || isSelected(captainId)) {
            this.captainId = captainId;
        }
    }

    /** @return the player ids in slot order (null = empty place), a copy */
    public List<Long> getPlayerIds() {
        return new ArrayList<>(slotsList());
    }

    @Override
    public String toString() {
        return getShape().label() + " " + getStyle() + " " + slotsList() + " captain " + captainId;
    }
}
