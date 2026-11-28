package com.rndmodgames.futtoboru.tables.club;

import java.math.BigDecimal;
import com.rndmodgames.futtoboru.engine.finances.Money;
import java.text.DecimalFormat;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.UiTheme;

/**
 * Club Info Screen Table v1
 * 
 * Displays information about the player's current club.
 * 
 * @author Geomancer86
 */
public class ClubInfoScreenTable extends VisTable {

    // keep track for easy access
    Futtoboru game;
    SaveGame currentGame;
    
    
    //
    public ClubInfoScreenTable(Game parent) {
        
        // automatic vis spacing
        super(true);
        
        //
        this.game = ((Futtoboru) parent);
        this.currentGame = game.getCurrentGame();
    }
    
    /**
     * Update display with current club information
     */
    public void updateDynamicComponents() {
        
        //
        this.clear();
        
        Club club = currentGame.getCurrentClub();
        if (club == null) {
            this.add(new VisLabel("No club selected.")).pad(UiTheme.space("lg"));
            return;
        }

        // U-11: details on the left, the club's season ground (picture, name, capacity) on the right
        VisTable details = new VisTable(true);

        // Club Name Header
        details.row();
        VisLabel clubNameHeader = new VisLabel("CLUB INFORMATION");
        clubNameHeader.setFontScale(UiTheme.fontScale("heading"));
        details.add(clubNameHeader).colspan(2).pad(UiTheme.space("sm"));
        details.row();
        details.addSeparator().colspan(2).pad(UiTheme.space("xs"));
        
        // Basic Club Info
        details.row();
        details.add(new VisLabel("Club:")).left();
        details.add(new VisLabel(club.getName())).left().expandX();
        details.row();
        
        if (club.getFullName() != null && !club.getFullName().equals(club.getName())) {
            details.add(new VisLabel("Full Name:")).left();
            details.add(new VisLabel(club.getFullName())).left().expandX();
            details.row();
        }
        
        // Country
        if (club.getCountry() != null) {
            details.add(new VisLabel("Country:")).left();
            details.add(new VisLabel(club.getCountry().getCommonName())).left().expandX();
            details.row();
        }
        
        // Foundation Year
        if (club.getYear() != null) {
            details.add(new VisLabel("Founded:")).left();
            details.add(new VisLabel(club.getYear().toString())).left().expandX();
            details.row();
        }
        
        // Stadium Information
        if (club.getStadium() != null) {
            details.addSeparator().colspan(2).padTop(UiTheme.space("sm")).padBottom(UiTheme.space("xs"));
            details.row();
            VisLabel stadiumHeader = new VisLabel("STADIUM INFORMATION");
            stadiumHeader.setFontScale(UiTheme.fontScale("subheading"));
            details.add(stadiumHeader).colspan(2).left().padTop(UiTheme.space("sm"));
            details.row();
            
            // Stadium Name
            details.add(new VisLabel("Stadium:")).left();
            details.add(new VisLabel(club.getStadium().getName())).left().expandX();
            details.row();
            
            // Capacity
            if (club.getStadium().getCapacity() != null) {
                details.add(new VisLabel("Capacity:")).left();
                details.add(new VisLabel(club.getStadium().getCapacity().toString())).left().expandX();
                details.row();
            }
            
            // Built Year
            if (club.getStadium().getBuiltYear() != null) {
                details.add(new VisLabel("Built Year:")).left();
                details.add(new VisLabel(club.getStadium().getBuiltYear().toString())).left().expandX();
                details.row();
            }
            
            // Value
            if (club.getStadium().getValue() != null && club.getStadium().getValue().compareTo(BigDecimal.ZERO) > 0) {
                details.add(new VisLabel("Estimated Value:")).left();
                details.add(new VisLabel(Money.format(club.getStadium().getValue()))).left().expandX();
                details.row();
            }
            
            // Ownership Status
            details.add(new VisLabel("Ownership:")).left();
            String ownershipStatus = club.getStadium().isOwned() ? "Owned" : "Rented";
            details.add(new VisLabel(ownershipStatus)).left().expandX();
            details.row();
            
            // Land Value (if owned)
            if (club.getStadium().isOwned() && club.getStadium().getLandValue() != null && 
                club.getStadium().getLandValue().compareTo(BigDecimal.ZERO) > 0) {
                details.add(new VisLabel("Land Value:")).left();
                details.add(new VisLabel(Money.format(club.getStadium().getLandValue()))).left().expandX();
                details.row();
            }
            
            // Annual Rent (if rented)
            if (!club.getStadium().isOwned() && club.getStadium().getAnnualRent() != null && 
                club.getStadium().getAnnualRent().compareTo(BigDecimal.ZERO) > 0) {
                details.add(new VisLabel("Annual Rent:")).left();
                details.add(new VisLabel(Money.format(club.getStadium().getAnnualRent()) + "/year")).left().expandX();
                details.row();
            }
            
            // Total Asset Value (if owned)
            if (club.getStadium().isOwned()) {
                BigDecimal totalAssetValue = club.getStadium().getTotalAssetValue();
                if (totalAssetValue.compareTo(BigDecimal.ZERO) > 0) {
                    details.add(new VisLabel("Total Asset Value:")).left();
                    VisLabel assetValueLabel = new VisLabel(Money.format(totalAssetValue));
                    assetValueLabel.setColor(UiTheme.color("state.positive")); // Green for asset value
                    details.add(assetValueLabel).left().expandX();
                    details.row();
                }
            }
            
            // Historical Description
            if (club.getStadium().getDescription() != null && !club.getStadium().getDescription().trim().isEmpty()) {
                details.addSeparator().colspan(2).padTop(UiTheme.space("xs")).padBottom(UiTheme.space("xs"));
                details.row();
                VisLabel descHeader = new VisLabel("Historical Summary:");
                descHeader.setFontScale(UiTheme.fontScale("subheading.minor"));
                details.add(descHeader).colspan(2).left();
                details.row();
                // Description text (may be long, so wrap it)
                VisLabel descriptionLabel = new VisLabel(club.getStadium().getDescription());
                descriptionLabel.setWrap(true);
                details.add(descriptionLabel).colspan(2).left().width(UiTheme.size("text.wrap.narrow")).padTop(UiTheme.space("xs"));
                details.row();
            }
            
            // URL Source (Wikipedia link)
            if (club.getStadium().getUrlSource() != null && !club.getStadium().getUrlSource().trim().isEmpty()) {
                details.addSeparator().colspan(2).padTop(UiTheme.space("xs")).padBottom(UiTheme.space("xs"));
                details.row();
                details.add(new VisLabel("Source:")).left();
                
                final String url = club.getStadium().getUrlSource();
                final VisLabel urlLabel = new VisLabel(url);
                urlLabel.setColor(UiTheme.color("link.normal")); // Blue for link
                
                // Make URL clickable and add hover feedback
                urlLabel.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
                    @Override
                    public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                        com.badlogic.gdx.Gdx.net.openURI(url);
                        return true;
                    }
                    
                    @Override
                    public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                        urlLabel.setColor(UiTheme.color("link.hover")); // Brighter blue on hover
                        com.badlogic.gdx.Gdx.graphics.setSystemCursor(com.badlogic.gdx.graphics.Cursor.SystemCursor.Hand);
                    }
                    
                    @Override
                    public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                        urlLabel.setColor(UiTheme.color("link.normal")); // Original blue
                        com.badlogic.gdx.Gdx.graphics.setSystemCursor(com.badlogic.gdx.graphics.Cursor.SystemCursor.Arrow);
                    }
                });
                
                details.add(urlLabel).left().expandX();
                details.row();
            }
        }
        
        // Finances
        if (club.getClubBalance() != null) {
            details.addSeparator().colspan(2).padTop(UiTheme.space("sm")).padBottom(UiTheme.space("xs"));
            details.row();
            VisLabel financesHeader = new VisLabel("FINANCES");
            financesHeader.setFontScale(UiTheme.fontScale("subheading"));
            details.add(financesHeader).colspan(2).left().padTop(UiTheme.space("sm"));
            details.row();
            details.add(new VisLabel("Balance:")).left();
            details.add(new VisLabel(Money.format(club.getClubBalance()))).left().expandX();
            details.row();
        }
        this.add(details).top().left().expandX().fillX();
        this.add(game.getGroundArt().panel(club.getStadium(), UiTheme.size("art.ground.panel.w"),
                UiTheme.size("art.ground.panel.h"))).top().right().pad(UiTheme.space("sm"));

    }
}