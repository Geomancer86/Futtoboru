package com.rndmodgames.futtoboru.data.recruitment;

import java.util.List;
import java.util.Map;

/**
 * R-03 (docs/design/RECRUITMENT_AND_TRIALS.md §3.2): a candidate pool region of {@code recruitment/pools.json}. Read-only.
 *
 * @param dataId      stable data id ({@code lancashire})
 * @param runtimeId   its {@code ids.json} number
 * @param country     country data id of the men born here
 * @param area        the language-rate area ({@code areas.json}) they are born in, or null (drawn from the country)
 * @param places      towns, for generated club names and the screen
 * @param clubs       the game clubs whose home is in this region (each club in one region)
 * @param size        the pool's size at the close-season refresh
 * @param monthly     new men a month during the season
 * @param sourceKinds the §3.1 kinds found here (drawn with equal weight)
 * @param originClubs sourced period clubs a man may come from (R-02 {@code origin_clubs.json})
 * @param localClubs  name patterns of generated local clubs, {@code {place}} replaced by a town
 * @param foreignBorn country data id -> share of the region's men born there (D28 Q13)
 * @param upbringing  the football culture a man raised here learned (D28 Q14)
 * @param people      real sourced men who enter this pool first (D28 Q2)
 */
public record PoolRegion(String dataId, long runtimeId, String name, String country, String area, List<String> places,
        List<String> clubs, int size, int monthly, List<String> sourceKinds, List<String> originClubs,
        List<String> localClubs, Map<String, Double> foreignBorn, Upbringing upbringing, List<PoolPerson> people) {

    public PoolRegion {
        places = List.copyOf(places);
        clubs = List.copyOf(clubs);
        sourceKinds = List.copyOf(sourceKinds);
        originClubs = List.copyOf(originClubs);
        localClubs = List.copyOf(localClubs);
        foreignBorn = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(foreignBorn));
        people = List.copyOf(people);
    }

    /**
     * The football culture of a region (D28 Q14): each tendency is taken by a man raised here with {@code chance}, so
     * it varies per person and is never a fixed per-country table.
     */
    public record Upbringing(String style, Map<String, Integer> tendencies, double chance) {
        public Upbringing {
            tendencies = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(tendencies));
        }
    }

    /**
     * A real man of {@code people.json} who enters this pool in seasons {@code from..until} (start years; until null =
     * open) when no squad holds him and he is 17 or older. Only his identity is real: his ability is generated.
     */
    public record PoolPerson(String person, int from, Integer until, String kind, String playsFor, String position) {
        /** @return true when he is listed for the season starting in {@code season} */
        public boolean listedIn(int season) {
            return season >= from && (until == null || season <= until);
        }
    }
}
