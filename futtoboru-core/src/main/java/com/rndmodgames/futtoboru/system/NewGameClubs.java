package com.rndmodgames.futtoboru.system;

import java.util.List;
import java.util.Map;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Country;

/**
 * The competitions and clubs of a new game (NewGameOverviewScreen step 6; tests build their games with it too).
 *
 * <p>P2-13: every club is listed once. The screen used to add the competitions' participants (some from countries
 * nobody selected, e.g. Linfield in the FA Cup) and then every club of each selected country again, so the clubs of
 * the selected countries were in {@link SaveGame#getAllClubs()} twice and every per-club daily and weekly step (tickets,
 * attribute drift, weekly expenses and snapshots) ran twice for them.
 */
public final class NewGameClubs {

    private NewGameClubs() {
    }

    /**
     * Adds the competitions to the game's cup and league lists, then the participants of all their editions, then the
     * clubs of the selected countries, each club once by id, in that order (stable: the data's order).
     *
     * @return the number of clubs the game lists afterwards
     */
    public static int addTo(SaveGame game, List<Competition> competitions, List<Country> selectedCountries) {
        if (competitions != null) {
            for (Competition comp : competitions) {
                if (comp == null) {
                    continue;
                }
                if (Competition.COMPETITION_CUP.equals(comp.getCompetitionType())) {
                    if (!game.getAllCups().contains(comp)) {
                        game.getAllCups().add(comp);
                    }
                } else if (Competition.COMPETITION_LEAGUE.equals(comp.getCompetitionType())) {
                    if (!game.getAllLeagues().contains(comp)) {
                        game.getAllLeagues().add(comp);
                    }
                }
                if (comp.getEditions() == null) {
                    continue;
                }
                for (CompetitionEdition edition : comp.getEditions()) {
                    if (edition == null || edition.getParticipantClubsIds() == null) {
                        continue;
                    }
                    for (Long clubId : edition.getParticipantClubsIds()) {
                        Club club = DatabaseLoader.getClubById(clubId);
                        if (club != null && game.addClub(club)) {
                            Log.debug(Log.UI_MENU, "NewGameClubs", "participant " + club.getName() + " (" + clubId
                                    + ") of " + comp.getName());
                        }
                    }
                }
            }
        }
        Map<Long, List<Club>> byCountry = DatabaseLoader.getClubsByCountry();
        if (selectedCountries != null && byCountry != null) {
            for (Country country : selectedCountries) {
                if (country == null || country.getId() == null) {
                    Log.error(Log.UI_MENU, "NewGameClubs", "a selected country is null or has no id: " + country);
                    continue;
                }
                List<Club> clubs = byCountry.get(country.getId());
                if (clubs == null) {
                    Log.error(Log.UI_MENU, "NewGameClubs", "no clubs for country " + country.getId() + " ("
                            + country.getCommonName() + ")");
                    continue;
                }
                int added = 0;
                for (Club club : clubs) {
                    if (game.addClub(club)) {
                        added++;
                    }
                }
                Log.info(Log.UI_MENU, "NewGameClubs", country.getCommonName() + ": " + clubs.size() + " clubs, "
                        + added + " not already listed as competition participants");
            }
        }
        return game.getAllClubs().size();
    }
}
