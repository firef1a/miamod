package mia.miamod.render.util.data.impl;

import mia.miamod.render.util.ARGB;
import mia.miamod.render.util.data.VertexButton;
import org.joml.Matrix4f;

public class VertexEnableButton extends VertexButton {
    public static final ARGB disabledARGB = new ARGB(0xff4230,1f);
    public static final ARGB disabledHighlightARGB = new ARGB(0xff9187,1f);
    public static final ARGB enabledARGB = new ARGB(0x66ff40,1f);
    public static final ARGB enabledHighlightARGB = new ARGB(0xafff9c,1f);
    public VertexEnableButton(Matrix4f matrix4f, float x, float y, float width, float height, float z, Runnable callback) {
        super(
                matrix4f,
                x, y,
                width, height,
                z,
                disabledARGB,
                disabledHighlightARGB,
                enabledARGB,
                enabledHighlightARGB,
                callback
        );
    }
}
