package mia.miamod.features.impl.general;

import mia.miamod.Mod;
import mia.miamod.core.NetworkManager;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.impl.internal.commands.CommandScheduler;
import mia.miamod.features.impl.internal.commands.ScheduledCommand;
import mia.miamod.features.listeners.ModifiableEventData;
import mia.miamod.features.listeners.ModifiableEventResult;
import mia.miamod.features.listeners.impl.ChatEventListener;
import mia.miamod.features.listeners.impl.PacketListener;
import mia.miamod.features.parameters.ParameterIdentifier;
import mia.miamod.features.parameters.impl.BooleanDataField;
import mia.miamod.features.parameters.impl.IntegerDataField;
import mia.miamod.features.parameters.impl.StringDataField;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.MessageAcknowledgmentC2SPacket;

public final class DotSlashBypass extends Feature implements PacketListener {
    public DotSlashBypass(Categories category) {
        super(category, "Dot Slash Bypass", "dotslash", "Automatically removes the . before / and @ commands");
    }

    @Override
    public void receivePacket(Packet<?> packet, CallbackInfo ci) {

    }

    @Override
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ChatMessageC2SPacket(String chatMessage, Instant timestamp, long salt, @Nullable MessageSignatureData signature, LastSeenMessageList.Acknowledgment acknowledgment)) {
            if (chatMessage.startsWith("./") || chatMessage.startsWith(".@")) {
                String modifiedMessage = chatMessage.substring(1);
                if (Mod.MC.getNetworkHandler() != null) {
                    Mod.MC.getNetworkHandler().sendChatMessage(modifiedMessage);
                    ci.cancel();
                }
            }
        }
    }
}
