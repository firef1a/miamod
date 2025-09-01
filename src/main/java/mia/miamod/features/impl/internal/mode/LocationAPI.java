package mia.miamod.features.impl.internal.mode;

import mia.miamod.Mod;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.FeatureManager;
import mia.miamod.features.impl.internal.commands.CommandScheduler;
import mia.miamod.features.impl.internal.commands.ScheduledCommand;
import mia.miamod.features.listeners.DFMode;
import mia.miamod.features.listeners.ModifiableEventData;
import mia.miamod.features.listeners.ModifiableEventResult;
import mia.miamod.features.listeners.impl.ChatEventListener;
import mia.miamod.features.listeners.impl.ModeSwitchEventListener;
import mia.miamod.features.listeners.impl.PacketListener;
import mia.miamod.features.listeners.impl.ServerConnectionEventListener;
import mia.miamod.features.parameters.ParameterIdentifier;
import mia.miamod.features.parameters.impl.BooleanDataField;
import mia.miamod.features.parameters.impl.IntegerDataField;
import mia.miamod.features.parameters.impl.IntegerSliderDataField;
import mia.miamod.features.parameters.impl.StringDataField;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LocationAPI extends Feature implements ChatEventListener, ServerConnectionEventListener, PacketListener {
    private static DFMode mode = DFMode.NONE;

    private static final Pattern SPAWN_ACTIONBAR_PATTERN = Pattern.compile("(⏵+ - )?⧈ -?\\d+ Tokens {2}ᛥ -?\\d+ Tickets {2}⚡ -?\\d+ Sparks");

    public LocationAPI(Categories category) {
        super(category, "LocationAPI", "locapi", "Tracks state and location across diamondfire");
    }

    public static DFMode getMode() { return mode; }

    private static void modeSwitch(DFMode newMode) {
        FeatureManager.implementFeatureListener(ModeSwitchEventListener.class, feature -> feature.onModeSwitch(newMode, mode));
        mode = newMode;
    }

    @Override
    public ModifiableEventResult<Text> chatEvent(ModifiableEventData<Text> message, CallbackInfo ci) {
        String content = message.base().getString();

        Matcher matcher;

        matcher = Pattern.compile("^» Joined game: (.*) by (.{3,16})\\.").matcher(content);
        if (matcher.find()) {
            modeSwitch(DFMode.PLAY);
            //locationStatus = LocationStatus.LOCATION_CHANGE;
        }

        matcher = Pattern.compile("» You are now in dev mode\\.").matcher(content);
        if (matcher.find()) {
            modeSwitch(DFMode.DEV);
        }

        matcher = Pattern.compile("^» You are now in build mode\\.").matcher(content);
        if (matcher.find()) {
            modeSwitch(DFMode.BUILD);
        }

        matcher = Pattern.compile("^» Sending you to ").matcher(content);
        if (matcher.find()) {
            modeSwitch(DFMode.NONE);
        }

        matcher = Pattern.compile("^» You are now spectating this plots code! Other people cannot see you and this action has been logged\\. Do /spawn to exit\\.").matcher(content);
        if (matcher.find()) {
            modeSwitch(DFMode.CODE_SPECTATE);
            //Mod.displayMessage("Entering code-spectate...");
            ci.cancel();
        }

        return message.pass();
    }

    @Override
    public void serverConnectInit(ClientPlayNetworkHandler networkHandler, MinecraftClient minecraftServer) {

    }

    @Override
    public void serverConnectJoin(ClientPlayNetworkHandler networkHandler, PacketSender sender, MinecraftClient minecraftServer) {

    }

    @Override
    public void serverConnectDisconnect(ClientPlayNetworkHandler networkHandler, MinecraftClient minecraftServer) {

    }

    @Override
    public void DFConnectJoin(ClientPlayNetworkHandler networkHandler) {
        mode = DFMode.SPAWN;
    }

    @Override
    public void DFConnectDisconnect(ClientPlayNetworkHandler networkHandler) {
        mode = DFMode.NONE;
    }

    @Override
    public void receivePacket(Packet<?> packet, CallbackInfo ci) {
        if (Mod.MC.player == null) return;
        Matcher matcher;

        if (packet instanceof OverlayMessageS2CPacket(Text text)) {
            matcher = SPAWN_ACTIONBAR_PATTERN.matcher(text.getString());
            if (matcher.find()) {
                mode = DFMode.SPAWN;
            }
        }
    }

    @Override
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {

    }
}
