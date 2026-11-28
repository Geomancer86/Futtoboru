package com.rndmodgames.futtoboru.tables.jobs;

import com.rndmodgames.futtoboru.menu.GameScreenId;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.jobs.JobConstants;
import com.rndmodgames.futtoboru.data.jobs.JobOffer;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;
import com.rndmodgames.futtoboru.data.jobs.OfferStatus;
import com.rndmodgames.futtoboru.engine.finances.Money;
import com.rndmodgames.futtoboru.engine.jobs.JobManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.UiDialogs;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Negotiation Screen Table v1
 *
 * Allows players to negotiate job offer terms.
 *
 * <p>J-01: the salary field starts with pounds and pence (it showed the raw BigDecimal "12.495"), the input is read
 * as money ({@link NegotiationInput}) with a message under the fields when it is not, the club's answer is shown,
 * and the rounds left count down.
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
    VisLabel problemLabel;

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
        LocalDateTime gameDate = currentGame != null ? currentGame.getGameDate() : null;
        if (currentOffer.getStatus() != OfferStatus.PENDING || currentOffer.isExpired(gameDate)) {
            // answered or lapsed since the screen was opened (a day passed)
            this.row();
            this.add(new VisLabel(LanguageModLoader.getValue("jobs_offer_closed")));
            this.row();
            this.add(button("back", this::backToOffers));
            return;
        }

        JobOpening jobOpening = jobManager != null ? jobManager.getJobOpeningById(currentOffer.getJobOpeningId()) : null;
        Club club = jobOpening != null ? currentGame.getClubById(jobOpening.getClubId()) : null;
        Profession profession = jobOpening != null ? DatabaseLoader.getInstance().getProfessionById(jobOpening.getProfessionId()) : null;

        // Title
        this.row();
        this.add(new VisLabel(club != null ? LanguageModLoader.format("jobs_negotiate_offer_club",
                club.getName() + (profession != null ? " (" + profession.getName() + ")" : ""))
                : LanguageModLoader.getValue("jobs_negotiate_offer"))).colspan(2);
        this.row();
        this.addSeparator().colspan(2); // V-01: a one-column separator pushed column 2 to the right edge

        // Current offer
        this.row();
        this.add(new VisLabel(LanguageModLoader.getValue("jobs_current_offer"))).colspan(2);
        this.row();
        this.add(new VisLabel(LanguageModLoader.format("jobs_salary_value",
                LanguageModLoader.format("jobs_per_month", Money.format(currentOffer.getSalary())))));
        this.add(new VisLabel(LanguageModLoader.format("jobs_contract_value", currentOffer.getContractLengthMonths())));
        this.row();
        this.add(new VisLabel(LanguageModLoader.format("jobs_negotiation_round", currentOffer.getNegotiationRound(),
                JobConstants.MAX_NEGOTIATION_ROUNDS))).colspan(2);
        this.row();
        this.addSeparator().colspan(2); // V-01: a one-column separator pushed column 2 to the right edge

        VisTable buttonsTable = new VisTable(true);
        if (currentOffer.canNegotiate(gameDate)) {
            // Counter-offer inputs
            this.row();
            this.add(new VisLabel(LanguageModLoader.getValue("jobs_counter_offer"))).colspan(2);
            this.row();
            this.add(new VisLabel(LanguageModLoader.format("jobs_new_salary_in", Money.SYMBOL)));
            salaryField = new VisTextField(NegotiationInput.salaryText(currentOffer.getSalary()));
            this.add(salaryField).width(UiTheme.size("col.wide"));
            this.row();
            this.add(new VisLabel(LanguageModLoader.getValue("jobs_new_contract_length")));
            contractLengthField = new VisTextField(String.valueOf(currentOffer.getContractLengthMonths()));
            this.add(contractLengthField).width(UiTheme.size("col.wide"));
            this.row();
            problemLabel = new VisLabel("", UiTheme.color("state.negative"));
            this.add(problemLabel).colspan(2);
            this.row();
            this.addSeparator().colspan(2); // V-01: a one-column separator pushed column 2 to the right edge
            buttonsTable.add(button("jobs_submit_counter_offer", this::submit));
        } else {
            this.row();
            this.add(new VisLabel(LanguageModLoader.getValue("jobs_no_more_rounds"))).colspan(2);
        }
        buttonsTable.add(button("cancel", this::backToOffers));

        this.row();
        this.add(buttonsTable).colspan(2);
    }

    /** Reads the fields; a counter-offer goes to the club, whose answer is shown on the offers screen. */
    private void submit() {
        if (jobManager == null || currentOffer == null) {
            return;
        }
        BigDecimal salary = NegotiationInput.parseSalary(salaryField.getText());
        Integer months = NegotiationInput.parseContractMonths(contractLengthField.getText());
        if (salary == null) {
            problemLabel.setText(LanguageModLoader.format("jobs_invalid_salary", Money.format(NegotiationInput.MAX_SALARY)));
            return;
        }
        if (months == null) {
            problemLabel.setText(LanguageModLoader.format("jobs_invalid_contract_length",
                    NegotiationInput.MIN_CONTRACT_MONTHS, NegotiationInput.MAX_CONTRACT_MONTHS));
            return;
        }
        Stage stage = getStage();
        JobOffer counter = jobManager.submitCounterOffer(currentOffer, salary, months);
        String terms = LanguageModLoader.format("jobs_terms", Money.format(currentOffer.getSalary()),
                currentOffer.getContractLengthMonths());
        if (counter != null && counter.getStatus() == OfferStatus.ACCEPTED) {
            info(stage, LanguageModLoader.getValue("jobs_counter_accepted_title"),
                    LanguageModLoader.format("jobs_counter_accepted_text", terms));
        } else if (counter != null) {
            info(stage, LanguageModLoader.getValue("jobs_counter_rejected_title"),
                    LanguageModLoader.format("jobs_counter_rejected_text", terms));
        } else {
            Log.info(Log.UI_JOBS, "NegotiationScreenTable", "Counter-offer could not be made");
        }
        backToOffers();
    }

    private void backToOffers() {
        if (menuManager != null) {
            menuManager.setActiveMainScreen(GameScreenId.JOB_OFFER);
        }
    }

    private static void info(Stage stage, String title, String text) {
        if (stage != null) {
            UiDialogs.info(stage, title, text);
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
                if (button.isPressed()) {
                    action.run();
                }
            }
        });
        return button;
    }
}
