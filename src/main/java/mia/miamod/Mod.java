package mia.miamod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mia.miamod.features.FeatureManager;
import mia.miamod.features.listeners.impl.*;
import mia.miamod.render.util.HudMatrixRegistry;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;

public class Mod implements ClientModInitializer {
	public static final String MOD_ID = "miamod";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final MinecraftClient MC = MinecraftClient.getInstance();
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static int tick = 0;

	@Override
	public void onInitializeClient() {
		System.setProperty("java.awt.headless", "false");

		FeatureManager.init();
		Mod.registerCallbacks();
		HudMatrixRegistry.register();
		FeatureManager.getFeaturesByIdentifier(RegisterKeyBindEvent.class).forEach(RegisterKeyBindEvent::registerKeyBind);

		Mod.log(":3");
	}

	public static void shutdownClient() {
		log("stopping client");
	}

	private static void registerCallbacks() {
		ClientTickEvents.START_CLIENT_TICK.register(client -> FeatureManager.getFeaturesByIdentifier(TickEvent.class).forEach(feature -> { feature.tickR(tick); }));
		ClientTickEvents.END_CLIENT_TICK.register(client -> FeatureManager.getFeaturesByIdentifier(TickEvent.class).forEach(feature -> { feature.tickF(tick); tick++; }));
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> FeatureManager.getFeaturesByIdentifier(ClientEventListener.class).forEach(ClientEventListener::clientInitialize));
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {FeatureManager.getFeaturesByIdentifier(ClientEventListener.class).forEach(ClientEventListener::clientShutdown); shutdownClient();});
		ItemTooltipCallback.EVENT.register(((itemStack, tooltipContext, tooltipType, list) -> FeatureManager.getFeaturesByIdentifier(RenderTooltip.class).forEach(feature -> feature.tooltip(itemStack, tooltipContext, tooltipType, list))));
		HudRenderCallback.EVENT.register((draw, tickCounter) -> FeatureManager.getFeaturesByIdentifier(RenderHUD.class).forEach(feature -> feature.renderHUD(draw, tickCounter)));
		ClientPlayConnectionEvents.INIT.register((handler, client) -> FeatureManager.getFeaturesByIdentifier(ServerConnectionEventListener.class).forEach(feature -> feature.serverConnectInit(handler, client)));
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> FeatureManager.getFeaturesByIdentifier(ServerConnectionEventListener.class).forEach(feature -> feature.serverConnectJoin(handler, sender, client)));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FeatureManager.getFeaturesByIdentifier(ServerConnectionEventListener.class).forEach(feature -> feature.serverConnectDisconnect(handler, client)));
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			FeatureManager.getFeaturesByIdentifier(PlayerUseEventListener.class).forEach(feature -> feature.useBlockCallback(player, world, hand, hitResult));
			return ActionResult.PASS;
		});
		UseItemCallback.EVENT.register((player, world, hand) -> {
			FeatureManager.getFeaturesByIdentifier(PlayerUseEventListener.class).forEach(feature -> feature.useItemCallback(player, world, hand));
			return ActionResult.PASS;
		});
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			FeatureManager.getFeaturesByIdentifier(PlayerUseEventListener.class).forEach(feature -> feature.useEntityCallback(player, world, hand, entity, hitResult));
			return ActionResult.PASS;
		});
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> FeatureManager.getFeaturesByIdentifier(RegisterCommandListener.class).forEach(feature -> feature.register(dispatcher, registryAccess)));
	}
	public static void sendCommand(String command) {
		if (command.charAt(0) == '/') {
			Objects.requireNonNull(Mod.MC.getNetworkHandler()).sendChatCommand(command.substring(1));
		}
	}

	public static String getModVersion() { return FabricLoader.getInstance().getModContainer(MOD_ID).isPresent() ? FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion().getFriendlyString() : null; }
	public static String getPlayerName() { return Mod.MC.getSession().getUsername(); }
	public static UUID getPlayerUUID() { return Mod.MC.getSession().getUuidOrNull(); }

	public static Screen getCurrentScreen() { return Mod.MC.currentScreen; }
	public static void setCurrentScreen(Screen screen) { Mod.MC.setScreen(screen); }
	public static int getScaledWindowWidth() {
		return Mod.MC.getWindow().getScaledWidth();
	}
	public static int getScaledWindowHeight() {
		return Mod.MC.getWindow().getScaledHeight();
	}

	public static void log(String msg) { LOGGER.info(msg); }
	public static void warn(String msg) { LOGGER.warn(msg); }
	public static void error(String msg) { LOGGER.error(msg); }
}