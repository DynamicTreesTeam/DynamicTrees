package com.ferreusveritas.dynamictrees.event.handlers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

//This has been put in place to counteract the effects of the FastLeafDecay mod
public class LeafUpdateEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void UpdateNeighbour(BlockEvent.NeighborNotifyEvent event) {
        LevelAccessor world = event.getLevel();
        for (Direction facing : event.getNotifiedSides()) {
            BlockPos blockPos = event.getPos().relative(facing);
            if (TreeHelper.isLeaves(world.getBlockState(blockPos))) {
                event.setCanceled(true);
            }
        }
    }

}