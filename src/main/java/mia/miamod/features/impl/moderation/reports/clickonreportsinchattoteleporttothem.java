package mia.miamod.features.impl.moderation.reports;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import mia.miamod.ColorBank;
import mia.miamod.Mod;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.impl.internal.commands.CommandScheduler;
import mia.miamod.features.impl.internal.commands.ScheduledCommand;
import mia.miamod.features.listeners.ModifiableEventData;
import mia.miamod.features.listeners.ModifiableEventResult;
import mia.miamod.features.listeners.impl.ChatEventListener;
import mia.miamod.features.listeners.impl.RegisterCommandListener;
import mia.miamod.features.listeners.impl.ServerConnectionEventListener;
import mia.miamod.features.parameters.ParameterIdentifier;
import mia.miamod.features.parameters.impl.BooleanDataField;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class clickonreportsinchattoteleporttothem extends Feature implements ChatEventListener, RegisterCommandListener {
    private final BooleanDataField runalts;

    public clickonreportsinchattoteleporttothem(Categories category) {
        super(category, "clickonreportsinchattoteleporttothem", "clickonreportsinchattoteleporttothem", "title");
        runalts = new BooleanDataField("Run /alts", ParameterIdentifier.of(this, "runalts"), true, true);

    }

    @Override
    public ModifiableEventResult<Text> chatEvent(ModifiableEventData<Text> message, CallbackInfo ci) {
        String base = message.base().getString();
        Matcher matcher = Pattern.compile("^! Incoming Report \\(([A-Za-z0-9_]{3,16})\\)\\n\\|  Offender: ([A-Za-z0-9_]{3,16})\\n\\|  Offense: (.*)\\n\\|  Location: (Private |)(.*) (\\d*) (?:Mode|Spawn|Existing).*$").matcher(base);
        if (matcher.find()) {
            String reporter = matcher.group(1);
            String offender = matcher.group(2);
            String offense = matcher.group(3);
            String private_text = matcher.group(4);
            String node_text = matcher.group(5);
            String node_number = matcher.group(6);
            Mod.error("REPORT DETECTED: " + reporter + " " + offender + " " + offender + " " + private_text + " " + node_text + " " + node_number);

            boolean is_private = private_text.isEmpty();


            String node_formated = private_text + node_text + " " + node_number;
            String node_id = is_private ? "node" + node_number : "private" + node_number;
            return message.modified(message.modified().copy().styled(
                    style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Text.empty()
                                    .append(Text.literal("Follow ").withColor(ColorBank.MC_GRAY))
                                    .append(Text.literal(offender).withColor(ColorBank.WHITE_GRAY))
                                    .append(Text.literal(" to ").withColor(ColorBank.MC_GRAY))
                                    .append(Text.literal(node_formated).withColor(ColorBank.WHITE_GRAY))
                            ))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/internal_report_teleport " + node_id + " " + offender))));
        }
        return message.pass();
    }



    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("internal_report_teleport")
            .then(ClientCommandManager.argument("node_id", StringArgumentType.string())
                .then(ClientCommandManager.argument("player_name", StringArgumentType.string())
                    .executes(commandContext -> {
                        String player_name = StringArgumentType.getString(commandContext, "player_name");
                        String node_id = StringArgumentType.getString(commandContext, "node_id");

                        CommandScheduler.addCommand(new ScheduledCommand("preference mod_vanish true"));
                        CommandScheduler.addCommand(new ScheduledCommand("server " + node_id));
                        CommandScheduler.addCommand(new ScheduledCommand("tp " + player_name, 250L));
                        if (runalts.getValue()) CommandScheduler.addCommand(new ScheduledCommand("alts " + player_name));

                        return 1;
                    })
                )
            )
        );
    }
}
