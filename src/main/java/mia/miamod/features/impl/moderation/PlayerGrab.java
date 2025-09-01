package mia.miamod.features.impl.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import mia.miamod.ColorBank;
import mia.miamod.Mod;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.FeatureManager;
import mia.miamod.features.impl.general.chat.BetterSCTags;
import mia.miamod.features.impl.internal.commands.CommandScheduler;
import mia.miamod.features.impl.internal.commands.ScheduledCommand;
import mia.miamod.features.listeners.ModifiableEventData;
import mia.miamod.features.listeners.ModifiableEventResult;
import mia.miamod.features.listeners.impl.ChatEventListener;
import mia.miamod.features.listeners.impl.RegisterCommandListener;
import mia.miamod.features.listeners.impl.TickEvent;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static mia.miamod.core.StreamUtils.getPlayerList;
import static mia.miamod.core.StreamUtils.playerListStream;

public final class PlayerGrab extends Feature implements RegisterCommandListener, TickEvent {
    private GrabbedPlayer grabbedPlayer;
    private long nextTimestamp;
    public PlayerGrab(Categories category) {
        super(category, "Player Grabber", "grab", "grabby");
    }

    private record GrabbedPlayer(String playerName, double distance) { }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("grab")
                .then(ClientCommandManager.argument("player_name", StringArgumentType.string())
                        .suggests((context, builder) -> CommandSource.suggestMatching(
                                playerListStream(true),
                                builder
                        ))
                        .then(ClientCommandManager.argument("distance", DoubleArgumentType.doubleArg())
                                .executes(commandContext -> {
                                    String player_name = StringArgumentType.getString(commandContext, "player_name");
                                    double distance = DoubleArgumentType.getDouble(commandContext, "distance");

                                    if (getPlayerList(true).contains(player_name)) {
                                        grabbedPlayer = new GrabbedPlayer(player_name, distance);
                                        nextTimestamp = System.currentTimeMillis();
                                        Mod.error("grabbed " + grabbedPlayer + " at dist " + distance);
                                    } else {
                                        Mod.error(grabbedPlayer + " isnt in player list");
                                    }
                                    return 1;
                                })
                        )
                )
        );

        dispatcher.register(ClientCommandManager.literal("release")
                .executes(commandContext -> {
                    if (grabbedPlayer != null) {
                        grabbedPlayer = null;
                        nextTimestamp = 0L;
                    }
                    return 1;
                })
        );
    }

    @Override
    public void tickR(int tick) {
        if (grabbedPlayer == null) return;
        //if (nextTimestamp > System.currentTimeMillis()) return;
        if (CommandScheduler.getScheduledCommands() == null) return;
        if (!CommandScheduler.getScheduledCommands().isEmpty()) return;

        ScheduledCommand scheduledCommand = new ScheduledCommand(String.format("tp %s ^ ^ ^%s", grabbedPlayer.playerName, grabbedPlayer.distance));
        //nextTimestamp = System.currentTimeMillis() + scheduledCommand.getDelay() + 50L;
        CommandScheduler.addCommand(scheduledCommand);
    }

    @Override
    public void tickF(int tick) {

    }
}
