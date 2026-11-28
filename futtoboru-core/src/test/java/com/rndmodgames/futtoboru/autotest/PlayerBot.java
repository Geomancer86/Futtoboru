package com.rndmodgames.futtoboru.autotest;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerAttributeSnapshot;
import com.rndmodgames.futtoboru.data.PlayerContract;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.data.jobs.JobApplication;
import com.rndmodgames.futtoboru.data.jobs.JobOffer;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;
import com.rndmodgames.futtoboru.data.jobs.OfferStatus;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.engine.contracts.ContractManager;
import com.rndmodgames.futtoboru.engine.contracts.ContractTerms;
import com.rndmodgames.futtoboru.engine.injuries.InjuryManager;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.team.TeamSheets;
import com.rndmodgames.futtoboru.engine.temporal.FriendlyArranger;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.NewGameClubs;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator;
import com.rndmodgames.futtoboru.system.save.LoadResult;

/**
 * A-01: plays a game as the human does, through the calls the screens make (never a shortcut into the engine's
 * internals), with a simple seeded strategy so a run is the same every time:
 * <ul>
 * <li>New Game: MenuScreen's {@code prepareNewGame}, then the steps of NewGameOverviewScreen's Start button;</li>
 * <li>each day: the inbox (every letter opened, as a click on its title does), the draws (the DRAW button and the
 * draw screen's Continue), the Match Preview with a checked team sheet (G-01 TeamSheetEditor: Auto-pick when a man
 * is out or a place is empty, a captain named, now and then a bench man moved in), Match Result, Continue;</li>
 * <li>a friendly a fortnight when the calendar is empty (U-08 ArrangeFriendlyTable);</li>
 * <li>the Retained List (G-04): keeps the younger and better men, releases veterans and the worst, sends the terms,
 * raises a refused offer twice;</li>
 * <li>injury letters (G-02): the sheet is re-picked; committee (G-05) and re-election (G-08) letters are read;</li>
 * <li>dismissed (J-01): applies for every open secretary's post on the Job Board, accepts the first offer;</li>
 * <li>saves and reloads (SaveGameDialog, LoadGameDialog) on the days it is told to, the manual slot or the
 * autosave; the live game's fingerprint must equal the loaded one.</li>
 * </ul>
 * The bot's own dice are forked from its seed and the day, never from the game, so a reload does not change them.
 */
public final class PlayerBot {

    /** Every choice the bot makes that a player could make differently. */
    public static final class Strategy {
        public String clubDataId = "preston-north-end";
        /** chance a month that he moves a bench man into the side */
        public double tinkerChance = 0.3;
        /** releases men this old or older */
        public int releaseAge = 33;
        /** keeps at least this many men at the rollover */
        public int keepAtLeast = 16;
        /** a friendly when there is no fixture this many days ahead */
        public int friendlyGapDays = 14;
    }

    /** What the bot did, for the run report. */
    public static final class Actions {
        public int lettersRead;
        public int drawsOpened;
        public int matchesPrepared;
        public int autoPicks;
        public int tinkers;
        public int friendliesProposed;
        public int retained;
        public int released;
        public int termsSent;
        public int offersRaised;
        public int applications;
        public int jobsAccepted;
        public int dismissals;
        public int manualReloads;
        public int autosaveReloads;
        public int injuryLetters;
        public int committeeLetters;
        public int reElectionLetters;
        public int recruitmentLetters;
        public int unemployedDays;
    }

    /** One reload: the fingerprints before and after (equal when the save is the game). */
    public record Reload(LocalDate day, String kind, String before, String after) {
    }

    public final Futtoboru app;
    public final long seed;
    public final Strategy strategy;
    public final Actions actions = new Actions();
    public final List<Reload> reloads = new ArrayList<>();
    private final RandomSource dice;
    private final Set<Long> readMessages = new HashSet<>();
    private final Set<Long> appliedTo = new HashSet<>();
    private LocalDate lastFriendlyTry;
    private LocalDate lastTinker;

    private PlayerBot(Futtoboru app, long seed, Strategy strategy) {
        this.app = app;
        this.seed = seed;
        this.strategy = strategy;
        this.dice = RandomSource.seeded(seed).fork("player-bot");
    }

    // ------------------------------------------------------------------------------------------------ new game

