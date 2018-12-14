package com.ferreusveritas.dynamictrees.api.client;

import java.util.stream.Collectors;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranchThick;
import com.ferreusveritas.dynamictrees.blocks.BlockSurfaceRoot;
import com.ferreusveritas.dynamictrees.models.experimental.ModelResourceLocationWrapped;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelHelper {

	/**
	 * Registers models associated with the tree.
	 * At the moment this only deals with {@link BlockBranch} blocks 
	 * 
	 * @param tree
	 */
	public static void regModel(TreeFamily tree) {
		
		if(tree.autoCreateBranch()) {
			BlockBranch branch = tree.getDynamicBranch();
			setBranchForAutoCreate(branch);
			if(branch instanceof BlockBranchThick) {
				setBranchForAutoCreate(((BlockBranchThick) branch).otherBlock);
			}
		} else {
			BlockBranch branch = tree.getDynamicBranch();
			setBranchForManualCreate(branch);
			if(branch instanceof BlockBranchThick) {
				setBranchForManualCreate(((BlockBranchThick) branch).otherBlock);
			}
		}
		
		BlockSurfaceRoot surfaceRoot = tree.getSurfaceRoots();
		if(surfaceRoot != null) {
			ModelLoader.setCustomStateMapper(surfaceRoot, new StateMap.Builder().ignore(surfaceRoot.getIgnorableProperties()).build());
		}
	}
	
	public static void setBranchForAutoCreate(BlockBranch blockBranch) {
		ModelLoader.setCustomStateMapper(blockBranch, state -> {
			ModelResourceLocationWrapped resloc = new ModelResourceLocationWrapped(new ResourceLocation(ModConstants.MODID, "branch"), blockBranch.getDefaultState());
			return blockBranch.getBlockState().getValidStates().stream().collect(Collectors.toMap(b -> b, b -> resloc));
		});
	}
	
	public static void setBranchForManualCreate(BlockBranch blockBranch) {
		ModelLoader.setCustomStateMapper( blockBranch, new StateMap.Builder().ignore(blockBranch.getIgnorableProperties()).build() );
	}
	
	public static void regModel(Block block) {
		if(block != Blocks.AIR) {
			regModel(Item.getItemFromBlock(block));
		}
		if (block instanceof BlockBranchThick) {
			Item item = Item.getItemFromBlock(((BlockBranchThick) block).otherBlock);
			regModel(item, 0, block.getRegistryName());
		}
	}
	
	public static void regModel(Item item) {
		regModel(item, 0);
	}
	
	public static void regModel(Item item, int meta) {
		regModel(item, meta, item.getRegistryName());
	}
	
	public static void regModel(Item item, int meta, ResourceLocation customResourceLocation) {
		if(item != null) {
			ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(customResourceLocation, "inventory"));
		}
	}
	
	public static void regColorHandler(Block block, IBlockColor blockColor) {
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(blockColor, new Block[] {block});
	}

	public static void regColorHandler(Item item, IItemColor itemColor) {
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(itemColor, new Item[] {item});
	}

}
