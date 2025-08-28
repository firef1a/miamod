package mia.miamod.features.impl.internal.server;

import mia.miamod.Mod;
import mia.miamod.features.Categories;
import mia.miamod.features.Category;
import mia.miamod.features.Feature;
import mia.miamod.features.FeatureManager;
import mia.miamod.features.listeners.impl.AlwaysEnabled;
import mia.miamod.features.listeners.impl.PacketListener;
import mia.miamod.features.listeners.impl.ServerConnectionEventListener;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

public final class ServerManager extends Feature implements ServerConnectionEventListener, PacketListener, AlwaysEnabled {
    public static RecognizedServers currentServer = RecognizedServers.NONE;
    public static ServerConnectionStatus connectionStatus = ServerConnectionStatus.NONE;

    public ServerManager(Categories category) {
        super(category,"Server Manager", "server_manager", "Detects and executes features when you join and leave DF. Will literally brick every other feature if disabled.");
    }

    public static boolean isNotOnDiamondFire() { return !isOnDiamondFire(); }
    public static boolean isOnDiamondFire() { return currentServer.equals(RecognizedServers.DIAMONDFIRE); }

    @Override
    public void serverConnectInit(ClientPlayNetworkHandler networkHandler, MinecraftClient minecraftServer) { }

    @Override
    public void serverConnectJoin(ClientPlayNetworkHandler networkHandler, PacketSender sender, MinecraftClient minecraftServer) {
        if (networkHandler.getServerInfo() == null) return;
        RecognizedServers server = ServerManager.recognizeServer(networkHandler.getServerInfo());
        if (server.equals(RecognizedServers.DIAMONDFIRE) && ServerManager.connectionStatus.equals(ServerConnectionStatus.CONNECTING)) {
            connectionStatus = ServerConnectionStatus.CONNECTED;
            currentServer = RecognizedServers.DIAMONDFIRE;
            Mod.warn("Joined DiamondFire");
            FeatureManager.implementFeatureListener(ServerConnectionEventListener.class, feature -> feature.DFConnectJoin(networkHandler));
        }
    }

    @Override
    public void serverConnectDisconnect(ClientPlayNetworkHandler networkHandler, MinecraftClient minecraftServer) {
        if (networkHandler.getServerInfo() == null) return;
        RecognizedServers server = ServerManager.recognizeServer(networkHandler.getServerInfo());
        if (server.equals(RecognizedServers.DIAMONDFIRE)) {
            connectionStatus = ServerConnectionStatus.NONE;
            currentServer = RecognizedServers.NONE;
            FeatureManager.implementFeatureListener(ServerConnectionEventListener.class, feature -> feature.DFConnectDisconnect(networkHandler));
        }
    }

    @Override
    public void DFConnectJoin(ClientPlayNetworkHandler networkHandler) {
        Mod.log("Connected to DiamondFire");
    };

    @Override
    public void DFConnectDisconnect(ClientPlayNetworkHandler networkHandler) {
        Mod.log("Disconnected from DiamondFire");
    };

    @Override
    public void receivePacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof LoginHelloS2CPacket) {
            connectionStatus = ServerConnectionStatus.CONNECTING;
        }
    }

    @Override
    public void sendPacket(Packet<?> packet, CallbackInfo ci) { }

    public static RecognizedServers recognizeServer(ServerInfo server) {
        String serverAddress = server.address;
        if (
                serverAddress.contains("mcdiamondfire.com") ||
                        serverAddress.contains("mcdiamondfire.net") ||
                        serverAddress.contains("54.39.29.75") ||
                        serverAddress.contains("diamondfire.games")
        ) return RecognizedServers.DIAMONDFIRE;
        return RecognizedServers.NONE;
    }
}
