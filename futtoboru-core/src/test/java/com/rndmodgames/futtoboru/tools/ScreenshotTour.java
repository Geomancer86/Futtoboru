package com.rndmodgames.futtoboru.tools;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.zip.Deflater;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.data.jobs.JobOffer;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;
import com.rndmodgames.futtoboru.dialogs.LoadGameDialog;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.menu.MenuEntries;
import com.rndmodgames.futtoboru.menu.topmenu.MainGameMenuTable;
import com.rndmodgames.futtoboru.screens.MainGameScreen;
import com.rndmodgames.futtoboru.screens.NewGameOverviewScreen;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.futtoboru.ui.UiDialogs;
import com.rndmodgames.futtoboru.ui.UiScale;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * V-01: a dev-only screenshot tour (not a JUnit test, never run by {@code verify}). It starts the real game in an
 * LWJGL3 window with a fixed seed, sets up a new 1888-89 game through {@link NewGameFixture} (manager at Preston),
 * visits every screen, drill-down and dialog, and writes one PNG per stop: first at 1280x720 with UI scale 1x, then
 * at 2560x1440 with 2x (the 2x pass needs a monitor that can hold a 2560x1440 window, e.g. 2880x1800).
 *
 * <p>Saves and manager files go to a temporary folder ({@link SaveLoadSystem#setGameFolder}); the UI scale is set
 * in memory only, so the player's preferences and Documents are untouched.
 *
 * <p>Run: {@code tools/ui/screenshots.sh [outDir] [passes] [locale]} (see docs/BUILD.md), e.g.
 * {@code tools/ui/screenshots.sh target/screenshots 1x,2x en_US}.
 */
public final class ScreenshotTour extends Futtoboru {

    /** frames to wait after each action before the capture (dialog fades take 0.4 s) */
    private static final int SETTLE_FRAMES = 40;

    private static final long SEED = 1888L;

    private final File outDir;
    private final String[] passes;
    private final String locale;

    private final List<Step> steps = new ArrayList<>();
    private int stepIndex;
    private int wait;
    private String pendingCapture;
    private String passName = "";
    private int shotNumber;
    private int shots;
    private final List<String> problems = new ArrayList<>();

    private SaveGame game;

    /** one stop of the tour: {@code action} runs until it returns true, then {@code capture} (if any) is taken */
    private static final class Step {
        final String capture;
        final BooleanSupplier action;

        Step(String capture, BooleanSupplier action) {
            this.capture = capture;
            this.action = action;
        }
    }

    public static void main(String[] args) throws Exception {
        File out = new File(args.length > 0 ? args[0] : "target/screenshots");
        String[] passes = (args.length > 1 ? args[1] : "1x,2x").split(",");
        String locale = args.length > 2 ? args[2] : "en_US";

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Futtoboru screenshot tour");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        config.setForegroundFPS(60);

        ScreenshotTour tour = new ScreenshotTour(out, passes, locale);
        new Lwjgl3Application(tour, config);
        System.out.println("[tour] " + tour.shots + " screenshots in " + out.getAbsolutePath());
        for (String problem : tour.problems) {
            System.out.println("[tour] PROBLEM " + problem);
        }
        System.exit(tour.problems.isEmpty() ? 0 : 1);
    }

    private ScreenshotTour(File outDir, String[] passes, String locale) {
        super(SEED);
        this.outDir = outDir;
        this.passes = passes;
        this.locale = locale;
    }

    @Override
    public void create() {
        try {
            File folder = Files.createTempDirectory("futtoboru-tour").toFile();
            folder.deleteOnExit();
            SaveLoadSystem.setGameFolder(folder);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        super.create();
        // the tour's language, not the saved preference
        LanguageModLoader.setBundle(I18NBundle.createBundle(Gdx.files.internal("mods/language/BaseBundle"),
                new Locale(locale)));
        for (String pass : passes) {
            addPass(pass.trim());
        }
        steps.add(new Step(null, () -> {
            Gdx.app.exit();
            return true;
        }));
    }

    @Override
    public void render() {
        super.render();
        if (pendingCapture != null) {
            if (wait > 0) {
                wait--;
                return;
            }
            capture(pendingCapture);
            pendingCapture = null;
        }
        if (stepIndex >= steps.size()) {
            return;
        }
        Step step = steps.get(stepIndex);
        boolean done;
        try {
            done = step.action.getAsBoolean();
        } catch (Throwable t) {
            problems.add(passName + " step " + (step.capture != null ? step.capture : "#" + stepIndex) + ": " + t);
            t.printStackTrace();
            done = true;
            if (step.capture != null) {
                stepIndex++;
                return; // no capture of a failed stop
            }
        }
        if (done) {
            stepIndex++;
            if (step.capture != null) {
                pendingCapture = step.capture;
                wait = SETTLE_FRAMES;
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ the tour

    private void addPass(String pass) {
        final boolean twoX = pass.startsWith("2");
        final int width = twoX ? 2560 : 1280;
        final int height = twoX ? 1440 : 720;

        once(null, () -> {
            passName = pass;
            shotNumber = 0;
            UiDialogs.closeOpen();
            UiScale.setSetting(twoX ? 2f : 1f);
            Gdx.graphics.setWindowedMode(width, height);
            changeScreen(MENU_SCREEN);
        });
        waitFrames(30);
        once(null, () -> {
            if (Gdx.graphics.getBackBufferWidth() != width || Gdx.graphics.getBackBufferHeight() != height) {
                problems.add(pass + ": window is " + Gdx.graphics.getBackBufferWidth() + "x"
                        + Gdx.graphics.getBackBufferHeight() + ", wanted " + width + "x" + height);
            }
            makeSaves();
            changeScreen(MENU_SCREEN);
        });

        // main menu and its screens
        once("menu", () -> changeScreen(MENU_SCREEN));
        once("menu-load-dialog", () -> UiDialogs.show(stage(), new LoadGameDialog(this)));
        once("menu-load-delete-confirm", () -> click(LanguageModLoader.getValue("delete")));
        // U-12: Esc on "Delete save?" is Cancel (the list comes back); Esc on the list closes it
        once(null, this::pressEscape);
        waitFrames(SETTLE_FRAMES);
        once(null, () -> expect(UiDialogs.isOpen() && UiDialogsCurrent.get() instanceof LoadGameDialog,
                "Esc on Delete save? did not bring the save list back"));
        once(null, this::pressEscape);
        waitFrames(SETTLE_FRAMES);
        once(null, () -> expect(!UiDialogs.isOpen(), "Esc did not close the load dialog"));
        once("settings", () -> {
            UiDialogs.closeOpen();
            changeScreen(SETTINGS_SCREEN);
        });
        once("new-game-season", () -> {
            prepareNewGame();
            changeScreen(NEW_GAME_SCREEN);
        });
        once("new-manager", () -> changeScreen(NEW_MANAGER_SCREEN));
        once("new-game-setup", () -> changeScreen(NEW_GAME_SETUP_SCREEN, season(), england()));
        once("new-game-overview", () -> changeScreen(NEW_GAME_OVERVIEW_SCREEN, season(), england()));
        once("match-engine-debug", () -> changeScreen(MATCH_ENGINE_DEBUG_SCREEN));

        // in game, manager at Preston
        once("game-home-start", () -> startGame(true));
        once("game-league-draw", () -> {
            playUntil(FuttoboruGameEngine.DRAW_ACTION, 60);
            topMenu().setMainContainerButton();
            openDraw();
        });
        once("game-league-draw-done", () -> click(LanguageModLoader.getValue("draw_all")));
        once("game-cup-draw", () -> {
            finishDraws();
            if (playUntilCupDraw(120)) {
                openDraw();
            } else {
                problems.add(passName + ": no cup draw within 120 days");
            }
        });
        once("game-cup-draw-done", () -> click(LanguageModLoader.getValue("draw_all")));
        once("game-match-day-top-bar", () -> {
            finishDraws();
            playUntil(FuttoboruGameEngine.MATCH_PREVIEW_ACTION, 200);
            refreshGameScreen();
        });
        once("game-match-preview", () -> topMenu().matchPreview());
        once("game-match-result", () -> topMenu().matchResult());
        // a few weeks of play so tables, history and finances have rows
        repeat(() -> playDays(5, 40));
        once(null, this::refreshGameScreen);
        for (MenuEntries.MenuEntry entry : MenuEntries.forProfession(true)) {
            once("game-" + entry.labelKey().replace('_', '-'), () -> menu().openMenuEntry(entry.screen()));
        }
        once(null, () -> menu().openMenuEntry(MainMenuManager.MAIN_SQUAD_SCREEN));
        waitFrames(5);
        once(null, () -> hover(LanguageModLoader.getValue("squad_col_acceleration")));
        waitFrames(40);
        once("game-squad-tooltip", () -> {
        });
        once("game-squad-team-sheet", () -> menu().openTeamSheet());
        once("game-player-detail", () -> {
            menu().openMenuEntry(MainMenuManager.MAIN_SQUAD_SCREEN);
            menu().setSelectedPlayer(game.getCurrentClub().getPlayers().get(0));
            menu().setActiveMainScreen(MainMenuManager.PLAYER_DETAIL_SCREEN);
        });
        // G-04: the retained list as it looks on 16 April (the date moved there for the picture, then back)
        java.time.LocalDateTime[] tourDate = new java.time.LocalDateTime[1];
        once("game-retained-list", () -> {
            tourDate[0] = game.getGameDate();
            game.setGameDate(java.time.LocalDateTime.of(1889, 4, 16, 4, 0));
            com.rndmodgames.futtoboru.engine.contracts.ContractManager contracts = getGameEngine().getContractManager();
            List<com.rndmodgames.futtoboru.data.PlayerContract> ending = contracts.endingContracts(game.getCurrentClub());
            contracts.retain(ending.get(0));
            contracts.changeOfferedWage(game.getCurrentClub(), ending.get(0), 3);
            contracts.release(ending.get(1));
            menu().setActiveMainScreen(MainMenuManager.RETAINED_LIST_SCREEN);
        });
        once(null, () -> game.setGameDate(tourDate[0]));
        once("game-league-detail", () -> {
            menu().openMenuEntry(MainMenuManager.COMPETITIONS_SCREEN);
            menu().setSelectedLeague(game.getMainAuthority().getLeagues().get(0));
            menu().setActiveMainScreen(MainMenuManager.LEAGUE_DETAIL_SCREEN);
        });
        once("game-cup-detail", () -> {
            menu().openMenuEntry(MainMenuManager.COMPETITIONS_SCREEN);
            menu().setSelectedCup(firstCup());
            menu().setActiveMainScreen(MainMenuManager.CUP_DETAIL_SCREEN);
        });
        once("game-cup-playoffs", () -> menu().setActiveMainScreen(MainMenuManager.CUP_PLAYOFFS_SCREEN));
        once("game-toast-f12", () -> {
            menu().openMenuEntry(MainMenuManager.HOME_SCREEN);
            ((MainGameScreen) getScreen()).toastShort(LanguageModLoader.getValue("screenshot_taken"));
        });
        once("game-save-dialog", () -> runOption(1));
        once("game-save-overwrite-confirm", () -> {
            com.kotcrab.vis.ui.widget.VisTextField name = (com.kotcrab.vis.ui.widget.VisTextField) field(
                    UiDialogsCurrent.get(), "nameField");
            name.setText("Preston 1888");
            invoke(UiDialogsCurrent.get(), "attemptSave");
        });
        once("game-unsaved-progress", () -> {
            UiDialogs.closeOpen();
            runOption(2);
        });
        // U-12: Esc on a question is its Cancel and stops there: the screen behind (a drill-down) does not go Back
        once(null, () -> {
            UiDialogs.closeOpen();
            menu().openMenuEntry(MainMenuManager.MAIN_SQUAD_SCREEN);
            menu().setSelectedPlayer(game.getCurrentClub().getPlayers().get(0));
            menu().setActiveMainScreen(MainMenuManager.PLAYER_DETAIL_SCREEN);
            runOption(2);
        });
        waitFrames(SETTLE_FRAMES);
        once(null, () -> expect(UiDialogs.isOpen(), "no unsaved-progress question to try Esc on"));
        once(null, this::pressEscape);
        waitFrames(SETTLE_FRAMES);
        once(null, () -> {
            expect(!UiDialogs.isOpen(), "Esc did not close the unsaved-progress question");
            expect(MainMenuManager.CURRENT_SCREEN == MainMenuManager.PLAYER_DETAIL_SCREEN,
                    "Esc on a dialog also went Back (screen " + MainMenuManager.CURRENT_SCREEN + ")");
            pressEscape(); // no dialog now: Esc is Back again, to the squad
        });
        waitFrames(5);
        once(null, () -> {
            expect(MainMenuManager.CURRENT_SCREEN == MainMenuManager.MAIN_SQUAD_SCREEN,
                    "Esc without a dialog did not go Back to the squad");
            menu().openMenuEntry(MainMenuManager.HOME_SCREEN);
            runOption(2);
        });
        waitFrames(SETTLE_FRAMES);
        once("game-load-dialog", () -> click(LanguageModLoader.getValue("discard_progress")));
        once("game-settings", () -> {
            UiDialogs.closeOpen();
            runOption(3);
        });
        // in game, without a club (job seeker)
        once(null, () -> startGame(false));
        for (MenuEntries.MenuEntry entry : MenuEntries.forProfession(false)) {
            once("jobs-" + entry.labelKey().replace('_', '-'), () -> menu().openMenuEntry(entry.screen()));
        }
        once("jobs-club-detail", () -> {
            menu().openMenuEntry(MainMenuManager.CLUB_BROWSER_SCREEN);
            menu().setSelectedClubForDetail(DatabaseLoader.getInstance().getClubByDataId("preston-north-end"));
            menu().setActiveMainScreen(MainMenuManager.CLUB_DETAIL_SCREEN);
        });
        once("jobs-my-applications-offer", () -> {
            JobOpening opening = getJobManager().getAvailableJobs().get(0);
            getJobManager().applyForJob(game.getOwner(), opening);
            JobOffer offer = getJobManager().makeOffer(opening, game.getOwner());
            menu().setCurrentNegotiationOffer(offer);
            menu().openMenuEntry(MainMenuManager.MY_APPLICATIONS_SCREEN);
        });
        once("jobs-job-offer", () -> menu().setActiveMainScreen(MainMenuManager.JOB_OFFER_SCREEN));
        once("jobs-negotiation", () -> menu().setActiveMainScreen(MainMenuManager.NEGOTIATION_SCREEN));
    }

    /** a fresh new game: manager at Preston, or a retired player without a club */
    private void startGame(boolean manager) {
        UiDialogs.closeOpen();
        game = NewGameFixture.newGameInSession(this);
        game.getOwner().setBirthDate(java.time.LocalDateTime.of(1850, 6, 1, 0, 0)); // the fixture's owner has none
        if (!manager) {
            game.getOwner().setPrimaryProfession(DatabaseLoader.getInstance().getProfessionByDataId(Profession.RETIRED_PLAYER));
            game.getOwner().setCurrentClubId(null);
        }
        // what NewGameOverviewScreen's Start does after the fixture's part
        NewGameOverviewScreen overview = new NewGameOverviewScreen(this, season(), england());
        invoke(overview, "generateAttributesForAllPlayers", game);
        invoke(overview, "createInitialAttributeSnapshots", game);
        overview.dispose();
        Message welcome = getGameEngine().getMessageManager().createWelcomeMessage(game.getOwner(),
                game.getOwner().getPrimaryProfession(), england().get(0), game.getCurrentClub(),
                game.getGameStartDate());
        if (welcome != null) {
            getGameEngine().getMessageManager().deliverMessage(welcome);
        }
        MainMenuManager.resetNavigation();
        changeScreen(GAME_SCREEN);
    }

    /** a new game screen after days were played outside it (the date widget only updates through Continue) */
    private void refreshGameScreen() {
        MainMenuManager.resetNavigation();
        changeScreen(GAME_SCREEN);
    }

    /** advances days (as Continue does) until the next action is {@code action} or {@code maxDays} passed */
    private void playUntil(int action, int maxDays) {
        for (int day = 0; day < maxDays; day++) {
            if (getGameEngine().getNextGameAction() == action) {
                return;
            }
            if (getGameEngine().getNextGameAction() == FuttoboruGameEngine.DRAW_ACTION) {
                finishDraws();
            } else if (getGameEngine().getNextGameAction() == FuttoboruGameEngine.MATCH_PREVIEW_ACTION) {
                getGameEngine().getMatchResult();
            }
            getGameEngine().continueGame();
        }
        problems.add(passName + ": next action " + action + " not reached in " + maxDays + " days");
    }

    private boolean playUntilCupDraw(int maxDays) {
        for (int day = 0; day < maxDays; day++) {
            if (getGameEngine().getNextGameAction() == FuttoboruGameEngine.DRAW_ACTION) {
                deliverScheduledDraws();
                if (mandatoryDraw("CUP_DRAW") != null) {
                    return true;
                }
                finishDraws();
            } else if (getGameEngine().getNextGameAction() == FuttoboruGameEngine.MATCH_PREVIEW_ACTION) {
                getGameEngine().getMatchResult();
            }
            getGameEngine().continueGame();
        }
        return false;
    }

    private int played;

    /** plays {@code perFrame} days a frame until {@code total} days were played (keeps the window responsive) */
    private boolean playDays(int perFrame, int total) {
        for (int i = 0; i < perFrame && played < total; i++, played++) {
            if (getGameEngine().getNextGameAction() == FuttoboruGameEngine.DRAW_ACTION) {
                finishDraws();
            } else if (getGameEngine().getNextGameAction() == FuttoboruGameEngine.MATCH_PREVIEW_ACTION) {
                getGameEngine().getMatchResult();
            }
            getGameEngine().continueGame();
        }
        if (played >= total) {
            played = 0;
            return true;
        }
        return false;
    }

    /** the INBOX -> DRAW button: delivers a scheduled draw at once, then opens its draw screen */
    private void openDraw() {
        deliverScheduledDraws();
        if (mandatoryDraw("CUP_DRAW") != null) {
            menu().setActiveMainScreen(MainMenuManager.CUP_DRAW_SCREEN);
        } else {
            menu().setActiveMainScreen(MainMenuManager.LEAGUE_DRAW_SCREEN);
        }
    }

    private void deliverScheduledDraws() {
        if (game.getScheduledMessages() != null) {
            for (Message m : new ArrayList<>(game.getScheduledMessages())) {
                if (m != null && Boolean.TRUE.equals(m.getIsMandatory())) {
                    getGameEngine().getMessageManager().deliverMessage(m);
                }
            }
        }
    }

    private Message mandatoryDraw(String type) {
        for (Message m : game.getAllMessages()) {
            if (Boolean.TRUE.equals(m.getIsMandatory()) && !Boolean.TRUE.equals(m.getIsRead())
                    && type.equals(m.getMessageType())) {
                return m;
            }
        }
        return null;
    }

    /** marks every mandatory draw read, as the player does by finishing the draw screens */
    private void finishDraws() {
        deliverScheduledDraws();
        for (Message m : game.getAllMessages()) {
            if (Boolean.TRUE.equals(m.getIsMandatory())) {
                m.setIsRead(true);
            }
        }
    }

    /** a saved game, a second one, the autosave and an unreadable file, for the Load dialog */
    private void makeSaves() {
        try {
            SaveGame saved = NewGameFixture.newGameInSession(this);
            SaveLoadSystem.saveGame(saved, "Preston 1888");
            File second = SaveLoadSystem.saveGame(saved, "Second season try");
            Files.copy(second.toPath(), SaveLoadSystem.autosaveFile().toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            SaveLoadSystem.writeSaveFile(new File(second.getParentFile(), "broken.json"), "{ not a save");
        } catch (Exception e) {
            problems.add(passName + ": saves not made: " + e);
        }
    }

    // ------------------------------------------------------------------------------------------------ helpers

    private void once(String capture, Runnable action) {
        steps.add(new Step(capture, () -> {
            action.run();
            return true;
        }));
    }

    private void repeat(BooleanSupplier action) {
        steps.add(new Step(null, action));
    }

    private void waitFrames(int frames) {
        int[] left = {frames};
        steps.add(new Step(null, () -> --left[0] <= 0));
    }

    private void capture(String name) {
        int w = Gdx.graphics.getBackBufferWidth();
        int h = Gdx.graphics.getBackBufferHeight();
        byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
        for (int i = 3; i < pixels.length; i += 4) {
            pixels[i] = (byte) 255;
        }
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
        shotNumber++;
        File file = new File(new File(outDir, passName), String.format("%02d-%s.png", shotNumber, name));
        PixmapIO.writePNG(Gdx.files.absolute(file.getAbsolutePath()), pixmap, Deflater.BEST_COMPRESSION, false);
        pixmap.dispose();
        shots++;
        System.out.println("[tour] " + file.getPath());
    }

    private Season season() {
        return DatabaseLoader.getInstance().getSeasonByDataId("1888-89");
    }

    private List<Country> england() {
        List<Country> countries = new ArrayList<>();
        countries.add(DatabaseLoader.getInstance().getCountryByDataId("england"));
        return countries;
    }

    private Competition firstCup() {
        return game.getAllCups().get(0);
    }

    private MainMenuManager menu() {
        return getGameEngine().getMainMenuManager();
    }

    private MainGameMenuTable topMenu() {
        return menu().getTopMenu();
    }

    /** the Options select box entry {@code option} (1 Save, 2 Load, 3 Settings) */
    private void runOption(int option) {
        invoke(topMenu(), "runOption", option, stage());
    }

    /** the current screen's stage */
    private Stage stage() {
        Screen screen = getScreen();
        for (Class<?> c = screen.getClass(); c != null; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (Stage.class.isAssignableFrom(f.getType())) {
                    try {
                        f.setAccessible(true);
                        return (Stage) f.get(screen);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
        throw new IllegalStateException("no stage on " + screen);
    }

    /** Esc through the screen's real input processor (stage first, then the screen's own keys) */
    private void pressEscape() {
        Gdx.input.getInputProcessor().keyDown(Keys.ESCAPE);
        Gdx.input.getInputProcessor().keyUp(Keys.ESCAPE);
    }

    /** a tour check: {@code problem} is reported (exit code 1) unless {@code ok} */
    private void expect(boolean ok, String problem) {
        if (!ok) {
            problems.add(passName + ": " + problem);
        }
    }

    /** clicks the last visible button reading {@code text} (dialogs are added last, so they win) */
    private void click(String text) {
        Stage stage = stage();
        TextButton button = findButton(stage.getRoot(), text, null);
        if (button == null) {
            problems.add(passName + ": no button \"" + text + "\" to click");
            return;
        }
        Vector2 at = button.localToStageCoordinates(new Vector2(button.getWidth() / 2f, button.getHeight() / 2f));
        stage.stageToScreenCoordinates(at);
        stage.touchDown((int) at.x, (int) at.y, 0, 0);
        stage.touchUp((int) at.x, (int) at.y, 0, 0);
    }

    /** moves the mouse over the last visible label reading {@code text} (shows its tooltip) */
    private void hover(String text) {
        Stage stage = stage();
        Actor label = findActor(stage.getRoot(), text, null);
        if (label == null) {
            problems.add(passName + ": no label \"" + text + "\" to hover");
            return;
        }
        Vector2 at = label.localToStageCoordinates(new Vector2(label.getWidth() / 2f, label.getHeight() / 2f));
        stage.stageToScreenCoordinates(at);
        stage.mouseMoved((int) at.x, (int) at.y);
    }

    private static Actor findActor(Actor actor, String text, Actor found) {
        if (!actor.isVisible()) {
            return found;
        }
        if (actor instanceof com.badlogic.gdx.scenes.scene2d.ui.Label
                && text.equals(((com.badlogic.gdx.scenes.scene2d.ui.Label) actor).getText().toString())) {
            found = actor;
        }
        if (actor instanceof Group) {
            for (Actor child : ((Group) actor).getChildren()) {
                found = findActor(child, text, found);
            }
        }
        return found;
    }

    private static TextButton findButton(Actor actor, String text, TextButton found) {
        if (!actor.isVisible()) {
            return found;
        }
        if (actor instanceof TextButton && text.equals(((TextButton) actor).getText().toString())) {
            found = (TextButton) actor;
        }
        if (actor instanceof Group) {
            for (Actor child : ((Group) actor).getChildren()) {
                found = findButton(child, text, found);
            }
        }
        return found;
    }

    /** the dialog open through {@link UiDialogs} */
    private static final class UiDialogsCurrent {
        static Object get() {
            return staticField(UiDialogs.class, "current");
        }
    }

    private static Object staticField(Class<?> type, String name) {
        try {
            Field f = type.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object field(Object target, String name) {
        for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(target);
            } catch (NoSuchFieldException next) {
                // superclass
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalStateException("no field " + name + " on " + target.getClass());
    }

    private static Object invoke(Object target, String name, Object... args) {
        for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == args.length) {
                    try {
                        m.setAccessible(true);
                        return m.invoke(target, args);
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException(name + ": " + e.getCause(), e);
                    }
                }
            }
        }
        throw new IllegalStateException("no method " + name + " on " + target.getClass());
    }
}
