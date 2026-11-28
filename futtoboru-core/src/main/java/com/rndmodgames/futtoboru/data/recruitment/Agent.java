package com.rndmodgames.futtoboru.data.recruitment;

/**
 * R-05 (docs/design/RECRUITMENT_AND_TRIALS.md §3.3, §7): a Scottish agent of {@code recruitment/agents.json} who writes
 * offering men's names for a fee (D28 Q3: agents, no scout role before it is sourced). Read-only.
 *
 * @param dataId    stable data id
 * @param runtimeId its {@code ids.json} number
 * @param region    the pool region his men come from
 * @param feePence  his fee per man, in pence
 * @param names     how many men a letter offers
 * @param inflate   how many attribute points he talks his men up (self-interest, design §4.1)
 */
public record Agent(String dataId, long runtimeId, String name, String place, String region, int feePence, int names,
        int inflate) {
}
