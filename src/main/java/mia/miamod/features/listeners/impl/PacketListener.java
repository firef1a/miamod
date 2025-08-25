package mia.miamod.features.listeners.impl;

import mia.miamod.features.listeners.AbstractEventListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface PacketListener extends AbstractEventListener {
    void receivePacket(Packet<?> packet, CallbackInfo ci);
    void sendPacket(Packet<?> packet, CallbackInfo ci);
}
