package com.rndmodgames.futtoboru.tables.jobs;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.jobs.ApplicationStatus;
import com.rndmodgames.futtoboru.data.jobs.JobApplication;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;
import com.rndmodgames.futtoboru.engine.jobs.JobManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Job Board Screen Table v1
 * 
 * Displays all available job openings with apply functionality.
 * 
 * @author Geomancer86
 */
public class JobBoardScreenTable extends VisTable {

    Game game;
    SaveGame currentGame;
    JobManager jobManager;
    
    VisTable jobsListTable;
    VisScrollPane jobsScrollPane;
    DecimalFormat df = new DecimalFormat("#,###.00");
    
    public JobBoardScreenTable(Game parent) {
        super(true);
        this.setDebug(true);
        
        // Force output immediately
        System.out.println("[JobBoardScreenTable] ===== CONSTRUCTOR CALLED =====");
        System.out.flush();
        System.err.println("[JobBoardScreenTable] ===== CONSTRUCTOR CALLED (stderr) =====");
        System.err.flush();
        Gdx.app.log("JobBoardScreenTable", "=== CONSTRUCTOR START ===");
        
        this.game = parent;
        this.currentGame = ((Futtoboru)game).getCurrentGame();
        this.jobManager = ((Futtoboru)game).getJobManager();
        
        Gdx.app.log("JobBoardScreenTable", "Game: " + (game != null ? "OK" : "NULL"));
        Gdx.app.log("JobBoardScreenTable", "CurrentGame: " + (currentGame != null ? "OK" : "NULL"));
        Gdx.app.log("JobBoardScreenTable", "JobManager: " + (jobManager != null ? "OK" : "NULL"));
        System.out.println("[JobBoardScreenTable] Game: " + (game != null ? "OK" : "NULL"));
        System.out.println("[JobBoardScreenTable] CurrentGame: " + (currentGame != null ? "OK" : "NULL"));
        System.out.println("[JobBoardScreenTable] JobManager: " + (jobManager != null ? "OK" : "NULL"));
        
        // Title
        this.row();
        this.add(new VisLabel("Job Board")).colspan(2);
        this.row();
        this.addSeparator();
        
        // Jobs list
        jobsListTable = new VisTable(true);
        jobsListTable.setDebug(true);
        Gdx.app.log("JobBoardScreenTable", "Created jobsListTable");
        System.out.println("[JobBoardScreenTable] Created jobsListTable");
        
        jobsScrollPane = new VisScrollPane(jobsListTable);
        jobsScrollPane.setFadeScrollBars(false);
        Gdx.app.log("JobBoardScreenTable", "Created jobsScrollPane");
        System.out.println("[JobBoardScreenTable] Created jobsScrollPane");
        
        this.row();
        this.add(jobsScrollPane).grow().colspan(2);
        Gdx.app.log("JobBoardScreenTable", "Added jobsScrollPane to main table");
        System.out.println("[JobBoardScreenTable] Added jobsScrollPane to main table");
        
        Gdx.app.log("JobBoardScreenTable", "Calling updateDynamicComponents from constructor...");
        System.out.println("[JobBoardScreenTable] Calling updateDynamicComponents from constructor...");
        updateDynamicComponents();
        
        Gdx.app.log("JobBoardScreenTable", "=== CONSTRUCTOR COMPLETE ===");
        System.out.println("[JobBoardScreenTable] ===== CONSTRUCTOR COMPLETE =====");
    }
    
