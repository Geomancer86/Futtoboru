package com.rndmodgames.futtoboru.engine.tactics;

import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.tactics.PlayerAttributeKeys;

/**
 * Reads {@link Player} attributes by string key for formation slot weights.
 */
public final class PlayerAttributeAccessor {

    private PlayerAttributeAccessor() {
    }

    public static float getAttribute(Player player, String key) {
        if (player == null || key == null) {
            return 0f;
        }
        Float value = null;
        switch (key) {
            case PlayerAttributeKeys.ACCELERATION:
                value = player.getAcceleration();
                break;
            case PlayerAttributeKeys.SPEED:
                value = player.getSpeed();
                break;
            case PlayerAttributeKeys.STRENGTH:
                value = player.getStrength();
                break;
            case PlayerAttributeKeys.STAMINA:
                value = player.getStamina();
                break;
            case PlayerAttributeKeys.DETERMINATION:
                value = player.getDetermination();
                break;
            case PlayerAttributeKeys.POSITIONING:
                value = player.getPositioning();
                break;
            case PlayerAttributeKeys.TEAMWORK:
                value = player.getTeamwork();
                break;
            case PlayerAttributeKeys.PASSING:
                value = player.getPassing();
                break;
            case PlayerAttributeKeys.KICKING:
                value = player.getKicking();
                break;
            case PlayerAttributeKeys.TACKLING:
                value = player.getTackling();
                break;
            case PlayerAttributeKeys.MARKING:
                value = player.getMarking();
                break;
            case PlayerAttributeKeys.HEADING:
                value = player.getHeading();
                break;
            case PlayerAttributeKeys.SHOT_STOPPING:
                value = player.getShotStopping();
                break;
            case PlayerAttributeKeys.AREA_CONTROL:
                value = player.getAreaControl();
                break;
            default:
                break;
        }
        return value != null ? value : 0f;
    }
}
