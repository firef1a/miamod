package mia.miamod.features.impl.moderation.tracker;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import mia.miamod.ColorBank;
import mia.miamod.Mod;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.impl.internal.commands.ChatConsumer;
import mia.miamod.features.impl.internal.commands.CommandScheduler;
import mia.miamod.features.impl.internal.commands.ScheduledCommand;
import mia.miamod.features.listeners.impl.RegisterCommandListener;
import mia.miamod.features.listeners.impl.RenderHUD;
import mia.miamod.features.listeners.impl.TickEvent;
import mia.miamod.features.parameters.ParameterIdentifier;
import mia.miamod.features.parameters.impl.EnumDataField;
import mia.miamod.render.util.ARGB;
import mia.miamod.render.util.RenderContextHelper;
import mia.miamod.render.util.RenderHelper;
import mia.miamod.render.util.elements.AxisBinding;
import mia.miamod.render.util.elements.DrawableBinding;
import mia.miamod.render.util.elements.TextBufferDrawable;
import mia.miamod.render.util.elements.VertexRect;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static mia.miamod.core.StreamUtils.getPlayerList;

public final class PlayerOutliner extends Feature implements RegisterCommandListener, RenderHUD, TickEvent {
    private ArrayList<TrackedPlayer> trackedPlayers;
    private final EnumDataField<OutlinerStyle> outlinerStyleEnumDataField;

