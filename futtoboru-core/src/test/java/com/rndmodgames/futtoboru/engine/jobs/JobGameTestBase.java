package com.rndmodgames.futtoboru.engine.jobs;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.jobs.JobApplication;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;
import com.rndmodgames.futtoboru.data.jobs.JobStatus;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * J-01: a new 1888-89 game (seed 1888, the manager at Preston) for the job market tests, with the helpers to turn
 * the owner into a job seeker and to let days pass for the job market only.
 */
abstract class JobGameTestBase extends BaseTest {

    protected SaveGame game;
    protected JobManager jobs;
    protected ClubStaffManager staff;
    protected Person owner;
    protected Profession manager;
    protected Profession director;

    @BeforeEach
    void newJobGame() {
        application.setSeed(1888L);
        game = NewGameFixture.newGame(application);
        jobs = application.getJobManager();
        staff = application.getClubStaffManager();
        owner = game.getOwner();
        manager = DatabaseLoader.getProfessionByDataId(Profession.MANAGER);
        director = DatabaseLoader.getProfessionByDataId(Profession.DIRECTOR);
        jobs.setStaffDeparturePerDay(0.0);
    }

    /** The owner leaves Preston: a retired player without a club looking for a post (the job-seeker start). */
    protected void becomeJobSeeker() {
        Club preston = game.getCurrentClub();
        staff.vacatePostsOf(preston, owner);
        owner.setCurrentClubId(null);
        owner.setPrimaryProfession(DatabaseLoader.getProfessionByDataId(Profession.RETIRED_PLAYER));
        jobs.updateJobOpenings();
    }

    protected JobOpening opening(String clubDataId, Profession profession) {
        Club club = DatabaseLoader.getClubByDataId(clubDataId);
        for (JobOpening job : jobs.getAvailableJobs(club)) {
            if (job.getProfessionId().equals(profession.getId())) {
                return job;
            }
        }
        return null;
    }

    protected List<JobOpening> openOpenings(Club club, Profession profession) {
        List<JobOpening> out = new ArrayList<>();
        for (JobOpening job : game.getActiveJobOpenings()) {
            if (job.getStatus() == JobStatus.OPEN && job.getClubId().equals(club.getId())
                    && job.getProfessionId().equals(profession.getId())) {
                out.add(job);
            }
        }
        return out;
    }

    protected JobApplication applicationFor(JobOpening opening) {
        for (JobApplication app : jobs.getPlayerApplications(owner)) {
            if (app.getJobOpeningId().equals(opening.getId())) {
                return app;
            }
        }
        return null;
    }

    /** Days pass for the job market only (the engine's daily call), without playing matches. */
    protected void passDays(int days) {
        for (int i = 0; i < days; i++) {
            game.setGameDate(game.getGameDate().plusDays(1));
            jobs.updateJobOpenings();
        }
    }
}
