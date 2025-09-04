package mia.miamod.render.hud_screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class InGameHudManager {
    private static InGameHudScreen inGameHudScreen;

    public static void setInGameHudScreen(InGameHudScreen screen) {
        inGameHudScreen = screen;
    }
    public static InGameHudScreen getInGameHudScreen() {
        return inGameHudScreen;
    }


    public static void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (inGameHudScreen != null) inGameHudScreen.onMouseButton(window, button, action, mods, ci);
    };
    public static void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (inGameHudScreen != null) inGameHudScreen.onRender(context, tickCounter, ci);
    };
    public static void renderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (inGameHudScreen != null) inGameHudScreen.renderCrosshair(context, tickCounter, ci);
    };
}
