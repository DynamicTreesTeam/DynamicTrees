package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSaplingRare;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSaplingVanilla;
import com.ferreusveritas.dynamictrees.blocks.BlockFruit;
import com.ferreusveritas.dynamictrees.blocks.BlockFruitCocoa;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirtFake;
import com.ferreusveritas.dynamictrees.blocks.BlockRootySand;
import com.ferreusveritas.dynamictrees.blocks.BlockTrunkShell;
import com.ferreusveritas.dynamictrees.blocks.BlockVerboseFire;
import com.ferreusveritas.dynamictrees.blocks.LeavesPaging;
import com.ferreusveritas.dynamictrees.blocks.LeavesProperties;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks {
	
	public static BlockRooty blockRootyDirt;
	public static BlockRooty blockRootySand;
	public static BlockRooty blockRootyDirtSpecies;
	public static Block blockRootyDirtFake;
	public static BlockDynamicSapling blockDynamicSapling;
	public static BlockDynamicSaplingRare blockDynamicSaplingSpecies;
	public static BlockFruit blockFruit;
	public static BlockFruitCocoa blockFruitCocoa;
	public static BlockBonsaiPot blockBonsaiPot;
	public static BlockVerboseFire blockVerboseFire;
	public static BlockTrunkShell blockTrunkShell;
	
	public static ILeavesProperties oakLeavesProperties;
	public static ILeavesProperties spruceLeavesProperties;
	public static ILeavesProperties birchLeavesProperties;
	public static ILeavesProperties jungleLeavesProperties;
	public static ILeavesProperties acaciaLeavesProperties;
	public static ILeavesProperties darkOakLeavesProperties;
	
	public static ILeavesProperties cactusLeavesProperties;
	
	//TODO: Temporary Code
	public static ILeavesProperties testLeavesProperties;
	
	public static CommonBlockStates blockStates;
	
	public static void preInit() {
		BlockDynamicLeaves.passableLeavesModLoaded = net.minecraftforge.fml.common.Loader.isModLoaded("passableleaves");
		
		blockStates = new CommonBlockStates();
		
		blockRootyDirt = new BlockRootyDirt(false);//Dirt
		blockRootySand = new BlockRootySand(false);//Sand
		blockRootyDirtSpecies = new BlockRootyDirt(true);//Special dirt for rarer species
		blockRootyDirtFake = new BlockRootyDirtFake("rootydirtfake");
		blockDynamicSapling = new BlockDynamicSaplingVanilla("sapling");//Dynamic version of a Vanilla sapling
		blockDynamicSaplingSpecies = new BlockDynamicSaplingRare("saplingrare");//Species extended sapling(Apple)
		blockBonsaiPot = new BlockBonsaiPot();//Bonsai Pot
		blockFruitCocoa = new BlockFruitCocoa();//Modified Cocoa pods
		blockFruit = new BlockFruit();//Apple
		blockVerboseFire = new BlockVerboseFire();//Verbose Fire
		blockTrunkShell = new BlockTrunkShell();
		
		setupLeavesProperties();
	}
	
	
	public static void setupLeavesProperties() {
		
		oakLeavesProperties = new LeavesProperties(
				Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK),
				new ItemStack(Blocks.LEAVES, 1, BlockPlanks.EnumType.OAK.getMetadata() & 3),
				TreeRegistry.findCellKit("deciduous")).assign();
		
		spruceLeavesProperties = new LeavesProperties(
				Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE),
				new ItemStack(Blocks.LEAVES, 1, BlockPlanks.EnumType.SPRUCE.getMetadata() & 3),
				TreeRegistry.findCellKit("conifer")) {
					@Override
					public int getSmotherLeavesMax() {
						return 3;
					}
					
					@Override
					public int foliageColorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos) {
						int color = super.foliageColorMultiplier(state, world, pos);
						return world.getBiome(pos).getModdedBiomeFoliageColor(color);//Spruce can now be access by modded foliage multipliers
					}
					
				}.assign();
		
		birchLeavesProperties = new LeavesProperties(
				Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.BIRCH),
				new ItemStack(Blocks.LEAVES, 1, BlockPlanks.EnumType.BIRCH.getMetadata() & 3),
				TreeRegistry.findCellKit("deciduous") ) {
				
				@Override
				public int foliageColorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos) {
					int color = super.foliageColorMultiplier(state, world, pos);
					return world.getBiome(pos).getModdedBiomeFoliageColor(color);//Birch can now be access by modded foliage multipliers
				}
			}.assign();
		
		jungleLeavesProperties = new LeavesProperties(
				Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE),
				new ItemStack(Blocks.LEAVES, 1, BlockPlanks.EnumType.JUNGLE.getMetadata() & 3),
				TreeRegistry.findCellKit("deciduous")) {
				
				@Override
				public int getLightRequirement() {
					return 12;//The jungle can be a dark place.  Give these trees a little advantage.
				}
		}.assign();
		
		acaciaLeavesProperties = new LeavesProperties(
				Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.ACACIA),
				new ItemStack(Blocks.LEAVES2, 1, BlockPlanks.EnumType.ACACIA.getMetadata() & 3),
				TreeRegistry.findCellKit("acacia")) {
					@Override
					public int getSmotherLeavesMax() {
						return 2;//very thin canopy
					}
				}.assign();
		
		darkOakLeavesProperties = new LeavesProperties(
				Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.DARK_OAK),
				new ItemStack(Blocks.LEAVES2, 1, BlockPlanks.EnumType.DARK_OAK.getMetadata() & 3),
				TreeRegistry.findCellKit("darkoak")) {
					@Override
					public int getSmotherLeavesMax() {
						return 3;//thin canopy
					}
				}.assign();
		
		testLeavesProperties = new LeavesProperties(Blocks.BOOKSHELF.getDefaultState()) {
			public int foliageColorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos){
				return Minecraft.getMinecraft().getBlockColors().colorMultiplier(Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.DARK_OAK), world, pos, 0);
			}
		}.assign();
		
		cactusLeavesProperties = new LeavesProperties(null, ItemStack.EMPTY, TreeRegistry.findCellKit("bare"));//Explicitly not assigned
	}
	
	public static void registerBlocks(IForgeRegistry<Block> registry) {
		
		ArrayList<Block> treeBlocks = new ArrayList<Block>();
		ModTrees.baseFamilies.forEach(tree -> tree.getRegisterableBlocks(treeBlocks));
		ModTrees.dynamicCactus.getRegisterableBlocks(treeBlocks);
		treeBlocks.addAll(LeavesPaging.getLeavesMapForModId(ModConstants.MODID).values());

		registry.registerAll(
			blockRootyDirt,
			blockRootySand,
			blockRootyDirtSpecies,
			blockRootyDirtFake,
			blockDynamicSapling,
			blockDynamicSaplingSpecies,
			blockBonsaiPot,
			blockFruitCocoa,
			blockFruit,
			blockVerboseFire,
			blockTrunkShell
		);
		
		registry.registerAll(treeBlocks.toArray(new Block[0]));
	}

	public static class CommonBlockStates {
		public final IBlockState air = Blocks.AIR.getDefaultState();
		public final IBlockState dirt = Blocks.DIRT.getDefaultState();
		public final IBlockState sand = Blocks.SAND.getDefaultState();
		public final IBlockState grass = Blocks.GRASS.getDefaultState();
		public final IBlockState podzol = dirt.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);
		public final IBlockState redMushroom = Blocks.RED_MUSHROOM.getDefaultState();
		public final IBlockState brownMushroom = Blocks.BROWN_MUSHROOM.getDefaultState();
	}
	
}
