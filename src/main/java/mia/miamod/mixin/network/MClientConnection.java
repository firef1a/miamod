package mia.miamod.mixin.network;

import com.mojang.brigadier.Message;
import mia.miamod.Mod;
import mia.miamod.features.FeatureManager;
import mia.miamod.features.impl.general.chat.MessageChatHudFeature;
import mia.miamod.features.listeners.ModifiableEventData;
import mia.miamod.features.listeners.ModifiableEventResult;
import mia.miamod.features.listeners.impl.ChatEventListener;
import mia.miamod.features.listeners.impl.PacketListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class MClientConnection {
    @Unique
    private static boolean canceled;

    @Unique
    private static boolean secondaryCanceled;

    @ModifyVariable(method = "handlePacket", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static Packet<?> handlePacket(Packet<?> packet) {
        if (packet instanceof GameMessageS2CPacket(Text content, boolean overlay)) {
            canceled = false; secondaryCanceled = false;

            CallbackInfo ci = new CallbackInfo("", true);
            ModifiableEventData<Text> eventData = new ModifiableEventData<>(content, content);

            Mod.warn(content.toString());

            for (ChatEventListener feature :  FeatureManager.getFeaturesByIdentifier(ChatEventListener.class)) {
                eventData = feature.chatEvent(eventData, ci).eventResult(content, eventData.modified());
            }

            Text modifiedText = eventData.modified();

            if (FeatureManager.getFeature(MessageChatHudFeature.class).addMessage(modifiedText)) {
                canceled = true;
            }
            if (ci.isCancelled()) {
                canceled = true;
            }


            return new GameMessageS2CPacket(modifiedText, overlay);
        }
        return packet;
    }

    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static void handlePacket(Packet<?> packet, net.minecraft.network.listener.PacketListener listener, CallbackInfo ci) {
        FeatureManager.implementFeatureListener(PacketListener.class, (feature) -> { feature.receivePacket(packet, ci); });
        if (packet instanceof GameMessageS2CPacket(Text content, boolean overlay)) {
            if (canceled) ci.cancel();
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V", at = @At("HEAD"), cancellable = true)
    private void handlePacket(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        FeatureManager.implementFeatureListener(PacketListener.class, (feature) -> { feature.sendPacket(packet, ci); });
    }
}
