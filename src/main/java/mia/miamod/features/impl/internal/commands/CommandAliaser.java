package mia.miamod.features.impl.internal.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import mia.miamod.Mod;
import mia.miamod.core.NetworkManager;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.listeners.impl.AlwaysEnabled;
import mia.miamod.features.listeners.impl.RegisterCommandListener;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

import java.util.List;

public class CommandAliaser extends Feature implements RegisterCommandListener, AlwaysEnabled {
    public CommandAliaser(Categories category) {
        super(category, "cmd aliser", "cmdaliser", "registers shortcuts for common commands");
    }

    record SimpleAlias(String command,  @NotBlank String ...aliases) { }
    record GreedyAlias(String command, String greedyName, @NotBlank String ...aliases) { }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        List<SimpleAlias> simpleAliasList = List.of(
                new SimpleAlias("mod vanish", "mv", "modv"),
                new SimpleAlias("support queue", "queue")
        );

        List<GreedyAlias> greedyAliasList = List.of(
                new GreedyAlias("plot vars", "pattern", "vars"),
                new GreedyAlias("support spectate-code", "id", "cs", "codespectate")
        );

        for (SimpleAlias simpleAlias : simpleAliasList) {
            for (String alias : simpleAlias.aliases()) {
                dispatcher.register(
                    ClientCommandManager.literal(alias)
                        .executes(commandContext -> {
                            Mod.sendCommand("/" + simpleAlias.command());
                            return 1;
                        })
                );
            }
        }

        for (GreedyAlias greedyAlias : greedyAliasList) {
            for (String alias : greedyAlias.aliases()) {
                dispatcher.register(
                    ClientCommandManager.literal(alias)
                        .then(
                            ClientCommandManager.argument(greedyAlias.greedyName, StringArgumentType.greedyString())
                                .executes(commandContext -> {
                                    String greedy = StringArgumentType.getString(commandContext, greedyAlias.greedyName);

                                    Mod.sendCommand("/" + greedyAlias.command() + " " + greedy);
                                    return 1;
                                })
                        )
                );
            }
        }
    }
}
