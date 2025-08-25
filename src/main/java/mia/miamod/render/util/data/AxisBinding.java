package mia.miamod.render.util.data;

public enum AxisBinding {
    NONE(0f),
    MIDDLE(0.5f),
    FULL(1f);

    private double scale;
    AxisBinding(double scale) {
        this.scale = scale;
    }
}
