package com.rndmodgames.futtoboru.match.ecs.pitch;

/** An axis-aligned rectangle on the ground (metres), used for era markings such as the 1902 penalty area. */
public record Rect(double minX, double maxX, double minY, double maxY) {

    public boolean contains(double x, double y) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }
}