    /**
     * MenuScreen's New Game ({@code prepareNewGame}), NewManagerScreen's manager, NewGameSeasonScreen's game and
     * NewGameOverviewScreen's Start button, for England and {@code strategy.clubDataId}.
     */
    public static PlayerBot newGame(long seed, Strategy strategy) {
        Futtoboru app = new Futtoboru(seed);
        app.prepareNewGame();

        Season season = DatabaseLoader.getInstance().getSeasonByDataId("1888-89");
        Country england = DatabaseLoader.getInstance().getCountryByDataId("england");
        Profession secretary = DatabaseLoader.getInstance().getProfessionByDataId(Profession.MANAGER);
        Person owner = new Person();
        owner.setName("Bot");
        owner.setLastname("Secretary");
        owner.setBirthDate(LocalDateTime.of(1855, 3, 1, 0, 0));
        owner.setCountry(england);
        SaveGame game = new SaveGame();
        app.setCurrentGame(game);
        game.setOwner(owner);

        // NewGameOverviewScreen, Start
        game.setMainAuthority(DatabaseLoader.getInstance().getMainAuthority());
        List<Country> selected = new ArrayList<>();
        selected.add(england);
        game.setSelectedCountries(selected);
        owner.setId(null);
        game.assignId(owner);
        game.getAllPersons().add(owner);
        owner.setPrimaryProfession(secretary);
        owner.setCurrentCountry(england);
        game.setGameStartDate(season.getStartDate());
        game.setGameDate(season.getStartDate());
        game.setAllClubs(new ArrayList<>());
        NewGameClubs.addTo(game, DatabaseLoader.getInstance().getCompetitions(), selected);
        Club start = null;
        for (Club c : game.getAllClubs()) {
            if (strategy.clubDataId.equals(c.getDataId())) {
                start = c;
            }
        }
        if (start == null) {
            throw new IllegalArgumentException("no club " + strategy.clubDataId + " in the new game");
        }
        owner.setCurrentClubId(start.getId());
        game.getGameScripts().addAll(season.getSeasonScripts());
        app.initializeJobSystem();
        app.getJobManager().initializeJobOpenings();
        PlayerAttributeGenerator attributes = new PlayerAttributeGenerator();
        for (Club club : game.getAllClubs()) {
            for (Player p : club.getPlayers()) {
                if (p != null && p.getPerson() != null && p.getAcceleration() == null) {
                    attributes.generatePlayerAttributes(p, p.getPerson(), game.getGameDate(),
                            app.random().fork("attributes:" + p.getPerson().getId()));
                }
            }
        }
        for (Club club : game.getAllClubs()) {
            for (Player p : club.getPlayers()) {
                if (p != null && p.getPerson() != null && p.getPerson().getId() != null && p.getAcceleration() != null) {
                    PlayerAttributeSnapshot s = PlayerAttributeSnapshot.fromPlayer(p, game.getGameDate(), "INITIAL");
                    if (s != null) {
                        game.getPlayerAttributeSnapshots().add(s);
                    }
                }
            }
        }
        app.getGameEngine().getMessageManager().deliverMessage(app.getGameEngine().getMessageManager()
                .createWelcomeMessage(owner, secretary, england, start, game.getGameStartDate()));
        return new PlayerBot(app, seed, strategy);
    }

    public SaveGame game() {
        return app.getCurrentGame();
    }

    FuttoboruGameEngine engine() {
        return app.getGameEngine();
    }

    LocalDate today() {
        return game().getGameDate().toLocalDate();
    }

    private RandomSource dice(String what) {
        return dice.fork(what + ":" + today());
    }

    // ------------------------------------------------------------------------------------------------ one day

    /**
     * Presses the main button until the date moves on (Continue; or Draw, Match Preview, Match Result first).
     *
     * @return the clubs' fit men before today's matches, by match id and side ("id:H"/"id:A"), for the side check
     */
    public java.util.Map<String, Integer> playDay() {
        LocalDateTime before = game().getGameDate();
        java.util.Map<String, Integer> fitBefore = new java.util.HashMap<>();
        for (int press = 0; press < 8; press++) {
            readInbox();
            manageClub();
            int action = engine().getNextGameAction();
            if (action == FuttoboruGameEngine.DRAW_ACTION) {
                openDraws();
                continue;
            }
            noteFitMen(fitBefore);
            if (action == FuttoboruGameEngine.MATCH_PREVIEW_ACTION) {
                prepareMatch();
                engine().getMatchResult();
                readInbox();
            }
            engine().continueGame();
            if (!game().getGameDate().equals(before)) {
                return fitBefore;
            }
        }
        throw new AssertionError("seed " + seed + ", " + before.toLocalDate()
                + ": the day does not move on after 8 presses (next action " + engine().getNextGameAction() + ")");
    }

