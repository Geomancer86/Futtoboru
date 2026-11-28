package com.rndmodgames.futtoboru.engine.recruitment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.StaffJudgement;
import com.rndmodgames.futtoboru.data.recruitment.PoolRegion;
import com.rndmodgames.futtoboru.engine.contracts.ContractManager;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.data.RecruitmentData;
import com.rndmodgames.futtoboru.system.generators.LanguageGenerator;
import com.rndmodgames.futtoboru.system.generators.NameGenerator;

/**
 * R-09 (docs/design/RECRUITMENT_AND_TRIALS.md §7): every club has a <b>trainer</b> and a <b>secretary</b>, AI clubs
 * too (D31); the player is the secretary of his own club. Each holder has a {@link StaffJudgement} (judging skills and
 * one bias, for R-04's perception), seeded on his own fork {@code fork("judge:" + personId)}.
 *
 * <p>The posts are kept in {@code Club.staff} (profession id -> person id) like the job-market posts, but they are not
 * on the job market ({@code ClubStaffManager.STAFF_POSITIONS}): an AI club's trainer and secretary are made here, and
 * the player's club's secretary post is never stored (it is the player, {@link #secretaryOf}), so the player never
 * "holds two posts". G-05 judges the holder of the manager post as the secretary(-manager): an AI club's secretary is
 * that man once the job market has appointed one, else the club's own {@code secretary} (so there always is one).
 * G-05's committee men (inside {@code Club.committee}, not persons) get a {@code committee-member} judgement too.
 *
 * <p>Profiles, ages and the bias draws are placeholders (D30 A3), listed for review.
 */
public final class StaffRoles {

    /** Mean judging skill per role and group: physical, technical, mental, keeper (placeholder). */
    static final int[] TRAINER_MEANS = { 15, 9, 10, 8 };
    static final int[] SECRETARY_MEANS = { 11, 11, 11, 10 };
    static final int[] COMMITTEE_MEANS = { 9, 9, 13, 7 };
    /** R-04: the captain judges on the field (technical and teamwork best, design §7); not a profession */
    static final int[] CAPTAIN_MEANS = { 10, 13, 12, 7 };
    public static final String CAPTAIN = "captain";
    /** A judging skill is its mean plus a spread of -3..+3 (two dice), on the 3..23 scale (placeholder). */
    static final int SPREAD = 3;
    /** Ages of a generated trainer and secretary (placeholder: the trainer an ex-sprinter or pedestrian). */
    static final int TRAINER_MIN_AGE = 30;
    static final int SECRETARY_MIN_AGE = 28;
    static final int AGE_SPAN = 26;
    /** The share of secretaries who know men in the west of Scotland too (placeholder; R§1.2: recruiting trips). */
    static final double SCOTS_NETWORK = 0.3;
    static final String SCOTLAND_WEST = "scotland-west";

    private StaffRoles() {
    }

    /**
     * Gives every club of the game the posts it lacks (a trainer, a secretary) and every holder (the player too) his
     * judgement. Idempotent: run it every day; it only acts on what is missing.
     *
     * @param root the game's root random source
     * @return how many people were appointed
     */
    public static int ensure(SaveGame game, LocalDateTime today, RandomSource root) {
        if (game == null || root == null) {
            return 0;
        }
        Profession trainer = DatabaseLoader.getInstance().getProfessionByDataId(Profession.TRAINER);
        Profession secretary = DatabaseLoader.getInstance().getProfessionByDataId(Profession.SECRETARY);
        if (trainer == null || secretary == null || trainer.getId() == null || secretary.getId() == null) {
            return 0;
        }
        LocalDateTime day = today != null ? today : game.getGameDate();
        Person owner = game.getOwner();
        Club mine = game.getCurrentClub();
        int appointed = 0;
        for (Club club : ContractManager.uniqueClubs(game)) {
            if (club.getId() == null) {
                continue;
            }
            if (holder(game, club, trainer) == null) {
                appoint(game, club, trainer, day, root);
                appointed++;
            }
            if (club == mine) {
                // the player is his club's secretary: a man left in the post (a club the player just joined) goes
                Person previous = holder(game, club, secretary);
                if (previous != null) {
                    club.setStaffId(secretary.getId(), null);
                    if (club.getId().equals(previous.getCurrentClubId())) {
                        previous.setCurrentClubId(null);
                    }
                }
            } else if (holder(game, club, secretary) == null) {
                appoint(game, club, secretary, day, root);
                appointed++;
            }
        }
        Profession manager = DatabaseLoader.getInstance().getProfessionByDataId(Profession.MANAGER);
        for (Club club : ContractManager.uniqueClubs(game)) {
            // the secretary-manager the committee judges (G-05), appointed by the job market
            Person secretaryManager = holder(game, club, manager);
            if (secretaryManager != null && secretaryManager != owner && secretaryManager.getJudgement() == null) {
                secretaryManager.setJudgement(judgementFor(Profession.SECRETARY, networkOf(club),
                        root.fork("judge:" + secretaryManager.getId())));
            }
            committeeJudgements(club, root);
        }
        if (owner != null && owner.getJudgement() == null && owner.getId() != null) {
            owner.setJudgement(judgementFor(Profession.SECRETARY, networkOf(mine), root.fork("judge:" + owner.getId())));
        }
        if (appointed > 0) {
            Log.info(Log.ENGINE_JOBS, "StaffRoles", appointed + " trainers and secretaries appointed");
        }
        return appointed;
    }

