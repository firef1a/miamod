package mia.miamod.features.impl.general;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import mia.miamod.features.Feature;
import mia.miamod.features.listeners.impl.RegisterCommandListener;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class merowwwaadfa extends Feature implements RegisterCommandListener {
    public merowwwaadfa() {
        super("ggagiya", "id", "description");
    }

    private enum idk {
        SURVIVAL(GameMode.SURVIVAL),
        CREATIVE(GameMode.CREATIVE),
        SPECTATOR(GameMode.SPECTATOR),
        ADVENTURE(GameMode.ADVENTURE);

        private final GameMode mode;
        idk(GameMode gameMode) {
            this.mode = gameMode;
        }
    }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
                ClientCommandManager.literal("forceclientstate")
                        .then(ClientCommandManager.argument("state", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    MinecraftClient client = MinecraftClient.getInstance();
                                    // Get the list of player names from the client's network handler
                                    List<String> list = Arrays.stream(idk.values()).map((d) -> d.name().toLowerCase()).collect(Collectors.toCollection(ArrayList::new));
                                    return CommandSource.suggestMatching(
                                            list.stream(),
                                            builder
                                    );
                                })
                                .executes(commandContext -> {
                                    String mode = StringArgumentType.getString(commandContext, "state");
                                    List<String> modes = Arrays.stream(idk.values()).map((d) -> d.name().toLowerCase()).collect(Collectors.toCollection(ArrayList::new));
                                    if (modes.contains(mode)) {
                                        MinecraftClient.getInstance().interactionManager.setGameMode(idk.valueOf(mode.toUpperCase()).mode);
                                    }
                                    return 1;
                                })
                        )
        );
    }
}
