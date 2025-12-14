package com.rndmodgames.futtoboru.tables.jobs;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.jobs.JobOffer;
import com.rndmodgames.futtoboru.engine.jobs.JobManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Negotiation Screen Table v1
 * 
 * Allows players to negotiate job offer terms.
 * 
 * @author Geomancer86
 */
public class NegotiationScreenTable extends VisTable {

    Game game;
    SaveGame currentGame;
    JobManager jobManager;
    MainMenuManager menuManager;
    JobOffer currentOffer;
    
    VisTextField salaryField;
    VisTextField contractLengthField;
    DecimalFormat df = new DecimalFormat("#,###.00");
    
    public NegotiationScreenTable(Game parent) {
        super(true);
        this.setDebug(true);
        
        this.game = parent;
        this.currentGame = ((Futtoboru)game).getCurrentGame();
        this.jobManager = ((Futtoboru)game).getJobManager();
        
        updateDynamicComponents();
    }
    
    public void setMenuManager(MainMenuManager menuManager) {
        this.menuManager = menuManager;
    }
    
    public void setCurrentOffer(JobOffer offer) {
        this.currentOffer = offer;
        updateDynamicComponents();
    }
    
    public void updateDynamicComponents() {
        this.clear();
        
        if (currentOffer == null) {
            this.row();
            this.add(new VisLabel("No offer selected for negotiation."));
            return;
        }
        
        // Get club info
        com.rndmodgames.futtoboru.data.jobs.JobOpening jobOpening = null;
        for (com.rndmodgames.futtoboru.data.jobs.JobOpening job : currentGame.getActiveJobOpenings()) {
            if (job.getId().equals(currentOffer.getJobOpeningId())) {
                jobOpening = job;
                break;
            }
        }
        
        Club club = jobOpening != null ? currentGame.getClubById(jobOpening.getClubId()) : null;
        
        // Title
        this.row();
        this.add(new VisLabel("Negotiate Job Offer" + (club != null ? " - " + club.getName() : ""))).colspan(2);
        this.row();
        this.addSeparator();
        
        // Current offer
        this.row();
        this.add(new VisLabel("Current Offer:")).colspan(2);
        this.row();
        this.add(new VisLabel("Salary: $" + df.format(currentOffer.getSalary())));
        this.add(new VisLabel("Contract: " + currentOffer.getContractLengthMonths() + " months"));
        this.row();
        this.add(new VisLabel("Negotiation Round: " + currentOffer.getNegotiationRound() + "/3")).colspan(2);
        this.row();
        this.addSeparator();
        
        // Counter-offer inputs
        this.row();
        this.add(new VisLabel("Counter-Offer:")).colspan(2);
        this.row();
        this.add(new VisLabel("New Salary:"));
        salaryField = new VisTextField(currentOffer.getSalary().toString());
        this.add(salaryField).width(200);
        this.row();
        this.add(new VisLabel("New Contract Length (months):"));
        contractLengthField = new VisTextField(currentOffer.getContractLengthMonths().toString());
        this.add(contractLengthField).width(200);
        this.row();
        this.addSeparator();
        
        // Buttons
        VisTable buttonsTable = new VisTable(true);
        
        // Submit counter-offer button
        VisTextButton submitButton = new VisTextButton("Submit Counter-Offer");
        submitButton.addCaptureListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            
            @Override
            public void touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                if (!submitButton.isPressed()) {
                    return;
                }
                if (jobManager != null && currentOffer != null) {
                    try {
                        BigDecimal newSalary = new BigDecimal(salaryField.getText());
                        Integer newContractLength = Integer.parseInt(contractLengthField.getText());
                        
                        JobOffer counterOffer = jobManager.submitCounterOffer(currentOffer, newSalary, newContractLength);
                        
                        if (counterOffer != null) {
                            Gdx.app.log("NegotiationScreenTable", "Counter-offer submitted");
                            // Go back to offers screen
                            if (menuManager != null) {
                                menuManager.setActiveMainScreen(MainMenuManager.JOB_OFFER_SCREEN);
                            }
                        } else {
                            Gdx.app.log("NegotiationScreenTable", "Counter-offer failed (may have been rejected)");
                            // Refresh to show updated offer status
                            updateDynamicComponents();
                        }
                    } catch (NumberFormatException e) {
                        Gdx.app.error("NegotiationScreenTable", "Invalid number format", e);
                    }
                }
            }
        });
        buttonsTable.add(submitButton);
        
        // Cancel button
        VisTextButton cancelButton = new VisTextButton("Cancel");
        cancelButton.addCaptureListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            
            @Override
            public void touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                if (cancelButton.isPressed() && menuManager != null) {
                    menuManager.setActiveMainScreen(MainMenuManager.JOB_OFFER_SCREEN);
                }
            }
        });
        buttonsTable.add(cancelButton);
        
        this.row();
        this.add(buttonsTable).colspan(2);
    }
}

