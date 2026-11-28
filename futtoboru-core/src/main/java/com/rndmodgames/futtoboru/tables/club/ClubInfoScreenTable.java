package com.rndmodgames.futtoboru.tables.club;

import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;
import java.math.BigDecimal;
import com.rndmodgames.futtoboru.engine.finances.Money;
import java.text.DecimalFormat;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.LinkLabel;
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
            this.add(new VisLabel(LanguageModLoader.getValue("club_none_selected"))).pad(UiTheme.space("lg"));
            return;
        }

        // U-11: details on the left, the club's season ground (picture, name, capacity) on the right
        VisTable details = new VisTable(true);

        // Club Name Header
        details.row();
        VisLabel clubNameHeader = new VisLabel(LanguageModLoader.getValue("club_information"));
        clubNameHeader.setFontScale(UiTheme.fontScale("heading"));
        details.add(clubNameHeader).colspan(2).pad(UiTheme.space("sm"));
        details.row();
        details.addSeparator().colspan(2).pad(UiTheme.space("xs"));
        
        // Basic Club Info
        details.row();
        details.add(new VisLabel(LanguageModLoader.getValue("club_label_club"))).left();
        details.add(new VisLabel(club.getName())).left().expandX();
        details.row();
        
        if (club.getFullName() != null && !club.getFullName().equals(club.getName())) {
            details.add(new VisLabel(LanguageModLoader.getValue("club_full_name"))).left();
            details.add(new VisLabel(club.getFullName())).left().expandX();
            details.row();
        }
        
        // Country
        if (club.getCountry() != null) {
            details.add(new VisLabel(LanguageModLoader.getValue("club_country"))).left();
            details.add(new VisLabel(club.getCountry().getCommonName())).left().expandX();
            details.row();
        }
        
        // Foundation Year
        if (club.getYear() != null) {
            details.add(new VisLabel(LanguageModLoader.getValue("club_founded"))).left();
            details.add(new VisLabel(club.getYear().toString())).left().expandX();
            details.row();
        }
        
        // Stadium Information
        if (club.getStadium() != null) {
            details.addSeparator().colspan(2).padTop(UiTheme.space("sm")).padBottom(UiTheme.space("xs"));
            details.row();
            VisLabel stadiumHeader = new VisLabel(LanguageModLoader.getValue("club_stadium_information"));
            stadiumHeader.setFontScale(UiTheme.fontScale("subheading"));
            details.add(stadiumHeader).colspan(2).left().padTop(UiTheme.space("sm"));
            details.row();
            
            // Stadium Name
            details.add(new VisLabel(LanguageModLoader.getValue("club_stadium"))).left();
            details.add(new VisLabel(club.getStadium().getName())).left().expandX();
            details.row();
            
            // Capacity
            if (club.getStadium().getCapacity() != null) {
                details.add(new VisLabel(LanguageModLoader.getValue("club_capacity"))).left();
                details.add(new VisLabel(GameFormat.integer(club.getStadium().getCapacity()))).left().expandX(); // U-12: "10,000"
                details.row();
            }
            
            // Built Year
            if (club.getStadium().getBuiltYear() != null) {
                details.add(new VisLabel(LanguageModLoader.getValue("club_built_year"))).left();
                details.add(new VisLabel(club.getStadium().getBuiltYear().toString())).left().expandX();
                details.row();
            }
            
            // Value
            if (club.getStadium().getValue() != null && club.getStadium().getValue().compareTo(BigDecimal.ZERO) > 0) {
                details.add(new VisLabel(LanguageModLoader.getValue("club_estimated_value"))).left();
                details.add(new VisLabel(Money.format(club.getStadium().getValue()))).left().expandX();
                details.row();
            }
            
            // Ownership Status
            details.add(new VisLabel(LanguageModLoader.getValue("club_ownership"))).left();
            String ownershipStatus = club.getStadium().isOwned() ? LanguageModLoader.getValue("club_owned") : LanguageModLoader.getValue("club_rented");
            details.add(new VisLabel(ownershipStatus)).left().expandX();
            details.row();
            
            // Land Value (if owned)
            if (club.getStadium().isOwned() && club.getStadium().getLandValue() != null && 
                club.getStadium().getLandValue().compareTo(BigDecimal.ZERO) > 0) {
                details.add(new VisLabel(LanguageModLoader.getValue("club_land_value"))).left();
                details.add(new VisLabel(Money.format(club.getStadium().getLandValue()))).left().expandX();
                details.row();
            }
            
            // Annual Rent (if rented)
            if (!club.getStadium().isOwned() && club.getStadium().getAnnualRent() != null && 
                club.getStadium().getAnnualRent().compareTo(BigDecimal.ZERO) > 0) {
                details.add(new VisLabel(LanguageModLoader.getValue("club_annual_rent"))).left();
                details.add(new VisLabel(LanguageModLoader.format("club_per_year", Money.format(club.getStadium().getAnnualRent())))).left().expandX();
                details.row();
            }
            
            // Total Asset Value (if owned)
            if (club.getStadium().isOwned()) {
                BigDecimal totalAssetValue = club.getStadium().getTotalAssetValue();
                if (totalAssetValue.compareTo(BigDecimal.ZERO) > 0) {
                    details.add(new VisLabel(LanguageModLoader.getValue("club_total_asset_value"))).left();
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
                VisLabel descHeader = new VisLabel(LanguageModLoader.getValue("club_historical_summary"));
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
                details.add(new VisLabel(LanguageModLoader.getValue("club_source"))).left();
                
                // U-12: a VisUI LinkLabel like every other link (skin colour, hand cursor, opens the browser)
                final String url = club.getStadium().getUrlSource();
                details.add(new LinkLabel(url, url)).left().expandX();
                details.row();
            }
        }
        
        // Finances
        if (club.getClubBalance() != null) {
            details.addSeparator().colspan(2).padTop(UiTheme.space("sm")).padBottom(UiTheme.space("xs"));
            details.row();
            VisLabel financesHeader = new VisLabel(LanguageModLoader.getValue("club_finances"));
            financesHeader.setFontScale(UiTheme.fontScale("subheading"));
            details.add(financesHeader).colspan(2).left().padTop(UiTheme.space("sm"));
            details.row();
            details.add(new VisLabel(LanguageModLoader.getValue("club_balance"))).left();
            details.add(new VisLabel(Money.format(club.getClubBalance()))).left().expandX();
            details.row();
        }
        this.add(details).top().left().expandX().fillX();
        this.add(game.getGroundArt().panel(club.getStadium(), UiTheme.size("art.ground.panel.w"),
                UiTheme.size("art.ground.panel.h"))).top().right().pad(UiTheme.space("sm"));

    }
}