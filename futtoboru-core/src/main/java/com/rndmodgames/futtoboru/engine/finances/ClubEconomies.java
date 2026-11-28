package com.rndmodgames.futtoboru.engine.finances;

import java.time.LocalDate;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubEconomy;
import com.rndmodgames.futtoboru.engine.contracts.ContractCalendar;
import com.rndmodgames.futtoboru.engine.contracts.ContractManager;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

/** G-12a: the economy ({@code economy.json}) a club runs on, by its standing in the season running on a day. */
public final class ClubEconomies {

    private ClubEconomies() {
    }

    /** @return the economy of {@code club}'s standing in the season of {@code day} (built-in without game data) */
    public static ClubEconomy of(SaveGame game, Club club, LocalDate day) {
        int season = ContractCalendar.seasonOf(day);
        return ClubEconomy.forStanding(DatabaseLoader.getEconomies(season),
                ContractManager.standingOf(game, club, season));
    }
}