    public PlayerOutliner(Categories category) {
        super(category, "Player Outliner", "outliner", "Outlines tracked players like a goofy fake cctv footage");
        trackedPlayers = new ArrayList<>();

        outlinerStyleEnumDataField = new EnumDataField<>("Outline Style", ParameterIdentifier.of(this, "outline_style"), OutlinerStyle.RAINBOW, true);
    }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("track")
                .then(ClientCommandManager.argument("player_name", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            List<String> list = getPlayerList(true);
                            list.add("clear");
                            list.addAll(trackedPlayers.stream().map(trackedPlayer -> trackedPlayer.playerName).collect(Collectors.toCollection(ArrayList::new)));
                            return CommandSource.suggestMatching(
                                    list,
                                    builder
                                    );
                        })
                        .executes(commandContext -> {
                            String player_name = StringArgumentType.getString(commandContext, "player_name");

                            if (player_name.equals("clear")) {
                                trackedPlayers.clear();
                                Mod.message("Cleared tracker list");
                            } else if (trackedPlayers.stream().map(trackedPlayer -> trackedPlayer.playerName).collect(Collectors.toCollection(ArrayList::new)).contains(player_name)) {
                                trackedPlayers = trackedPlayers.stream().filter(trackedPlayer -> !trackedPlayer.playerName.equals(player_name)).collect(Collectors.toCollection(ArrayList::new));

                                Mod.message(Text.empty()
                                        .append(Text.literal("Stopped tracking ").withColor(ColorBank.WHITE))
                                        .append(Text.literal(player_name).withColor(ColorBank.WHITE_GRAY)));
                            } else {
                                if (Pattern.matches("[a-zA-Z0-9_]{3,16}",player_name)) {
                                    trackedPlayers.add(new TrackedPlayer(player_name, new PlayerLocation("null"), new ARGB(0xFFFFFF, 1f)));

                                    Mod.message(Text.empty()
                                            .append(Text.literal("Tracking ").withColor(ColorBank.WHITE))
                                            .append(Text.literal(player_name).withColor(ColorBank.WHITE_GRAY))
                                    );
                                } else {
                                    Mod.messageError(Text.empty()
                                            .append(Text.literal(player_name).withColor(0xfa4b3c))
                                            .append(Text.literal(" is an invalid player name."))
                                    );
                                }
                            }

                            return 1;
                        })
                )
        );
    }

    @Override
    public void tickR(int tick) {
        if (Mod.MC.world == null) return;

        if (trackedPlayers != null) {
            List<String> renderedPlayerNames = Mod.MC.world.getPlayers().stream().map(player -> player.getName().getString()).collect(Collectors.toCollection(ArrayList::new));
            List<String> playerListNames = getPlayerList(true);
            for (final TrackedPlayer trackedPlayer : trackedPlayers) {
                String playerName = trackedPlayer.playerName;

                if (renderedPlayerNames.contains(playerName)) {
                    trackedPlayer.setPlayerState(PlayerState.RENDERED);
                } else if (playerListNames.contains(playerName)) {
                    trackedPlayer.setPlayerState(PlayerState.ONLINE);
                } else if (trackedPlayer.playerState.equals(PlayerState.RENDERED) || trackedPlayer.playerState.equals(PlayerState.ONLINE)) {
                    trackedPlayer.setPlayerState(PlayerState.UNKNOWN);

                    ChatConsumer chatConsumer = new ChatConsumer(
                            Pattern.compile( "^(?:(?: {39}\\n" + playerName + " (?s).*→ Server: (.*)\n {39})|(Error: Could not find that player\\.))"),
                            matcher -> {
                                String match = matcher.group(1);
                                if (match == null || match.equals("Error: Could not find that player.")) {
                                    trackedPlayer.setPlayerState(PlayerState.OFFLINE);
                                } else {
                                    trackedPlayer.setPlayerLocation(new PlayerLocation(match));
                                    trackedPlayer.setPlayerState(PlayerState.DIFF_NODE);
                                }
                            },
                            () -> {
                                trackedPlayer.setPlayerState(PlayerState.OFFLINE);
                            },
                            10000L,
                            true
                    );

                    CommandScheduler.addCommand(new ScheduledCommand("locate " + playerName, 500L, new ArrayList<>(List.of(chatConsumer))));
                }
            }
        }
    }

    @Override
    public void tickF(int tick) {

    }

    private static final class TrackedPlayer {
        public String playerName;
        public PlayerLocation playerLocation;
        public ARGB outlineColor;
        public PlayerState playerState;

        public TrackedPlayer(String playerName, PlayerLocation playerLocation, ARGB outlineColor) {
            this.playerName = playerName;
            this.playerLocation = playerLocation;
            this.outlineColor = outlineColor;
            this.playerState = PlayerState.ONLINE;
        }

        public void setPlayerState(PlayerState playerState) {
            this.playerState = playerState;
        }

        public void setPlayerLocation(PlayerLocation playerLocation) {
            this.playerLocation = playerLocation;
        }

        public Text playerText() {
            return Text.empty()
                    .append(Text.literal(playerName).withColor(ColorBank.WHITE))
                    .append(Text.literal(" ᛬ ").withColor(ColorBank.MC_GRAY))
                    .append(Text.literal(playerState.equals(PlayerState.DIFF_NODE) ? playerLocation.node() : playerState.name().toLowerCase()   ).withColor(playerState.highlight.getRGB()));
        }
    }

    private record PlayerLocation(String node) { }

    private enum PlayerState {
        OFFLINE(new ARGB(0xff745c, 0.65)),
        UNKNOWN(new ARGB(0x4a4a4a, 0.65)),
        DIFF_NODE(new ARGB(0xffb05c, 0.65)),
        ONLINE(new ARGB(0xfffd75, 0.65)),
        RENDERED(new ARGB(0x8aff63, 0.65));

        private final ARGB highlight;
        PlayerState(ARGB highlight) {
            this.highlight = highlight;
        }
    }

    @Override
    public void renderHUD(DrawContext context, RenderTickCounter tickCounter) {
        WorldRenderer worldRenderer = Mod.MC.worldRenderer;
        Frustum frustum = worldRenderer.frustum;

        if (Mod.MC.world == null) return;
        if (Mod.MC.player == null) return;
        if (Mod.MC.getNetworkHandler() == null) return;
        if (frustum == null) return;


        LinkedHashMap<String, PlayerEntity> nodePlayerEntities = new LinkedHashMap<>();
        for (PlayerEntity player : Mod.MC.world.getPlayers()) {
            nodePlayerEntities.put(player.getName().getString(), player);
        }

        int x = 0;
        int y = 80;
        int width = 75;
        int height = 18;

        RenderHelper renderHelper = new RenderHelper(Tessellator.getInstance(), 0,0, tickCounter.getTickDelta(false));

        for (TrackedPlayer trackedPlayer : trackedPlayers) {
            String playerName = trackedPlayer.playerName;

            if (nodePlayerEntities.containsKey(playerName)) {
                PlayerEntity playerEntity = nodePlayerEntities.get(playerName);
                if (playerEntity.getId() != Mod.MC.player.getId() && frustum.isVisible(playerEntity.getBoundingBox())) renderPlayerOutline(context, tickCounter, trackedPlayer, playerEntity);
            }
            int textWidth = Mod.MC.textRenderer.getWidth(trackedPlayer.playerText()) + 10;
            width = Math.max(width, textWidth);
        }

        for (TrackedPlayer trackedPlayer : trackedPlayers) {
            String playerName = trackedPlayer.playerName;

            Matrix4f matrix4f = new Matrix4f().identity();
            VertexRect container = new VertexRect(
                    matrix4f,
                    x, y,
                    width, height,
                    101,
                    new ARGB(0x000000, 0.65)
            );

            VertexRect bottom = new VertexRect(
                    matrix4f,
                    0, 0,
                    width, 2,
                    101,
                    new ARGB(0x4a4a4a, 0.65)
            );
            bottom.setParentBinding(new DrawableBinding(AxisBinding.NONE, AxisBinding.FULL));
            //bottom.setSelfBinding(new DrawableBinding(AxisBinding.NONE, AxisBinding.FULL));
            container.addDrawable(bottom);

            TextBufferDrawable nameplate = new TextBufferDrawable(
                    matrix4f,
                    trackedPlayer.playerText(),
                    5, 0,
                    102,
                    new ARGB(ColorBank.WHITE, 1f),
                    true
            );
            nameplate.setParentBinding(new DrawableBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
            nameplate.setSelfBinding(new DrawableBinding(AxisBinding.NONE, AxisBinding.MIDDLE));
            container.addDrawable(nameplate);

            y += height + 4;
            renderHelper.contextDraw(matrix4f, context, container);
        }
    }

    private void renderPlayerOutline(DrawContext context, RenderTickCounter tickCounter, TrackedPlayer trackedPlayer, PlayerEntity player) {
        ArrayList<Double> xCords = new ArrayList<>();
        ArrayList<Double> yCords = new ArrayList<>();
        List<Vec3d> boundingBox = RenderContextHelper.getBoundingBoxCorners(player);
        for (Vec3d cornerPos : boundingBox) {
            // lerp each corner
            Vec3d screenCornerPos = RenderContextHelper.worldToScreen(cornerPos.subtract(player.getPos()).add(player.getLerpedPos(tickCounter.getTickDelta(false))));
            xCords.add(screenCornerPos.x);
            yCords.add(screenCornerPos.y);
        }
        Collections.sort(xCords);
        Collections.sort(yCords);
        Box screenBoundingBox = new Box(xCords.getFirst(), yCords.getFirst(), 0, xCords.getLast(), yCords.getLast(), 0);
        Vec3d eyePos = RenderContextHelper.worldToScreen(player.getLerpedPos(tickCounter.getTickDelta(false)));

        int margin = 2;
        int boundingX = (int) screenBoundingBox.minX - margin;
        int boundingY = (int) screenBoundingBox.minY - margin;
        int boundingWidth = (int) (screenBoundingBox.getLengthX() + margin * 2);
        int boundingHeight = (int) (screenBoundingBox.getLengthY() + margin * 2);


        OutlinerStyle style = outlinerStyleEnumDataField.getValue();
        boolean isRainbow = style.equals(OutlinerStyle.RAINBOW);

        int purple = new ARGB(0xed7aff, 1f).getARGB();

        int period = 5;
        int c1 = isRainbow ? getRainbowARGB(0, period).getARGB() : purple;
        int c2 = isRainbow ? getRainbowARGB(100L, period).getARGB() : purple;
        int c3 = isRainbow ?getRainbowARGB(200L, period).getARGB() : purple;
        int c4 = isRainbow ? getRainbowARGB(300L, period).getARGB() : purple;

        int x = boundingX;
        int y = boundingY;
        int width = boundingWidth;
        int height = boundingHeight;
        int z = 100;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        //RenderSystem.disableDepthTest();

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);


        enableScissorsGL(x, y, x + width, y + 1);
        BufferRenderer.drawWithGlobalProgram(bufferRect(x, y, width, height, z, c1, c2, c3, c4));
        RenderSystem.disableScissor();

        enableScissorsGL(x, y + height - 1, x + width, y + height);
        BufferRenderer.drawWithGlobalProgram(bufferRect(x, y, width, height, z, c1, c2, c3, c4));
        RenderSystem.disableScissor();

        enableScissorsGL(x, y + 1, x + 1, y + height - 1);
        BufferRenderer.drawWithGlobalProgram(bufferRect(x, y, width, height, z, c1, c2, c3, c4));
        RenderSystem.disableScissor();

        enableScissorsGL(x + width - 1, y + 1, x + width, y + height - 1);
        BufferRenderer.drawWithGlobalProgram(bufferRect(x, y, width, height, z, c1, c2, c3, c4));
        RenderSystem.disableScissor();

        RenderSystem.disableBlend();
        //RenderSystem.enableDepthTest();
        RenderSystem.clearShader();
    }


    private void enableScissorsGL(int x1, int y1, int x2, int y2) {
        int scissorWidth = x2 - x1;
        int scissorHeight = y2 - y1;
        Window window = Mod.MC.getWindow();
        double scaleFactor = window.getScaleFactor();
        int windowHeight = window.getScaledHeight();
        int glScissorX = (int) (x1 * scaleFactor);
        int glScissorY = (int) ((windowHeight - y1 - scissorHeight) * scaleFactor);
        int glScissorWidth = (int) (scissorWidth * scaleFactor);
        int glScissorHeight = (int) (scissorHeight * scaleFactor);

        RenderSystem.enableScissor(glScissorX, glScissorY, glScissorWidth, glScissorHeight);
    }

    private BuiltBuffer bufferRect(
            int x, int y,
            int width, int height,
            int z,
            int rainbow1, int rainbow2, int rainbow3, int rainbow4
    ) {
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        buffer.vertex(x, y, z).color(rainbow4);
        buffer.vertex(x, y+height, z).color(rainbow3);
        buffer.vertex(x+width, y+height, z).color(rainbow2);
        buffer.vertex(x+width, y, z).color(rainbow1);

        return buffer.end();
    }

    private ARGB getRainbowARGB(long phase, int period) {
        return new ARGB(Color.HSBtoRGB((((System.currentTimeMillis() + (phase * period)) % (1000L * period)) / (1000f * period)), 1f , 1f), 1f);
    }
}
