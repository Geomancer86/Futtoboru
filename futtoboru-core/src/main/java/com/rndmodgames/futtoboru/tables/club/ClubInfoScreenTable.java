package com.rndmodgames.futtoboru.tables.club;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

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
    
    DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    
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
            this.add(new VisLabel("No club selected.")).pad(20);
            return;
        }

        // Club Name Header
        this.row();
        VisLabel clubNameHeader = new VisLabel("CLUB INFORMATION");
        clubNameHeader.setFontScale(1.2f);
        this.add(clubNameHeader).colspan(2).pad(10);
        this.row();
        this.addSeparator().colspan(2).pad(5);
        
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
            this.addSeparator().colspan(2).padTop(10).padBottom(5);
            this.row();
            VisLabel stadiumHeader = new VisLabel("STADIUM INFORMATION");
            stadiumHeader.setFontScale(1.1f);
            this.add(stadiumHeader).colspan(2).left().padTop(10);
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
                this.add(new VisLabel(currencyFormat.format(club.getStadium().getValue()))).left().expandX();
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
                this.add(new VisLabel(currencyFormat.format(club.getStadium().getLandValue()))).left().expandX();
                this.row();
            }
            
            // Annual Rent (if rented)
            if (!club.getStadium().isOwned() && club.getStadium().getAnnualRent() != null && 
                club.getStadium().getAnnualRent().compareTo(BigDecimal.ZERO) > 0) {
                this.add(new VisLabel("Annual Rent:")).left();
                this.add(new VisLabel(currencyFormat.format(club.getStadium().getAnnualRent()) + "/year")).left().expandX();
                this.row();
            }
            
            // Total Asset Value (if owned)
            if (club.getStadium().isOwned()) {
                BigDecimal totalAssetValue = club.getStadium().getTotalAssetValue();
                if (totalAssetValue.compareTo(BigDecimal.ZERO) > 0) {
                    this.add(new VisLabel("Total Asset Value:")).left();
                    VisLabel assetValueLabel = new VisLabel(currencyFormat.format(totalAssetValue));
                    assetValueLabel.setColor(0.2f, 1.0f, 0.2f, 1.0f); // Green for asset value
                    this.add(assetValueLabel).left().expandX();
                    this.row();
                }
            }
            
            // Historical Description
            if (club.getStadium().getDescription() != null && !club.getStadium().getDescription().trim().isEmpty()) {
                this.addSeparator().colspan(2).padTop(5).padBottom(5);
                this.row();
                VisLabel descHeader = new VisLabel("Historical Summary:");
                descHeader.setFontScale(1.05f);
                this.add(descHeader).colspan(2).left();
                this.row();
                // Description text (may be long, so wrap it)
                VisLabel descriptionLabel = new VisLabel(club.getStadium().getDescription());
                descriptionLabel.setWrap(true);
                this.add(descriptionLabel).colspan(2).left().width(600).padTop(5);
                this.row();
            }
            
            // URL Source (Wikipedia link)
            if (club.getStadium().getUrlSource() != null && !club.getStadium().getUrlSource().trim().isEmpty()) {
                this.addSeparator().colspan(2).padTop(5).padBottom(5);
                this.row();
                this.add(new VisLabel("Source:")).left();
                VisLabel urlLabel = new VisLabel(club.getStadium().getUrlSource());
                urlLabel.setColor(0.3f, 0.5f, 1.0f, 1.0f); // Blue for link
                this.add(urlLabel).left().expandX();
                this.row();
            }
        }
        
        // Finances
        if (club.getClubBalance() != null) {
            this.addSeparator().colspan(2).padTop(10).padBottom(5);
            this.row();
            VisLabel financesHeader = new VisLabel("FINANCES");
            financesHeader.setFontScale(1.1f);
            this.add(financesHeader).colspan(2).left().padTop(10);
            this.row();
            this.add(new VisLabel("Balance:")).left();
            this.add(new VisLabel(currencyFormat.format(club.getClubBalance()))).left().expandX();
            this.row();
        }
    }
}