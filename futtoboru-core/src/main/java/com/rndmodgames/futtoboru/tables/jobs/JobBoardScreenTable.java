package com.rndmodgames.futtoboru.tables.jobs;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Profession;
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
        
        this.game = parent;
        this.currentGame = ((Futtoboru)game).getCurrentGame();
        this.jobManager = ((Futtoboru)game).getJobManager();
        
        // Title
        this.row();
        this.add(new VisLabel("Job Board")).colspan(2);
        this.row();
        this.addSeparator();
        
        // Jobs list
        jobsListTable = new VisTable(true);
        jobsListTable.setDebug(true);
        
        jobsScrollPane = new VisScrollPane(jobsListTable);
        jobsScrollPane.setFadeScrollBars(false);
        
        this.row();
        this.add(jobsScrollPane).grow().colspan(2);
        
        updateDynamicComponents();
    }
    
    public void updateDynamicComponents() {
        if (jobManager == null || currentGame == null) {
            return;
        }
        
        jobsListTable.clear();
        
        List<JobOpening> jobs = jobManager.getAvailableJobs();
        
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
            
            // Apply button
            VisTextButton applyButton = new VisTextButton("Apply");
            applyButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
                @Override
                public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                    if (jobManager != null && currentGame != null) {
                        jobManager.applyForJob(currentGame.getOwner(), job);
                        Gdx.app.log("JobBoardScreenTable", "Applied for job: " + job.getId());
                        updateDynamicComponents(); // Refresh
                    }
                }
            });
            jobsListTable.add(applyButton).width(100);
        }
    }
}

