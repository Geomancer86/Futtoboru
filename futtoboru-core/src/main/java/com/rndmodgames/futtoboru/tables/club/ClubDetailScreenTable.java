package com.rndmodgames.futtoboru.tables.club;

import java.math.BigDecimal;
import com.rndmodgames.futtoboru.engine.finances.Money;
import java.text.DecimalFormat;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kotcrab.vis.ui.widget.VisDialog;
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
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.UiTheme;
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
            this.add(new VisLabel("No club selected."));
            return;
        }
        
        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Updating display for club: " + club.getName());
        
        this.displayedClub = club;
        
        // Club Name
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
            this.addSeparator();
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
            this.addSeparator();
            this.row();
            this.add(new VisLabel("Balance:")).left();
            this.add(new VisLabel(Money.format(club.getClubBalance()))).left().expandX();
            this.row();
        }
        
        // Staff Positions
        this.addSeparator();
        this.row();
        this.add(new VisLabel("Staff Positions:")).colspan(2).left();
        this.row();
        
        if (clubStaffManager != null) {
            List<Profession> vacantPositions = clubStaffManager.getVacantPositions(club);
            List<Profession> allStaffPositions = DatabaseLoader.getInstance().getSelectableProfessions();
            
            for (Profession profession : allStaffPositions) {
                // Skip Player and Retired Player (not staff positions)
                if (profession.is(Profession.PLAYER) || profession.is(Profession.RETIRED_PLAYER)) {
                    continue;
                }
                
                boolean isVacant = vacantPositions.contains(profession);
                String status = isVacant ? "VACANT" : "FILLED";
                
                this.row();
                this.add(new VisLabel(profession.getName() + ":")).left();
                VisLabel statusLabel = new VisLabel(status);
                if (isVacant) {
                    statusLabel.setColor(UiTheme.color("status.vacant")); // Red for vacant
                } else {
                    statusLabel.setColor(UiTheme.color("status.filled")); // Green for filled
                }
                this.add(statusLabel).left().expandX();
                
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
                        com.rndmodgames.futtoboru.data.jobs.JobOpening existingOpening = jobManager.getAvailableJobs(club).stream()
                            .filter(job -> job != null && job.getProfessionId() != null && job.getProfessionId().equals(profession.getId()))
                            .findFirst()
                            .orElse(null);
                        
                        if (existingOpening != null) {
                            List<com.rndmodgames.futtoboru.data.jobs.JobApplication> playerApps = jobManager.getPlayerApplications(owner);
                            Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Checking " + playerApps.size() + " existing applications for opening ID: " + existingOpening.getId());
                            Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Checking " + playerApps.size() + " existing applications");
                            for (com.rndmodgames.futtoboru.data.jobs.JobApplication app : playerApps) {
                                if (app != null && app.getJobOpeningId() != null && app.getJobOpeningId().equals(existingOpening.getId()) && app.isActive()) {
                                    alreadyApplied = true;
                                    applicationStatus = app.getStatus();
                                    Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Found existing application: ID=" + app.getId() + ", Status=" + applicationStatus);
                                    Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Found existing application: ID=" + app.getId() + ", Status=" + applicationStatus);
                                    break;
                                }
                            }
                        }
                        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Already applied: " + alreadyApplied + ", Status: " + applicationStatus);
                        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Already applied: " + alreadyApplied + ", Status: " + applicationStatus);
                        
                        VisTextButton applyButton;
                        if (alreadyApplied) {
                            // Show status instead of Apply button
                            String statusText = "Applied";
                            if (applicationStatus == ApplicationStatus.OFFER_RECEIVED) {
                                statusText = "Offer Received";
                            } else if (applicationStatus == ApplicationStatus.REJECTED) {
                                statusText = "Rejected";
                            } else if (applicationStatus == ApplicationStatus.PENDING) {
                                statusText = "Pending";
                            }
                            applyButton = new VisTextButton(statusText);
                            applyButton.setDisabled(true); // Disable the button
                        } else {
                            applyButton = new VisTextButton("Apply");
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
                                                        self.showMessage("Application Submitted", 
                                                                      "Your application for " + finalClub.getName() + " (" + finalProfession.getName() + ") has been submitted successfully.");
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
                                                        self.showMessage("Application Failed", 
                                                                      "Unable to submit application. The position may no longer be available or you may have already applied.");
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
                        this.add(applyButton).width(UiTheme.size("col.number"));
                        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "Button added to table successfully");
                        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] Button added to table for: " + finalProfession.getName() + " at " + finalClub.getName());
                    }
                }
            }
        }
        
        Log.info(Log.UI_CLUB, "ClubDetailScreenTable", "=== updateDynamicComponents COMPLETE ===");
        Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] ===== updateDynamicComponents COMPLETE =====");
        
        // Back button
        this.addSeparator();
        this.row();
        VisTextButton backButton = new VisTextButton("Back to Club Browser");
        backButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (backButton.isPressed() && menuManager != null) {
                    menuManager.setActiveMainScreen(MainMenuManager.CLUB_BROWSER_SCREEN);
                }
            }
        });
        this.add(backButton).colspan(2).fillX().pad(UiTheme.space("xs"));
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
            VisDialog dialog = new VisDialog(title);
            dialog.text(message);
            dialog.button("OK");
            dialog.show(stage);
        } else {
            // Fallback to console if stage not available
            Log.info(Log.UI_CLUB, "ClubDetailScreenTable", title + ": " + message);
            Log.info(Log.UI_CLUB, "[ClubDetailScreenTable] " + title + ": " + message);
        }
    }
}

