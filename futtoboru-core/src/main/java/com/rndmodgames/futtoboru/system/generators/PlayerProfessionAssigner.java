package com.rndmodgames.futtoboru.system.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ContractType;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerProfession;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.DebugLogManager;
import com.rndmodgames.futtoboru.system.loaders.PlayerProfessionsLoader;

/**
 * Player Profession Assigner v1.0
 * 
 * Assigns day jobs (professions) to amateur and semi-professional players.
 * Professional players typically don't have day jobs (full-time football).
 * 
 * For season 1: Random assignment based on club region and profession categories.
 * For future seasons: Load from scripts with historical data.
 * 
 * @author Geomancer86
 */
public class PlayerProfessionAssigner {
    
    /**
     * Assign profession to a player based on contract type and club region
     * 
     * @param player The player
     * @param club The player's club (for regional profession selection)
     * @param contractType The player's contract type (AMATEUR, SEMI_PRO, PROFESSIONAL)
     */
    public static void assignProfession(Player player, Club club, Integer contractType) {
        if (player == null) {
            return;
        }
        
        // Professional players don't have day jobs (full-time football)
        if (contractType != null && contractType == ContractType.PROFESSIONAL) {
            player.setPlayerProfession(null);
            return;
        }
        
        // Amateur and Semi-Professional players get professions
        // For now, assign randomly based on club region
        // Future: Load from scripts with historical data
        
        PlayerProfession profession = selectRandomProfessionForClub(club);
        player.setPlayerProfession(profession);
        
        if (profession != null) {
            DebugLogManager.getInstance().debug(DebugLogManager.CATEGORY_DATA_GENERATION, "PlayerProfessionAssigner", 
                "Assigned profession " + profession.getName() + " to player " + 
                (player.getPerson() != null ? player.getPerson().getName() : "Unknown"));
        }
    }
    
    /**
     * Select a random profession appropriate for the club's region
     * 
     * For 1888-89, clubs are in industrial areas:
     * - Blackburn/Preston: Textile industry
     * - Derby/Nottingham: Railway, lace
     * - Birmingham: Metalworking, foundries
     * - Stoke: Pottery, mining
     * - Bolton: Textiles, engineering
     * 
     * For now, select from all professions with weighted probability
     * Future: Use regional availability from profession data
     */
    private static PlayerProfession selectRandomProfessionForClub(Club club) {
        Map<Long, PlayerProfession> allProfessions = PlayerProfessionsLoader.getAllProfessions();
        
        if (allProfessions == null || allProfessions.isEmpty()) {
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_DATA_GENERATION, "PlayerProfessionAssigner", "No professions loaded, cannot assign profession");
            return null;
        }
        
        // Convert to list for random selection
        List<PlayerProfession> professionList = new ArrayList<>(allProfessions.values());
        
        if (professionList.isEmpty()) {
            return null;
        }
        
        // For now, random selection
        // Future: Weight by club region, profession category, etc.
        int randomIndex = DatabaseLoader.RNG.nextInt(professionList.size());
        return professionList.get(randomIndex);
    }
    
    /**
     * Assign professions to all players in a club
     * 
     * @param club The club
     * @param contracts Map of player ID to contract type (if available)
     */
    public static void assignProfessionsToClub(Club club, Map<Long, Integer> contracts) {
        if (club == null || club.getPlayers() == null) {
            return;
        }
        
        int assigned = 0;
        for (Player player : club.getPlayers()) {
            if (player == null) {
                continue;
            }
            
            // Get contract type (if available)
            Integer contractType = null;
            if (contracts != null && player.getId() != null) {
                contractType = contracts.get(player.getId());
            }
            
            // Assign profession
            assignProfession(player, club, contractType);
            
            if (player.getPlayerProfession() != null) {
                assigned++;
            }
        }
        
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_DATA_GENERATION, "PlayerProfessionAssigner", 
            "Assigned professions to " + assigned + " players in " + club.getName());
    }
}
