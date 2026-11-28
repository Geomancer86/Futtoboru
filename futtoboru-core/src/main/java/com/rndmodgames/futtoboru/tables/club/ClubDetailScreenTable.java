package com.rndmodgames.futtoboru.tables.club;

import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;
import java.math.BigDecimal;
import com.rndmodgames.futtoboru.engine.finances.Money;
import java.text.DecimalFormat;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kotcrab.vis.ui.widget.LinkLabel;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.jobs.ApplicationStatus;
import com.rndmodgames.futtoboru.engine.jobs.ClubStaffManager;
import com.rndmodgames.futtoboru.engine.jobs.JobManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.ui.UiDialogs;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Club Detail Screen Table v1
 * 
 * Displays detailed information about a specific club.
 * Can be used to view any club, not just the player's current club.
 * 
 * @author Geomancer86
 */
public class ClubDetailScreenTable extends VisTable {
    
    Futtoboru game;
    SaveGame currentGame;
    JobManager jobManager;
    ClubStaffManager clubStaffManager;
    MainMenuManager menuManager;
    
    private Club displayedClub;
    
    
    public ClubDetailScreenTable(Game parent) {
        super(true);
        
        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "=== CONSTRUCTOR START ===");
        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] ===== CONSTRUCTOR CALLED =====");
        
        this.game = ((Futtoboru) parent);
        this.currentGame = game.getCurrentGame();
        this.jobManager = game.getJobManager();
        this.clubStaffManager = game.getClubStaffManager();
        
        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Game: " + (game != null ? "OK" : "NULL"));
        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "CurrentGame: " + (currentGame != null ? "OK" : "NULL"));
        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "JobManager: " + (jobManager != null ? "OK" : "NULL"));
        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "ClubStaffManager: " + (clubStaffManager != null ? "OK" : "NULL"));
        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Game: " + (game != null ? "OK" : "NULL"));
        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] CurrentGame: " + (currentGame != null ? "OK" : "NULL"));
        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] JobManager: " + (jobManager != null ? "OK" : "NULL"));
        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] ClubStaffManager: " + (clubStaffManager != null ? "OK" : "NULL"));
        
        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "=== CONSTRUCTOR COMPLETE ===");
        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] ===== CONSTRUCTOR COMPLETE =====");
    }
    
    /**
     * Update to display a specific club
     * 
     * @param club The club to display (can be null)
     */
    public void updateDynamicComponents(Club club) {
        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "=== updateDynamicComponents START ===");
        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] updateDynamicComponents called for club: " + (club != null ? club.getName() : "NULL"));
        
        this.clear();
        
        if (club == null) {
            Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Club is null, showing message");
            this.add(new VisLabel(LanguageModLoader.getValue("club_none_selected")));
            return;
        }
        
        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Updating display for club: " + club.getName());
        
        this.displayedClub = club;
        
        // U-11: details on the left, the club's season ground (picture, name, capacity) on the right
        VisTable details = new VisTable(true);

        // Club Name
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
            details.addSeparator();
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
            details.addSeparator();
            details.row();
            details.add(new VisLabel(LanguageModLoader.getValue("club_balance"))).left();
            details.add(new VisLabel(Money.format(club.getClubBalance()))).left().expandX();
            details.row();
        }
        
        // Staff Positions
        details.addSeparator();
        details.row();
        details.add(new VisLabel(LanguageModLoader.getValue("club_staff_positions"))).colspan(2).left();
        details.row();
        
        if (clubStaffManager != null) {
            List<Profession> vacantPositions = clubStaffManager.getVacantPositions(club);
            List<Profession> allStaffPositions = clubStaffManager.getStaffPositions(); // J-01: manager, director, scout
            
            for (Profession profession : allStaffPositions) {
                // Skip Player and Retired Player (not staff positions)
                if (profession.is(Profession.PLAYER) || profession.is(Profession.RETIRED_PLAYER)) {
                    continue;
                }
                
                boolean isVacant = vacantPositions.contains(profession);
                String status = isVacant ? LanguageModLoader.getValue("club_vacant") : LanguageModLoader.getValue("club_filled");
                
                details.row();
                details.add(new VisLabel(profession.getName() + ":")).left();
                VisLabel statusLabel = new VisLabel(status);
                if (isVacant) {
                    statusLabel.setColor(UiTheme.color("status.vacant")); // Red for vacant
                } else {
                    statusLabel.setColor(UiTheme.color("status.filled")); // Green for filled
                }
                details.add(statusLabel).left().expandX();
                
                // Apply button for vacant positions
                if (isVacant && jobManager != null && currentGame.getOwner() != null) {
                    // Check if player is unemployed
                    if (currentGame.getOwner().getCurrentClubId() == null) {
                        final Profession finalProfession = profession; // Make final for inner class
                        final Club finalClub = club; // Make final for inner class
                        
                        // Check if player has already applied for this position
                        Person owner = currentGame.getOwner();
                        boolean alreadyApplied = false;
                        ApplicationStatus applicationStatus = null;
                        com.rndmodgames.futtoboru.data.jobs.JobOpening existingOpening = jobManager.getOpenOpening(club, profession);
                        
                        if (existingOpening != null) {
                            // J-01: one application per post, also after a refusal or a declined or lapsed offer
                            com.rndmodgames.futtoboru.data.jobs.JobApplication app = jobManager.getApplicationFor(owner, existingOpening);
                            if (app != null) {
                                alreadyApplied = true;
                                applicationStatus = app.getStatus();
                            }
                        }
                        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Already applied: " + alreadyApplied + ", Status: " + applicationStatus);
                        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Already applied: " + alreadyApplied + ", Status: " + applicationStatus);
                        
                        VisTextButton applyButton;
                        if (alreadyApplied) {
                            // Show status instead of Apply button
                            String statusText = LanguageModLoader.getValue("club_applied");
                            if (applicationStatus == ApplicationStatus.OFFER_RECEIVED) {
                                statusText = LanguageModLoader.getValue("club_offer_received");
                            } else if (applicationStatus == ApplicationStatus.REJECTED) {
                                statusText = LanguageModLoader.getValue("club_rejected");
                            } else if (applicationStatus == ApplicationStatus.PENDING) {
                                statusText = LanguageModLoader.getValue("club_pending");
                            } else if (applicationStatus != null) {
                                statusText = com.rndmodgames.futtoboru.tables.jobs.JobBoardScreenTable.statusName(applicationStatus);
                            }
                            applyButton = new VisTextButton(statusText);
                            applyButton.setDisabled(true); // Disable the button
                        } else {
                            applyButton = new VisTextButton(LanguageModLoader.getValue("club_apply"));
                        }
                        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", ">>> Creating Apply button for: " + finalProfession.getName() + " at " + finalClub.getName());
                        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] >>> Creating Apply button");
                        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable]     Profession: " + finalProfession.getName());
                        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable]     Club: " + finalClub.getName());
                        
                        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Button created successfully, adding listener...");
                        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Button created, adding listener...");
                        
                        // Only add listener if not already applied
                        if (!alreadyApplied) {
                                applyButton.addCaptureListener(new InputListener() {
                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "*** TOUCH DOWN on Apply button! ***");
                                Log.info(Log.UI_CLUB, "========================================");
                                Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] *** TOUCH DOWN EVENT ***");
                                Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Club: " + (finalClub != null ? finalClub.getName() : "NULL"));
                                Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Profession: " + (finalProfession != null ? finalProfession.getName() : "NULL"));
                                Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Coordinates: x=" + x + ", y=" + y);
                                Log.info(Log.UI_CLUB, "========================================");
                                return true; // Return true to indicate we want to receive the touchUp event
                            }
                            
                                    @Override
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                if (!applyButton.isPressed()) {
                                    return; // Button was released outside, ignore
                                }
                                Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "*** CLICKED EVENT FIRED ***");
                                Log.info(Log.UI_CLUB, "========================================");
                                Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] *** CLICKED EVENT ***");
                                Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Club: " + (finalClub != null ? finalClub.getName() : "NULL"));
                                Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Profession: " + (finalProfession != null ? finalProfession.getName() : "NULL"));
                                Log.info(Log.UI_CLUB, "========================================");
                                try {
                                    Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "=== Apply button clicked ===");
                                    Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Apply button clicked for: " + finalProfession.getName() + " at " + finalClub.getName());
                                    
                                    if (jobManager == null) {
                                        Log.error(Log.UI_CLUB, "ClubDetailScreenTable", "jobManager is NULL!");
                                        Log.error(Log.UI_CLUB, "[ClubDetailScreenTable] ERROR: jobManager is NULL!");
                                        return;
                                    }
                                    
                                    if (currentGame == null) {
                                        Log.error(Log.UI_CLUB, "ClubDetailScreenTable", "currentGame is NULL!");
                                        Log.error(Log.UI_CLUB, "[ClubDetailScreenTable] ERROR: currentGame is NULL!");
                                        return;
                                    }
                                    
                                    Person owner = currentGame.getOwner();
                                    if (owner == null) {
                                        Log.error(Log.UI_CLUB, "ClubDetailScreenTable", "currentGame.getOwner() is NULL!");
                                        Log.error(Log.UI_CLUB, "[ClubDetailScreenTable] ERROR: currentGame.getOwner() is NULL!");
                                        return;
                                    }
                                    
                                    Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Step 1: Finding or creating job opening...");
                                    Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Step 1: Finding or creating job opening...");
                                    // Find or create job opening for this position
                                    com.rndmodgames.futtoboru.data.jobs.JobOpening jobOpening = null;
                                    
                                    try {
                                        List<com.rndmodgames.futtoboru.data.jobs.JobOpening> availableJobs = jobManager.getAvailableJobs(finalClub);
                                        if (availableJobs != null) {
                                            jobOpening = availableJobs.stream()
                                                .filter(job -> job != null && job.getProfessionId() != null && 
                                                              job.getProfessionId().equals(finalProfession.getId()))
                                                .findFirst()
                                                .orElse(null);
                                        }
                                    } catch (Exception e) {
                                        Log.error(Log.UI_CLUB, "ClubDetailScreenTable", "Error getting available jobs", e);
                                        Log.error(Log.UI_CLUB, "[ClubDetailScreenTable] ERROR getting available jobs:");
                                        Log.error(Log.UI_CLUB, e);
                                    }
                                    
                                    if (jobOpening == null) {
                                        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "No existing job opening found, creating new one...");
                                        // Create job opening if it doesn't exist
                                        try {
                                            jobOpening = jobManager.createJobOpening(finalClub, finalProfession);
                                            if (jobOpening == null) {
                                                Log.error(Log.UI_CLUB, "ClubDetailScreenTable", "createJobOpening returned NULL!");
                                                Log.error(Log.UI_CLUB, "[ClubDetailScreenTable] ERROR: createJobOpening returned NULL!");
                                                return;
                                            }
                                            Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Job opening created: " + jobOpening.getId());
                                        } catch (Exception e) {
                                            Log.error(Log.UI_CLUB, "ClubDetailScreenTable", "ERROR creating job opening!", e);
                                            Log.error(Log.UI_CLUB, "[ClubDetailScreenTable] ERROR creating job opening:");
                                            Log.error(Log.UI_CLUB, e);
                                            return;
                                        }
                                    } else {
                                        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Found existing job opening: " + jobOpening.getId());
                                    }
                                    
                                    if (jobOpening != null) {
                                        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Step 2: Applying for job...");
                                        // Apply for the job
                                        try {
                                            com.rndmodgames.futtoboru.data.jobs.JobApplication result = 
                                                jobManager.applyForJob(owner, jobOpening);
                                            
                                            if (result != null) {
                                                Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Application successful: " + result.getId());
                                                Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Applied for " + finalProfession.getName() + " at " + finalClub.getName() + " - SUCCESS");
                                                Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Application ID: " + result.getId() + ", Status: " + result.getStatus());
                                                
                                                // Show success message
                                                final ClubDetailScreenTable self = ClubDetailScreenTable.this;
                                                Gdx.app.postRunnable(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        self.showMessage(LanguageModLoader.getValue("club_application_submitted"), 
                                                                      LanguageModLoader.format("club_application_submitted_text", finalClub.getName(), finalProfession.getName()));
                                                    }
                                                });
                                                
                                                // Force immediate refresh to show updated status
                                                Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Forcing immediate screen refresh...");
                                                Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Forcing immediate screen refresh...");
                                                
                                                // Post to main thread to ensure UI update happens
                                                Gdx.app.postRunnable(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Refreshing screen in postRunnable...");
                                                        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Refreshing screen in postRunnable...");
                                                        try {
                                                            updateDynamicComponents(finalClub);
                                                            Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Display refreshed successfully in postRunnable");
                                                            Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Display refreshed successfully");
                                                        } catch (Exception e) {
                                                            Log.error(Log.UI_CLUB, "ClubDetailScreenTable", "ERROR refreshing display in postRunnable!", e);
                                                            Log.error(Log.UI_CLUB, "[ClubDetailScreenTable] ERROR refreshing display:");
                                                            Log.error(Log.UI_CLUB, e);
                                                        }
                                                    }
                                                });
                                            } else {
                                                Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Application returned null (may be rejected or already exists)");
                                                Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Application returned null");
                                                
                                                // Show error message
                                                final ClubDetailScreenTable self = ClubDetailScreenTable.this;
                                                Gdx.app.postRunnable(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        self.showMessage(LanguageModLoader.getValue("club_application_failed"), 
                                                                      LanguageModLoader.getValue("club_application_failed_text"));
                                                    }
                                                });
                                            }
                                        } catch (Exception e) {
                                            Log.error(Log.UI_CLUB, "ClubDetailScreenTable", "ERROR applying for job!", e);
                                            Log.error(Log.UI_CLUB, "[ClubDetailScreenTable] ERROR applying for job:");
                                            Log.error(Log.UI_CLUB, e);
                                            return;
                                        }
                                    }
                                    
                                    Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "=== Apply button click completed ===");
                                    
                                } catch (Exception e) {
                                    Log.error(Log.UI_CLUB, "ClubDetailScreenTable", "CRITICAL ERROR in Apply button click!", e);
                                    Log.error(Log.UI_CLUB, "========================================");
                                    Log.error(Log.UI_CLUB, "[ClubDetailScreenTable] CRITICAL ERROR in Apply button!");
                                    Log.error(Log.UI_CLUB, "Exception: " + e.getClass().getName());
                                    Log.error(Log.UI_CLUB, "Message: " + e.getMessage());
                                    Log.error(Log.UI_CLUB, "========================================");
                                    Log.error(Log.UI_CLUB, e);
                                    Log.error(Log.UI_CLUB, "========================================");
                                    }
                                }
                            });
                            } // End if (!alreadyApplied)
                        
                        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Listener added to button, now adding button to table...");
                        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Adding button to table...");
                        details.add(applyButton).width(UiTheme.size("col.number"));
                        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Button added to table successfully");
                        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Button added to table for: " + finalProfession.getName() + " at " + finalClub.getName());
                    }
                }
            }
        }
        

        this.add(details).top().left().expandX().fillX();
        this.add(game.getGroundArt().panel(club.getStadium(), UiTheme.size("art.ground.panel.w"),
                UiTheme.size("art.ground.panel.h"))).top().right().pad(UiTheme.space("sm"));
        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "=== updateDynamicComponents COMPLETE ===");
        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] ===== updateDynamicComponents COMPLETE =====");
        
    }
    
    public void setMenuManager(MainMenuManager menuManager) {
        this.menuManager = menuManager;
    }
    
    public Club getDisplayedClub() {
        return displayedClub;
    }
    
    /**
     * Show a message dialog to the user
     */
    void showMessage(String title, String message) {
        Stage stage = this.getStage();
        if (stage != null) {
            UiDialogs.info(stage, title, message);
        } else {
            // Fallback to console if stage not available
            Log.info(Log.UI_CLUB, "ClubDetailScreenTable", title + ": " + message);
            Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] " + title + ": " + message);
        }
    }
}

