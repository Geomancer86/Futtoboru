package com.rndmodgames.futtoboru.engine.finances;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.FriendlyTerms;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.MatchIncome;
import com.rndmodgames.futtoboru.engine.temporal.AiFriendlyPlanner;
import com.rndmodgames.futtoboru.engine.temporal.FriendlyArranger;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * G-13: the visitors of a friendly are paid by the home club: their guarantee or their share of the gate, the larger
 * ({@link FriendlyTerms#visitorsTake}, by the visitors' standing). Booked once, when the match is recorded
 * ({@code MatchResultRecorder}): the amount is kept on the match ({@link Match#getGuarantee}), both clubs' balances and
 * period totals move (an expense of the home club, income of the visitors), and each club's gate entry of the match
 * ({@link MatchIncome#getGuarantee}) shows it.
 */
public final class FriendlyGuarantee {

    private FriendlyGuarantee() {
    }

    /**
     * Pays the visitors of a played friendly once.
     *
     * @return the amount paid, or null when nothing was booked (not a friendly, or already booked)
     */
    public static BigDecimal book(SaveGame game, Match match, Club home, Club away) {
        if (!FriendlyArranger.isFriendly(match) || match.getGuarantee() != null || home == null || away == null
                || match.getMatchDateTime() == null) {
            return null;
        }
        FriendlyTerms terms = AiFriendlyPlanner.termsOf(game, away, match.getMatchDateTime().toLocalDate());
        BigDecimal amount = terms.visitorsTake(match.getMatchRevenue());
        match.setGuarantee(amount);
        if (amount.signum() <= 0) {
            return amount;
        }
        home.setClubBalance(home.getClubBalance().subtract(amount));
        home.setSeasonExpenditure(home.getSeasonExpenditure().add(amount));
        home.setMonthExpenditure(home.getMonthExpenditure().add(amount));
        away.setClubBalance(away.getClubBalance().add(amount));
        away.setSeasonIncome(away.getSeasonIncome().add(amount));
        away.setMonthIncome(away.getMonthIncome().add(amount));
        MatchIncome paid = entryOf(match, home);
        paid.setGuarantee(paid.getGuarantee().subtract(amount));
        MatchIncome received = entryOf(match, away);
        received.setGuarantee(received.getGuarantee().add(amount));
        Log.info(Log.ENGINE_SCHEDULING, "FriendlyGuarantee", home.getName() + " pay " + away.getName() + " "
                + Money.format(amount) + " for the friendly " + match.getId());
        return amount;
    }

    /** The club's gate entry of the match (made when it sold no tickets for it). */
    private static MatchIncome entryOf(Match match, Club club) {
        for (MatchIncome income : club.getMatchIncomes()) {
            if (income != null && match.getId() != null && match.getId().equals(income.getMatchId())) {
                return income;
            }
        }
        LocalDateTime date = match.getMatchDateTime();
        MatchIncome income = new MatchIncome(match.getId(), club.getId(), BigDecimal.ZERO, 0, date,
                match.getMatchType());
        club.addMatchIncome(income);
        return income;
    }
}
