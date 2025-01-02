package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.util.TreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

//This has been put in place to counteract the effects of the FastLeafDecay mod
public class LeafUpdateEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void UpdateNeighbour(BlockEvent.NeighborNotifyEvent event) {
        LevelAccessor level = event.getLevel();
        for (Direction facing : event.getNotifiedSides()) {
            BlockPos blockPos = event.getPos().relative(facing);
            if (TreeHelper.isLeaves(level.getBlockState(blockPos))) {
                event.setCanceled(true);
            }
        }
    }

}