package com.ferreusveritas.dynamictrees.trees;

import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.network.GrowSignal;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.inspectors.NodeFruit;
import com.ferreusveritas.dynamictrees.inspectors.NodeFruitCocoa;
import com.ferreusveritas.dynamictrees.worldgen.TreeCodeStore;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public interface ISpecies {

	public String getName();
	
	public String getModId();
	
	public DynamicTree getTree();
	
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
		
	public ItemStack getSeedStack();

	public boolean placeSaplingBlock(World world, BlockPos pos);
	
	///////////////////////////////////////////
	//DIRT
	///////////////////////////////////////////
	
	public BlockRootyDirt getRootyDirtBlock();
	
	public int getSoilLongevity(World world, BlockPos rootPos);

	/**
	 * Soil acceptability tester.  Mostly to test if the block is dirt but could 
	 * be overridden to allow gravel, sand, or whatever makes sense for the tree
	 * species.
	 * 
	 * @param soilBlockState
	 * @return
	 */
	public boolean isAcceptableSoil(IBlockState soilBlockState);
	
	/**
	 * Position sensitive version of soil acceptability tester
	 * 
	 * @param blockAccess
	 * @param pos
	 * @param soilBlockState
	 * @return
	 */
	public boolean isAcceptableSoil(IBlockAccess blockAccess, BlockPos pos, IBlockState soilBlockState);
	
	/**
	 * Version of soil acceptability tester that is only run for worldgen.  This allows for Swamp oaks and stuff.
	 * 
	 * @param blockAccess
	 * @param pos
	 * @param soilBlockState
	 * @return
	 */
	public boolean isAcceptableSoilForWorldgen(IBlockAccess blockAccess, BlockPos pos, IBlockState soilBlockState);
	
	
	///////////////////////////////////////////
	//GROWTH
	///////////////////////////////////////////
	
	/**
	* Selects a new direction for the branch(grow) signal to turn to.
	* This function uses a probability map to make the decision and is acted upon by the GrowSignal() function in the branch block.
	* Can be overridden for different species but it's preferable to override customDirectionManipulation.
	* 
	* @param world The World
	* @param pos
	* @param branch The branch block the GrowSignal is traveling in.
	* @param signal The grow signal.
	* @return
	*/
	public EnumFacing selectNewDirection(World world, BlockPos pos, BlockBranch branch, GrowSignal signal);

	/** Gets the fruiting node analyzer for this tree.  See {@link NodeFruitCocoa} for an example.
	*  
	* @param world The World
	* @param x X-Axis of block
	* @param y Y-Axis of block
	* @param z Z-Axis of block
	* @return The NodeFruit or return null to disable fruiting. Most species do return null
	*/
	public NodeFruit getNodeFruit(World world, BlockPos pos);
	
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
	
	
	//////////////////////////////
	// WORLDGEN STUFF
	//////////////////////////////
	
	/**
	 * Default worldgen spawn mechanism.
	 * This method uses JoCodes to generate tree models.
	 * Override to use other methods.
	 * 
	 * @param world The world
	 * @param pos The position of {@link BlockRootyDirt} this tree is planted in
	 * @param biome The biome this tree is generating in
	 * @param facing The orientation of the tree(rotates JoCode)
	 * @param radius The radius of the tree generation boundary
	 * @return true if tree was generated. false otherwise.
	 */
	public boolean generate(World world, BlockPos pos, Biome biome, Random random, int radius);
	
	public TreeCodeStore getJoCodeStore();
	
	/**
	 * Allows the tree to decorate itself after it has been generated.  Add vines, fruit, etc.
	 * 
	 * @param world The world
	 * @param pos The position of {@link BlockRootyDirt} this tree is planted in
	 * @param biome The biome this tree is generating in
	 * @param radius The radius of the tree generation boundary
	 * @param endPoints A {@link List} of {@link BlockPos} in the world designating branch endpoints
	 */
	public void postGeneration(World world, BlockPos pos, Biome biome, int radius, List<BlockPos> endPoints);
	
	/**
	 * Worldgen can produce thin sickly trees from the underinflation caused by not living it's full life.
	 * This factor is an attempt to compensate for the problem.
	 * 
	 * @return
	 */
	public float getWorldGenTaperingFactor();
	
}
