package com.rndmodgames.futtoboru.tables.jobs;

import com.rndmodgames.localization.LanguageModLoader;
import java.math.BigDecimal;
import com.rndmodgames.futtoboru.engine.finances.Money;
import java.text.DecimalFormat;

import com.badlogic.gdx.Game;
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
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.system.Log;

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
            this.add(new VisLabel(LanguageModLoader.getValue("jobs_no_offer_selected")));
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
        this.add(new VisLabel(club != null ? LanguageModLoader.format("jobs_negotiate_offer_club", club.getName()) : LanguageModLoader.getValue("jobs_negotiate_offer"))).colspan(2);
        this.row();
        this.addSeparator().colspan(2); // V-01: a one-column separator pushed column 2 to the right edge
        
        // Current offer
        this.row();
        this.add(new VisLabel(LanguageModLoader.getValue("jobs_current_offer"))).colspan(2);
        this.row();
        this.add(new VisLabel(LanguageModLoader.format("jobs_salary_value", Money.format(currentOffer.getSalary()))));
        this.add(new VisLabel(LanguageModLoader.format("jobs_contract_value", currentOffer.getContractLengthMonths())));
        this.row();
        this.add(new VisLabel(LanguageModLoader.format("jobs_negotiation_round", currentOffer.getNegotiationRound(), 3))).colspan(2);
        this.row();
        this.addSeparator().colspan(2); // V-01: a one-column separator pushed column 2 to the right edge
        
        // Counter-offer inputs
        this.row();
        this.add(new VisLabel(LanguageModLoader.getValue("jobs_counter_offer"))).colspan(2);
        this.row();
        this.add(new VisLabel(LanguageModLoader.getValue("jobs_new_salary")));
        salaryField = new VisTextField(currentOffer.getSalary().toString());
        this.add(salaryField).width(UiTheme.size("col.wide"));
        this.row();
        this.add(new VisLabel(LanguageModLoader.getValue("jobs_new_contract_length")));
        contractLengthField = new VisTextField(currentOffer.getContractLengthMonths().toString());
        this.add(contractLengthField).width(UiTheme.size("col.wide"));
        this.row();
        this.addSeparator().colspan(2); // V-01: a one-column separator pushed column 2 to the right edge
        
        // Buttons
        VisTable buttonsTable = new VisTable(true);
        
        // Submit counter-offer button
        VisTextButton submitButton = new VisTextButton(LanguageModLoader.getValue("jobs_submit_counter_offer"));
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
                            Log.info(Log.UI_JOBS, "NegotiationScreenTable", "Counter-offer submitted");
                            // Go back to offers screen
                            if (menuManager != null) {
                                menuManager.setActiveMainScreen(MainMenuManager.JOB_OFFER_SCREEN);
                            }
                        } else {
                            Log.info(Log.UI_JOBS, "NegotiationScreenTable", "Counter-offer failed (may have been rejected)");
                            // Refresh to show updated offer status
                            updateDynamicComponents();
                        }
                    } catch (NumberFormatException e) {
                        Log.error(Log.UI_JOBS, "NegotiationScreenTable", "Invalid number format", e);
                    }
                }
            }
        });
        buttonsTable.add(submitButton);
        
        // Cancel button
        VisTextButton cancelButton = new VisTextButton(LanguageModLoader.getValue("cancel"));
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

