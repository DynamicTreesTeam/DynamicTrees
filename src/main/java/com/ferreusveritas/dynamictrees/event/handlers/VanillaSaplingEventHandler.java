package com.ferreusveritas.dynamictrees.event.handlers;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.DynamicSaplingBlock;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.ItemUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;

public class VanillaSaplingEventHandler {

	@SubscribeEvent
	public void onPlayerPlaceBlock(BlockEvent.EntityPlaceEvent event) {
		final BlockState state = event.getPlacedBlock();

		LogManager.getLogger().debug(Species.REGISTRY.getValue(new ResourceLocation(DynamicTrees.MOD_ID, "spruce")).getDisplayInfo());

		if (!(event.getWorld() instanceof World) || !TreeRegistry.SAPLING_REPLACERS.containsKey(state))
			return;

		final Species species = TreeRegistry.SAPLING_REPLACERS.get(state);
		final World world = (World) event.getWorld();
		final BlockPos pos = event.getPos();

		world.removeBlock(pos, false); // Remove the block so the plantTree function won't automatically fail.

		if (!species.plantSapling(world, pos)) { // If it fails then give a seed back to the player.
			ItemUtils.spawnItemStack(world, pos, species.getSeedStack(1));
		}
	}

	@SubscribeEvent
	public void onSaplingGrowTree(SaplingGrowTreeEvent event) {
		final IWorld iWorld = event.getWorld();
		final BlockPos pos = event.getPos();
		final BlockState blockState = iWorld.getBlockState(pos);

		if (!(iWorld instanceof World) || !TreeRegistry.SAPLING_REPLACERS.containsKey(blockState))
			return;

		final World world = ((World) iWorld);
		final Species species = TreeRegistry.SAPLING_REPLACERS.get(blockState);

		world.removeBlock(pos, false); // Remove the block so the plantTree function won't automatically fail.
		event.setResult(Event.Result.DENY);

		if (species.isValid()) {
			if (DynamicSaplingBlock.canSaplingStay(world, species, pos)) {
				species.transitionToTree(world, pos);
			}
		}
	}

}