    /** Fit men of both sides of every match today, before it is played. */
    private void noteFitMen(java.util.Map<String, Integer> out) {
        SaveGame g = game();
        for (Club c : g.getAllClubs()) {
            for (Match m : c.getScheduledMatches()) {
                if (m.getMatchDateTime() != null && m.getMatchDateTime().toLocalDate().equals(today())
                        && !Boolean.TRUE.equals(m.getIsPlayed())) {
                    // a later cup round is listed by every entrant: only its two clubs count
                    boolean home = c.getId().equals(m.getHomeClubId());
                    if (!home && !c.getId().equals(m.getAwayClubId())) {
                        continue;
                    }
                    String side = home ? "H" : "A";
                    out.putIfAbsent(m.getId() + ":" + side, TeamSheets.available(c).size());
                }
            }
        }
    }

    /** InboxScreenTable: a click on a letter's title marks it read; the bot then does what the letter asks. */
    void readInbox() {
        SaveGame g = game();
        for (Message m : new ArrayList<>(g.getAllMessages())) {
            if (m == null || m.getId() == null || Boolean.TRUE.equals(m.getIsDeleted()) || !readMessages.add(m.getId())) {
                continue;
            }
            if (isDraw(m) && Boolean.TRUE.equals(m.getIsMandatory())) {
                readMessages.remove(m.getId()); // the draw screen clears it
                continue;
            }
            m.setIsRead(true);
            actions.lettersRead++;
            String type = m.getMessageType() != null ? m.getMessageType() : "";
            if (type.equals(InjuryManager.MSG_INJURY) || type.equals(InjuryManager.MSG_FIT_AGAIN)) {
                actions.injuryLetters++;
                if (g.getCurrentClub() != null) {
                    pick(g.getCurrentClub());
                }
            } else if (type.startsWith("COMMITTEE_")) {
                actions.committeeLetters++;
                if (type.equals("COMMITTEE_DISMISSAL")) {
                    actions.dismissals++;
                }
            } else if (type.startsWith("REELECTION_")) {
                actions.reElectionLetters++;
            } else if (type.startsWith("RECRUIT_")) {
                actions.recruitmentLetters++;
                answerRecruitment(g, m);
            }
        }
    }

    /**
     * R-05, the inbox's letter buttons (RecruitmentLetterActions): the first man offered goes on the shortlist, the
     * others are ignored, an agent is never paid; the Shortlist screen's Remove keeps the list at {@link #SHORTLIST_MAX}.
     */
    void answerRecruitment(SaveGame g, Message m) {
        Club club = g.getCurrentClub();
        if (club == null) {
            return;
        }
        boolean first = true;
        for (com.rndmodgames.futtoboru.data.recruitment.ShortlistEntry e
                : com.rndmodgames.futtoboru.engine.recruitment.Shortlists.ofLetter(g, m.getId())) {
            if (first && com.rndmodgames.futtoboru.engine.recruitment.Shortlists.accept(e, g.getGameDate())) {
                first = false;
            } else {
                com.rndmodgames.futtoboru.engine.recruitment.Shortlists.ignore(e);
            }
        }
        List<com.rndmodgames.futtoboru.data.recruitment.ShortlistEntry> list =
                com.rndmodgames.futtoboru.engine.recruitment.Shortlists.listed(g, club);
        for (int i = 0; i < list.size() - SHORTLIST_MAX; i++) {
            com.rndmodgames.futtoboru.engine.recruitment.Shortlists.remove(g, club, list.get(i).getPlayer());
        }
    }

    static final int SHORTLIST_MAX = 15;

    static boolean isDraw(Message m) {
        String t = m.getMessageType();
        return "LEAGUE_DRAW".equals(t) || "CUP_DRAW".equals(t) || "FIXTURE_DRAW".equals(t);
    }

