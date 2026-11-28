package com.rndmodgames.futtoboru.system.generators;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ContractBand;
import com.rndmodgames.futtoboru.data.ContractType;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerContract;
import com.rndmodgames.futtoboru.engine.contracts.ContractCalendar;
import com.rndmodgames.futtoboru.engine.contracts.ContractTerms;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.data.IdAllocator;

/**
 * G-04: the engagement a man starts the game with. One season ("signed articles to play ... during the forthcoming
 * season", docs/data/history/RECRUITMENT_1888-1900.md 4.1), to 30 April (D28 Q8), on the terms of his club's wage band
 * ({@code seasons/<id>/contracts.json}): a professional (the band's share) at about his asking wage, some with summer
 * pay; an amateur takes no wage, only weekly expenses in the playing season (4.5). No bonuses: none is sourced for
 * 1888 (the signing-on present is from the 1890s).
 *
 * @author Geomancer86
 */
public class PlayerContractGenerator {

    /**
     * @param player     the man (his attributes and birth date set the wage he asks)
     * @param club       his club
     * @param startDate  when the engagement starts (the data season's start)
     * @param season     the season it is for (1888 = 1888-89); it ends on 30 April of the next year
     * @param band       his club's wage band
     * @param ids        gives the contract its runtime id (the data allocator at load, the game's during play)
     * @param rng        the terms' dice (a fork of the game's random source for this contract)
     * @return the engagement, or null when the player or the club has no id
     */
    public static PlayerContract generateSeasonContract(Player player, Club club, LocalDateTime startDate, int season,
            ContractBand band, IdAllocator ids, RandomSource rng) {
        if (player == null || club == null || startDate == null || band == null) {
            Log.error(Log.DATA_GENERATION, "PlayerContractGenerator", "Cannot generate contract: player=" + (player != null)
                    + ", club=" + (club != null) + ", date=" + (startDate != null) + ", band=" + (band != null));
            return null;
        }
        Long playerId = player.getId() != null ? player.getId()
                : player.getPerson() != null ? player.getPerson().getId() : null;
        if (playerId == null || club.getId() == null) {
            Log.error(Log.DATA_GENERATION, "PlayerContractGenerator", "Cannot generate contract: player id " + playerId
                    + ", club id " + club.getId());
            return null;
        }

        PlayerContract contract = new PlayerContract();
        contract.setId(ids.nextId());
        contract.setPlayerId(playerId);
        contract.setClubId(club.getId());
        contract.setSeason(season);
        contract.setStartDate(startDate);
        contract.setEndDate(ContractCalendar.seasonEnd(season));
        contract.setContractLengthMonths((int) Math.max(1, ChronoUnit.MONTHS.between(startDate, contract.getEndDate())));

        boolean professional = band.getProfessionalShare() > 0 && rng.nextDouble() < band.getProfessionalShare();
        double noise = rng.nextDouble();
        boolean summerPay = rng.nextDouble() < band.getSummerPayShare();
        if (professional) {
            contract.setContractType(ContractType.PROFESSIONAL);
            BigDecimal wage = withNoise(ContractTerms.askingWage(player, band, startDate.toLocalDate()), noise, band);
            contract.setWeeklyWage(wage);
            contract.setSummerWage(summerPay ? ContractTerms.summerFor(wage, band) : BigDecimal.ZERO);
        } else {
            contract.setContractType(ContractType.AMATEUR);
            contract.setWeeklyWage(BigDecimal.ZERO);
            contract.setSummerWage(BigDecimal.ZERO);
            contract.setWeeklyExpenses(band.getAmateurExpenses());
        }
        contract.setIsActive(true);

        Log.debug(Log.DATA_GENERATION, "PlayerContractGenerator", "Contract " + contract.getId() + ": player "
                + playerId + " at " + club.getName() + ", " + ContractType.getName(contract.getContractType()) + ", wage "
                + contract.getWeeklyWage() + ", summer " + contract.getSummerWage() + ", to " + contract.getEndDate());
        return contract;
    }

    /** The asking wage, a few shillings either way (noise 0-1 = -10% to +10%), whole shillings inside the band */
    public static BigDecimal withNoise(BigDecimal asking, double noise, ContractBand band) {
        int min = ContractBand.shillings(band.getSeasonWageMin());
        int max = ContractBand.shillings(band.getSeasonWageMax());
        int s = (int) Math.round(ContractBand.shillings(asking) * (0.9 + 0.2 * noise));
        return ContractBand.ofShillings(Math.max(min, Math.min(max, s)));
    }
}
