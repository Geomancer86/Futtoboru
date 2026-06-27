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
import com.rndmodgames.futtoboru.system.DebugLogManager;
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
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "===== CONSTRUCTOR CALLED =====");
        
        this.game = parent;
        this.currentGame = ((Futtoboru)game).getCurrentGame();
        this.jobManager = ((Futtoboru)game).getJobManager();
        
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Game: " + (game != null ? "OK" : "NULL"));
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "CurrentGame: " + (currentGame != null ? "OK" : "NULL"));
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "JobManager: " + (jobManager != null ? "OK" : "NULL"));
        
        // Title
        this.row();
        this.add(new VisLabel("Job Board")).colspan(2);
        this.row();
        this.addSeparator();
        
        // Jobs list
        jobsListTable = new VisTable(true);
        jobsListTable.setDebug(true);
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Created jobsListTable");
        
        jobsScrollPane = new VisScrollPane(jobsListTable);
        jobsScrollPane.setFadeScrollBars(false);
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Created jobsScrollPane");
        
        this.row();
        this.add(jobsScrollPane).grow().colspan(2);
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Added jobsScrollPane to main table");
        
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Calling updateDynamicComponents from constructor...");
        updateDynamicComponents();
        
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "=== CONSTRUCTOR COMPLETE ===");
    }
    
    public void updateDynamicComponents() {
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "=== updateDynamicComponents START ===");
        
        if (jobManager == null) {
            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "jobManager is NULL!");
            return;
        }
        
        if (currentGame == null) {
            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "currentGame is NULL!");
            return;
        }
        
        jobsListTable.clear();
        
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Getting available jobs...");
        List<JobOpening> jobs = jobManager.getAvailableJobs();
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Found " + (jobs != null ? jobs.size() : 0) + " available jobs");
        
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
                DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Checking " + playerApps.size() + " existing applications for job ID: " + jobId);
                for (JobApplication app : playerApps) {
                    if (app != null && app.getJobOpeningId() != null && app.getJobOpeningId().equals(jobId) && app.isActive()) {
                        alreadyApplied = true;
                        applicationStatus = app.getStatus();
                        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Found existing application: ID=" + app.getId() + ", Status=" + applicationStatus);
                        break;
                    }
                }
            }
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Already applied: " + alreadyApplied + ", Status: " + applicationStatus);
            
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
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Creating Apply button for job ID: " + jobId + ", Club: " + (jobClub != null ? jobClub.getName() : "NULL") + ", Profession: " + (jobProfession != null ? jobProfession.getName() : "NULL"));
            
            // Verify button is not null
            if (applyButton == null) {
                DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "CRITICAL: applyButton is NULL after creation!");
                continue; // Skip this job
            }
            
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Button created successfully, adding listener...");
            
            // Only add listener if not already applied
            if (!alreadyApplied) {
                applyButton.addCaptureListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
                @Override
                public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                    DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "*** TOUCH DOWN on Apply button! Job ID: " + jobId + ", Club: " + (jobClub != null ? jobClub.getName() : "NULL") + ", Profession: " + (jobProfession != null ? jobProfession.getName() : "NULL"));
                    return true; // Return true to indicate we want to receive the touchUp event
                }
                
                @Override
                public void touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                    DebugLogManager.getInstance().debug(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "touchUp called, checking isPressed...");
                    if (!applyButton.isPressed()) {
                        DebugLogManager.getInstance().debug(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Button not pressed, ignoring");
                        return; // Button was released outside, ignore
                    }
                    DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Button IS pressed, processing click!");
                    try {
                        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "*** CLICKED EVENT FIRED *** Job ID: " + jobId + ", Club: " + (jobClub != null ? jobClub.getName() : "NULL") + ", Profession: " + (jobProfession != null ? jobProfession.getName() : "NULL"));
                        
                        if (jobManager == null) {
                            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "jobManager is NULL!");
                            return;
                        }
                        
                        if (currentGame == null) {
                            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "currentGame is NULL!");
                            return;
                        }
                        
                        Person owner = currentGame.getOwner();
                        if (owner == null) {
                            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "currentGame.getOwner() is NULL!");
                            return;
                        }
                        
                        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Calling applyForJob for: " + owner.getName());
                        
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
                            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Could not find job opening with ID: " + jobId);
                            return;
                        }
                        
                        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Found job opening, calling applyForJob for job ID: " + jobId);
                        JobApplication result = jobManager.applyForJob(owner, jobToApply);
                        
                        if (result != null) {
                            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Application successful: ID=" + result.getId() + ", Status=" + result.getStatus());
                            
                            // Show success message
                            showMessage("Application Submitted", 
                                      "Your application for " + jobClub.getName() + " (" + jobProfession.getName() + ") has been submitted successfully.");
                            
                            // Force immediate refresh to show updated status
                            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Forcing immediate screen refresh...");
                            
                            // Post to main thread to ensure UI update happens
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Refreshing screen in postRunnable...");
                                    updateDynamicComponents();
                                    DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Screen refresh completed in postRunnable");
                                }
                            });
                        } else {
                            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Application returned null (may be rejected or already exists)");
                            
                            // Show error message
                            showMessage("Application Failed", 
                                      "Unable to submit application. The position may no longer be available or you may have already applied.");
                        }
                        
                        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "=== Apply button click completed ===");
                        
                    } catch (Exception e) {
                        DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "CRITICAL ERROR in Apply button click!", e);
                    }
                }
                });
            } // End if (!alreadyApplied)
            
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Listener added to button, now adding button to table...");
            jobsListTable.add(applyButton).width(100);
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "Button added to table successfully for job ID: " + jobId);
        }
        
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", "=== updateDynamicComponents COMPLETE === Total buttons created: " + (jobs != null ? jobs.size() : 0));
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
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_JOBS, "JobBoardScreenTable", title + ": " + message);
        }
    }
}

