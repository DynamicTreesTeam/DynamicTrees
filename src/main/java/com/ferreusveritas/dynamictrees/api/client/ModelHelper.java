package com.ferreusveritas.dynamictrees.api.client;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
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
	public static void regModel(DynamicTree tree) {
		ModelLoader.setCustomStateMapper(tree.getDynamicBranch(), new StateMap.Builder().ignore(BlockBranch.RADIUS).build());
	}	
	
	public static void regModel(Block block) {
		if(block != Blocks.AIR) {
			regModel(Item.getItemFromBlock(block));
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

	//Register Sapling Colorizer
	public static void regDynamicSaplingColorHandler(BlockDynamicSapling sapling) {
		regColorHandler(sapling, new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess access, BlockPos pos, int tintIndex) {
				return sapling.getSpecies(access, pos, state).getTree().foliageColorMultiplier(state, access, pos);
			}
		});
	}
	
	public static void regColorHandler(Block block, IBlockColor blockColor) {
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(blockColor, new Block[] {block});
	}

	public static void regColorHandler(Item item, IItemColor itemColor) {
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(itemColor, new Item[] {item});
	}

}
