package com.ferreusveritas.dynamictrees.api.treedata;

import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.network.GrowSignal;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.worldgen.TreeCodeStore;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.util.Constants.NBT;

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
	
	public Seed getSeed();
	
	/**
	 * Get a copy of the {@link Seed} stack with the supplied quantity.
	 * This is necessary because the stack may be combined with
	 * {@link NBT} data.
	 * 
	 * @param qty The number of items in the newly copied stack.
	 * @return A copy of the {@link ItemStack} with the {@link Seed} inside.
	 */
	public ItemStack getSeedStack(int qty);

	public boolean placeSaplingBlock(World world, BlockPos pos);
	
	///////////////////////////////////////////
	//DIRT
	///////////////////////////////////////////
	
	public BlockRootyDirt getRootyDirtBlock();
	
	public int getSoilLongevity(World world, BlockPos rootPos);
	
	/**
	 * Position sensitive soil acceptability tester.  Mostly to test if the block is dirt but could 
	 * be overridden to allow gravel, sand, or whatever makes sense for the tree
	 * species.
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

	
	/**
	 * Allows a species to do things after a grow event just occured.  Currently used
	 * by Jungle trees to create cocoa pods on the trunk
	 * 
	 * @param world
	 * @param rootPos
	 * @param treePos
	 * @param soilLife
	 */
	public void postGrow(World world, BlockPos rootPos, BlockPos treePos, int soilLife);
	
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
	// INTERACTIVE
	//////////////////////////////
	
	public boolean onTreeActivated(World world, BlockPos rootPos, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ);	

	//////////////////////////////
	// WORLDGEN
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
	 * @param worldGen true if this is being generated by the world generator, false if it's the staff, dendrocoil, etc.
	 */
	public void postGeneration(World world, BlockPos rootPos, Biome biome, int radius, List<BlockPos> endPoints, boolean worldGen);
	
	/**
	 * Worldgen can produce thin sickly trees from the underinflation caused by not living it's full life.
	 * This factor is an attempt to compensate for the problem.
	 * 
	 * @return
	 */
	public float getWorldGenTaperingFactor();
	
}