    /** @return the club's trainer, or null */
    public static Person trainerOf(SaveGame game, Club club) {
        return holder(game, club, DatabaseLoader.getInstance().getProfessionByDataId(Profession.TRAINER));
    }

    /**
     * @return the club's secretary: the player at his own club; else the secretary-manager (the manager post, whom
     *         G-05's committee judges) when the job market has appointed one; else the club's secretary (or null)
     */
    public static Person secretaryOf(SaveGame game, Club club) {
        if (game != null && club != null && club == game.getCurrentClub() && game.getOwner() != null) {
            return game.getOwner();
        }
        Person secretaryManager = holder(game, club, DatabaseLoader.getInstance().getProfessionByDataId(Profession.MANAGER));
        if (secretaryManager != null) {
            return secretaryManager;
        }
        return holder(game, club, DatabaseLoader.getInstance().getProfessionByDataId(Profession.SECRETARY));
    }

    /** G-05's committee men get their judgement once their committee exists ({@code fork("judge:committee:...")}). */
    private static void committeeJudgements(Club club, RandomSource root) {
        if (club.getCommittee() == null) {
            return;
        }
        List<com.rndmodgames.futtoboru.data.CommitteeMember> members = club.getCommittee().getMembers();
        for (int i = 0; i < members.size(); i++) {
            com.rndmodgames.futtoboru.data.CommitteeMember m = members.get(i);
            if (m != null && m.getJudgement() == null) {
                String key = (club.getDataId() != null ? club.getDataId() : "id" + club.getId()) + ":" + i + ":"
                        + m.getFullName();
                m.setJudgement(judgementFor(Profession.COMMITTEE_MEMBER, networkOf(club),
                        root.fork("judge:committee:" + key)));
            }
        }
    }

    private static Person holder(SaveGame game, Club club, Profession post) {
        if (game == null || club == null || post == null || post.getId() == null) {
            return null;
        }
        Long id = club.getStaffId(post.getId());
        if (id == null) {
            return null;
        }
        for (Person p : game.getAllPersons()) {
            if (p != null && id.equals(p.getId())) {
                return p;
            }
        }
        return null;
    }

