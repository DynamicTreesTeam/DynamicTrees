package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.worldgen.TreeCodeStore;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISpecies {

	public float getEnergy(World world, BlockPos rootPos);
	
	public float getGrowthRate(World world, BlockPos rootPos);
	
	/** Probability reinforcer for up direction which is arguably the direction most trees generally grow in.*/
	public int getUpProbability();
	
	/** Thickness of the branch connected to a twig(radius == 1).. This should probably always be 2 [default = 2] */
	public float getSecondaryThickness();
	
	/** Probability reinforcer for current travel direction */
	public int getReinfTravel();
	
	public int getLowestBranchHeight();
	
	/**
	* @param world
	* @param pos 
	* @return The lowest number of blocks from the RootyDirtBlock that a branch can form.
	*/
	public int getLowestBranchHeight(World world, BlockPos pos);
		
	public int getRetries();
	
	public float getTapering();
	
	///////////////////////////////////////////
	//DIRT
	///////////////////////////////////////////
	
	public int getSoilLongevity(World world, BlockPos rootPos);

	//////////////////////////////
	// BIOME HANDLING
	//////////////////////////////
	
	/**
	*
	* @param world The World
	* @param pos
	* @return range from 0.0 - 1.0.  (0.0f for completely unsuited.. 1.0f for perfectly suited)
	*/
	public float biomeSuitability(World world, BlockPos pos);
	
	public TreeCodeStore getJoCodeStore();
}
