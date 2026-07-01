package com.rndmodgames.futtoboru.tables.match.preview;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.tactics.ClubTacticsSetup;
import com.rndmodgames.futtoboru.data.tactics.FormationTemplate;
import com.rndmodgames.futtoboru.data.tactics.MatchLineup;
import com.rndmodgames.futtoboru.engine.tactics.FormationRegistry;
import com.rndmodgames.futtoboru.engine.tactics.MatchLineupBuilder;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Match preview — fixture, lineups, kick off (Layer B3).
 */
public class MatchPreviewScreenTable extends VisTable {

    private final Futtoboru game;
    private Match pendingMatch;

    public MatchPreviewScreenTable(Game parent) {
        super(true);
        this.game = (Futtoboru) parent;
    }

    public void updateDynamicComponents() {
        this.clear();
        pendingMatch = resolvePlayableMatch();
        if (pendingMatch == null) {
            this.row();
            this.add("No upcoming match available");
            return;
        }

        Club homeClub = DatabaseLoader.getClubById(pendingMatch.getHomeClubId());
        Club awayClub = DatabaseLoader.getClubById(pendingMatch.getAwayClubId());
        if (homeClub == null || awayClub == null) {
            this.row();
            this.add("Match teams not yet determined");
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
        String matchDate = pendingMatch.getMatchDateTime() != null
                ? pendingMatch.getMatchDateTime().format(dateFormatter)
                : "Unknown";

        this.row();
        VisLabel header = new VisLabel("MATCH PREVIEW");
        header.setFontScale(1.3f);
        this.add(header).colspan(2).padBottom(12);
        this.row();
        this.add(homeClub.getName() + " vs " + awayClub.getName()).colspan(2);
        this.row().padTop(4);
        this.add("Date: " + matchDate).colspan(2).left();

        Club currentClub = game.getCurrentGame().getCurrentClub();
        appendLineupSection("Your XI", currentClub, MatchLineupBuilder.buildForClub(currentClub, pendingMatch));
        Club opponent = currentClub.getId().equals(homeClub.getId()) ? awayClub : homeClub;
        appendLineupSection("Opponent XI", opponent, MatchLineupBuilder.buildForClub(opponent, pendingMatch));

        VisTextButton kickOffButton = new VisTextButton(LanguageModLoader.getValue("kick_off"));
        kickOffButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent event,
                    com.badlogic.gdx.scenes.scene2d.Actor actor) {
                kickOffMatch();
            }
        });
        this.row().padTop(16);
        this.add(kickOffButton).colspan(2);
    }

    private void appendLineupSection(String title, Club club, MatchLineup lineup) {
        if (club == null || lineup == null) {
            return;
        }
        FormationTemplate template = FormationRegistry.getRequiredTemplate(lineup.getFormationTemplateId());

        this.row().padTop(12);
        this.add(title + " (" + template.getDisplayName() + ")").colspan(2).left();
        this.row().padTop(4);

        VisTable lineupTable = new VisTable(true);
        for (int i = 0; i < template.getSlots().size(); i++) {
            String slotId = template.getSlots().get(i).getSlotId();
            Long playerId = null;
            if (lineup.getPlayerIdsInSlotOrder() != null && i < lineup.getPlayerIdsInSlotOrder().size()) {
                playerId = lineup.getPlayerIdsInSlotOrder().get(i);
            }
            lineupTable.add(slotId).width(60);
            lineupTable.add(resolvePlayerName(club, playerId)).growX().left();
            lineupTable.row();
        }
        this.add(lineupTable).colspan(2).growX().fillX();
    }

    private void kickOffMatch() {
        if (pendingMatch == null) {
            return;
        }
        Match played = game.getGameEngine().kickOffMatch(pendingMatch);
        if (played == null) {
            return;
        }
        MainMenuManager menuManager = game.getGameEngine().getMainMenuManager();
        if (menuManager != null) {
            menuManager.showMatchResult(played);
        }
    }

    private Match resolvePlayableMatch() {
        Club currentClub = game.getCurrentGame().getCurrentClub();
        if (currentClub == null) {
            return null;
        }
        return game.getGameEngine().findNextPlayableMatchForClub(currentClub);
    }

    private String resolvePlayerName(Club club, Long playerId) {
        if (club == null || playerId == null || club.getPlayers() == null) {
            return "-";
        }
        for (com.rndmodgames.futtoboru.data.Player player : club.getPlayers()) {
            if (player != null && playerId.equals(player.getId()) && player.getPerson() != null) {
                return player.getPerson().getName() + " " + player.getPerson().getLastname();
            }
        }
        return "Player#" + playerId;
    }
}