    /** A man generated for the post: a name of the club's country (English without names), languages, judgement. */
    private static void appoint(SaveGame game, Club club, Profession post, LocalDateTime today, RandomSource root) {
        String key = (club.getDataId() != null ? club.getDataId() : "id" + club.getId()) + ":" + today.toLocalDate();
        RandomSource rng = root.fork("staff:" + post.getDataId() + ":" + key);
        Country country = club.getCountry();
        if (country == null || DatabaseLoader.getInstance().getFirstNames(country).size() < 2
                || DatabaseLoader.getInstance().getLastNames(country).size() < 2) {
            country = DatabaseLoader.getInstance().getCountryByDataId("england");
        }
        Person person = new Person();
        String[] name = NameGenerator.generateName(country, rng);
        person.setName(name[0]);
        person.setLastname(name[1]);
        person.setCountry(club.getCountry() != null ? club.getCountry() : country);
        person.setCurrentCountry(person.getCountry());
        int minAge = post.is(Profession.TRAINER) ? TRAINER_MIN_AGE : SECRETARY_MIN_AGE;
        person.setBirthDate(today.minusYears(minAge + rng.nextInt(AGE_SPAN)).minusDays(rng.nextInt(365)));
        person.setPrimaryProfession(post);
        game.assignId(person);
        game.getAllPersons().add(person);
        person.setCurrentClubId(club.getId());
        club.setStaffId(post.getId(), person.getId());
        person.setJudgement(judgementFor(post.getDataId(), networkOf(club), root.fork("judge:" + person.getId())));
        RandomSource languages = DatabaseLoader.getRandom() != null ? DatabaseLoader.getRandom() : root;
        LanguageGenerator.assign(person, DatabaseLoader.getInstance().getLanguageData(), null, languages);
    }

    /** @return the pool regions a man at this club knows men in: the club's region, or none */
    static List<String> networkOf(Club club) {
        List<String> out = new ArrayList<>();
        RecruitmentData data = DatabaseLoader.getInstance().getRecruitmentData();
        if (club != null && data != null) {
            PoolRegion region = data.poolOfClub(club.getDataId());
            if (region != null) {
                out.add(region.dataId());
            }
        }
        return out;
    }

    /**
     * The judgement of a holder of a post (placeholder profiles, design §7): the trainer judges physique and pace
     * well and likes pace; a committee man judges character and likes a local lad; the secretary is middling at all,
     * with his own bias. Some secretaries know men in the west of Scotland.
     *
     * @param profession {@link Profession#TRAINER}, {@link Profession#SECRETARY}, {@link Profession#COMMITTEE_MEMBER}
     *                   or {@link #CAPTAIN} (R-04)
     * @param network    his own region(s)
     * @param rng        his own fork ({@code "judge:" + id})
     */
    public static StaffJudgement judgementFor(String profession, List<String> network, RandomSource rng) {
        int[] means = Profession.TRAINER.equals(profession) ? TRAINER_MEANS
                : Profession.COMMITTEE_MEMBER.equals(profession) ? COMMITTEE_MEANS
                : CAPTAIN.equals(profession) ? CAPTAIN_MEANS : SECRETARY_MEANS;
        int[] v = new int[4];
        for (int i = 0; i < 4; i++) {
            v[i] = clamp(means[i] - SPREAD + rng.nextInt(SPREAD + 1) + rng.nextInt(SPREAD + 1));
        }
        double u = rng.nextDouble();
        String bias;
        if (Profession.TRAINER.equals(profession)) {
            bias = u < 0.7 ? StaffJudgement.BIAS_PACE : StaffJudgement.BIAS_SIZE;
        } else if (CAPTAIN.equals(profession)) {
            bias = u < 0.6 ? StaffJudgement.BIAS_FLAIR : u < 0.8 ? StaffJudgement.BIAS_SIZE : StaffJudgement.BIAS_LOCAL;
        } else if (Profession.COMMITTEE_MEMBER.equals(profession)) {
            bias = u < 0.6 ? StaffJudgement.BIAS_LOCAL : u < 0.85 ? StaffJudgement.BIAS_SIZE : StaffJudgement.BIAS_SCOTS;
        } else {
            bias = StaffJudgement.BIASES.get(Math.min(StaffJudgement.BIASES.size() - 1,
                    (int) (u * StaffJudgement.BIASES.size())));
        }
        List<String> known = new ArrayList<>(network);
        boolean scots = Profession.SECRETARY.equals(profession)
                && (StaffJudgement.BIAS_SCOTS.equals(bias) || rng.nextDouble() < SCOTS_NETWORK);
        if (scots && !known.contains(SCOTLAND_WEST)) {
            known.add(SCOTLAND_WEST);
        }
        return new StaffJudgement(v[0], v[1], v[2], v[3], bias, known);
    }

    private static int clamp(int x) {
        return Math.max(StaffJudgement.MIN, Math.min(StaffJudgement.MAX, x));
    }
}
