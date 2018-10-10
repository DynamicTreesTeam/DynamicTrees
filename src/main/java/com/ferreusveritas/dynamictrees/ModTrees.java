package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranchThick;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.trees.Mushroom;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeAcacia;
import com.ferreusveritas.dynamictrees.trees.TreeBirch;
import com.ferreusveritas.dynamictrees.trees.TreeCactus;
import com.ferreusveritas.dynamictrees.trees.TreeDarkOak;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.trees.TreeFamilyVanilla;
import com.ferreusveritas.dynamictrees.trees.TreeJungle;
import com.ferreusveritas.dynamictrees.trees.TreeOak;
import com.ferreusveritas.dynamictrees.trees.TreeSpruce;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;

public class ModTrees {

	public static ArrayList<TreeFamilyVanilla> baseFamilies = new ArrayList<>();
	// keeping the cactus 'tree' out of baseTrees prevents automatic registration of seed/sapling conversion recipes, transformation potion recipes, and models
	public static TreeCactus dynamicCactus;

	public static TreeFamily testFamily;
	
	/**
	 * Pay Attn! This should be run after the Dynamic Trees Mod
	 * has created it's Blocks and Items.  These trees depend
	 * on the Dynamic Sapling
	 */
	public static void preInit() {
		Species.REGISTRY.register(Species.NULLSPECIES.setRegistryName(new ResourceLocation(ModConstants.MODID, "null")));
		Collections.addAll(baseFamilies, new TreeOak(), new TreeSpruce(), new TreeBirch(), new TreeJungle(), new TreeAcacia(), new TreeDarkOak());
		baseFamilies.forEach(tree -> tree.registerSpecies(Species.REGISTRY));
		dynamicCactus = new TreeCactus();
		dynamicCactus.registerSpecies(Species.REGISTRY);
		
		//Registers a fake species for generating mushrooms
		Species.REGISTRY.register(new Mushroom(true));
		Species.REGISTRY.register(new Mushroom(false));
		
		for(TreeFamilyVanilla vanillaFamily: baseFamilies) {
			TreeRegistry.registerSaplingReplacer(Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, vanillaFamily.woodType), vanillaFamily.getCommonSpecies());
		}
		
		testFamily = makeTestFamily();
	}
	
	public static TreeFamily makeTestFamily() {
		TreeFamily testFamily = new TreeFamily(new ResourceLocation(ModConstants.MODID, "test")) {
			
			@Override
			public void createSpecies() {
				// TODO Auto-generated method stub
				super.createSpecies();
			}
			
			@Override
			public BlockBranch createBranch() {
				return new BlockBranchThick(Material.WOOD, getName().getResourcePath() + "branch");
			}
			
			@Override
			public List<Block> getRegisterableBlocks(List<Block> blockList) {
				BlockBranchThick branch = (BlockBranchThick) getDynamicBranch();
				for(int i = 0; i < 2; i++) {
					boolean side = i != 0;
					BlockBranchThick b = branch.getPairSide(side);
					blockList.add(b);
					b.setCreativeTab(DynamicTrees.dynamicTreesTab);
				}
				return blockList;
			}
			
			@Override
			public List<Item> getRegisterableItems(List<Item> itemList) {
				BlockBranchThick branch = (BlockBranchThick) getDynamicBranch();
				
				//Register an itemBlock for the branch block
				itemList.add(new ItemBlock(branch.getPairSide(false)).setRegistryName(branch.getPairSide(false).getRegistryName()));
				itemList.add(new ItemBlock(branch.getPairSide(true)).setRegistryName(branch.getPairSide(true).getRegistryName()));
				
				Seed seed = getCommonSpecies().getSeed();
				if(seed != Seed.NULLSEED) {
					itemList.add(seed);
				}
				return itemList;
			}
		};

		testFamily.registerSpecies(Species.REGISTRY);
		
		return testFamily;
	}
	
}
