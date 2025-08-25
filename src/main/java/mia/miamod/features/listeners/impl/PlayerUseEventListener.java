package mia.miamod.features.listeners.impl;

import mia.miamod.features.listeners.AbstractEventListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public interface PlayerUseEventListener extends AbstractEventListener {
    void useBlockCallback(PlayerEntity player, World world, Hand hand, HitResult hitResult);
    void useItemCallback(PlayerEntity player, World world, Hand hand);
    void useEntityCallback(PlayerEntity player, World world, Hand hand, Entity entity, HitResult hitResult);
}
