package com.rndmodgames.futtoboru.tables.authority;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.LeagueStandingRow;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * U-12, headless: the Authorities screen shows the season the league is in (it used to be the first season of the
 * data, hard-coded), and from the 1889-90 rollover on, 1889-90 and its clubs.
 */
class AuthorityScreenTableTest {

    private static final java.util.function.Supplier<java.util.List<com.rndmodgames.futtoboru.data.Season>> DATA_SEASONS =
            () -> com.rndmodgames.futtoboru.system.DatabaseLoader.getInstance().getSeasons();

    private static Club club(long id, String name) {
        Club club = new Club();
        club.setId(id);
        club.setName(name);
        return club;
    }

    private static CompetitionEdition edition(String name, LocalDateTime start, Long... clubs) {
        CompetitionEdition edition = new CompetitionEdition();
        edition.setName(name);
        edition.setStartDate(start);
        edition.getParticipantClubsIds().addAll(List.of(clubs));
        return edition;
    }

    private static SaveGame game(LocalDateTime date, League league, Club... clubs) {
        SaveGame game = new SaveGame();
        game.setGameDate(date);
        game.setAllClubs(new ArrayList<>(List.of(clubs)));
        Authority authority = new Authority();
        if (league != null) {
            authority.getLeagues().add(league);
        }
        game.setMainAuthority(authority);
        return game;
    }

    @Test
    void beforeTheLeagueIsFoundedItIsTheSeasonOfTheDate() {
        assertEquals("1888-89", AuthorityScreenTable.seasonLabel(game(LocalDateTime.of(1888, 4, 1, 4, 0), null)));
        assertNull(AuthorityScreenTable.currentEdition(null));
    }

    @Test
    void theSeasonBeingPlayedEvenWhenTheNextOneIsAlreadyGenerated() {
        Club preston = club(1, "Preston North End");
        Club villa = club(2, "Aston Villa");
        League league = new League();
        CompetitionEdition first = edition("1888-89 Football League", LocalDateTime.of(1888, 9, 8, 15, 0), 1L, 2L);
        // the engine creates 1889-90 on 1 January 1889, while 1888-89 is still played
        CompetitionEdition next = edition("Football League 1889-90", LocalDateTime.of(1889, 9, 1, 0, 0), 2L);
        league.getEditions().add(first);
        league.getEditions().add(next);
        SaveGame game = game(LocalDateTime.of(1889, 2, 2, 15, 0), league, preston, villa);

        assertSame(first, AuthorityScreenTable.currentEdition(league));
        assertEquals("1888-89", AuthorityScreenTable.seasonLabel(game));
        assertEquals(List.of(preston, villa), AuthorityScreenTable.seasonClubs(game, DATA_SEASONS));

        // the rollover: 1888-89 keeps its final table, the new season starts
        first.getFinalStandings().add(LeagueStandingRow.of(1, preston));
        game.setGameDate(LocalDateTime.of(1889, 4, 21, 15, 0));

        assertSame(next, AuthorityScreenTable.currentEdition(league));
        assertEquals("1889-90", AuthorityScreenTable.seasonLabel(game));
        assertEquals(List.of(villa), AuthorityScreenTable.seasonClubs(game, DATA_SEASONS), "the new season's clubs");
    }

    @Test
    void seasonLabelsKeepTwoDigitsAfterTheDash() {
        // edition names used "1899-0" and "1900-1" (and needsNextSeasonGeneration looked for those)
        assertEquals("1888-89", Season.label(1888));
        assertEquals("1899-00", Season.label(1899));
        assertEquals("1900-01", Season.label(1900));
        assertEquals("1909-10", Season.label(1909));
    }

    @Test
    void whenEverySeasonIsFinishedTheLastOne() {
        League league = new League();
        CompetitionEdition only = edition("1888-89 Football League", LocalDateTime.of(1888, 9, 8, 15, 0));
        only.getFinalStandings().add(new LeagueStandingRow());
        league.getEditions().add(only);
        assertSame(only, AuthorityScreenTable.currentEdition(league));
    }

    @Test
    void withoutEntrantsTheLeagueClubs() {
        Club stoke = club(3, "Stoke");
        League league = new League();
        league.setLeagueClubs(new ArrayList<>(List.of(stoke)));
        league.getEditions().add(edition("1888-89 Football League", LocalDateTime.of(1888, 9, 8, 15, 0)));
        assertEquals(List.of(stoke), AuthorityScreenTable.seasonClubs(game(LocalDateTime.of(1888, 9, 1, 0, 0), league, stoke), DATA_SEASONS));
    }
}
