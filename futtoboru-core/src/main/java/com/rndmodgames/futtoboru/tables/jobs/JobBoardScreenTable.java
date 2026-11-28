package com.rndmodgames.futtoboru.tables.jobs;

import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.jobs.ApplicationStatus;
import com.rndmodgames.futtoboru.data.jobs.JobApplication;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;
import com.rndmodgames.futtoboru.engine.finances.Money;
import com.rndmodgames.futtoboru.engine.jobs.JobManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.UiDialogs;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Job Board Screen Table v1
 *
 * Displays all available job openings with apply functionality.
 *
 * <p>J-01: the openings taking applications (before their deadline, the post still vacant), the salary through
 * {@link Money#format}, the deadline as a date, and for a post the player applied to the state of his
 * application (one per post) instead of Apply.
 *
 * @author Geomancer86
 */
public class JobBoardScreenTable extends VisTable {

    private static final int COLUMNS = 6;

    Game game;
    SaveGame currentGame;
    JobManager jobManager;

    VisTable jobsListTable;
    VisScrollPane jobsScrollPane;

    public JobBoardScreenTable(Game parent) {
        super(true);

        this.game = parent;
        this.currentGame = ((Futtoboru)game).getCurrentGame();
        this.jobManager = ((Futtoboru)game).getJobManager();

        // Title
        this.row();
        this.add(new VisLabel(LanguageModLoader.getValue("job_board"))).colspan(2);
        this.row();
        this.addSeparator();

        // Jobs list
        jobsListTable = new VisTable(true);
        jobsListTable.top();

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
            jobsListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_no_openings"))).colspan(COLUMNS);
            return;
        }

        // Header
        jobsListTable.row();
        jobsListTable.add(new VisLabel(LanguageModLoader.getValue("table_club"))).width(UiTheme.size("col.wide"));
        jobsListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_position"))).width(UiTheme.size("col.label"));
        jobsListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_salary"))).width(UiTheme.size("col.number"));
        jobsListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_contract"))).width(UiTheme.size("col.number"));
        jobsListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_deadline"))).width(UiTheme.size("col.label"));
        jobsListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_action"))).width(UiTheme.size("col.number"));
        jobsListTable.row();
        jobsListTable.addSeparator().colspan(COLUMNS); // V-01: full width, not the first column only

        Person owner = currentGame.getOwner();
        for (JobOpening job : jobs) {
            Club club = currentGame.getClubById(job.getClubId());
            Profession profession = DatabaseLoader.getInstance().getProfessionById(job.getProfessionId());
            if (club == null || profession == null) {
                continue;
            }

            jobsListTable.row();
            jobsListTable.add(new VisLabel(club.getName())).width(UiTheme.size("col.wide"));
            jobsListTable.add(new VisLabel(profession.getName())).width(UiTheme.size("col.label"));
            jobsListTable.add(new VisLabel(Money.format(job.getSalary()))).width(UiTheme.size("col.number"));
            jobsListTable.add(new VisLabel(LanguageModLoader.format("jobs_months", job.getContractLengthMonths()))).width(UiTheme.size("col.number"));
            jobsListTable.add(new VisLabel(job.getApplicationDeadline() != null
                    ? JobOfferScreenTable.DATE.format(job.getApplicationDeadline())
                    : LanguageModLoader.getValue("not_available"))).width(UiTheme.size("col.label"));

            JobApplication application = owner != null ? jobManager.getApplicationFor(owner, job) : null;
            VisTextButton applyButton;
            if (application != null) {
                // one application per post: its state instead of Apply
                applyButton = new VisTextButton(statusName(application.getStatus()));
                applyButton.setDisabled(true);
            } else {
                applyButton = new VisTextButton(LanguageModLoader.getValue("jobs_apply"));
                final Long jobId = job.getId();
                applyButton.addCaptureListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        return true;
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        if (applyButton.isPressed()) {
                            apply(jobId, club, profession);
                        }
                    }
                });
            }
            jobsListTable.add(applyButton).width(UiTheme.size("col.number"));
        }
    }

    private void apply(Long jobId, Club club, Profession profession) {
        Person owner = currentGame.getOwner();
        JobOpening job = jobManager.getJobOpeningById(jobId);
        JobApplication result = owner != null && job != null ? jobManager.applyForJob(owner, job) : null;
        if (result != null) {
            Log.info(Log.UI_JOBS, "JobBoardScreenTable", "Applied: " + result.getId() + " " + result.getStatus());
            showMessage(LanguageModLoader.getValue("jobs_application_submitted"),
                    LanguageModLoader.format("jobs_application_submitted_text", club.getName(), profession.getName()));
        } else {
            showMessage(LanguageModLoader.getValue("jobs_application_failed"),
                    LanguageModLoader.getValue("jobs_application_failed_text"));
        }
        Gdx.app.postRunnable(this::updateDynamicComponents);
    }

    /** The state of an application as the player reads it (also used by My Applications and Club detail). */
    public static String statusName(ApplicationStatus status) {
        if (status == null) {
            return LanguageModLoader.getValue("unknown");
        }
        switch (status) {
        case PENDING:
            return LanguageModLoader.getValue("jobs_status_pending");
        case REJECTED:
            return LanguageModLoader.getValue("jobs_status_rejected");
        case OFFER_RECEIVED:
            return LanguageModLoader.getValue("jobs_status_offer_received");
        case ACCEPTED:
            return LanguageModLoader.getValue("jobs_status_accepted");
        case DECLINED:
            return LanguageModLoader.getValue("jobs_status_declined");
        case EXPIRED:
            return LanguageModLoader.getValue("jobs_status_expired");
        default:
            return LanguageModLoader.getValue("jobs_status_withdrawn");
        }
    }

    /**
     * Show a message dialog to the user
     */
    void showMessage(String title, String message) {
        Stage stage = this.getStage();
        if (stage != null) {
            UiDialogs.info(stage, title, message);
        } else {
            Log.info(Log.UI_JOBS, "JobBoardScreenTable", title + ": " + message);
        }
    }
}
