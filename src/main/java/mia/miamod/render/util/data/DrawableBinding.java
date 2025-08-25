package mia.miamod.render.util.data;

public class DrawableBinding {
    private AxisBinding xBinding, yBinding;
    public DrawableBinding(AxisBinding xBinding, AxisBinding yBinding) {
        this.xBinding = xBinding;
        this.yBinding = yBinding;
    }

    public AxisBinding getXBinding() { return xBinding; }
    public AxisBinding getYBinding() { return yBinding; }

    public void setXBinding(AxisBinding binding) { xBinding = binding; }
    public void setYBinding(AxisBinding binding) { yBinding = binding; }
}
