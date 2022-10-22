package com.ferreusveritas.dynamictrees.event.handler;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

//This has been put in place to counteract the effects of the FastLeafDecay mod
public class LeafUpdateEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void UpdateNeighbour(BlockEvent.NeighborNotifyEvent event) {
        LevelAccessor level = event.getWorld();
        for (Direction facing : event.getNotifiedSides()) {
            BlockPos blockPos = event.getPos().relative(facing);
            if (TreeHelper.isLeaves(level.getBlockState(blockPos))) {
                event.setCanceled(true);
            }
        }
    }

}