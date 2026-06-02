package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.block.sapling.DynamicSaplingBlock;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;

public class VanillaSaplingEventHandler {

    @SubscribeEvent
    public void onPlayerPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        final BlockState state = event.getPlacedBlock();
        final Block block = state.getBlock();

        if (!(event.getLevel() instanceof Level level) || !DynamicSaplingBlock.shouldReplaceSaplingWhenPlaced(state)) {
            return;
        }

        //Ignore if the block has not actually changed
        if (event.getBlockSnapshot().getState().getBlock() == event.getBlockSnapshot().getCurrentState().getBlock()){
            return;
        }

        final BlockPos pos = event.getPos();
        final Species targetSpecies = DynamicSaplingBlock.SAPLING_REPLACERS.get(block);

        // If we should be overriding for this location, then correct the species to the override.
        final Species species = targetSpecies.selfOrLocationOverride(level, pos);

        //Allow species to override sapling replacement when crouching
        if (species.overrideSaplingReplacementWhenCrouching() && event.getEntity() != null && event.getEntity().isCrouching()){
            return;
        }

        level.removeBlock(pos, false); // Remove the block so the plantTree function won't automatically fail.

        if (!species.plantSapling(level, pos, targetSpecies != species)) { // If it fails then give a seed back to the player.
            ItemUtils.spawnItemStack(level, pos, species.getSeedStack(1));
        }
    }

    @SubscribeEvent
    public void onSaplingGrowTree(BlockGrowFeatureEvent event) {
        final LevelAccessor levelAccess = event.getLevel();
        final BlockPos pos = event.getPos();
        final BlockState state = levelAccess.getBlockState(pos);
        final Block block = state.getBlock();

        if (!(levelAccess instanceof Level level) || !DynamicSaplingBlock.shouldReplaceSaplingWhenGrown(state)) {
            return;
        }

        final Species species = DynamicSaplingBlock.SAPLING_REPLACERS.get(block)
                .selfOrLocationOverride(level, pos);

        level.removeBlock(pos, false); // Remove the block so the plantTree function won't automatically fail.
        event.setCanceled(true);

        if (species.isValid()) {
            if (DynamicSaplingBlock.canSaplingStay(level, species, pos)) {
                species.transitionToTree(level, pos);
            }
        }
    }

}
