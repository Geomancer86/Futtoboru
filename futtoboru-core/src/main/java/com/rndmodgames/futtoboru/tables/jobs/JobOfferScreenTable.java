package com.rndmodgames.futtoboru.tables.jobs;

import com.rndmodgames.futtoboru.menu.GameScreenId;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.jobs.JobOffer;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;
import com.rndmodgames.futtoboru.engine.finances.Money;
import com.rndmodgames.futtoboru.engine.jobs.JobManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.UiDialogs;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Job Offer Screen Table v1
 *
 * Displays job offers with accept/reject/negotiate options.
 *
 * <p>J-01: one row per offer the player can still answer (with its post and the day it lapses), money through
 * {@link Money#format}; accepting switches to the club menu and opens Home.
 *
 * @author Geomancer86
 */
public class JobOfferScreenTable extends VisTable {

    private static final int COLUMNS = 6;
    static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    Game game;
    SaveGame currentGame;
    JobManager jobManager;
    MainMenuManager menuManager;

    VisTable offersListTable;
    VisScrollPane offersScrollPane;

    public JobOfferScreenTable(Game parent) {
        super(true);

        this.game = parent;
        this.currentGame = ((Futtoboru)game).getSession().getGame();
        this.jobManager = ((Futtoboru)game).getJobManager();

        // Title
        this.row();
        this.add(new VisLabel(LanguageModLoader.getValue("jobs_offers"))).colspan(COLUMNS);
        this.row();
        this.addSeparator();

        // Offers list
        offersListTable = new VisTable(true);
        offersListTable.top(); // V-01: rows start under the title, not mid-screen

        offersScrollPane = new VisScrollPane(offersListTable);
        offersScrollPane.setFadeScrollBars(false);

        this.row();
        this.add(offersScrollPane).grow().colspan(COLUMNS);

        updateDynamicComponents();
    }

    public void setMenuManager(MainMenuManager menuManager) {
        this.menuManager = menuManager;
    }

    public void updateDynamicComponents() {
        if (jobManager == null || currentGame == null) {
            return;
        }

        offersListTable.clear();

        List<JobOffer> offers = jobManager.getPlayerOffers(currentGame.getOwner());
        if (offers.isEmpty()) {
            offersListTable.row();
            offersListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_no_offers"))).colspan(COLUMNS);
            return;
        }

        // Header
        offersListTable.row();
        offersListTable.add(new VisLabel(LanguageModLoader.getValue("table_club"))).width(UiTheme.size("col.wide"));
        offersListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_position"))).width(UiTheme.size("col.label"));
        offersListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_salary"))).width(UiTheme.size("col.label"));
        offersListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_contract"))).width(UiTheme.size("col.label"));
        offersListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_offer_answer_by"))).width(UiTheme.size("col.label"));
        offersListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_actions"))).width(UiTheme.size("col.xwide"));
        offersListTable.row();
        offersListTable.addSeparator().colspan(COLUMNS); // V-01: full width, not the first column only

        LocalDateTime gameDate = currentGame.getGameDate();
        for (JobOffer offer : offers) {
            JobOpening jobOpening = jobManager.getJobOpeningById(offer.getJobOpeningId());
            Club club = jobOpening != null ? currentGame.getClubById(jobOpening.getClubId()) : null;
            Profession profession = jobOpening != null ? ((Futtoboru) game).getSession().database().getProfessionById(jobOpening.getProfessionId()) : null;
            if (club == null || profession == null) {
                continue;
            }

            offersListTable.row();
            offersListTable.add(new VisLabel(club.getName())).width(UiTheme.size("col.wide"));
            offersListTable.add(new VisLabel(profession.getName())).width(UiTheme.size("col.label"));
            offersListTable.add(new VisLabel(LanguageModLoader.format("jobs_per_month", Money.format(offer.getSalary())))).width(UiTheme.size("col.label"));
            offersListTable.add(new VisLabel(LanguageModLoader.format("jobs_months", offer.getContractLengthMonths()))).width(UiTheme.size("col.label"));
            offersListTable.add(new VisLabel(offer.getExpirationDate() != null ? DATE.format(offer.getExpirationDate())
                    : LanguageModLoader.getValue("not_available"))).width(UiTheme.size("col.label"));

            VisTable buttonsTable = new VisTable(true);
            buttonsTable.add(button("jobs_accept", () -> accept(offer, club, profession)));
            buttonsTable.add(button("jobs_reject", () -> {
                jobManager.rejectOffer(offer);
                Log.info(Log.UI_JOBS, "JobOfferScreenTable", "Declined offer: " + offer.getId());
                updateDynamicComponents();
            }));
            if (offer.canNegotiate(gameDate)) {
                buttonsTable.add(button("jobs_negotiate", () -> {
                    if (menuManager != null) {
                        menuManager.setCurrentNegotiationOffer(offer);
                        menuManager.setActiveMainScreen(GameScreenId.NEGOTIATION);
                    }
                }));
            }
            offersListTable.add(buttonsTable).width(UiTheme.size("col.xwide"));
        }
    }

    /** Takes the post: the club menu replaces the job-seeker menu and Home opens (J-01). */
    private void accept(JobOffer offer, Club club, Profession profession) {
        if (jobManager.acceptOffer(offer)) {
            Log.info(Log.UI_JOBS, "JobOfferScreenTable", "Accepted offer: " + offer.getId());
            // the message first: this table leaves the stage when Home opens
            showMessage(LanguageModLoader.getValue("jobs_accepted_title"),
                    LanguageModLoader.format("jobs_accepted_text", profession.getName(), club.getName()));
            if (menuManager != null) {
                menuManager.setDynamicButtonsMenu();
                menuManager.openMenuEntry(GameScreenId.HOME);
            }
        } else {
            Log.info(Log.UI_JOBS, "JobOfferScreenTable", "Offer could not be accepted: " + offer.getId());
            showMessage(LanguageModLoader.getValue("jobs_accept_failed_title"),
                    LanguageModLoader.getValue("jobs_accept_failed_text"));
            updateDynamicComponents();
        }
    }

    private VisTextButton button(String labelKey, Runnable action) {
        VisTextButton button = new VisTextButton(LanguageModLoader.getValue(labelKey));
        button.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int b) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int b) {
                if (button.isPressed() && jobManager != null) {
                    action.run();
                }
            }
        });
        return button;
    }

    private void showMessage(String title, String text) {
        if (getStage() != null) {
            UiDialogs.info(getStage(), title, text);
        }
    }
}
