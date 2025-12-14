package com.rndmodgames.futtoboru.tables.club;

import java.math.BigDecimal;
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
import com.rndmodgames.futtoboru.data.jobs.JobApplication;
import com.rndmodgames.futtoboru.engine.jobs.ClubStaffManager;
import com.rndmodgames.futtoboru.engine.jobs.JobManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

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
    
    DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    
    public ClubDetailScreenTable(Game parent) {
        super(true);
        this.setDebug(true);
        
        Gdx.app.log("ClubDetailScreenTable", "=== CONSTRUCTOR START ===");
        System.out.println("[ClubDetailScreenTable] ===== CONSTRUCTOR CALLED =====");
        
        this.game = ((Futtoboru) parent);
        this.currentGame = game.getCurrentGame();
        this.jobManager = game.getJobManager();
        this.clubStaffManager = game.getClubStaffManager();
        
        Gdx.app.log("ClubDetailScreenTable", "Game: " + (game != null ? "OK" : "NULL"));
        Gdx.app.log("ClubDetailScreenTable", "CurrentGame: " + (currentGame != null ? "OK" : "NULL"));
        Gdx.app.log("ClubDetailScreenTable", "JobManager: " + (jobManager != null ? "OK" : "NULL"));
        Gdx.app.log("ClubDetailScreenTable", "ClubStaffManager: " + (clubStaffManager != null ? "OK" : "NULL"));
        System.out.println("[ClubDetailScreenTable] Game: " + (game != null ? "OK" : "NULL"));
        System.out.println("[ClubDetailScreenTable] CurrentGame: " + (currentGame != null ? "OK" : "NULL"));
        System.out.println("[ClubDetailScreenTable] JobManager: " + (jobManager != null ? "OK" : "NULL"));
        System.out.println("[ClubDetailScreenTable] ClubStaffManager: " + (clubStaffManager != null ? "OK" : "NULL"));
        
        Gdx.app.log("ClubDetailScreenTable", "=== CONSTRUCTOR COMPLETE ===");
        System.out.println("[ClubDetailScreenTable] ===== CONSTRUCTOR COMPLETE =====");
    }
    
    /**
     * Update to display a specific club
     * 
     * @param club The club to display (can be null)
     */
    public void updateDynamicComponents(Club club) {
        Gdx.app.log("ClubDetailScreenTable", "=== updateDynamicComponents START ===");
        System.out.println("[ClubDetailScreenTable] updateDynamicComponents called for club: " + (club != null ? club.getName() : "NULL"));
        
        this.clear();
        
        if (club == null) {
            Gdx.app.log("ClubDetailScreenTable", "Club is null, showing message");
            this.add(new VisLabel("No club selected."));
            return;
        }
        
        Gdx.app.log("ClubDetailScreenTable", "Updating display for club: " + club.getName());
        
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
        
        // Stadium
        if (club.getStadium() != null) {
            this.addSeparator();
            this.row();
            this.add(new VisLabel("Stadium:")).left();
            this.add(new VisLabel(club.getStadium().getName())).left().expandX();
            this.row();
            
            if (club.getStadium().getCapacity() != null) {
                this.add(new VisLabel("Capacity:")).left();
                this.add(new VisLabel(club.getStadium().getCapacity().toString())).left().expandX();
                this.row();
            }
        }
        
        // Finances
        if (club.getClubBalance() != null) {
            this.addSeparator();
            this.row();
            this.add(new VisLabel("Balance:")).left();
            this.add(new VisLabel(currencyFormat.format(club.getClubBalance()))).left().expandX();
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
                if (profession.getId().equals(1L) || profession.getId().equals(2L)) {
                    continue;
                }
                
                boolean isVacant = vacantPositions.contains(profession);
                String status = isVacant ? "VACANT" : "FILLED";
                
                this.row();
                this.add(new VisLabel(profession.getName() + ":")).left();
                VisLabel statusLabel = new VisLabel(status);
                if (isVacant) {
                    statusLabel.setColor(1, 0, 0, 1); // Red for vacant
                } else {
                    statusLabel.setColor(0, 1, 0, 1); // Green for filled
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
                            Gdx.app.log("ClubDetailScreenTable", "Checking " + playerApps.size() + " existing applications for opening ID: " + existingOpening.getId());
                            System.out.println("[ClubDetailScreenTable] Checking " + playerApps.size() + " existing applications");
                            for (com.rndmodgames.futtoboru.data.jobs.JobApplication app : playerApps) {
                                if (app != null && app.getJobOpeningId() != null && app.getJobOpeningId().equals(existingOpening.getId()) && app.isActive()) {
                                    alreadyApplied = true;
                                    applicationStatus = app.getStatus();
                                    Gdx.app.log("ClubDetailScreenTable", "Found existing application: ID=" + app.getId() + ", Status=" + applicationStatus);
                                    System.out.println("[ClubDetailScreenTable] Found existing application: ID=" + app.getId() + ", Status=" + applicationStatus);
                                    break;
                                }
                            }
                        }
                        Gdx.app.log("ClubDetailScreenTable", "Already applied: " + alreadyApplied + ", Status: " + applicationStatus);
                        System.out.println("[ClubDetailScreenTable] Already applied: " + alreadyApplied + ", Status: " + applicationStatus);
                        
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
                        Gdx.app.log("ClubDetailScreenTable", ">>> Creating Apply button for: " + finalProfession.getName() + " at " + finalClub.getName());
                        System.out.println("[ClubDetailScreenTable] >>> Creating Apply button");
                        System.out.println("[ClubDetailScreenTable]     Profession: " + finalProfession.getName());
                        System.out.println("[ClubDetailScreenTable]     Club: " + finalClub.getName());
                        
                        if (applyButton == null) {
                            Gdx.app.error("ClubDetailScreenTable", "CRITICAL: applyButton is NULL after creation!");
                            System.err.println("[ClubDetailScreenTable] CRITICAL ERROR: applyButton is NULL!");
                        } else {
                            Gdx.app.log("ClubDetailScreenTable", "Button created successfully, adding listener...");
                            System.out.println("[ClubDetailScreenTable] Button created, adding listener...");
                            
                            // Only add listener if not already applied
                            if (!alreadyApplied) {
                                applyButton.addCaptureListener(new InputListener() {
                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                Gdx.app.log("ClubDetailScreenTable", "*** TOUCH DOWN on Apply button! ***");
                                System.out.println("========================================");
                                System.out.println("[ClubDetailScreenTable] *** TOUCH DOWN EVENT ***");
                                System.out.println("[ClubDetailScreenTable] Club: " + (finalClub != null ? finalClub.getName() : "NULL"));
                                System.out.println("[ClubDetailScreenTable] Profession: " + (finalProfession != null ? finalProfession.getName() : "NULL"));
                                System.out.println("[ClubDetailScreenTable] Coordinates: x=" + x + ", y=" + y);
                                System.out.println("========================================");
                                return true; // Return true to indicate we want to receive the touchUp event
                            }
                            
                                    @Override
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                if (!applyButton.isPressed()) {
                                    return; // Button was released outside, ignore
                                }
                                Gdx.app.log("ClubDetailScreenTable", "*** CLICKED EVENT FIRED ***");
                                System.out.println("========================================");
                                System.out.println("[ClubDetailScreenTable] *** CLICKED EVENT ***");
                                System.out.println("[ClubDetailScreenTable] Club: " + (finalClub != null ? finalClub.getName() : "NULL"));
                                System.out.println("[ClubDetailScreenTable] Profession: " + (finalProfession != null ? finalProfession.getName() : "NULL"));
                                System.out.println("========================================");
                                try {
                                    Gdx.app.log("ClubDetailScreenTable", "=== Apply button clicked ===");
                                    System.out.println("[ClubDetailScreenTable] Apply button clicked for: " + finalProfession.getName() + " at " + finalClub.getName());
                                    
                                    if (jobManager == null) {
                                        Gdx.app.error("ClubDetailScreenTable", "jobManager is NULL!");
                                        System.err.println("[ClubDetailScreenTable] ERROR: jobManager is NULL!");
                                        return;
                                    }
                                    
                                    if (currentGame == null) {
                                        Gdx.app.error("ClubDetailScreenTable", "currentGame is NULL!");
                                        System.err.println("[ClubDetailScreenTable] ERROR: currentGame is NULL!");
                                        return;
                                    }
                                    
                                    Person owner = currentGame.getOwner();
                                    if (owner == null) {
                                        Gdx.app.error("ClubDetailScreenTable", "currentGame.getOwner() is NULL!");
                                        System.err.println("[ClubDetailScreenTable] ERROR: currentGame.getOwner() is NULL!");
                                        return;
                                    }
                                    
                                    Gdx.app.log("ClubDetailScreenTable", "Step 1: Finding or creating job opening...");
                                    System.out.println("[ClubDetailScreenTable] Step 1: Finding or creating job opening...");
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
                                        Gdx.app.error("ClubDetailScreenTable", "Error getting available jobs", e);
                                        System.err.println("[ClubDetailScreenTable] ERROR getting available jobs:");
                                        e.printStackTrace();
                                    }
                                    
                                    if (jobOpening == null) {
                                        Gdx.app.log("ClubDetailScreenTable", "No existing job opening found, creating new one...");
                                        // Create job opening if it doesn't exist
                                        try {
                                            jobOpening = jobManager.createJobOpening(finalClub, finalProfession);
                                            if (jobOpening == null) {
                                                Gdx.app.error("ClubDetailScreenTable", "createJobOpening returned NULL!");
                                                System.err.println("[ClubDetailScreenTable] ERROR: createJobOpening returned NULL!");
                                                return;
                                            }
                                            Gdx.app.log("ClubDetailScreenTable", "Job opening created: " + jobOpening.getId());
                                        } catch (Exception e) {
                                            Gdx.app.error("ClubDetailScreenTable", "ERROR creating job opening!", e);
                                            System.err.println("[ClubDetailScreenTable] ERROR creating job opening:");
                                            e.printStackTrace();
                                            return;
                                        }
                                    } else {
                                        Gdx.app.log("ClubDetailScreenTable", "Found existing job opening: " + jobOpening.getId());
                                    }
                                    
                                    if (jobOpening != null) {
                                        Gdx.app.log("ClubDetailScreenTable", "Step 2: Applying for job...");
                                        // Apply for the job
                                        try {
                                            com.rndmodgames.futtoboru.data.jobs.JobApplication result = 
                                                jobManager.applyForJob(owner, jobOpening);
                                            
                                            if (result != null) {
                                                Gdx.app.log("ClubDetailScreenTable", "Application successful: " + result.getId());
                                                System.out.println("[ClubDetailScreenTable] Applied for " + finalProfession.getName() + " at " + finalClub.getName() + " - SUCCESS");
                                                System.out.println("[ClubDetailScreenTable] Application ID: " + result.getId() + ", Status: " + result.getStatus());
                                                
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
                                                Gdx.app.log("ClubDetailScreenTable", "Forcing immediate screen refresh...");
                                                System.out.println("[ClubDetailScreenTable] Forcing immediate screen refresh...");
                                                
                                                // Post to main thread to ensure UI update happens
                                                Gdx.app.postRunnable(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Gdx.app.log("ClubDetailScreenTable", "Refreshing screen in postRunnable...");
                                                        System.out.println("[ClubDetailScreenTable] Refreshing screen in postRunnable...");
                                                        try {
                                                            updateDynamicComponents(finalClub);
                                                            Gdx.app.log("ClubDetailScreenTable", "Display refreshed successfully in postRunnable");
                                                            System.out.println("[ClubDetailScreenTable] Display refreshed successfully");
                                                        } catch (Exception e) {
                                                            Gdx.app.error("ClubDetailScreenTable", "ERROR refreshing display in postRunnable!", e);
                                                            System.err.println("[ClubDetailScreenTable] ERROR refreshing display:");
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                            } else {
                                                Gdx.app.log("ClubDetailScreenTable", "Application returned null (may be rejected or already exists)");
                                                System.out.println("[ClubDetailScreenTable] Application returned null");
                                                
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
                                            Gdx.app.error("ClubDetailScreenTable", "ERROR applying for job!", e);
                                            System.err.println("[ClubDetailScreenTable] ERROR applying for job:");
                                            e.printStackTrace();
                                            return;
                                        }
                                    }
                                    
                                    Gdx.app.log("ClubDetailScreenTable", "=== Apply button click completed ===");
                                    
                                } catch (Exception e) {
                                    Gdx.app.error("ClubDetailScreenTable", "CRITICAL ERROR in Apply button click!", e);
                                    System.err.println("========================================");
                                    System.err.println("[ClubDetailScreenTable] CRITICAL ERROR in Apply button!");
                                    System.err.println("Exception: " + e.getClass().getName());
                                    System.err.println("Message: " + e.getMessage());
                                    System.err.println("========================================");
                                    e.printStackTrace();
                                    System.err.println("========================================");
                                    }
                                }
                            });
                            } // End if (!alreadyApplied)
                        } // End else block
                        
                        Gdx.app.log("ClubDetailScreenTable", "Listener added to button, now adding button to table...");
                        System.out.println("[ClubDetailScreenTable] Adding button to table...");
                        this.add(applyButton).width(100);
                        Gdx.app.log("ClubDetailScreenTable", "Button added to table successfully");
                        System.out.println("[ClubDetailScreenTable] Button added to table for: " + finalProfession.getName() + " at " + finalClub.getName());
                    }
                }
            }
        }
        
        Gdx.app.log("ClubDetailScreenTable", "=== updateDynamicComponents COMPLETE ===");
        System.out.println("[ClubDetailScreenTable] ===== updateDynamicComponents COMPLETE =====");
        
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
        this.add(backButton).colspan(2).fillX().pad(5);
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
            Gdx.app.log("ClubDetailScreenTable", title + ": " + message);
            System.out.println("[ClubDetailScreenTable] " + title + ": " + message);
        }
    }
}

