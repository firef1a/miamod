package mia.miamod.render.screens;

public enum ConfigScreenStage {
    OPENING(true, 1),
    OPEN(false, 1),
    CLOSING(true, -1),
    CLOSED(false, -1);
    private final boolean hasAnimation;
    private final int direction;
    ConfigScreenStage(boolean hasAnimation, int direction) { this.hasAnimation = hasAnimation; this.direction = direction; }

    public boolean hasAnimation() { return this.hasAnimation; }
    public int direction() { return this.direction; }
}
