package com.ferreusveritas.dynamictrees.event.handler;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.block.DynamicSaplingBlock;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VanillaSaplingEventHandler {

    @SubscribeEvent
    public void onPlayerPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        final BlockState state = event.getPlacedBlock();
        final Block block = state.getBlock();

        if (!(event.getLevel() instanceof Level level) || !TreeRegistry.shouldReplaceSaplingWhenPlaced(state)) {
            return;
        }

        //Ignore if the block has not actually changed
        if (event.getBlockSnapshot().getReplacedBlock().getBlock() == event.getBlockSnapshot().getCurrentBlock().getBlock()){
            return;
        }

        final BlockPos pos = event.getPos();
        final Species targetSpecies = TreeRegistry.SAPLING_REPLACERS.get(block);

        // If we should be overriding for this location, then correct the species to the override.
        final Species species = targetSpecies.selfOrLocationOverride(level, pos);

        level.removeBlock(pos, false); // Remove the block so the plantTree function won't automatically fail.

        if (!species.plantSapling(level, pos, targetSpecies != species)) { // If it fails then give a seed back to the player.
            ItemUtils.spawnItemStack(level, pos, species.getSeedStack(1));
        }
    }

    @SubscribeEvent
    public void onSaplingGrowTree(SaplingGrowTreeEvent event) {
        final LevelAccessor levelAccess = event.getLevel();
        final BlockPos pos = event.getPos();
        final BlockState state = levelAccess.getBlockState(pos);
        final Block block = state.getBlock();

        if (!(levelAccess instanceof Level level) || !TreeRegistry.shouldReplaceSaplingWhenGrown(state)) {
            return;
        }

        final Species species = TreeRegistry.SAPLING_REPLACERS.get(block)
                .selfOrLocationOverride(level, pos);

        level.removeBlock(pos, false); // Remove the block so the plantTree function won't automatically fail.
        event.setResult(Event.Result.DENY);

        if (species.isValid()) {
            if (DynamicSaplingBlock.canSaplingStay(level, species, pos)) {
                species.transitionToTree(level, pos);
            }
        }
    }

}
