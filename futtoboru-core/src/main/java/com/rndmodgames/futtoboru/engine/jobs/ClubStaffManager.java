package com.rndmodgames.futtoboru.engine.jobs;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.generators.NameGenerator;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Club Staff Manager v1
 *
 * Manages club staff positions and detects vacancies.
 *
 * <p>J-01: a club's posts are {@link #STAFF_POSITIONS} only (agents, investors and owners are not staff); the
 * owner holds his own post ({@link #ensureHoldsPost}); AI clubs appoint candidates ({@link #appointCandidate}) and
 * AI staff leave ({@link #leavePost}) so every club runs the same job market as the player's (D31).
 *
 * @author Geomancer86
 */
public class ClubStaffManager {

    /** J-01: the posts a club fills, in this order (data ids of professions). */
    public static final List<String> STAFF_POSITIONS = List.of(Profession.MANAGER, Profession.DIRECTOR,
            Profession.SCOUT);

    /** Age range of an appointed AI candidate (placeholder, D30). */
    static final int CANDIDATE_MIN_AGE = 28;
    static final int CANDIDATE_AGE_SPAN = 30;

    private Futtoboru gameInstance;
    private SaveGame currentGame;

    public ClubStaffManager(Futtoboru gameInstance) {
        this.gameInstance = gameInstance;
        this.currentGame = gameInstance.getCurrentGame();
    }

    /**
     * Get staff member for a profession at a club
     *
     * @param club The club
     * @param profession The profession
     * @return Person holding the position, or null if vacant
     */
    public Person getClubStaff(Club club, Profession profession) {
        if (club == null || profession == null) {
            return null;
        }

        return getPersonById(club.getStaffId(profession.getId()));
    }

    /** @return the game's person with that id, or null */
    Person getPersonById(Long personId) {
        if (personId == null || currentGame == null) {
            return null;
        }
        for (Person person : currentGame.getAllPersons()) {
            if (person != null && personId.equals(person.getId())) {
                return person;
            }
        }
        return null;
    }

    /**
     * Set staff member for a profession at a club
     *
     * @param club The club
     * @param profession The profession
     * @param person The person (null to vacate position)
     */
    public void setClubStaff(Club club, Profession profession, Person person) {
        if (club == null || profession == null) {
            return;
        }

        Long personId = person != null ? person.getId() : null;
        club.setStaffId(profession.getId(), personId);

        // Update person's current club
        if (person != null) {
            person.setCurrentClubId(club.getId());
        }
    }

    /**
     * Check if a position is vacant
     *
     * @param club The club
     * @param profession The profession
     * @return true if position is vacant
     */
    public boolean isPositionVacant(Club club, Profession profession) {
        if (club == null || profession == null) {
            return false;
        }

        return club.isPositionVacant(profession.getId());
    }

    /** @return true for a profession a club appoints ({@link #STAFF_POSITIONS}) */
    public static boolean isStaffPosition(Profession profession) {
        return profession != null && profession.getDataId() != null
                && STAFF_POSITIONS.contains(profession.getDataId());
    }

    /** @return the posts every club fills ({@link #STAFF_POSITIONS}), as the database's professions */
    public List<Profession> getStaffPositions() {
        List<Profession> posts = new ArrayList<>();
        for (String dataId : STAFF_POSITIONS) {
            Profession profession = DatabaseLoader.getProfessionByDataId(dataId);
            if (profession != null && profession.getId() != null) {
                posts.add(profession);
            }
        }
        return posts;
    }

    /**
     * Get all vacant positions for a club
     *
     * @param club The club
     * @return List of professions that are vacant
     */
    public List<Profession> getVacantPositions(Club club) {
        List<Profession> vacant = new ArrayList<>();
        if (club == null) {
            return vacant;
        }
        for (Profession profession : getStaffPositions()) {
            if (isPositionVacant(club, profession)) {
                vacant.add(profession);
            }
        }
        return vacant;
    }

    /** Vacates every post {@code person} holds at {@code club} (a move to another club, a departure). */
    public void vacatePostsOf(Club club, Person person) {
        if (club == null || person == null || person.getId() == null) {
            return;
        }
        for (Long professionId : new ArrayList<>(club.getStaff().keySet())) {
            if (person.getId().equals(club.getStaffId(professionId))) {
                club.setStaffId(professionId, null);
            }
        }
    }

    /**
     * The owner holds the post of his profession at his club (J-01): a new game only sets the manager's club, so
     * his own post looked vacant (advertised, and an AI club would have appointed someone to it).
     */
    public void ensureHoldsPost(Person person) {
        if (person == null || person.getId() == null || person.getCurrentClubId() == null
                || !isStaffPosition(person.getPrimaryProfession()) || currentGame == null) {
            return;
        }
        Club club = currentGame.getClubById(person.getCurrentClubId());
        if (club == null) {
            return;
        }
        Long professionId = person.getPrimaryProfession().getId();
        Long holder = club.getStaffId(professionId);
        if (person.getId().equals(holder)) {
            return;
        }
        Person previous = getPersonById(holder);
        if (previous != null && club.getId().equals(previous.getCurrentClubId())) {
            previous.setCurrentClubId(null);
        }
        club.setStaffId(professionId, person.getId());
        Log.info(Log.ENGINE_JOBS, "ClubStaffManager", person.getName() + " holds the post of "
                + person.getPrimaryProfession().getName() + " at " + club.getName());
    }

    /**
     * An AI club appoints someone to a vacant post (D31): an out-of-work person of that profession (lowest ids
     * first, one drawn at random) or, when there is none, a new person generated for the post.
     *
     * @param rng a stream forked for this appointment
     * @return the person appointed
     */
    public Person appointCandidate(Club club, Profession profession, RandomSource rng) {
        if (club == null || profession == null || rng == null || currentGame == null) {
            return null;
        }
        Person owner = currentGame.getOwner();
        List<Person> pool = new ArrayList<>();
        for (Person person : currentGame.getAllPersons()) {
            if (person != null && person != owner && person.getId() != null && person.getCurrentClubId() == null
                    && person.getPrimaryProfession() != null
                    && profession.getId().equals(person.getPrimaryProfession().getId())
                    && !com.rndmodgames.futtoboru.engine.committee.Committee.hasDismissed(club, person.getId())) {
                pool.add(person); // G-05: never the man this club's committee dismissed
            }
        }
        pool.sort((a, b) -> Long.compare(a.getId(), b.getId()));
        Person candidate = !pool.isEmpty() ? pool.get(rng.nextInt(pool.size())) : newCandidate(club, profession, rng);
        hireStaff(club, profession, candidate);
        return candidate;
    }

    /** A person generated for a post: a name of the club's country (English without one), 28 to 57 years old. */
    private Person newCandidate(Club club, Profession profession, RandomSource rng) {
        Country country = club.getCountry();
        if (DatabaseLoader.getFirstNames(country).size() < 2 || DatabaseLoader.getLastNames(country).size() < 2) {
            country = DatabaseLoader.getCountryByDataId("england");
        }
        Person person = new Person();
        String[] name = NameGenerator.generateName(country, rng);
        person.setName(name[0]);
        person.setLastname(name[1]);
        person.setCountry(country);
        person.setCurrentCountry(country);
        LocalDateTime today = currentGame.getGameDate() != null ? currentGame.getGameDate() : LocalDateTime.now();
        person.setBirthDate(today.minusYears(CANDIDATE_MIN_AGE + rng.nextInt(CANDIDATE_AGE_SPAN))
                .minusDays(rng.nextInt(365)));
        person.setPrimaryProfession(profession);
        currentGame.assignId(person);
        currentGame.getAllPersons().add(person);
        return person;
    }

    /**
     * The holder of an AI club's post leaves it (D31 turnover): the post is vacant and he is out of work, a
     * candidate for other clubs. The owner never leaves this way.
     *
     * @return the person who left, or null
     */
    public Person leavePost(Club club, Profession profession) {
        Person holder = getClubStaff(club, profession);
        if (holder == null || holder == currentGame.getOwner()) {
            return null;
        }
        club.setStaffId(profession.getId(), null);
        if (club.getId().equals(holder.getCurrentClubId())) {
            holder.setCurrentClubId(null);
        }
        Log.info(Log.ENGINE_JOBS, "ClubStaffManager", holder.getName() + " " + holder.getLastname() + " left the post of "
                + profession.getName() + " at " + club.getName());
        return holder;
    }

    /**
     * Fire staff member (creates vacancy)
     *
     * @param club The club
     * @param profession The profession
     */
    public void fireStaff(Club club, Profession profession) {
        if (club == null || profession == null) {
            return;
        }

        Person currentStaff = getClubStaff(club, profession);
        if (currentStaff != null) {
            // Remove from club
            setClubStaff(club, profession, null);

            // Set person as unemployed
            currentStaff.setCurrentClubId(null);

            Log.info(Log.ENGINE_JOBS, "ClubStaffManager", "Fired " + profession.getName() +
                       " from " + club.getName() + ": " + currentStaff.getName());
        }
    }

    /**
     * Hire staff member (fills vacancy)
     *
     * @param club The club
     * @param profession The profession
     * @param person The person to hire
     */
    public void hireStaff(Club club, Profession profession, Person person) {
        if (club == null || profession == null || person == null) {
            return;
        }

        // J-01: a move leaves the old club's post vacant
        if (person.getCurrentClubId() != null && !person.getCurrentClubId().equals(club.getId())) {
            vacatePostsOf(currentGame.getClubById(person.getCurrentClubId()), person);
        }
        vacatePostsOf(club, person);

        // Set staff
        setClubStaff(club, profession, person);

        // Update person's profession if needed
        if (person.getPrimaryProfession() == null ||
            !person.getPrimaryProfession().getId().equals(profession.getId())) {
            person.setPrimaryProfession(profession);
        }

        Log.info(Log.ENGINE_JOBS, "ClubStaffManager", "Hired " + person.getName() +
                   " as " + profession.getName() + " at " + club.getName());
    }
}
