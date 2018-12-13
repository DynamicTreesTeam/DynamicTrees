package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.blocks.BlockFruit;
import com.ferreusveritas.dynamictrees.blocks.BlockFruitCocoa;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirtFake;
import com.ferreusveritas.dynamictrees.blocks.BlockRootySand;
import com.ferreusveritas.dynamictrees.blocks.BlockTrunkShell;
import com.ferreusveritas.dynamictrees.blocks.LeavesPaging;
import com.ferreusveritas.dynamictrees.blocks.LeavesProperties;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks {
	
	public static BlockRooty blockRootyDirt;
	public static BlockRooty blockRootySand;
	public static BlockRooty blockRootyDirtSpecies;
	public static Block blockRootyDirtFake;
	public static BlockDynamicSapling blockDynamicSapling;
	public static BlockFruit blockApple;
	public static BlockFruitCocoa blockFruitCocoa;
	public static BlockBonsaiPot blockBonsaiPot;
	public static BlockTrunkShell blockTrunkShell;
	
	public static Block experimental;
	
	public static Map<String, ILeavesProperties> leaves = new HashMap<>();
	
	public static CommonBlockStates blockStates;
	
	public static void preInit() {
		BlockDynamicLeaves.passableLeavesModLoaded = net.minecraftforge.fml.common.Loader.isModLoaded("passableleaves");
		
		blockRootyDirt = new BlockRootyDirt(false);//Dirt
		blockRootySand = new BlockRootySand(false);//Sand
		blockRootyDirtSpecies = new BlockRootyDirt(true);//Special dirt for rarer species
		blockRootyDirtFake = new BlockRootyDirtFake("rootydirtfake");
		blockDynamicSapling = new BlockDynamicSapling("sapling");//Dynamic version of a Vanilla sapling
		blockBonsaiPot = new BlockBonsaiPot();//Bonsai Pot
		blockFruitCocoa = new BlockFruitCocoa();//Modified Cocoa pods
		blockApple = new BlockFruit().setDroppedItem(new ItemStack(Items.APPLE));//Apple
		blockTrunkShell = new BlockTrunkShell();
		
		blockStates = new CommonBlockStates();
		
		experimental = new Block(Material.WOOD)
				.setCreativeTab(DynamicTrees.dynamicTreesTab)
				.setRegistryName(new ResourceLocation(ModConstants.MODID, "experimental"))
				.setUnlocalizedName("experimental");
		
		setupLeavesProperties();
	}
	
	public static void setupLeavesProperties() {
		leaves = LeavesPaging.build(new ResourceLocation(ModConstants.MODID, "leaves/common.json"));
		leaves.put("cactus", new LeavesProperties(null, ItemStack.EMPTY, TreeRegistry.findCellKit("bare")));//Explicitly unbuilt since there's no leaves
	}
	
	public static void register(IForgeRegistry<Block> registry) {
		
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
			blockBonsaiPot,
			blockFruitCocoa,
			blockApple,
			blockTrunkShell,
			experimental
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