    public void updateDynamicComponents() {
        Gdx.app.log("JobBoardScreenTable", "=== updateDynamicComponents START ===");
        System.out.println("[JobBoardScreenTable] updateDynamicComponents called");
        
        if (jobManager == null) {
            Gdx.app.error("JobBoardScreenTable", "jobManager is NULL!");
            System.err.println("[JobBoardScreenTable] ERROR: jobManager is NULL!");
            return;
        }
        
        if (currentGame == null) {
            Gdx.app.error("JobBoardScreenTable", "currentGame is NULL!");
            System.err.println("[JobBoardScreenTable] ERROR: currentGame is NULL!");
            return;
        }
        
        jobsListTable.clear();
        
        Gdx.app.log("JobBoardScreenTable", "Getting available jobs...");
        List<JobOpening> jobs = jobManager.getAvailableJobs();
        Gdx.app.log("JobBoardScreenTable", "Found " + (jobs != null ? jobs.size() : 0) + " available jobs");
        System.out.println("[JobBoardScreenTable] Found " + (jobs != null ? jobs.size() : 0) + " available jobs");
        
        if (jobs.isEmpty()) {
            jobsListTable.row();
            jobsListTable.add(new VisLabel("No job openings available at this time.")).colspan(3);
            return;
        }
        
        // Header
        jobsListTable.row();
        jobsListTable.add(new VisLabel("Club")).width(200);
        jobsListTable.add(new VisLabel("Position")).width(150);
        jobsListTable.add(new VisLabel("Salary")).width(100);
        jobsListTable.add(new VisLabel("Contract")).width(100);
        jobsListTable.add(new VisLabel("Deadline")).width(150);
        jobsListTable.add(new VisLabel("Action")).width(100);
        jobsListTable.row();
        jobsListTable.addSeparator();
        
        // Jobs
        for (JobOpening job : jobs) {
            Club club = currentGame.getClubById(job.getClubId());
            Profession profession = DatabaseLoader.getProfessionById(job.getProfessionId());
            
            if (club == null || profession == null) {
                continue;
            }
            
            jobsListTable.row();
            jobsListTable.add(new VisLabel(club.getName())).width(200);
            jobsListTable.add(new VisLabel(profession.getName())).width(150);
            jobsListTable.add(new VisLabel("$" + df.format(job.getSalary()))).width(100);
            jobsListTable.add(new VisLabel(job.getContractLengthMonths() + " months")).width(100);
            jobsListTable.add(new VisLabel(job.getApplicationDeadline().toString())).width(150);
            
            // Apply button - Check if already applied
            final Long jobId = job.getId(); // Make final for inner class
            final Club jobClub = club; // Make final for inner class
            final Profession jobProfession = profession; // Make final for inner class
            
            // Check if player has already applied for this job
            Person owner = currentGame.getOwner();
            boolean alreadyApplied = false;
            ApplicationStatus applicationStatus = null;
            if (owner != null && jobManager != null) {
                List<JobApplication> playerApps = jobManager.getPlayerApplications(owner);
                Gdx.app.log("JobBoardScreenTable", "Checking " + playerApps.size() + " existing applications for job ID: " + jobId);
                System.out.println("[JobBoardScreenTable] Checking " + playerApps.size() + " existing applications for job ID: " + jobId);
                for (JobApplication app : playerApps) {
                    if (app != null && app.getJobOpeningId() != null && app.getJobOpeningId().equals(jobId) && app.isActive()) {
                        alreadyApplied = true;
                        applicationStatus = app.getStatus();
                        Gdx.app.log("JobBoardScreenTable", "Found existing application: ID=" + app.getId() + ", Status=" + applicationStatus);
                        System.out.println("[JobBoardScreenTable] Found existing application: ID=" + app.getId() + ", Status=" + applicationStatus);
                        break;
                    }
                }
            }
            Gdx.app.log("JobBoardScreenTable", "Already applied: " + alreadyApplied + ", Status: " + applicationStatus);
            System.out.println("[JobBoardScreenTable] Already applied: " + alreadyApplied + ", Status: " + applicationStatus);
            
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
            Gdx.app.log("JobBoardScreenTable", "Creating Apply button for job ID: " + jobId + ", Club: " + (jobClub != null ? jobClub.getName() : "NULL") + ", Profession: " + (jobProfession != null ? jobProfession.getName() : "NULL"));
            System.out.println("[JobBoardScreenTable] >>> Creating Apply button for job ID: " + jobId);
            System.out.println("[JobBoardScreenTable]     Club: " + (jobClub != null ? jobClub.getName() : "NULL"));
            System.out.println("[JobBoardScreenTable]     Profession: " + (jobProfession != null ? jobProfession.getName() : "NULL"));
            
            // Verify button is not null
            if (applyButton == null) {
                Gdx.app.error("JobBoardScreenTable", "CRITICAL: applyButton is NULL after creation!");
                System.err.println("[JobBoardScreenTable] CRITICAL ERROR: applyButton is NULL!");
                continue; // Skip this job
            }
            
            Gdx.app.log("JobBoardScreenTable", "Button created successfully, adding listener...");
            System.out.println("[JobBoardScreenTable] Button created, adding listener...");
            
            // Only add listener if not already applied
            if (!alreadyApplied) {
                applyButton.addCaptureListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
                @Override
                public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                    System.err.println("========================================");
                    System.err.println("[JobBoardScreenTable] *** TOUCH DOWN EVENT ***");
                    System.err.println("[JobBoardScreenTable] Job ID: " + jobId);
                    System.err.println("[JobBoardScreenTable] Club: " + (jobClub != null ? jobClub.getName() : "NULL"));
                    System.err.println("[JobBoardScreenTable] Profession: " + (jobProfession != null ? jobProfession.getName() : "NULL"));
                    System.err.println("[JobBoardScreenTable] Coordinates: x=" + x + ", y=" + y);
                    System.err.println("[JobBoardScreenTable] Pointer: " + pointer + ", Button: " + button);
                    System.err.println("========================================");
                    System.err.flush();
                    System.out.println("========================================");
                    System.out.println("[JobBoardScreenTable] *** TOUCH DOWN EVENT ***");
                    System.out.println("[JobBoardScreenTable] Job ID: " + jobId);
                    System.out.flush();
                    Gdx.app.log("JobBoardScreenTable", "*** TOUCH DOWN on Apply button! ***");
                    return true; // Return true to indicate we want to receive the touchUp event
                }
                
                @Override
                public void touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                    System.err.println("[JobBoardScreenTable] touchUp called, checking isPressed...");
                    System.err.flush();
                    if (!applyButton.isPressed()) {
                        System.err.println("[JobBoardScreenTable] Button not pressed, ignoring");
                        System.err.flush();
                        return; // Button was released outside, ignore
                    }
                    System.err.println("[JobBoardScreenTable] Button IS pressed, processing click!");
                    System.err.flush();
                    try {
                        System.err.println("========================================");
                        System.err.println("[JobBoardScreenTable] *** CLICKED EVENT ***");
                        System.err.println("[JobBoardScreenTable] Job ID: " + jobId);
                        System.err.println("[JobBoardScreenTable] Club: " + (jobClub != null ? jobClub.getName() : "NULL"));
                        System.err.println("[JobBoardScreenTable] Profession: " + (jobProfession != null ? jobProfession.getName() : "NULL"));
                        System.err.println("========================================");
                        System.err.flush();
                        System.out.println("========================================");
                        System.out.println("[JobBoardScreenTable] *** CLICKED EVENT ***");
                        System.out.println("[JobBoardScreenTable] Job ID: " + jobId);
                        System.out.flush();
                        Gdx.app.log("JobBoardScreenTable", "*** CLICKED EVENT FIRED ***");
                        
                        if (jobManager == null) {
                            Gdx.app.error("JobBoardScreenTable", "jobManager is NULL!");
                            System.err.println("[JobBoardScreenTable] ERROR: jobManager is NULL!");
                            return;
                        }
                        
                        if (currentGame == null) {
                            Gdx.app.error("JobBoardScreenTable", "currentGame is NULL!");
                            System.err.println("[JobBoardScreenTable] ERROR: currentGame is NULL!");
                            return;
                        }
                        
                        Person owner = currentGame.getOwner();
                        if (owner == null) {
                            Gdx.app.error("JobBoardScreenTable", "currentGame.getOwner() is NULL!");
                            System.err.println("[JobBoardScreenTable] ERROR: currentGame.getOwner() is NULL!");
                            return;
                        }
                        
                        Gdx.app.log("JobBoardScreenTable", "Calling applyForJob...");
                        System.out.println("[JobBoardScreenTable] Calling applyForJob for: " + owner.getName());
                        
                        // Get the job opening object again (using the captured jobId)
                        JobOpening jobToApply = null;
                        List<JobOpening> allJobs = jobManager.getAvailableJobs();
                        if (allJobs != null) {
                            for (JobOpening j : allJobs) {
                                if (j != null && j.getId() != null && j.getId().equals(jobId)) {
                                    jobToApply = j;
                                    break;
                                }
                            }
                        }
                        
                        if (jobToApply == null) {
                            Gdx.app.error("JobBoardScreenTable", "Could not find job opening with ID: " + jobId);
                            System.err.println("[JobBoardScreenTable] ERROR: Could not find job opening!");
                            return;
                        }
                        
                        Gdx.app.log("JobBoardScreenTable", "Found job opening, calling applyForJob...");
                        System.out.println("[JobBoardScreenTable] Calling applyForJob for job ID: " + jobId);
                        JobApplication result = jobManager.applyForJob(owner, jobToApply);
                        
                        if (result != null) {
                            Gdx.app.log("JobBoardScreenTable", "Application successful: " + result.getId());
                            System.out.println("[JobBoardScreenTable] Application successful! ID: " + result.getId());
                            System.out.println("[JobBoardScreenTable] Application status: " + result.getStatus());
                            
                            // Show success message
                            showMessage("Application Submitted", 
                                      "Your application for " + jobClub.getName() + " (" + jobProfession.getName() + ") has been submitted successfully.");
                            
                            // Force immediate refresh to show updated status
                            Gdx.app.log("JobBoardScreenTable", "Forcing immediate screen refresh...");
                            System.out.println("[JobBoardScreenTable] Forcing immediate screen refresh...");
                            
                            // Post to main thread to ensure UI update happens
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    Gdx.app.log("JobBoardScreenTable", "Refreshing screen in postRunnable...");
                                    System.out.println("[JobBoardScreenTable] Refreshing screen in postRunnable...");
                                    updateDynamicComponents();
                                    Gdx.app.log("JobBoardScreenTable", "Screen refresh completed in postRunnable");
                                    System.out.println("[JobBoardScreenTable] Screen refresh completed");
                                }
                            });
                        } else {
                            Gdx.app.log("JobBoardScreenTable", "Application returned null (may be rejected or already exists)");
                            System.out.println("[JobBoardScreenTable] Application returned null");
                            
                            // Show error message
                            showMessage("Application Failed", 
                                      "Unable to submit application. The position may no longer be available or you may have already applied.");
                        }
                        
