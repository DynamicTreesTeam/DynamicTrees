package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.block.sapling.DynamicSaplingBlock;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.helper.ItemUtils;
import com.dtteam.dynamictrees.utility.helper.TreeRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;

public class VanillaSaplingEventHandler {

    @SubscribeEvent
    public void onPlayerPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        final Block block = event.getPlacedBlock().getBlock();

        if (!(event.getLevel() instanceof Level level) || !TreeRegistryHelper.SAPLING_REPLACERS.containsKey(block)) {
            return;
        }

        //Ignore if the block has not actually changed
        if (event.getBlockSnapshot().getState().getBlock() == event.getBlockSnapshot().getCurrentState().getBlock()){
            return;
        }

        final BlockPos pos = event.getPos();
        final Species targetSpecies = TreeRegistryHelper.SAPLING_REPLACERS.get(block);

        // If we should be overriding for this location, then correct the species to the override.
        final Species species = targetSpecies.selfOrLocationOverride(level, pos);

        level.removeBlock(pos, false); // Remove the block so the plantTree function won't automatically fail.

        if (!species.plantSapling(level, pos, targetSpecies != species)) { // If it fails then give a seed back to the player.
            ItemUtils.spawnItemStack(level, pos, species.getSeedStack(1));
        }
    }

    @SubscribeEvent
    public void onSaplingGrowTree(BlockGrowFeatureEvent event) {
        final LevelAccessor levelAccess = event.getLevel();
        final BlockPos pos = event.getPos();
        final Block block = levelAccess.getBlockState(pos).getBlock();

        if (!(levelAccess instanceof Level level) || !TreeRegistryHelper.SAPLING_REPLACERS.containsKey(block)) {
            return;
        }

        final Species species = TreeRegistryHelper.SAPLING_REPLACERS.get(block)
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
