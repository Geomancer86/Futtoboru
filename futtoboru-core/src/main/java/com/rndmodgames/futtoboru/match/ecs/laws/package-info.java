/**
 * E-16 (docs/design/MATCH_ENGINE_ECS.md §4.2, MATCH_ENGINE_V2.md §1): the Laws of the season and the officials.
 *
 * <ul>
 * <li>{@link com.rndmodgames.futtoboru.match.ecs.laws.RulesProfile}: every season-dependent law (offside players and
 * own-half exemption, referee on the field, penalties, keeper protection and handling area, restart laws).</li>
 * <li>{@link com.rndmodgames.futtoboru.match.ecs.laws.Officials} / {@link com.rndmodgames.futtoboru.match.ecs.laws.OfficialComponent}
 * / {@link com.rndmodgames.futtoboru.match.ecs.laws.OfficialSystem}: referee and two club umpires on the ground.</li>
 * <li>{@link com.rndmodgames.futtoboru.match.ecs.laws.LawsSystem}: dead ball and restarts, out of play and goals,
 * offside snapshots, fouls, handball, carrying, appeals ({@link com.rndmodgames.futtoboru.match.ecs.laws.Adjudicator}),
 * half-time and full time; {@link com.rndmodgames.futtoboru.match.ecs.laws.LawRecord}s for events (E-17).</li>
 * <li>{@link com.rndmodgames.futtoboru.match.ecs.laws.PlayStateComponent} on the ball: the read-only state of play for
 * the AI (E-15).</li>
 * </ul>
 */
package com.rndmodgames.futtoboru.match.ecs.laws;
