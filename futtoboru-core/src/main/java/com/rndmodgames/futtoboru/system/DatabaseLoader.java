package com.rndmodgames.futtoboru.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Continent;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.DataBase;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.system.loaders.AuthoritiesLoader;
import com.rndmodgames.futtoboru.system.loaders.NamesLoader;
import com.rndmodgames.futtoboru.system.loaders.ScriptsLoader;
import com.rndmodgames.futtoboru.system.loaders.SeasonsLoader;

/**
 * Database Loader v1
 * 
 *  - Use this class to load all the Game Objects from /mods and /mods/* folders
 *  - Keep the data on memory to be accessed later
 * 
 * @author Geomancer86
 */
public class DatabaseLoader {

    //
    private static final String SEASONS_FILE = "mods/seasons.txt";
    private static final String DATABASES_FILE = "mods/database.txt";
    private static final String CONTINENTS_FILE = "mods/continents.txt";
    private static final String COUNTRIES_FILE = "mods/countries.txt";
    private static final String LEAGUES_FILE = "mods/leagues.txt";
    
    //
    private static final String LEAGUES_FOLDER = "mods/leagues/";
    
    //
    private static final String PROFESSIONS_FILE = "mods/professions.txt";
    
    //
    private static final String FILE_EXTENSION = ".txt";
    
    //
    private static List<Season> seasons = new ArrayList<>();
    private static List<DataBase> databases = new ArrayList<>();
    private static List<Authority> authorities = new ArrayList<>();
    private static List<Continent> continents = new ArrayList<>();
    private static List<Country> countries = new ArrayList<>();
    private static List<League> leagues = new ArrayList<>();
    private static List<Club> clubs = new ArrayList<>();
    private static List<Person> persons = new ArrayList<>();
    
    //
    private static HashMap<Long, Continent> continentsById = new HashMap<>();
    private static HashMap<Long, Country> countriesById = new HashMap<>();
    private static HashMap<Long, League> leaguesById = new HashMap<>();
    private static HashMap<Long, Club> clubsById = new HashMap<>();
    
    private static HashMap<Long, List<Country>> countriesByContinent = new HashMap<>();
    private static HashMap<Long, List<League>> leaguesByCountry = new HashMap<>();
    private static HashMap<Long, List<Club>> clubsByLeague = new HashMap<>();
    private static HashMap<Long, List<Club>> clubsByCountry = new HashMap<>();

    //
    private static List<Profession> professions = new ArrayList<>();
    private static List<Profession> selectableProfessions = new ArrayList<>();
    
    // keep track of main/top authority
    private static Authority mainAuthority;
    
    //
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a", Locale.ENGLISH);
    
    // singleton
    private static DatabaseLoader instance;

    /**
     * TODO: use the same random generator all over
     * TODO: set a game seed to allow to replay the same game over and over
     */
    public static Random RNG = new Random();
    
    public DatabaseLoader() {

    }

    /**
     * Initialize the Game Data in Required Order
     * 
     * TODO: revamp for script use
     */
    public static DatabaseLoader getInstance() {
        
        if (instance == null) {
            
            instance = new DatabaseLoader();
            
            // Initialize Databases
            initializeDatabases();
            
            // Initialize Continents
            initializeContinents();
            
            // Initialize Countries
            initializeCountries();
            
            // Initialize Name Generators (depends on Countries)
            NamesLoader.loadAllCountryNames(countries);
            
            // Initialize Authorities
            AuthoritiesLoader.loadAuthorities();

            // Initialize Available Professions
            initializeProfessions();
            
            // Initialize Seasons
            initializeSeasons();
            
            // Load Seasons Data
            SeasonsLoader.load(seasons);
            
            /**
             * TODO WIP:
             * 
             *  Load or Generate Club Players and Free Agents
             */
            
            // Load Seasons Scripts
            ScriptsLoader.load(seasons);
        } 
            
        //
        return instance;
    }
    
    /**
     * @return the complete list of Seasons
     */
    public List<Season> getSeasons() {
     
        //
        return seasons;
    }
    
    /**
     * @return the complete list of DataBases
     */
    public List<DataBase> getDataBases(){
        
        //
        return databases;
    }
    
    public List<Authority> getAuthorities() {
        return authorities;
    }
    
