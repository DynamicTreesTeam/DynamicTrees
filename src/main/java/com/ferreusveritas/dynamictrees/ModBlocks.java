package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSaplingRare;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSaplingVanilla;
import com.ferreusveritas.dynamictrees.blocks.BlockFruit;
import com.ferreusveritas.dynamictrees.blocks.BlockFruitCocoa;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirtSpecies;
import com.ferreusveritas.dynamictrees.blocks.BlockVerboseFire;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks {
	
	public static BlockRootyDirt blockRootyDirt;
	public static BlockRootyDirt blockRootyDirtSpecies;
	public static BlockDynamicSapling blockDynamicSapling;
	public static BlockDynamicSaplingRare blockDynamicSaplingSpecies;
	public static BlockFruit blockFruit;
	public static BlockFruitCocoa blockFruitCocoa;
	public static BlockBonsaiPot blockBonsaiPot;
	public static BlockVerboseFire blockVerboseFire;
	
	public static CommonBlockStates blockStates;
	
	public static void preInit() {
		blockStates = new CommonBlockStates();
		
		blockRootyDirt = new BlockRootyDirt();//Dirt
		blockRootyDirtSpecies = new BlockRootyDirtSpecies();//Special dirt for rarer species
		blockDynamicSapling = new BlockDynamicSaplingVanilla("sapling");//Dynamic version of a Vanilla sapling
		blockDynamicSaplingSpecies = new BlockDynamicSaplingRare("saplingrare");//Species extended sapling(Apple)
		blockBonsaiPot = new BlockBonsaiPot();//Bonsai Pot
		blockFruitCocoa = new BlockFruitCocoa();//Modified Cocoa pods
		blockFruit = new BlockFruit();//Apple
		blockVerboseFire = new BlockVerboseFire();//Verbose Fire
	}
	
	public static void registerBlocks() {
		
		ArrayList<Block> treeBlocks = new ArrayList<Block>();
		ModTrees.baseTrees.forEach(tree -> tree.getRegisterableBlocks(treeBlocks));
		treeBlocks.addAll(TreeHelper.getLeavesMapForModId(ModConstants.MODID).values());

		GameRegistry.register(blockRootyDirt);
		GameRegistry.register(blockRootyDirtSpecies);
		GameRegistry.register(blockDynamicSapling);
		GameRegistry.register(blockDynamicSaplingSpecies);
		GameRegistry.register(blockBonsaiPot);
		GameRegistry.register(blockFruitCocoa);
		GameRegistry.register(blockFruit);
		GameRegistry.register(blockVerboseFire);
		
		for(Block block : treeBlocks) {
			GameRegistry.register(block);
		}
		
		DynamicTrees.compatProxy.registerBlocks();
	}

	public static class CommonBlockStates {
		public final IBlockState dirt = Blocks.DIRT.getDefaultState();
		public final IBlockState podzol = dirt.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);
		public final IBlockState redMushroom = Blocks.RED_MUSHROOM.getDefaultState();
		public final IBlockState brownMushroom = Blocks.BROWN_MUSHROOM.getDefaultState();
	}
	
}
