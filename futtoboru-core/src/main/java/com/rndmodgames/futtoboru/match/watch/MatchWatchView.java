package com.rndmodgames.futtoboru.match.watch;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.engine.report.MatchEvents;
import com.rndmodgames.futtoboru.engine.simulation.MatchFixture;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.match.ecs.EcsMatchEngine;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEvent;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEventLog;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEventType;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.match.ecs.view.IndexActorSprites;
import com.rndmodgames.futtoboru.match.ecs.view.MatchViewer;
import com.rndmodgames.futtoboru.match.ecs.view.PitchArt;
import com.rndmodgames.futtoboru.match.ecs.view.ViewFrame;
import com.rndmodgames.futtoboru.match.ecs.view.ViewerClock;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.ui.UiScale;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.ui.gfx.GraphicsQuality;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * The match screen (E-21f): the manager's own match, played on the ECS world, watched in the side-on viewer with a
 * newspaper-column ticker (H2, D26) of its events beside it. The match is the one the engine records: the world is
 * the {@link FuttoboruGameEngine.WatchSession}'s, ticked by the engine's own step ({@code Live.tick}); the draw passes
 * only read it. Controls: pause, 1x/2x/4x, "Skip to result" (the rest is played at once, not drawn) and, at full time,
 * "Match result". Keys: {@code SPACE} pause, {@code 1}/{@code 2}/{@code 4} speed, {@code ESC} skip.
 *
 * <p>Owned by the game screen, which draws it in place of its own stage while it is {@link #active()}; call every
 * method on the render thread.
 */
public final class MatchWatchView {

    /** Real time one frame may spend playing the rest of the match when skipping, seconds. */
    static final double SKIP_BUDGET_SECONDS = 0.025;
    private static final int MAX_LINES = 120;
    private static final double[] SPEEDS = { 1, 2, 4 };

    private final FuttoboruGameEngine.WatchSession session;
    private final Runnable onDone;
    private final EcsMatchEngine.Live live;
    private final Match match;
    private final Club homeClub;
    private final Club awayClub;
    private final MatchTickerText text;

    private final Stage stage;
    private final SpriteBatch batch;
    private final ViewFrame frame;
    private final MatchViewer viewer;

    private final VisLabel scoreLabel = new VisLabel();
    private final VisLabel minuteLabel = new VisLabel();
    private final VisTable lines = new VisTable();
    private final VisScrollPane scroll;
    private final VisTextButton pauseButton;
    private final VisTextButton skipButton;
    private final VisTextButton resultButton;
    private final java.util.ArrayDeque<MatchEvent> printed = new java.util.ArrayDeque<>();

    private int eventsRead;
    private boolean skipping;
    private boolean done;
    private boolean disposed;
    private double mood;
    private int viewWidth = -1;
    private int viewHeight = -1;

    public MatchWatchView(Futtoboru game, FuttoboruGameEngine.WatchSession session, Runnable onDone) {
        this.session = session;
        this.onDone = onDone;
        this.live = session.watched().live();
        this.match = session.prepared().match();
        this.homeClub = session.prepared().homeClub();
        this.awayClub = session.prepared().awayClub();
        MatchFixture fixture = session.prepared().fixture();
        this.text = new MatchTickerText(homeClub.getName(), awayClub.getName(),
                ref -> MatchEvents.surname(game.getCurrentGame(), match, ref));

        // the viewer: the engine's own world, the engine's own tick
        PitchGeometry pitch = new PitchGeometry(PitchParams.defaults1888());
        frame = new ViewFrame(GraphicsQuality.load());
        frame.setPitch(pitch, PitchArt.Style.of1888(), fixture.matchKey().hashCode());
        frame.setSprites(new IndexActorSprites(IndexActorSprites.DEFAULT_SEASON, homeClub.getDataId(),
                awayClub.getDataId()));
        batch = new SpriteBatch(4000);
        frame.setBatch(batch);
        viewer = new MatchViewer(live.world(), frame, new ViewerClock(live.world().tickSeconds()), this::step);

        // the ticker column and the controls
        stage = new Stage(UiScale.newViewport());
        pauseButton = new VisTextButton(LanguageModLoader.getValue("watch_pause"), UiTheme.style("button.toggle"));
        skipButton = new VisTextButton(LanguageModLoader.getValue("watch_skip"));
        resultButton = new VisTextButton(LanguageModLoader.getValue("watch_continue"));
        scroll = new VisScrollPane(lines);
        stage.addActor(buildLayout());
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        refreshScore();
        Log.info(Log.UI_MATCH, "MatchWatchView", "watching " + homeClub.getName() + " - " + awayClub.getName());
    }

    /** One simulation tick, the engine's own (events, goals) while the match is on. */
    private void step() {
        if (!live.finished()) {
            live.tick();
        }
    }

    // ---- layout ----------------------------------------------------------------------------------------------------

    private Actor buildLayout() {
        Table root = new Table();
        root.setFillParent(true);
        root.add().grow(); // the viewer shows through here
        root.add(buildColumn()).width(UiTheme.size("ticker.w")).growY();
        return root;
    }

    private VisTable buildColumn() {
        Drawable paper = tinted("ticker.paper");
        VisTable column = new VisTable();
        column.setBackground(paper);
        column.pad(UiTheme.space("sm"));

        VisLabel title = new VisLabel(LanguageModLoader.getValue("watch_title"));
        title.setFontScale(UiTheme.fontScale("heading"));
        title.setColor(UiTheme.color("ticker.ink"));
        column.add(title).center().padBottom(UiTheme.space("xs")).row();
        column.add(rule()).growX().height(UiTheme.size("ticker.rule.heavy")).row();
        column.add(rule()).growX().height(UiTheme.size("ticker.rule")).padTop(UiTheme.space("xxs")).padBottom(UiTheme.space("xs")).row();

        scoreLabel.setColor(UiTheme.color("ticker.ink"));
        scoreLabel.setFontScale(UiTheme.fontScale("subheading"));
        scoreLabel.setAlignment(Align.center);
        scoreLabel.setWrap(true);
        column.add(scoreLabel).growX().row();
        minuteLabel.setColor(UiTheme.color("ticker.ink.soft"));
        column.add(minuteLabel).center().padBottom(UiTheme.space("xs")).row();
        column.add(rule()).growX().height(UiTheme.size("ticker.rule")).padBottom(UiTheme.space("xs")).row();

        lines.top().left();
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        column.add(scroll).grow().row();

        column.add(rule()).growX().height(UiTheme.size("ticker.rule")).padTop(UiTheme.space("xs")).padBottom(UiTheme.space("xs")).row();
        column.add(controls()).growX();
        return column;
    }

    private VisTable controls() {
        VisTable controls = new VisTable(true);
        pauseButton.setProgrammaticChangeEvents(false);
        pauseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setPaused(pauseButton.isChecked());
            }
        });
        controls.add(pauseButton).growX();
        ButtonGroup<VisTextButton> speeds = new ButtonGroup<>();
        for (final double speed : SPEEDS) {
            final VisTextButton b = new VisTextButton((int) speed + "x", UiTheme.style("button.toggle"));
            b.setProgrammaticChangeEvents(false);
            b.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    setSpeed(speed);
                }
            });
            speeds.add(b);
            controls.add(b);
        }
        speeds.getButtons().get(0).setChecked(true);
        controls.row();
        skipButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                skip();
            }
        });
        controls.add(skipButton).colspan(4).growX().row();
        resultButton.setVisible(false);
        resultButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                finish();
            }
        });
        controls.add(resultButton).colspan(4).growX();
        return controls;
    }

    private Drawable tinted(String colorToken) {
        return VisUI.getSkin().newDrawable("white", UiTheme.color(colorToken));
    }

    private Image rule() {
        Image rule = new Image(tinted("ticker.ink"));
        return rule;
    }

    // ---- controls --------------------------------------------------------------------------------------------------

    public void setPaused(boolean paused) {
        viewer.setPaused(paused);
        pauseButton.setProgrammaticChangeEvents(false);
        pauseButton.setChecked(paused);
        pauseButton.setText(LanguageModLoader.getValue(paused ? "watch_resume" : "watch_pause"));
    }

    public void setSpeed(double speed) {
        viewer.setSpeed(speed);
    }

    /** Plays the rest of the match at once (a few ms of each frame, not drawn) and goes to the result. */
    public void skip() {
        if (done) {
            return;
        }
        skipping = true;
        viewer.setPaused(false);
        skipButton.setDisabled(true);
        pauseButton.setDisabled(true);
    }

    public boolean skipping() {
        return skipping;
    }

    public boolean finished() {
        return live.finished();
    }

    public boolean active() {
        return !done;
    }

    /** Ends the screen: the match is recorded (the engine's outcome) and the result screen opens. */
    public void finish() {
        if (done || !live.finished()) {
            return;
        }
        done = true;
        onDone.run();
    }

    /** Input of the screen (the stage and its keys); the game screen sets it while the screen is active. */
    public InputProcessor input() {
        InputMultiplexer in = new InputMultiplexer();
        in.addProcessor(stage);
        in.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (live.finished()) {
                    return false;
                }
                switch (keycode) {
                case Input.Keys.SPACE:
                    if (!skipping) {
                        setPaused(!viewer.paused());
                    }
                    return true;
                case Input.Keys.NUM_1:
                    setSpeed(1);
                    return true;
                case Input.Keys.NUM_2:
                    setSpeed(2);
                    return true;
                case Input.Keys.NUM_4:
                    setSpeed(4);
                    return true;
                case Input.Keys.ESCAPE:
                    skip();
                    return true;
                default:
                    return false;
                }
            }
        });
        return in;
    }

    // ---- frame -----------------------------------------------------------------------------------------------------

    public void resize(int width, int height) {
        UiScale.updateStageViewport(stage, width, height);
        viewWidth = -1; // refit the viewer
    }

    public void render(float delta) {
        if (disposed) {
            return;
        }
        UiTheme.clearScreen();
        if (skipping && !live.finished()) {
            long start = System.nanoTime();
            while (!live.finished() && (System.nanoTime() - start) / 1e9 < SKIP_BUDGET_SECONDS) {
                live.tick();
            }
            if (live.finished()) {
                syncTicker();
                finish();
                return;
            }
        } else {
            drawViewer(Math.min(0.1f, delta));
        }
        syncTicker();
        stage.getViewport().apply(true);
        stage.act(delta);
        stage.draw();
    }

    /** The viewer fills the window left of the ticker column. */
    private void drawViewer(double dt) {
        int bw = Gdx.graphics.getBackBufferWidth();
        int bh = Gdx.graphics.getBackBufferHeight();
        float worldW = stage.getViewport().getWorldWidth();
        int vw = Math.max(1, Math.round(bw * Math.max(0f, worldW - UiTheme.size("ticker.w")) / worldW));
        if (vw != viewWidth || bh != viewHeight) {
            viewWidth = vw;
            viewHeight = bh;
            viewer.view().viewCamera().resize(vw, bh);
        }
        Gdx.gl.glViewport(0, 0, vw, bh);
        mood += ((recentGoal() ? 0.9 : 0.0) - mood) * Math.min(1, dt * 1.5);
        frame.setCrowdMood(mood);
        viewer.frame(dt);
    }

    private long lastGoalTick = Long.MIN_VALUE;

    private boolean recentGoal() {
        return lastGoalTick != Long.MIN_VALUE && live.ticksDone() - lastGoalTick < 12L * 30;
    }

    /** Prints the events the match has made since the last frame and refreshes the score. */
    private void syncTicker() {
        MatchEventLog log = live.events();
        boolean added = false;
        for (; eventsRead < log.size(); eventsRead++) {
            MatchEvent e = log.get(eventsRead);
            if (e.type() == MatchEventType.GOAL) {
                lastGoalTick = e.tick();
            }
            if (text.shows(e)) {
                printed.addFirst(e);
                added = true;
            }
        }
        while (printed.size() > MAX_LINES) {
            printed.removeLast();
        }
        if (added) {
            rebuildLines();
        }
        refreshScore();
        if (live.finished() && !resultButton.isVisible()) {
            resultButton.setVisible(true);
            skipButton.setVisible(false);
            pauseButton.setDisabled(true);
        }
    }

    private void refreshScore() {
        scoreLabel.setText(homeClub.getName() + " " + live.homeGoals() + " - " + live.awayGoals() + " "
                + awayClub.getName());
        MatchEventLog log = live.events();
        minuteLabel.setText(live.finished() ? LanguageModLoader.getValue("watch_full_time")
                : LanguageModLoader.format("match_goal_minute", log.minuteOf(live.world().tickCount())));
    }

    private void rebuildLines() {
        lines.clearChildren();
        float lineW = UiTheme.size("ticker.line.w");
        for (MatchEvent e : printed) {
            boolean strong = MatchTickerText.emphasised(e);
            VisLabel minute = new VisLabel(e.minute() + "'");
            minute.setColor(UiTheme.color("ticker.ink.soft"));
            VisLabel words = new VisLabel(text.line(e));
            words.setWrap(true);
            words.setColor(UiTheme.color(e.type() == MatchEventType.GOAL ? "ticker.goal" : "ticker.ink"));
            if (strong) {
                words.setFontScale(UiTheme.fontScale("subheading.minor"));
            }
            lines.add(minute).top().left().width(UiTheme.size("col.short") * 0.8f);
            lines.add(words).width(lineW - UiTheme.size("col.short") * 0.8f).left().padBottom(UiTheme.space("xs"))
                    .row();
        }
        scroll.setScrollPercentY(0f);
    }

    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        viewer.dispose();
        batch.dispose();
        stage.dispose();
    }
}
