package mia.miamod.mixin.hud;

import mia.miamod.features.FeatureManager;
import mia.miamod.features.impl.general.chat.MessageChatHudFeature;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ChatHud.class)
public class MChatHud {
    @Inject(method = "getVisibleLineCount", at = @At("RETURN"), cancellable = true)
    private void getVisibleLineCount(CallbackInfoReturnable<Integer> cir) {
        boolean exists = FeatureManager.hasFeature(MessageChatHudFeature.class);
        if (exists) cir.setReturnValue(FeatureManager.getFeature(MessageChatHudFeature.class).getMessageChatHud().getVisibleLineCount());
    }
    @Inject(method = "resetScroll", at = @At("RETURN"))
    private void resetScroll(CallbackInfo ci) {
        FeatureManager.getFeature(MessageChatHudFeature.class).getMessageChatHud().resetScroll();
    }
}
