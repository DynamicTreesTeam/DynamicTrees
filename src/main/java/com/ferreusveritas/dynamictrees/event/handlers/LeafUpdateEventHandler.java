package com.ferreusveritas.dynamictrees.event.handlers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

//This has been put in place to counteract the effects of the FastLeafDecay mod
public class LeafUpdateEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void UpdateNeighbour(BlockEvent.NeighborNotifyEvent event) {
        IWorld world = event.getWorld();
        for (Direction facing : event.getNotifiedSides()) {
            BlockPos blockPos = event.getPos().relative(facing);
            if (TreeHelper.isLeaves(world.getBlockState(blockPos))) {
                event.setCanceled(true);
            }
        }
    }

}