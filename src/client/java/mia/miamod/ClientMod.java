package mia.miamod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mia.miamod.config.ConfigStore;
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
import net.minecraft.text.Text;
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
	public static int tick = 0;

	@Override
	public void onInitializeClient() {
		System.setProperty("java.awt.headless", "false");

		ConfigStore.load();
		FeatureManager.init();
		Mod.registerCallbacks();
		HudMatrixRegistry.register();
		FeatureManager.getFeaturesByIdentifier(RegisterKeyBindEvent.class).forEach(RegisterKeyBindEvent::registerKeyBind);

		Mod.log(":3");
	}
}