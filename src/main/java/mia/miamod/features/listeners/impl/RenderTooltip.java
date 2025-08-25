package mia.miamod.features.listeners.impl;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import mia.miamod.features.listeners.AbstractEventListener;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

import java.util.List;

public interface RenderTooltip extends AbstractEventListener {
    /*
        converts components into nbt compounds
     */
    static NbtCompound encodeStack(ItemStack stack, DynamicOps<NbtElement> ops) {
        DataResult<NbtElement> result = ComponentChanges.CODEC.encodeStart(ops, stack.getComponentChanges());
        NbtElement nbtElement = result.getOrThrow();
        return (NbtCompound) nbtElement;
    }

    void tooltip(ItemStack item, Item.TooltipContext context, TooltipType type, List<Text> textList);
}
