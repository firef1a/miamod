package mia.miamod.features.impl.development;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import mia.miamod.ColorBank;
import mia.miamod.features.Categories;
import mia.miamod.features.Feature;
import mia.miamod.features.impl.internal.server.ServerManager;
import mia.miamod.features.listeners.impl.RenderTooltip;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// This code is ancient and should prob be recoded for the new component system at some point
// I just copied in some inputs from another mod to make it work with components

public class ItemTagViewer extends Feature implements RenderTooltip {
    private static final int regularKeyColor = 0xf0c2ff;
    private static final int componentDataKeyColor = 0x9cffca;
    private static final int stringValueColor = 0xc2daff;
    private static final int numberValueColor = 0xff5555;
    private static final int cKeyColor = 0xaaaaff;

    private long lastCtrl, lastShift;

    private static final Text delimiterText = Text.literal(" : ").withColor(ColorBank.MC_DARK_GRAY);

    public ItemTagViewer(Categories category) {
        super(category, "Item Tag Viewer", "itemtagviewer", "Press shift to show item tags, ctrl to enable verbose mode.");
    }

    private static NbtCompound encodeStack(ItemStack stack, DynamicOps<NbtElement> ops) {
        DataResult<NbtElement> result = ComponentChanges.CODEC.encodeStart(ops, stack.getComponentChanges());
        NbtElement nbtElement = result.getOrThrow();
        return (NbtCompound) nbtElement;
    }

    public void tooltip(ItemStack item, Item.TooltipContext context, TooltipType type, List<Text> textList) {
        if (!ServerManager.isOnDiamondFire()) return;
        if (Screen.hasControlDown()) lastCtrl = System.currentTimeMillis();
        if (Screen.hasShiftDown()) lastShift = System.currentTimeMillis();

        if (hasShiftDown()) {
            renderItemTagTooltip(item, context, textList);
        }
    }

    private boolean hasShiftDown() {
        return System.currentTimeMillis() - lastShift < 200L;
    }

    private boolean hasControlDown() {
        return System.currentTimeMillis() - lastCtrl < 200L;
    }

    public void renderItemTagTooltip(ItemStack item, Item.TooltipContext context, List<Text> textList) {
        if (context.getRegistryLookup() == null) return;
        NbtCompound nbt = encodeStack(item, context.getRegistryLookup().getOps(NbtOps.INSTANCE));
        //Mod.log(nbt.toString());



        NbtCompound mcData = nbt.getCompound("minecraft:custom_data");
        NbtCompound bukkitValues = mcData.getCompound("PublicBukkitValues");

        ArrayList<Text> addLoreList = new ArrayList<>();

        if (bukkitValues != null) {
            Set<String> keys = bukkitValues.getKeys();
            if (!keys.isEmpty()) {

                for (String key : keys) {
                    int valueColor = stringValueColor; //0x96d0ff; //0x6fd6f2;
                    NbtElement element = bukkitValues.get(key);
                    if (element != null) {
                        String value = element.toString();
                        if ((!(value.startsWith("\"") && value.endsWith("\""))) && !(value.startsWith("'") && value.endsWith("'"))) {
                            valueColor = numberValueColor;
                        }

                        Text valueText = Text.literal(value).withColor(valueColor);
                        if (key.equals("hypercube:codetemplatedata") || key.equals("hypercube:varitem")) {
                            if (!(hasControlDown())) valueText = Text.literal("<hidden>").withColor(ColorBank.MC_GRAY);
                        };

                        Text addText = Text.literal(key.substring(10)).withColor(regularKeyColor).append(delimiterText).append(valueText);
                        addLoreList.add(addText);
                    }
                }

            }
        }

        if ((hasControlDown())){
            Set<String> compoundKeys = nbt.getKeys();
            for (String key : compoundKeys) {
                if (key.equals("minecraft:custom_data")) continue;
                NbtCompound value = nbt.getCompound(key);
                if (!value.isEmpty()) {
                    String valueText = value.toString();
                    int size = valueText.length();
                    int chunkSize = 55;
                    if (size > chunkSize) {
                        addLoreList.add(Text.literal(key).withColor(componentDataKeyColor).append(delimiterText).append(Text.literal("{").withColor(stringValueColor)));
                        while (size > 0) {
                            String appendText = valueText.substring(0, Math.min(chunkSize-1, valueText.length()));
                            valueText = valueText.substring(Math.min(chunkSize, valueText.length()));
                            size = valueText.length();
                            addLoreList.add(Text.literal("  " + appendText).withColor(stringValueColor));
                        }
                        addLoreList.add(Text.literal("}").withColor(stringValueColor));
                    } else {
                        addLoreList.add(Text.literal(key).withColor(componentDataKeyColor).append(delimiterText).append(Text.literal(valueText).withColor(stringValueColor)));
                    }
                }
            }
        }

        ArrayList<String> tags = new ArrayList<>(List.of(
                "custom_model_data",
                "max_stack_size",
                "enchantment_glint_override"
        ));
        ArrayList<Text> extTags = new ArrayList<>();
        for (String tag : tags) {
            NbtElement element = nbt.get("minecraft:" + tag);
            if (element != null) { extTags.add(Text.literal(tag).withColor(cKeyColor).append(delimiterText).append(Text.literal(element.toString()).withColor(numberValueColor))); }
        }
        addLoreList.addAll(extTags);

        if (!addLoreList.isEmpty()){
            textList.add(Text.empty());
            Text tagsText = (hasControlDown()) ? Text.literal("Tags").withColor(ColorBank.WHITE).append(Text.literal(" (verbose)").withColor(ColorBank.MC_GRAY)).append(Text.literal(":").withColor(ColorBank.WHITE)) : Text.literal("Tags:").withColor(ColorBank.WHITE);
            textList.add(Text.literal("› ").withColor(ColorBank.MC_DARK_GRAY).append(tagsText));
            textList.addAll(addLoreList);
        }
    }
}

