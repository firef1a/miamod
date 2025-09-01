package mia.miamod.features.impl.support;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mia.miamod.Mod;
import mia.miamod.core.NetworkManager;
import mia.miamod.core.items.DFItem;
import mia.miamod.features.Categories;
import mia.miamod.features.Category;
import mia.miamod.features.Feature;
import mia.miamod.features.impl.internal.mode.LocationAPI;
import mia.miamod.features.listeners.DFMode;
import mia.miamod.features.listeners.impl.PacketListener;
import mia.miamod.features.listeners.impl.RenderHUD;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CSChestPreview extends Feature implements RenderHUD, PacketListener {
    private BlockPos lastBlockPos;
    private List<Text> overlayTextList;

    private int outstandingSyncRequests;
    private final ArrayList<Integer> syncIdList;

    public CSChestPreview(Categories category) {
        super(category, "CS › Code Chest Preview", "chest preview", "codeclient chest peeker for liberals");
        syncIdList = new ArrayList<>();
        outstandingSyncRequests = 0;
    }

    @Override
    public void renderHUD(DrawContext context, RenderTickCounter tickCounter) {
        if (!LocationAPI.getMode().equals(DFMode.CODE_SPECTATE)) return;
        if (Mod.MC.player == null) return;
        if (Mod.MC.world == null) return;

        HitResult hitResult = Mod.MC.player.raycast(4.5, 0, false);
        if (hitResult instanceof BlockHitResult blockHitResult) {
            BlockPos blockPos = blockHitResult.getBlockPos();

            BlockEntity blockState = Mod.MC.world.getBlockEntity(blockPos);
            if (blockState instanceof ChestBlockEntity) {
                if (!blockPos.equals(lastBlockPos)) {
                    outstandingSyncRequests++;
                    overlayTextList = null;
                    NetworkManager.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult, 0));
                }
            } else {
                overlayTextList = null;
            }

            if (overlayTextList != null) {
                context.drawTooltip(Mod.MC.textRenderer, overlayTextList, Mod.getScaledWindowWidth() / 2, Mod.getScaledWindowHeight() / 2);

            }

            lastBlockPos = blockPos;
        }
    }

    @Override
    public void receivePacket(Packet<?> packet, CallbackInfo ci) {
        if (!LocationAPI.getMode().equals(DFMode.CODE_SPECTATE)) return;

        if (packet instanceof OpenScreenS2CPacket openScreenS2CPacket) {
            if (outstandingSyncRequests > 0)  {
                ci.cancel();
                syncIdList.add(openScreenS2CPacket.getSyncId());
            }
        }

        if (packet instanceof InventoryS2CPacket inventoryS2CPacket) {
            if (outstandingSyncRequests > 0) {
                if (syncIdList.contains( inventoryS2CPacket.getSyncId())) {
                    ci.cancel();
                    overlayTextList = getOverlayText(inventoryS2CPacket.getContents());
                    outstandingSyncRequests--;
                    syncIdList.remove((Object) inventoryS2CPacket.getSyncId());
                }
            }
        }

    }

    @Override
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {

    }

    private List<Text> getOverlayText(List<ItemStack> items) {
        ArrayList<Text> texts = new ArrayList<>();

        int i = 0;
        for (ItemStack item : items) {
            i++;
            if (item.isEmpty()) continue;
            if (i > items.size()-36) continue;

            DFItem dfItem = DFItem.of(item);
            List<Text> currentLore = dfItem.getLore();
            ArrayList<Text> lore = new ArrayList<>(currentLore);


            MutableText text = Text.empty();
            text.append(Text.literal(" • ").formatted(Formatting.DARK_GRAY));
            String varItem = dfItem.getHypercubeStringValue("varitem");
            if (Objects.equals(varItem, "")) {
                text.append(item.getCount() + "x ");
                text.append(item.getName());
            } else {
                try {
                    JsonObject object = JsonParser.parseString(varItem).getAsJsonObject();
                    Type type = Type.valueOf(object.get("id").getAsString());
                    JsonObject data = object.get("data").getAsJsonObject();
                    //                            JsonArray lore = data.get("display").getAsJsonObject().get("Lore").getAsJsonArray();
                    text.append(Text.literal(type.name.toUpperCase()).fillStyle(Style.EMPTY.withColor(type.color)).append(" "));
                    if (type == Type.var) {
                        Scope scope = Scope.valueOf(data.get("scope").getAsString());
                        text.append(scope.getShortName()).fillStyle(Style.EMPTY.withColor(scope.color)).append(" ");
                    }
                    if (type == Type.num || type == Type.txt || type == Type.comp || type == Type.var || type == Type.g_val || type == Type.pn_el) {
                        text.append(item.getName());
                    }
                    if (type == Type.loc) {
                        JsonObject loc = data.get("loc").getAsJsonObject();
                        text.append("[%.2f, %.2f, %.2f, %.2f, %.2f]".formatted(
                                loc.get("x").getAsFloat(),
                                loc.get("y").getAsFloat(),
                                loc.get("z").getAsFloat(),
                                loc.get("pitch").getAsFloat(),
                                loc.get("yaw").getAsFloat()));
                    }
                    if (type == Type.vec) {
                        text.append(Text.literal("<%.2f, %.2f, %.2f>".formatted(
                                data.get("x").getAsFloat(),
                                data.get("y").getAsFloat(),
                                data.get("z").getAsFloat())
                        ).fillStyle(Style.EMPTY.withColor(Type.vec.color)));
                    }
                    if (type == Type.snd) {
                        text.append(lore.getFirst());
                        text.append(Text.literal(" P: ").formatted(Formatting.GRAY));
                        text.append(Text.literal("%.1f".formatted(data.get("pitch").getAsFloat())));
                        text.append(Text.literal(" V: ").formatted(Formatting.GRAY));
                        text.append(Text.literal("%.1f".formatted(data.get("vol").getAsFloat())));
                    }
                    if (type == Type.part) {
                        text.append(Text.literal("%dx ".formatted(data.get("cluster").getAsJsonObject().get("amount").getAsInt())));
                        text.append(lore.getFirst());
                    }
                    if (type == Type.pot) {
                        text.append(lore.getFirst());
                        text.append(Text.literal(" %d ".formatted(data.get("amp").getAsInt() + 1)));
                        int dur = data.get("dur").getAsInt();
                        text.append(dur >= 1000000 ? "Infinite" : dur % 20 == 0 ? "%d:%02d".formatted((dur / 1200), (dur / 20) % 60) : (dur + "ticks"));
                    }
                    if (type == Type.bl_tag) {
                        text.append(Text.literal(data.get("tag").getAsString()).formatted(Formatting.YELLOW));
                        text.append(Text.literal(" » ").formatted(Formatting.DARK_AQUA));
                        text.append(Text.literal(data.get("option").getAsString()).formatted(Formatting.AQUA));
                    }
                    if (type == Type.hint) continue;
                } catch (Exception ignored) {
                    continue;
                }
            }
            texts.add(text);
        }
        if (texts.isEmpty()) {
            texts.add(Text.literal("Empty").formatted(Formatting.GOLD));
        } else {
            texts.addFirst(Text.literal("Contents").formatted(Formatting.GOLD));
        }

        return texts;

    }

    enum Type {
        txt("str", Formatting.AQUA),
        comp("txt", TextColor.fromRgb(0x7fd42a)),
        num("num", Formatting.RED),
        loc("loc", Formatting.GREEN),
        vec("vec", TextColor.fromRgb(0x2affaa)),
        snd("snd", Formatting.BLUE),
        part("par", TextColor.fromRgb(0xaa55ff)),
        pot("pot", TextColor.fromRgb(0xff557f)),
        var("var", Formatting.YELLOW),
        g_val("val", TextColor.fromRgb(0xffd47f)),
        pn_el("param", TextColor.fromRgb(0xaaffaa)),
        bl_tag("tag", Formatting.YELLOW),
        hint("hint", TextColor.fromRgb(0xaaff55));

        public final String name;
        public final TextColor color;

        Type(String name, TextColor color) {
            this.name = name;
            this.color = color;
        }

        Type(String name, Formatting color) {
            this.name = name;
            this.color = TextColor.fromFormatting(color);
        }
    }

    enum Scope {
        unsaved(TextColor.fromFormatting(Formatting.GRAY), "GAME", "G", "g"),
        local(TextColor.fromFormatting(Formatting.GREEN), "LOCAL", "L", "l"),
        saved(TextColor.fromFormatting(Formatting.YELLOW), "SAVE", "S", "s"),
        line(TextColor.fromRgb(0x55aaff), "LINE", "I", "i");

        public final TextColor color;
        public final String longName;
        public final String shortName;
        public final String tag;

        Scope(TextColor color, String longName, String shortName, String tag) {
            this.color = color;
            this.longName = longName;
            this.shortName = shortName;
            this.tag = tag;
        }

        public String getShortName() {
            return shortName;
        }
    }
}
