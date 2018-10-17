package com.ferreusveritas.dynamictrees.event;

import java.util.List;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This event fires after a tree has been created and decorated.
 * Use this to add things like beehives, bird nests, extra vines, hanging lights, etc.
 * 
 * @author ferreusveritas
 */
public class SpeciesPostGenerationEvent extends Event {
	
	private final World world;
	private final Species species;
	private final BlockPos rootPos;
	private final List<BlockPos> endPoints;
	private final SafeChunkBounds safeBounds;
	private final IBlockState initialDirtState;
	
	public SpeciesPostGenerationEvent(World world, Species species, BlockPos rootPos, List<BlockPos> endPoints, SafeChunkBounds safeBounds, IBlockState initialDirtState) {
		this.world = world;
		this.species = species;
		this.rootPos = rootPos;
		this.endPoints = endPoints;
		this.safeBounds = safeBounds;
		this.initialDirtState = initialDirtState;
	}
	
	public World getWorld() {
		return world;
	}
	
	public Species getSpecies() {
		return species;
	}
	
	public BlockPos getRootPos() {
		return rootPos;
	}
	
	/**
	 * An endpoint is a {@link BlockPos} of the end of each branch in a tree.
	 * 
	 * @return Endpoint list
	 */
	public List<BlockPos> getEndPoints() {
		return endPoints;
	}
	
	/**
	 * Safebounds offers a method to test if a block is about to be put in an unloaded chunk.
	 * 
	 * @return The current safe bounds for placing blocks
	 */
	public SafeChunkBounds getSafeBounds() {
		return safeBounds;
	}
	
	/**
	 * Get the state of the dirt before a tree was planted there
	 * 
	 * @return The initial state of the dirt block before it was changed
	 */
	public IBlockState getInitialDirtState() {
		return initialDirtState;
	}
}
