package com.rndmodgames.futtoboru.data.recruitment;

/**
 * R-05 (docs/design/RECRUITMENT_AND_TRIALS.md §3.3): a paper of {@code recruitment/newspapers.json} the secretary may
 * advertise in. Read-only.
 *
 * @param dataId       stable data id ({@code athletic-news})
 * @param runtimeId    its {@code ids.json} number
 * @param region       the pool region its readers live in (the replies come from that pool)
 * @param founded      first year of the paper (it takes adverts from then)
 * @param costPence    an advert's cost, in pence (240 to the pound)
 * @param replies      the expected number of replies to one advert
 * @param replyMinDays the first reply comes this many days after the advert at the earliest
 * @param replyMaxDays and the last at the latest
 */
public record Newspaper(String dataId, long runtimeId, String name, String place, String region, int founded,
        int costPence, double replies, int replyMinDays, int replyMaxDays) {
}
