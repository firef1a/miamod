package mia.miamod.render.util.elements.impl.hud;

import mia.miamod.render.util.ARGB;
import mia.miamod.render.util.elements.VertexButton;
import org.joml.Matrix4f;

public class HudObjectContainer extends VertexButton {
    public HudObjectContainer(Matrix4f matrix4f, float x, float y, float width, float height, float z, ARGB argb, ARGB highlightARGB, ARGB enabledARGB, ARGB highlighEnabledARGB, Runnable callback) {
        super(matrix4f, x, y, width, height, z, argb, highlightARGB, enabledARGB, highlighEnabledARGB, callback);
    }
}
