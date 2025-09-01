package mia.miamod.render.util.elements;

public enum AxisBinding {
    NONE(0f),
    MIDDLE(0.5f),
    FULL(1f);

    private float scale;
    AxisBinding(float scale) {
        this.scale = scale;
    }

    public float getScale() { return scale; }
}
