package com.rndmodgames.futtoboru.engine;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.engine.cup.CupBracketManager;
import com.rndmodgames.futtoboru.engine.finances.Money;
import com.rndmodgames.futtoboru.engine.simulation.MatchResultRecorder;

/**
 * P2-7: cup final taken from the bracket, pounds in money text, champion prize from one constant.
 */
public class CupFinalAndPrizeTests extends BaseTest {

    private EngineTestWorld world;
    private MatchResultRecorder recorder;

    @BeforeEach
    void setUp() {
        world = new EngineTestWorld(application, 4);
        recorder = new MatchResultRecorder(application, world.engine.getMessageManager());
    }

    /** Play both semi-finals of the 4-club bracket (home sides win) and return the final */
    private Match playSemis(EngineTestWorld.Cup cup) {
        Match sf1 = cup.byPath("R1M1");
        Match sf2 = cup.byPath("R1M2");
        EngineTestWorld.score(sf1, 1, 0);
        recorder.record(sf1);
        EngineTestWorld.score(sf2, 0, 1);
        recorder.record(sf2);
        Match fin = cup.byPath("R2M1");
        assertEquals(sf1.getHomeClubId(), fin.getHomeClubId());
        assertEquals(sf2.getAwayClubId(), fin.getAwayClubId());
        return fin;
    }

    @Test
    void finalIsTheBracketsLastTie() {
        EngineTestWorld.Cup cup = world.cup(world.clubs);
        CupBracketManager bracket = new CupBracketManager(world.game, application);
        assertFalse(bracket.isBracketFinal(cup.byPath("R1M1")));
        assertTrue(bracket.isBracketFinal(cup.byPath("R2M1")), "a 4-club bracket has its final in round 2, not round 5");
    }

    @Test
    void cupFinalFromBracketCompletesEdition() {
        EngineTestWorld.Cup cup = world.cup(world.clubs);
        Match fin = playSemis(cup);

        EngineTestWorld.score(fin, 2, 1);
        recorder.record(fin);

        assertEquals(fin.getHomeClubId(), cup.edition.getChampionsId());
        assertEquals(fin.getAwayClubId(), cup.edition.getRunnersUpId());
        assertEquals(1, countMessages("CUP_COMPLETE"));

        // the daily authority check must not complete it again
        world.authorityManager.checkCompetitionsSchedule();
        assertEquals(1, countMessages("CUP_COMPLETE"));
        assertEquals(fin.getHomeClubId(), cup.edition.getChampionsId());
    }

    @Test
    void drawnFinalIsReplayedAndTheReplayDecidesTheCup() {
        EngineTestWorld.Cup cup = world.cup(world.clubs);
        Match fin = playSemis(cup);
        EngineTestWorld.score(fin, 1, 1);
        recorder.record(fin);
        assertNull(cup.edition.getChampionsId(), "a drawn final does not finish the cup");

        Club finalist = world.game.getClubById(fin.getAwayClubId());
        Match replay = null;
        for (Match m : finalist.getScheduledMatches()) {
            if (fin.getId().equals(m.getParentMatch1Id())) {
                replay = m;
            }
        }
        assertNotNull(replay, "final replay scheduled");

        EngineTestWorld.score(replay, 3, 0); // the replay is at the other ground: away finalist at home
        recorder.record(replay);

        assertEquals(fin.getAwayClubId(), cup.edition.getChampionsId());
        assertEquals(fin.getHomeClubId(), cup.edition.getRunnersUpId());
    }

    @Test
    void moneyIsInPounds() {
        assertEquals("£500.00", Money.format(new BigDecimal("500")));
        assertEquals("£1,234.50", Money.format(new BigDecimal("1234.5")));
        assertEquals("-£12.00", Money.format(new BigDecimal("-12")));
        assertEquals("£0.00", Money.format((BigDecimal) null));
        assertEquals("£2.50", Money.format(2.5d));
    }

    @Test
    void championMessageShowsPrizeInPounds() {
        CompetitionEdition season = new CompetitionEdition();
        season.setId(world.game.getIdAllocator().nextId());
        season.setName("Test League 1888-89");
        season.setStartDate(LocalDateTime.of(1888, 9, 1, 0, 0));
        world.league.getEditions().add(season);
        for (int i = 0; i < world.clubs.size(); i++) {
            Club c = world.club(i);
            c.setMatchesPlayed(6);
            c.setPoints(12 - 2 * i);
            c.setClubBalance(BigDecimal.ZERO);
        }
        world.game.setGameDate(LocalDateTime.of(1889, 4, 30, 12, 0));

        world.authorityManager.checkCompetitionsSchedule();

        Message champion = null;
        for (Message m : world.game.getAllMessages()) {
            if ("LEAGUE_CHAMPION".equals(m.getMessageType())) {
                champion = m;
            }
        }
        assertNotNull(champion);
        assertTrue(champion.getPlainTextMessage().contains(Money.format(AuthorityManager.LEAGUE_CHAMPION_PRIZE)),
            champion.getPlainTextMessage());
        assertFalse(champion.getPlainTextMessage().contains("$"));
        assertEquals(0, AuthorityManager.LEAGUE_CHAMPION_PRIZE.compareTo(world.club(0).getClubBalance()));
    }

    @Test
    void gameFontHasThePoundSign() {
        BitmapFont.BitmapFontData font = new BitmapFont.BitmapFontData(Gdx.files.internal("skin/droid-sans-13.fnt"), false);
        BitmapFont.Glyph pound = font.getGlyph('£');
        assertNotNull(pound, "droid-sans-13 needs a £ glyph");

        TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(
            Gdx.files.internal("skin/tixel.atlas"), Gdx.files.internal("skin"), false);
        TextureAtlas.TextureAtlasData.Region region = null;
        for (TextureAtlas.TextureAtlasData.Region r : atlas.getRegions()) {
            if ("droid-sans-13".equals(r.name)) {
                region = r;
            }
        }
        assertNotNull(region);
        // the font region is trimmed to its top rows: every glyph must lie inside the packed rows
        int packedTop = region.originalHeight - region.height - (int) region.offsetY;
        assertTrue(pound.srcY + pound.height <= packedTop + region.height, "glyph inside the atlas region");
    }

    private int countMessages(String type) {
        int n = 0;
        for (Message m : world.game.getAllMessages()) {
            if (type.equals(m.getMessageType())) {
                n++;
            }
        }
        return n;
    }
}