    /**
     * @return the complete list of Continents
     */
    public List<Continent> getContinents(){
        
        //
        return continents;
    }
    
    /**
     * @return the complete list of Professions
     */
    public List<Profession> getProfessions() {
        return professions;
    }
    
    public static List<Person> getPersons() {
        return persons;
    }

    public static void setPersons(List<Person> persons) {
        DatabaseLoader.persons = persons;
    }

    /**
     * @param playerSelectable
     * @return the List of selectable or not Player Professions
     */
    public List<Profession> getSelectableProfessions() {
        
        //
        if (selectableProfessions.isEmpty()) {
            
            for (Profession profession : professions) {
                
                if (profession.getPlayerSelectable()) {
                 
                    selectableProfessions.add(profession);
                }
            }
            
        }
        
        return selectableProfessions;
    }

    /**
     * Returns a Country by ID or null if doesn't exist
     * @param id
     * @return
     */
    public static Country getCountryById(Long id) {
 
        for (Country country : countries) {

            if (country.getId().equals(id)) {

                return country;
            }
        }
        
        return null;
    }
    
    /**
     * @param A Continent
     * @return the list of available Countries by Continent
     */
    public List<Country> getCountriesByContinent(Continent continent){
     
        // 
        return countriesByContinent.get(continent.getId());
    }
    
    /**
     * @param A Country
     * @return the list of available Leagues by Continent
     */
    public List<League> getLeaguesByCountry(Country country){
        
        //
        return leaguesByCountry.get(country.getId());
    }
    
    /**
     * @param a League
     * @return the list of available Clubs by League
     */
    public List<Club> getClubsByLeague(League league) {
        
        return clubsByLeague.get(league.getId());
    }
    
    public List<Club> getClubsByCountry(Country country){
        
        return clubsByCountry.get(country.getId());
    }
    
    /**
     * @param id
     * @return a Club by ID or null
     */
    public static Club getClubById(Long id) {

//        System.out.println("GET CLUB BY ID: " + id);
//        System.out.println("Total Clubs: " + clubs.size());
        
        for (Club club : clubs) {
            
            if (club.getId().equals(id)) {
                
                return club;
            }
        }
        
        return null;
    }
    
    /**
     * Add a Club
     */
    public static void addClub(Club club) {
        
//        System.out.println("ADD CLUB ID: " + club.getId() + ", NAME: " + club.getName());
        
        //
        if (!clubs.contains(club)) {
            clubs.add(club);
        }
        
        System.out.println("Total Clubs: " + clubs.size());
    }
    
    /**
     * Load the Available Seasons from /mods/seasons.txt
     */
    private static void initializeSeasons() {
        
        FileHandle seasonsTxt = Gdx.files.internal(SEASONS_FILE);
        
        if (seasonsTxt.exists()){
            
            BufferedReader reader = new BufferedReader(seasonsTxt.reader());
            
            String line;
            
            try {
                line = reader.readLine();

                // Use # symbol as starting for comment (to enable or disable available resolutions)
                while (line != null) {
                    
                    if (!line.startsWith("#")) {
  
                        /**
                         * This regex ignore commas inside double quotes (for descriptions, etc)
                         */
                        String [] splitted = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                        
                        /**
                         * Season Format
                         * 
                         * # COLUMNS
                         * # id, name, description, urlSource, startDate
                         */
                        Season season = new Season();
                        
                        season.setId(Long.valueOf(splitted[0]));
                        season.setName(splitted[1]);
                        season.setDescription(splitted[2].replace("\"", "")); // remove opening and closing quotes
                        season.setUrlSource(splitted[3]);
                        
                        // Parse Start Date
                        try {
                            
                            season.setStartDate(LocalDateTime.parse(splitted[4], formatter));
                            
                        } catch (Exception e) {
                            
                            System.out.println("Error Parsing Season Starting Date & Time!");
                        }
                        
                        season.setCountries(new ArrayList<>());
                        season.setLeagues(new ArrayList<>());
                        season.setClubs(new ArrayList<>());
                        
                        seasons.add(season);
                    }
                    
                    line = reader.readLine();
                }
                
            } catch (IOException e) {
                // TODO: If error, restore default resolutions.txt file
                e.printStackTrace();
            }
            
        } else {
            // TODO: If file doesn't exist, restore from default
        }
        
        System.out.println("FINISHED LOADING " + seasons.size() + " SEASONS");
    }
    
