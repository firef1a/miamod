package mia.miamod.features.listeners.impl;

import mia.miamod.features.listeners.AbstractEventListener;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

public interface ServerConnectionEventListener extends AbstractEventListener {
    void serverConnectInit(ClientPlayNetworkHandler networkHandler, MinecraftClient minecraftServer);
    void serverConnectJoin(ClientPlayNetworkHandler networkHandler, PacketSender sender, MinecraftClient minecraftServer);
    void serverConnectDisconnect(ClientPlayNetworkHandler networkHandler, MinecraftClient minecraftServer);

    void DFConnectJoin(ClientPlayNetworkHandler networkHandler);
    void DFConnectDisconnect(ClientPlayNetworkHandler networkHandler);
}
