package mia.miamod.mixin.hud;

import mia.miamod.Mod;
import mia.miamod.features.FeatureManager;
import mia.miamod.features.impl.general.chat.MessageChatHud;
import mia.miamod.features.impl.general.chat.MessageChatHudFeature;
import mia.miamod.features.impl.internal.server.ServerManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class MChatScreen {
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        MessageChatHudFeature messageChatHudFeature = FeatureManager.getFeature(MessageChatHudFeature.class);
        if (!messageChatHudFeature.getEnabled()) return;
        if (ServerManager.isNotOnDiamondFire()) return;

        if (mouseX > (double) Mod.getScaledWindowWidth() / 2) {
            messageChatHudFeature.getMessageChatHud().scroll((int) verticalAmount * 3);
            cir.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MessageChatHudFeature messageChatHudFeature = FeatureManager.getFeature(MessageChatHudFeature.class);
        if (!messageChatHudFeature.getEnabled()) return;
        if (ServerManager.isNotOnDiamondFire()) return;

        MessageChatHud hud = messageChatHudFeature.getMessageChatHud();
        hud.render(context, Mod.tick, mouseX, mouseY, true);
        MessageIndicator messageIndicator = hud.getIndicatorAt(mouseX, mouseY);
        if (messageIndicator != null && messageIndicator.text() != null) {
            context.drawOrderedTooltip(Mod.MC.textRenderer, Mod.MC.textRenderer.wrapLines(messageIndicator.text(), 210), mouseX, mouseY);
        } else {
            Style style = this.getTextStyleAt(mouseX, mouseY);
            if (style != null && style.getHoverEvent() != null) {
                context.drawHoverEvent(Mod.MC.textRenderer, style, mouseX, mouseY);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        MessageChatHudFeature messageChatHudFeature = FeatureManager.getFeature(MessageChatHudFeature.class);
        if (!messageChatHudFeature.getEnabled()) return;
        if (ServerManager.isNotOnDiamondFire()) return;

        if (messageChatHudFeature.getMessageChatHud().mouseClicked(mouseX- messageChatHudFeature.getMessageChatHud().xShift, mouseY)) {
            cir.setReturnValue(true);
            return;
        };

        if (button == 0) {
            MessageChatHud chatHud = messageChatHudFeature.getMessageChatHud();
            if (chatHud.mouseClicked(mouseX, mouseY)) {
                cir.setReturnValue(true);
                return;
            }
            Style style = chatHud.getTextStyleAt(mouseX, mouseY);
            if (style != null) {
                Mod.getCurrentScreen().handleTextClick(style);
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    @Nullable
    private Style getTextStyleAt(double x, double y) {
        return FeatureManager.getFeature(MessageChatHudFeature.class).getMessageChatHud().getTextStyleAt(x, y);
    }
}
