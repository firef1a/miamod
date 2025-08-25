package mia.miamod.features.listeners.impl;

import com.mojang.brigadier.CommandDispatcher;
import mia.miamod.features.listeners.AbstractEventListener;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public interface RegisterCommandListener extends AbstractEventListener {
    void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess);
}
