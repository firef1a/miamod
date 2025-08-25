package mia.miamod.features.listeners.impl;

import mia.miamod.features.listeners.AbstractEventListener;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public interface RenderHUD extends AbstractEventListener {
    void renderHUD(DrawContext context, RenderTickCounter tickCounter);
}
