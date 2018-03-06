package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSaplingRare;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSaplingVanilla;
import com.ferreusveritas.dynamictrees.blocks.BlockFruit;
import com.ferreusveritas.dynamictrees.blocks.BlockFruitCocoa;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirtFake;
import com.ferreusveritas.dynamictrees.blocks.BlockRootySand;
import com.ferreusveritas.dynamictrees.blocks.BlockVerboseFire;
import com.ferreusveritas.dynamictrees.blocks.LeavesProperties;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
	
	public static LeavesProperties oakLeavesProperties;
	public static LeavesProperties spruceLeavesProperties;
	public static LeavesProperties birchLeavesProperties;
	public static LeavesProperties jungleLeavesProperties;
	public static LeavesProperties acaciaLeavesProperties;
	public static LeavesProperties darkOakLeavesProperties;
	public static LeavesProperties[] vanillaLeavesProperties;
	
	public static LeavesProperties cactusLeavesProperties;
	
	public static CommonBlockStates blockStates;
	
	public static void preInit() {
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
		
		oakLeavesProperties = new LeavesProperties(
				Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK),
				new ItemStack(Blocks.LEAVES, 1, BlockPlanks.EnumType.OAK.getMetadata() & 3),
				TreeRegistry.findCellKit("deciduous"));
		
		spruceLeavesProperties = new LeavesProperties(
				Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE),
				new ItemStack(Blocks.LEAVES, 1, BlockPlanks.EnumType.SPRUCE.getMetadata() & 3),
				TreeRegistry.findCellKit("conifer")) {
					@Override
					public int getSmotherLeavesMax() {
						return 3;
					}
					
					@Override
					@SideOnly(Side.CLIENT)
					public int foliageColorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos) {
						return ColorizerFoliage.getFoliageColorPine();
					}
				};
				
		birchLeavesProperties = new LeavesProperties(
				Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.BIRCH),
				new ItemStack(Blocks.LEAVES, 1, BlockPlanks.EnumType.BIRCH.getMetadata() & 3),
				TreeRegistry.findCellKit("deciduous") ) {
			@Override
			@SideOnly(Side.CLIENT)
			public int foliageColorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos) {
				return ColorizerFoliage.getFoliageColorBirch();
			}
		};
		
		jungleLeavesProperties = new LeavesProperties(
				Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE),
				new ItemStack(Blocks.LEAVES, 1, BlockPlanks.EnumType.JUNGLE.getMetadata() & 3),
				TreeRegistry.findCellKit("deciduous")) {
			
			@Override
			public int getLightRequirement() {
				return 12;//The jungle can be a dark place.  Give these trees a little advantage.
			}
		};
		
		acaciaLeavesProperties = new LeavesProperties(
				Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.ACACIA),
				new ItemStack(Blocks.LEAVES2, 1, BlockPlanks.EnumType.ACACIA.getMetadata() & 3),
				TreeRegistry.findCellKit("acacia")) {
					@Override
					public int getSmotherLeavesMax() {
						return 2;//very thin canopy
					}
				};
		
		darkOakLeavesProperties = new LeavesProperties(
				Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.DARK_OAK),
				new ItemStack(Blocks.LEAVES2, 1, BlockPlanks.EnumType.DARK_OAK.getMetadata() & 3),
				TreeRegistry.findCellKit("darkoak")) {
					@Override
					public int getSmotherLeavesMax() {
						return 3;//thin canopy
					}
				};
		
		vanillaLeavesProperties = new LeavesProperties[] {
				oakLeavesProperties,
				spruceLeavesProperties,
				birchLeavesProperties,
				jungleLeavesProperties,
				acaciaLeavesProperties,
				darkOakLeavesProperties
		};
		
		int seq = 0;
		
		for(LeavesProperties lp : vanillaLeavesProperties) {
			TreeHelper.getLeavesBlockForSequence(ModConstants.MODID, seq++, lp);
		}
		
		cactusLeavesProperties = new LeavesProperties(null, ItemStack.EMPTY, TreeRegistry.findCellKit("bare"));
		
	}
	
	public static void registerBlocks(IForgeRegistry<Block> registry) {
		
		ArrayList<Block> treeBlocks = new ArrayList<Block>();
		ModTrees.baseFamilies.forEach(tree -> tree.getRegisterableBlocks(treeBlocks));
		ModTrees.dynamicCactus.getRegisterableBlocks(treeBlocks);
		treeBlocks.addAll(TreeHelper.getLeavesMapForModId(ModConstants.MODID).values());

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
			blockVerboseFire
		);
		registry.registerAll(treeBlocks.toArray(new Block[0]));
		
		DynamicTrees.compatProxy.registerBlocks(registry);
	}

	public static class CommonBlockStates {
		public final IBlockState air = Blocks.AIR.getDefaultState();
		public final IBlockState dirt = Blocks.DIRT.getDefaultState();
		public final IBlockState podzol = dirt.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);
		public final IBlockState redMushroom = Blocks.RED_MUSHROOM.getDefaultState();
		public final IBlockState brownMushroom = Blocks.BROWN_MUSHROOM.getDefaultState();
	}
	
}