                        Gdx.app.log("JobBoardScreenTable", "=== Apply button click completed ===");
                        
                    } catch (Exception e) {
                        Gdx.app.error("JobBoardScreenTable", "CRITICAL ERROR in Apply button click!", e);
                        System.err.println("========================================");
                        System.err.println("[JobBoardScreenTable] CRITICAL ERROR in Apply button!");
                        System.err.println("Exception: " + e.getClass().getName());
                        System.err.println("Message: " + e.getMessage());
                        System.err.println("========================================");
                        e.printStackTrace();
                        System.err.println("========================================");
                    }
                }
                });
            } // End if (!alreadyApplied)
            
            Gdx.app.log("JobBoardScreenTable", "Listener added to button, now adding button to table...");
            System.out.println("[JobBoardScreenTable] Adding button to jobsListTable...");
            jobsListTable.add(applyButton).width(100);
            Gdx.app.log("JobBoardScreenTable", "Button added to table successfully for job ID: " + jobId);
            System.out.println("[JobBoardScreenTable] Button added to table for job ID: " + jobId);
        }
        
        Gdx.app.log("JobBoardScreenTable", "=== updateDynamicComponents COMPLETE ===");
        System.out.println("[JobBoardScreenTable] ===== updateDynamicComponents COMPLETE =====");
        System.out.println("[JobBoardScreenTable] Total buttons created: " + (jobs != null ? jobs.size() : 0));
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
            Gdx.app.log("JobBoardScreenTable", title + ": " + message);
            System.out.println("[JobBoardScreenTable] " + title + ": " + message);
        }
    }
}

