package mia.miamod.mixin.hud;

import mia.miamod.Mod;
import mia.miamod.render.util.ARGB;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class MInGameHUD {

    @Shadow private float autosaveIndicatorAlpha;

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {

        //Mod.MC.mouse.unlockCursor();
        //context.fill(100,100,500,500,new ARGB(0xFFFFFF, 0.3F).getARGB());
    }

    @Inject(at = @At("RETURN"), method = "render")
    private void afterRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {

    }

    @Inject(at = @At("HEAD"), method = "renderCrosshair", cancellable = true)
    private void renderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
       //ci.cancel();
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;shouldRenderSpectatorCrosshair(Lnet/minecraft/util/hit/HitResult;)Z"), method = "renderCrosshair")
    private void blitRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {

    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;shouldRenderSpectatorCrosshair(Lnet/minecraft/util/hit/HitResult;)Z", shift = At.Shift.AFTER), method = "renderCrosshair")
    private void afterBlitRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {

    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;shouldRenderSpectatorCrosshair(Lnet/minecraft/util/hit/HitResult;)Z"), method = "renderCrosshair")
    private void blitRenderCrosshairSliced(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {

    }


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;shouldRenderSpectatorCrosshair(Lnet/minecraft/util/hit/HitResult;)Z", shift = At.Shift.AFTER), method = "renderCrosshair")
    private void afterBlitRenderCrosshairSliced(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {

    }
    {

    }
}