    /**
     * Load the Available Databases from /mods/databases.txt
     */
    private static void initializeDatabases() {

        FileHandle databasesTxt = Gdx.files.internal(DATABASES_FILE);

        if (databasesTxt.exists()){
                
            BufferedReader reader = new BufferedReader(databasesTxt.reader());
            
            String line;
            
            try {
                line = reader.readLine();

                // Use # symbol as starting for comment (to enable or disable available resolutions)
                while (line != null) {
                    
                    if (!line.startsWith("#")) {
  
                        String [] splitted = line.split(",");
                        
                        /**
                         * DataBase Format
                         * 
                         *  # COLUMNS
                         *  # id, name
                         */                
                        DataBase database = new DataBase();
                        database.setId(Long.valueOf(splitted[0]));
                        database.setName(splitted[1]);
                        database.setNumber(Long.valueOf(splitted[2]));
                        database.setVersion(splitted[3]);
                        
                        databases.add(database);
                    }
                    
                    line = reader.readLine();
                }
                
            } catch (IOException e) {
                // TODO: If error, restore default resolutions.txt file
                e.printStackTrace();
            }
            
        } else {
            // TODO: If file doesn't exist, restore from default
        }
        
        System.out.println("FINISHED LOADING " + databases.size() + " DATABASES");
    }
    
    /**
     * Load the Available Continents from /mods/continents.txt 
     */
    private static void initializeContinents() {

        FileHandle continentsTxt = Gdx.files.internal(CONTINENTS_FILE);

        if (continentsTxt.exists()){
            
            BufferedReader reader = new BufferedReader(continentsTxt.reader());
            
            String line;
            
            try {
                line = reader.readLine();

                // Use # symbol as starting for comment (to enable or disable available resolutions)
                while (line != null) {
                    
                    if (!line.startsWith("#")) {
  
                        String [] splitted = line.split(",");
                        
                        /**
                         * Continent Format
                         * 
                         *  # COLUMNS
                         *  # id, name
                         */
                        Continent continent = new Continent();
                        
                        continent.setId(Long.valueOf(splitted[0]));
                        continent.setName(splitted[1]);
                        
                        continents.add(continent);
                    }
                    
                    line = reader.readLine();
                }
                
            } catch (IOException e) {
                // TODO: If error, restore default resolutions.txt file
                e.printStackTrace();
            }
            
        } else {
            // TODO: If file doesn't exist, restore from default
        }
        
        System.out.println("FINISHED LOADING " + continents.size() + " CONTINENTS");
        
        /**
         * Fill the Continents Hash Map so we can get them by ID
         * 
         * Fill the Countries By Continent HashMap with empty lists so they're filled later when iterating Countries
         */
        for (Continent continent : continents) {
            
            continentsById.put(continent.getId(), continent);
            countriesByContinent.put(continent.getId(), new ArrayList<>());
        }
    }
    
    /**
     * Load the Available Continents from /mods/countries.txt 
     */
    private static void initializeCountries() {
        
        FileHandle countriesTxt = Gdx.files.internal(COUNTRIES_FILE);

        if (countriesTxt.exists()){
            
            BufferedReader reader = new BufferedReader(countriesTxt.reader());
            
            String line;
            
            try {
                
                line = reader.readLine();

                // Use # symbol as starting for comment (to enable or disable available resolutions)
                while (line != null) {
                    
                    if (!line.startsWith("#")) {
                        
//                        System.out.println(line);
  
                        String [] splitted = line.split(",");
                        
                        /**
                         * Continent Format
                         * 
                         *  # COLUMNS
                         *  # id, common name, formal name, continent_id, default_country
                         */
                        Country country = new Country();
                        
                        country.setId(Long.valueOf(splitted[0]));
                        country.setCommonName(splitted[1]);
                        country.setFormalName(splitted[2]);

                        country.setContinent(continentsById.get(Long.valueOf(splitted[3])));

                        //
                        if (splitted.length >= 5 && splitted[4] != null) {
                            
                            country.setDefaultCountry(Boolean.valueOf(splitted[4]));
                        } else {
                            
                            country.setDefaultCountry(false);
                        }
                        
                        //
                        countries.add(country);
                    }
                    
                    line = reader.readLine();
                }
                
            } catch (IOException e) {
                // TODO: If error, restore default resolutions.txt file
                e.printStackTrace();
            }
            
        } else {
            // TODO: If file doesn't exist, restore from default
        }
        
        System.out.println("FINISHED LOADING " + countries.size() + " COUNTRIES");
        
        /**
         * Fill the Countries By Continent HashMap so we can get them by Continent ID
         */
        for (Country country : countries) {
            
            // Add the Country by ID
            countriesById.put(country.getId(), country);
            
            // Add the Continent Key
            countriesByContinent.get(country.getContinent().getId()).add(country);
            leaguesByCountry.put(country.getId(), new ArrayList<>());
            clubsByCountry.put(country.getId(), new ArrayList<>());
        }
    }
    
