package com.rndmodgames.futtoboru.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Stadium;
import com.rndmodgames.futtoboru.system.data.DataRecord;
import com.rndmodgames.futtoboru.system.data.GameData;

/** U-11, headless (no GL): ground id resolution, paths, whole-number scale, missing-file fallback. */
class GroundArtTest {

    private static Club clubAt(String groundId) {
        Club club = new Club();
        if (groundId != null) {
            Stadium stadium = new Stadium();
            stadium.setDataId(groundId);
            club.setStadium(stadium);
        }
        return club;
    }

    @Test
    void groundIdIsTheClubsSeasonGround() {
        assertEquals("anfield", GroundArt.groundId(clubAt("anfield")));
        assertNull(GroundArt.groundId(clubAt(null)), "club without a ground");
        assertNull(GroundArt.groundId((Club) null));
        assertNull(GroundArt.groundId(clubAt(" ")), "blank id");
    }

    @Test
    void pathsFollowTheGeneratorsLayout() {
        assertEquals("art/grounds/deepdale.png", GroundArt.path("deepdale", false));
        assertEquals("art/grounds/deepdale_thumb.png", GroundArt.path("deepdale", true));
        assertNull(GroundArt.path(null, false));
    }

    @Test
    void scaleIsAWholeNumberAndAtLeastOne() {
        assertEquals(1, GroundArt.integerScale(320, 180, 320, 180));
        assertEquals(2, GroundArt.integerScale(320, 180, 700, 400));
        assertEquals(1, GroundArt.integerScale(320, 180, 639, 1000), "1.99 rounds down, never blurs");
        assertEquals(1, GroundArt.integerScale(320, 180, 100, 50), "too small a box still shows 1x");
        assertEquals(2, GroundArt.integerScale(64, 36, 128, 72));
        assertEquals(1, GroundArt.integerScale(0, 0, 100, 100));
    }

    @Test
    void missingPictureGivesNullOnceAndNeverCallsTheLoader() {
        AtomicInteger existsCalls = new AtomicInteger();
        AtomicInteger loads = new AtomicInteger();
        GroundArt art = new GroundArt(path -> {
            existsCalls.incrementAndGet();
            return false;
        }, path -> {
            loads.incrementAndGet();
            return null;
        });

        assertNull(art.texture("no-such-ground", false));
        assertNull(art.texture("no-such-ground", false));
        assertFalse(art.hasPicture("no-such-ground", false));
        assertNull(art.texture(null, true), "no id, no lookup");
        assertEquals(1, existsCalls.get(), "a miss is cached");
        assertEquals(0, loads.get());
        assertEquals(1, art.cachedPaths());
        art.dispose();
        assertEquals(0, art.cachedPaths());
    }

    @Test
    void aFailingLoadFallsBackInsteadOfThrowing() {
        GroundArt art = new GroundArt(path -> true, path -> {
            throw new IllegalStateException("no GL context");
        });
        assertNull(art.texture("anfield", false));
        assertNull(art.texture("anfield", true));
        art.dispose();
    }

    @Test
    void shippedPicturesExistForEveryLeagueGroundOf1888() {
        GameData data = GameData.loadDefault();
        List<String> missing = new ArrayList<>();
        int checked = 0;
        for (DataRecord squad : data.records("squad", "1888-89")) {
            String ground = squad.ref("ground");
            DataRecord club = data.get(GameData.CLUB, squad.ref("club"));
            assertNotNull(club);
            if (ground == null || !isFootballLeague1888(squad.ref("club"))) {
                continue;
            }
            checked++;
            for (boolean thumb : new boolean[] { false, true }) {
                String path = GroundArt.path(ground, thumb);
                if (GroundArtTest.class.getClassLoader().getResource(path) == null) {
                    missing.add(path);
                }
            }
        }
        assertEquals(12, checked);
        assertTrue(missing.isEmpty(), "pictures missing: " + missing);
    }

    private static boolean isFootballLeague1888(String clubId) {
        return List.of("accrington", "aston-villa", "blackburn-rovers", "bolton-wanderers", "burnley", "derby-county",
                "everton", "notts-county", "preston-north-end", "stoke", "west-bromwich-albion",
                "wolverhampton-wanderers").contains(clubId);
    }
}
