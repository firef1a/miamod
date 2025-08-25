package mia.miamod.core;

import mia.miamod.Mod;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;

public abstract class NetworkManager {
    public static final ClientPlayNetworkHandler net = Mod.MC.getNetworkHandler();

    public static void sendPacket(Packet<?> packet) {
        if (net == null) return;
        net.sendPacket(packet);
    }
}