    /**
     * Load the Available Leagues from /mods/leagues.txt
     */
    private static void initializeLeagues() {
        
        FileHandle leaguesTxt = Gdx.files.internal(LEAGUES_FILE);
        
        if (leaguesTxt.exists()){
            
            BufferedReader reader = new BufferedReader(leaguesTxt.reader());
            
            String line;
            
            try {
                
                line = reader.readLine();

                // Use # symbol as starting for comment (to enable or disable available resolutions)
                while (line != null) {
                    
                    if (!line.startsWith("#")) {
  
                        String [] splitted = line.split(",");
                        
                        /**
                         * League Format
                         * 
                         *  # COLUMNS
                         *  
                         */
                        
                        League league = new League();
                        league.setId(Long.valueOf(splitted[0]));
                        league.setName(splitted[1]);
                        league.setCountry(countriesById.get(Long.valueOf(splitted[2])));
                        league.setLevel(Integer.valueOf(splitted[3]));

                        leagues.add(league);
                    }
                    
                    line = reader.readLine();
                }
                
            } catch (IOException e) {
                // TODO: If error, restore default leagues.txt file
                e.printStackTrace();
            }
            
        } else {
            // TODO: If file doesn't exist, restore from default
        }
        
        System.out.println("FINISHED LOADING " + leagues.size() + " LEAGUES");
        
        /**
         * - Iterate and fill the Leagues by Country Hash Map
         * - Iterate and Initialize the Clubs By League Hash Map
         */
        
        for (League league : leagues) {
            
            leaguesByCountry.get(league.getCountry().getId()).add(league);
            clubsByLeague.put(league.getId(), new ArrayList<>());
            leaguesById.put(league.getId(), league);
        }
    }
    
    /**
     * Load the Available Professions from /mods/professions.txt
     */
    private static void initializeProfessions() {
        
        FileHandle professionsTxt = Gdx.files.internal(PROFESSIONS_FILE);
        
        if (professionsTxt.exists()){
            
            BufferedReader reader = new BufferedReader(professionsTxt.reader());
            
            String line;
            
            try {
                
                line = reader.readLine();

                // Use # symbol as starting for comment (to enable or disable available resolutions)
                while (line != null) {
                    
                    if (!line.startsWith("#")) {
  
                        String [] splitted = line.split(",");
                        
                        /**
                         * League Format
                         * 
                         *  # COLUMNS
                         *  
                         */
                        
                        Profession profession = new Profession();
                        profession.setId(Long.valueOf(splitted[0]));
                        profession.setName(splitted[1]);
                        profession.setPlayerSelectable(Boolean.valueOf(splitted[2]));

                        professions.add(profession);
                    }
                    
                    line = reader.readLine();
                }
                
            } catch (IOException e) {
                // TODO: If error, restore default leagues.txt file
                e.printStackTrace();
            }
            
        } else {
            // TODO: If file doesn't exist, restore from default
        }
        
        System.out.println("FINISHED LOADING " + professions.size() + " PROFESSIONS");
    }

    public static HashMap<Long, List<Club>> getClubsByCountry() {
        return clubsByCountry;
    }

    public static void setClubsByCountry(HashMap<Long, List<Club>> clubsByCountry) {
        DatabaseLoader.clubsByCountry = clubsByCountry;
    }

    public static Authority getMainAuthority() {
        return mainAuthority;
    }

    public static void setMainAuthority(Authority mainAuthority) {
        DatabaseLoader.mainAuthority = mainAuthority;
    }
}