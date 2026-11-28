package com.rndmodgames.futtoboru.engine.recruitment;

import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CommitteeMember;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.StaffJudgement;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * R-04 (docs/design/RECRUITMENT_AND_TRIALS.md §4.1, §7): the men of a club who judge footballers: the secretary (the
 * player at his own club), the trainer, the committee men and the captain of the standing team sheet. Each comes with
 * his {@link StaffJudgement} (R-09) and a stable key for his exposure records ({@code Knowledge}).
 */
public final class Observers {

    public static final String SECRETARY = "secretary";
    public static final String TRAINER = "trainer";
    public static final String COMMITTEE = "committee";
    public static final String CAPTAIN = "captain";

    /** One observer: key, role, shown name, judgement. */
    public static final class Observer {
        private final String key;
        private final String role;
        private final String name;
        private final StaffJudgement judgement;

        public Observer(String key, String role, String name, StaffJudgement judgement) {
            this.key = key;
            this.role = role;
            this.name = name;
            this.judgement = judgement;
        }

        public String key() {
            return key;
        }

        /** one of {@link Observers#SECRETARY}, {@link Observers#TRAINER}, {@link Observers#COMMITTEE}, {@link Observers#CAPTAIN} */
        public String role() {
            return role;
        }

        public String name() {
            return name;
        }

        public StaffJudgement judgement() {
            return judgement;
        }

        @Override
        public String toString() {
            return role + " " + name + " " + judgement;
        }
    }

    private Observers() {
    }

    /** @return the exposure key of a person (secretary, trainer, captain) */
    public static String personKey(Person person) {
        return "p" + person.getId();
    }

    /** @return the exposure key of a committee man (they are not persons: club and full name) */
    public static String committeeKey(Club club, CommitteeMember member) {
        String clubKey = club.getDataId() != null ? club.getDataId() : "id" + club.getId();
        return "c:" + clubKey + ":" + member.getFullName();
    }

    /**
     * @param root the game's root source (the captain's judgement is drawn from {@code fork("judge:captain:" + id)},
     *             not stored: any man may be made captain)
     * @return the club's observers who have a judgement: secretary, trainer, committee men, captain
     */
    public static List<Observer> of(SaveGame game, Club club, RandomSource root) {
        List<Observer> out = new ArrayList<>();
        if (game == null || club == null) {
            return out;
        }
        Person secretary = StaffRoles.secretaryOf(game, club);
        if (secretary != null && secretary.getId() != null && secretary.getJudgement() != null) {
            out.add(new Observer(personKey(secretary), SECRETARY, fullName(secretary), secretary.getJudgement()));
        }
        Person trainer = StaffRoles.trainerOf(game, club);
        if (trainer != null && trainer.getId() != null && trainer.getJudgement() != null) {
            out.add(new Observer(personKey(trainer), TRAINER, fullName(trainer), trainer.getJudgement()));
        }
        for (Observer o : committee(club)) {
            out.add(o);
        }
        Observer captain = captain(club, club.getTeamSheet(), root);
        if (captain != null) {
            out.add(captain);
        }
        return out;
    }

    /** @return the committee men with a judgement */
    public static List<Observer> committee(Club club) {
        List<Observer> out = new ArrayList<>();
        if (club == null || club.getCommittee() == null) {
            return out;
        }
        for (CommitteeMember m : club.getCommittee().getMembers()) {
            if (m != null && m.getJudgement() != null) {
                out.add(new Observer(committeeKey(club, m), COMMITTEE, m.getFullName(), m.getJudgement()));
            }
        }
        return out;
    }

    /** @return the captain of this sheet as an observer (his judgement from his own fork), or null */
    public static Observer captain(Club club, TeamSheet sheet, RandomSource root) {
        if (club == null || sheet == null || sheet.getCaptainId() == null || root == null) {
            return null;
        }
        Player captain = null;
        for (Player p : club.getPlayers()) {
            if (p != null && sheet.getCaptainId().equals(p.getId())) {
                captain = p;
            }
        }
        if (captain == null || captain.getPerson() == null || captain.getPerson().getId() == null) {
            return null;
        }
        Person person = captain.getPerson();
        StaffJudgement j = person.getJudgement() != null ? person.getJudgement()
                : StaffRoles.judgementFor(StaffRoles.CAPTAIN, StaffRoles.networkOf(club),
                        root.fork("judge:captain:" + person.getId()));
        return new Observer(personKey(person), CAPTAIN, fullName(person), j);
    }

    private static String fullName(Person p) {
        return ((p.getName() == null ? "" : p.getName()) + " " + (p.getLastname() == null ? "" : p.getLastname()))
                .trim();
    }
}
