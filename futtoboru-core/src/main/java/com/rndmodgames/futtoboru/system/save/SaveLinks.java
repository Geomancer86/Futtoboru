package com.rndmodgames.futtoboru.system.save;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * The end of loading (P2-5): what the generic reading can't do. Drops the references that didn't resolve from their
 * lists, gives {@code Club.staff} its {@code Long} keys back, binds the database lookups to the loaded game and
 * checks the references kept as numbers (club, person and profession ids), reporting the ones that don't resolve.
 */
final class SaveLinks {

    private SaveLinks() {
    }

    static void afterLoad(SaveGame game, SaveCodec.Reading reading) {
        List<String> problems = reading.problems;

        dropNulls(game.getAllClubs());
        // P2-13: a save made before the fix lists the clubs of the selected countries twice (each entry is the one
        // saved club object); the daily and weekly club steps ran twice for them
        int duplicateClubs = game.removeDuplicateClubs();
        int duplicateSnapshots = game.removeDuplicateSnapshots();
        if (duplicateClubs > 0 || duplicateSnapshots > 0) {
            com.rndmodgames.futtoboru.system.Log.info(com.rndmodgames.futtoboru.system.Log.SYSTEM_INIT, "SaveLinks",
                    "clubs listed twice: " + duplicateClubs + ", attribute snapshots taken twice: "
                            + duplicateSnapshots + " (dropped)");
        }
        dropNulls(game.getAllPersons());
        dropNulls(game.getAllPlayers());
        dropNulls(game.getAllCups());
        dropNulls(game.getAllLeagues());
        dropNulls(game.getSelectedCountries());
        dropNulls(game.getGameScripts());
        for (Authority a : reading.all(Authority.class)) {
            dropNulls(a.getLeagues());
        }
        for (Competition c : reading.all(Competition.class)) {
            dropNulls(c.getEditions());
        }
        for (League l : reading.all(League.class)) {
            dropNulls(l.getLeagueClubs());
            dropNulls(l.getEditions());
        }
        for (Club club : reading.all(Club.class)) {
            dropNulls(club.getProposedMatches());
            dropNulls(club.getScheduledMatches());
            dropNulls(club.getPlayedMatches());
            if (club.getPlayers() != null) {
                for (Iterator<Player> it = club.getPlayers().iterator(); it.hasNext();) {
                    Player p = it.next();
                    if (p == null || p.getPerson() == null) {
                        it.remove();
                        problems.add("club " + club.getId() + ": a player without person was left out");
                    }
                }
            }
            club.setStaff(longKeys(club.getStaff(), club, problems));
        }

        DatabaseLoader.bindGame(reading.all(Club.class), reading.all(Person.class), reading.all(Competition.class),
                game.getMainAuthority());

        checkNumberReferences(game, reading, problems);
    }

    private static <T> void dropNulls(List<T> list) {
        if (list != null) {
            list.removeIf(Objects::isNull);
        }
    }

    /** JSON object keys are text: {@code Map<Long, Long>} comes back with String keys unless converted. */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Map<Long, Long> longKeys(Map staff, Club club, List<String> problems) {
        Map<Long, Long> out = new HashMap<>();
        if (staff == null) {
            return out;
        }
        for (Object o : staff.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            try {
                Long key = e.getKey() instanceof Number ? ((Number) e.getKey()).longValue() : Long.valueOf(e.getKey().toString());
                Object v = e.getValue();
                out.put(key, v == null ? null : v instanceof Number ? ((Number) v).longValue() : Long.valueOf(v.toString()));
            } catch (NumberFormatException ex) {
                problems.add("club " + club.getId() + ": staff entry `" + e.getKey() + "` is not an id, left out");
            }
        }
        return out;
    }

    /** References kept as numbers must point at a club, person or profession that exists after the load. */
    private static void checkNumberReferences(SaveGame game, SaveCodec.Reading reading, List<String> problems) {
        Set<Long> persons = new HashSet<>();
        for (Person p : reading.all(Person.class)) {
            persons.add(p.getId());
        }
        for (Person p : DatabaseLoader.getPersons()) {
            persons.add(p.getId());
        }
        Person owner = game.getOwner();
        if (owner != null && owner.getCurrentClubId() != null && DatabaseLoader.getClubById(owner.getCurrentClubId()) == null) {
            problems.add("the manager's club " + owner.getCurrentClubId() + " doesn't exist");
        }
        for (Match m : reading.all(Match.class)) {
            club(m.getHomeClubId(), "match " + m.getId(), problems);
            club(m.getAwayClubId(), "match " + m.getId(), problems);
        }
        for (CompetitionEdition e : reading.all(CompetitionEdition.class)) {
            if (e.getParticipantClubsIds() != null) {
                for (Long id : e.getParticipantClubsIds()) {
                    club(id, "edition " + e.getId(), problems);
                }
            }
        }
        for (JobOpening j : game.getActiveJobOpenings()) {
            club(j.getClubId(), "job " + j.getId(), problems);
            if (j.getProfessionId() != null && DatabaseLoader.getProfessionById(j.getProfessionId()) == null) {
                problems.add("job " + j.getId() + ": profession " + j.getProfessionId() + " doesn't exist");
            }
        }
        for (Club club : reading.all(Club.class)) {
            for (Map.Entry<Long, Long> s : club.getStaff().entrySet()) {
                if (DatabaseLoader.getProfessionById(s.getKey()) == null) {
                    problems.add("club " + club.getId() + ": staff profession " + s.getKey() + " doesn't exist");
                }
                if (s.getValue() != null && !persons.contains(s.getValue())) {
                    problems.add("club " + club.getId() + ": staff person " + s.getValue() + " doesn't exist");
                }
            }
        }
    }

    private static void club(Long id, String where, List<String> problems) {
        if (id != null && DatabaseLoader.getClubById(id) == null) {
            problems.add(where + ": club " + id + " doesn't exist");
        }
    }
}
