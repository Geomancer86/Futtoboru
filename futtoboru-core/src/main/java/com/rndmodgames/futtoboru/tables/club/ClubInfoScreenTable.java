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

        // Club Name Header
        this.row();
        VisLabel clubNameHeader = new VisLabel("CLUB INFORMATION");
        clubNameHeader.setFontScale(UiTheme.fontScale("heading"));
        this.add(clubNameHeader).colspan(2).pad(UiTheme.space("sm"));
        this.row();
        this.addSeparator().colspan(2).pad(UiTheme.space("xs"));
        
        // Basic Club Info
        this.row();
        this.add(new VisLabel("Club:")).left();
        this.add(new VisLabel(club.getName())).left().expandX();
        this.row();
        
        if (club.getFullName() != null && !club.getFullName().equals(club.getName())) {
            this.add(new VisLabel("Full Name:")).left();
            this.add(new VisLabel(club.getFullName())).left().expandX();
            this.row();
        }
        
        // Country
        if (club.getCountry() != null) {
            this.add(new VisLabel("Country:")).left();
            this.add(new VisLabel(club.getCountry().getCommonName())).left().expandX();
            this.row();
        }
        
        // Foundation Year
        if (club.getYear() != null) {
            this.add(new VisLabel("Founded:")).left();
            this.add(new VisLabel(club.getYear().toString())).left().expandX();
            this.row();
        }
        
        // Stadium Information
        if (club.getStadium() != null) {
            this.addSeparator().colspan(2).padTop(UiTheme.space("sm")).padBottom(UiTheme.space("xs"));
            this.row();
            VisLabel stadiumHeader = new VisLabel("STADIUM INFORMATION");
            stadiumHeader.setFontScale(UiTheme.fontScale("subheading"));
            this.add(stadiumHeader).colspan(2).left().padTop(UiTheme.space("sm"));
            this.row();
            
            // Stadium Name
            this.add(new VisLabel("Stadium:")).left();
            this.add(new VisLabel(club.getStadium().getName())).left().expandX();
            this.row();
            
            // Capacity
            if (club.getStadium().getCapacity() != null) {
                this.add(new VisLabel("Capacity:")).left();
                this.add(new VisLabel(club.getStadium().getCapacity().toString())).left().expandX();
                this.row();
            }
            
            // Built Year
            if (club.getStadium().getBuiltYear() != null) {
                this.add(new VisLabel("Built Year:")).left();
                this.add(new VisLabel(club.getStadium().getBuiltYear().toString())).left().expandX();
                this.row();
            }
            
            // Value
            if (club.getStadium().getValue() != null && club.getStadium().getValue().compareTo(BigDecimal.ZERO) > 0) {
                this.add(new VisLabel("Estimated Value:")).left();
                this.add(new VisLabel(Money.format(club.getStadium().getValue()))).left().expandX();
                this.row();
            }
            
            // Ownership Status
            this.add(new VisLabel("Ownership:")).left();
            String ownershipStatus = club.getStadium().isOwned() ? "Owned" : "Rented";
            this.add(new VisLabel(ownershipStatus)).left().expandX();
            this.row();
            
            // Land Value (if owned)
            if (club.getStadium().isOwned() && club.getStadium().getLandValue() != null && 
                club.getStadium().getLandValue().compareTo(BigDecimal.ZERO) > 0) {
                this.add(new VisLabel("Land Value:")).left();
                this.add(new VisLabel(Money.format(club.getStadium().getLandValue()))).left().expandX();
                this.row();
            }
            
            // Annual Rent (if rented)
            if (!club.getStadium().isOwned() && club.getStadium().getAnnualRent() != null && 
                club.getStadium().getAnnualRent().compareTo(BigDecimal.ZERO) > 0) {
                this.add(new VisLabel("Annual Rent:")).left();
                this.add(new VisLabel(Money.format(club.getStadium().getAnnualRent()) + "/year")).left().expandX();
                this.row();
            }
            
            // Total Asset Value (if owned)
            if (club.getStadium().isOwned()) {
                BigDecimal totalAssetValue = club.getStadium().getTotalAssetValue();
                if (totalAssetValue.compareTo(BigDecimal.ZERO) > 0) {
                    this.add(new VisLabel("Total Asset Value:")).left();
                    VisLabel assetValueLabel = new VisLabel(Money.format(totalAssetValue));
                    assetValueLabel.setColor(UiTheme.color("state.positive")); // Green for asset value
                    this.add(assetValueLabel).left().expandX();
                    this.row();
                }
            }
            
            // Historical Description
            if (club.getStadium().getDescription() != null && !club.getStadium().getDescription().trim().isEmpty()) {
                this.addSeparator().colspan(2).padTop(UiTheme.space("xs")).padBottom(UiTheme.space("xs"));
                this.row();
                VisLabel descHeader = new VisLabel("Historical Summary:");
                descHeader.setFontScale(UiTheme.fontScale("subheading.minor"));
                this.add(descHeader).colspan(2).left();
                this.row();
                // Description text (may be long, so wrap it)
                VisLabel descriptionLabel = new VisLabel(club.getStadium().getDescription());
                descriptionLabel.setWrap(true);
                this.add(descriptionLabel).colspan(2).left().width(UiTheme.size("text.wrap")).padTop(UiTheme.space("xs"));
                this.row();
            }
            
            // URL Source (Wikipedia link)
            if (club.getStadium().getUrlSource() != null && !club.getStadium().getUrlSource().trim().isEmpty()) {
                this.addSeparator().colspan(2).padTop(UiTheme.space("xs")).padBottom(UiTheme.space("xs"));
                this.row();
                this.add(new VisLabel("Source:")).left();
                
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
                
                this.add(urlLabel).left().expandX();
                this.row();
            }
        }
        
        // Finances
        if (club.getClubBalance() != null) {
            this.addSeparator().colspan(2).padTop(UiTheme.space("sm")).padBottom(UiTheme.space("xs"));
            this.row();
            VisLabel financesHeader = new VisLabel("FINANCES");
            financesHeader.setFontScale(UiTheme.fontScale("subheading"));
            this.add(financesHeader).colspan(2).left().padTop(UiTheme.space("sm"));
            this.row();
            this.add(new VisLabel("Balance:")).left();
            this.add(new VisLabel(Money.format(club.getClubBalance()))).left().expandX();
            this.row();
        }
    }
}