    /**
     * MainGameMenuTable's DRAW button (a mandatory draw only scheduled is delivered at once) and the draw screen's
     * Continue (read, no longer mandatory).
     */
    void openDraws() {
        SaveGame g = game();
        for (Message m : new ArrayList<>(g.getScheduledMessages())) {
            if (m != null && Boolean.TRUE.equals(m.getIsMandatory()) && isDraw(m)) {
                engine().getMessageManager().deliverMessage(m);
            }
        }
        for (Message m : g.getAllMessages()) {
            if (m != null && Boolean.TRUE.equals(m.getIsMandatory()) && isDraw(m)) {
                m.setIsRead(true);
                m.setIsMandatory(false);
                actions.drawsOpened++;
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ the club

    void manageClub() {
        SaveGame g = game();
        Club club = g.getCurrentClub();
        if (club == null) {
            seekWork();
            return;
        }
        tinker(club);
        retainedList(club);
        friendly(club);
    }

    /** The Match Preview: the standing sheet checked as the editor shows it. */
    void prepareMatch() {
        Club club = game().getCurrentClub();
        if (club == null) {
            return;
        }
        actions.matchesPrepared++;
        pick(club);
    }

    /** TeamSheetEditor: Auto-pick when a man on the sheet is out or a place is empty; a captain named. */
    void pick(Club club) {
        TeamSheet sheet = TeamSheets.standing(club);
        boolean unfit = false;
        for (Long id : sheet.selectedIds()) {
            Player p = TeamSheets.squadById(club).get(id);
            if (p == null || !TeamSheets.isAvailable(p)) {
                unfit = true;
            }
        }
        TeamSheets.Check check = TeamSheets.check(sheet, club);
        if (unfit || check.hasEmptyPlaces()) {
            TeamSheets.autoPick(club);
            sheet = TeamSheets.standing(club);
            actions.autoPicks++;
            check = TeamSheets.check(sheet, club);
        }
        if (check.noCaptain() && !sheet.selectedIds().isEmpty()) {
            Long best = null;
            float lead = -1;
            for (Long id : sheet.selectedIds()) {
                Player p = TeamSheets.squadById(club).get(id);
                float l = p != null && p.getLeadership() != null ? p.getLeadership() : 0;
                if (l > lead) {
                    lead = l;
                    best = id;
                }
            }
            sheet.setCaptainId(best);
        }
    }

    /** Now and then the editor's man box: a fit bench man into a seeded place (he swaps if already on). */
    void tinker(Club club) {
        LocalDate today = today();
        if (lastTinker != null && today.isBefore(lastTinker.plusDays(30))) {
            return;
        }
        lastTinker = today;
        RandomSource r = dice("tinker");
        if (r.nextDouble() >= strategy.tinkerChance) {
            return;
        }
        TeamSheet sheet = TeamSheets.standing(club);
        List<Player> bench = new ArrayList<>();
        for (Player p : TeamSheets.available(club)) {
            if (!sheet.isSelected(p.getId())) {
                bench.add(p);
            }
        }
        if (bench.isEmpty()) {
            return;
        }
        sheet.setPlayerId(r.nextInt(com.rndmodgames.futtoboru.data.TeamShape.SLOTS),
                bench.get(r.nextInt(bench.size())).getId());
        actions.tinkers++;
    }

    /** RetainedListScreenTable: retain or release each man whose engagement ends, send the terms, raise a "no". */
    void retainedList(Club club) {
        ContractManager cm = engine().getContractManager();
        if (cm == null || !cm.isRetainedListOpen()) {
            return;
        }
        List<PlayerContract> ending = cm.endingContracts(club);
        if (ending.isEmpty()) {
            return;
        }
        // decide once: the worst and the veterans go, as long as enough men stay
        List<PlayerContract> undecided = new ArrayList<>();
        for (PlayerContract c : ending) {
            if (c.getRetain() == null) {
                undecided.add(c);
            }
        }
        if (!undecided.isEmpty()) {
            undecided.sort((a, b) -> Double.compare(ability(club, a), ability(club, b)));
            int keep = club.getPlayers().size();
            for (PlayerContract c : undecided) {
                Player p = ContractManager.playerOf(club, c);
                int age = p != null ? ContractTerms.age(p, today()) : 99;
                boolean worst = undecided.indexOf(c) < 2;
                if (keep > strategy.keepAtLeast && (age >= strategy.releaseAge || worst)) {
                    cm.release(c);
                    actions.released++;
                    keep--;
                } else {
                    cm.retain(c);
                    actions.retained++;
                }
            }
        }
        // a refused offer is raised (2 shillings) twice at most, then sent again
        for (PlayerContract c : ending) {
            if (c.getRetain() == PlayerContract.Retain.RETAINED && c.getAnswer() == PlayerContract.Answer.DECLINED
                    && c.getOffers() < 3) {
                cm.changeOfferedWage(club, c, 2);
                actions.offersRaised++;
            }
        }
        actions.termsSent += cm.sendTerms(club);
    }

    private static double ability(Club club, PlayerContract c) {
        Player p = ContractManager.playerOf(club, c);
        return p != null ? ContractTerms.ability(p) : 0;
    }

    /** ArrangeFriendlyTable: a free Saturday ahead and a seeded opponent the arranger allows. */
    void friendly(Club club) {
        LocalDate today = today();
        if (lastFriendlyTry != null && today.isBefore(lastFriendlyTry.plusDays(7))) {
            return;
        }
        lastFriendlyTry = today;
        LocalDate horizon = today.plusDays(strategy.friendlyGapDays);
        List<Match> all = new ArrayList<>(club.getScheduledMatches());
        all.addAll(club.getProposedMatches());
        for (Match m : all) {
            if (m.getMatchDateTime() != null && !m.getMatchDateTime().toLocalDate().isBefore(today)
                    && !m.getMatchDateTime().toLocalDate().isAfter(horizon)) {
                return; // a fixture soon: no friendly needed
            }
        }
        FriendlyArranger arranger = new FriendlyArranger(app);
        List<Club> opponents = arranger.possibleOpponents(club);
        if (opponents.isEmpty()) {
            return;
        }
        RandomSource r = dice("friendly");
        LocalDate day = today.plusDays(FriendlyArranger.MIN_NOTICE_DAYS);
        while (day.getDayOfWeek() != DayOfWeek.SATURDAY) {
            day = day.plusDays(1);
        }
        for (int tries = 0; tries < 4; tries++) {
            Club opponent = opponents.get(r.nextInt(opponents.size()));
            if (arranger.check(club, opponent, day) == FriendlyArranger.Reason.NONE) {
                if (arranger.propose(club, opponent, day, r.nextBoolean()) != null) {
                    actions.friendliesProposed++;
                }
                return;
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ job seeker

    /** JobBoardScreenTable's Apply for every open secretary's post; JobOfferScreenTable's Accept on the first offer. */
    void seekWork() {
        SaveGame g = game();
        Person owner = g.getOwner();
        actions.unemployedDays++;
        var jobs = app.getJobManager();
        if (jobs == null) {
            return;
        }
        for (JobOffer offer : new ArrayList<>(jobs.getPlayerOffers(owner))) {
            if (offer.getStatus() == OfferStatus.PENDING && jobs.acceptOffer(offer)) {
                actions.jobsAccepted++;
                return;
            }
        }
        for (JobOpening job : new ArrayList<>(jobs.getAvailableJobs())) {
            if (job.getId() == null || appliedTo.contains(job.getId())
                    || !owner.getPrimaryProfession().getId().equals(job.getProfessionId())) {
                continue;
            }
            appliedTo.add(job.getId());
            JobApplication a = jobs.getApplicationFor(owner, job);
            if (a == null && jobs.applyForJob(owner, job) != null) {
                actions.applications++;
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ saves

    /**
     * SaveGameDialog's Save into the slot "bot", then LoadGameDialog's Load of it. The game goes on from the
     * loaded copy, as it does for the player.
     */
    public Reload saveAndReload(String kind) {
        String before = com.rndmodgames.futtoboru.system.save.GameFingerprint.of(game()).toString();
        File file;
        if ("autosave".equals(kind)) {
            app.awaitAutosave();
            file = SaveLoadSystem.autosaveFile();
        } else {
            try {
                file = SaveLoadSystem.saveGame(game(), "bot");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        app.awaitAutosave();
        LoadResult result = SaveLoadSystem.loadGame(file);
        if (!result.isLoaded()) {
            throw new AssertionError("seed " + seed + ", " + today() + ": the " + kind + " does not load: " + result);
        }
        app.startLoadedGame(result.getGame());
        String after = com.rndmodgames.futtoboru.system.save.GameFingerprint.of(game()).toString();
        if ("autosave".equals(kind)) {
            actions.autosaveReloads++;
        } else {
            actions.manualReloads++;
        }
        Reload r = new Reload(today(), kind, before, after);
        reloads.add(r);
        return r;
    }
}
