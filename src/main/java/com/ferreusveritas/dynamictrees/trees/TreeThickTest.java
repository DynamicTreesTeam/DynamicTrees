package com.ferreusveritas.dynamictrees.trees;

import java.util.List;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranchThick;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;

public class TreeThickTest extends TreeFamily {
	
	public TreeThickTest() {
		super(new ResourceLocation(ModConstants.MODID, "test"));

		setPrimitiveLog(Blocks.BOOKSHELF.getDefaultState());
		
		ModBlocks.testLeavesProperties.setTree(this);
	}
	
	@Override
	public void createSpecies() {
		Species species = new Species(this.getName(), this, ModBlocks.testLeavesProperties) {
			{
				setBasicGrowingParameters(0.9f, 14.0f, 4, 4, 1.0f);
				setupStandardSeedDropping();
				setDynamicSapling(new BlockDynamicSapling("testsapling").getDefaultState());
				

				generateSeed();
			}
		};
		
		setCommonSpecies(species);
	}
	
	@Override
	public BlockBranch createBranch() {
		return new BlockBranchThick(Material.WOOD, getName().getResourcePath() + "branch");
	}
	
	@Override
	public List<Block> getRegisterableBlocks(List<Block> blockList) {
		BlockBranchThick branch = (BlockBranchThick) getDynamicBranch();
		for(int i = 0; i < 2; i++) {
			BlockBranchThick b = branch.getPairSide(i == 1);
			blockList.add(b);
			b.setCreativeTab(DynamicTrees.dynamicTreesTab);
		}
		
		blockList.add(getCommonSpecies().getDynamicSapling().getBlock());
		
		return blockList;
	}
	
	@Override
	public List<Item> getRegisterableItems(List<Item> itemList) {
		BlockBranchThick branch = (BlockBranchThick) getDynamicBranch();
		
		//Register an itemBlock for the branch block
		itemList.add(new ItemBlock(branch.getPairSide(false)).setRegistryName(branch.getPairSide(false).getRegistryName()));
		itemList.add(new ItemBlock(branch.getPairSide(true )).setRegistryName(branch.getPairSide(true ).getRegistryName()));
		
		//Register seed item
		itemList.add(getCommonSpecies().getSeed());
		
		return itemList;
	}

}
