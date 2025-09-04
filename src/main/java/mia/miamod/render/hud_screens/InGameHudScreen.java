package mia.miamod.render.hud_screens;

import mia.miamod.Mod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public abstract class InGameHudScreen {
    public InGameHudScreen() {
        Mod.MC.mouse.unlockCursor();
        init();
    }

    protected abstract void init();

    public abstract void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci);
    public abstract void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci);
    public void renderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ci.cancel();
    };

    public void close() {
        Mod.MC.mouse.lockCursor();
        InGameHudManager.setInGameHudScreen(null);
    };

    protected double getMouseX() { return Mod.MC.mouse.getX() / Mod.MC.getWindow().getScaleFactor(); }
    protected double getMouseY() { return Mod.MC.mouse.getY() / Mod.MC.getWindow().getScaleFactor(); }
}

