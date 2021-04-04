package com.ferreusveritas.dynamictrees.api.client;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.SurfaceRootBlock;
import com.ferreusveritas.dynamictrees.trees.Family;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ModelHelper {

	/**
	 * Registers models associated with the tree.
	 * At the moment this only deals with {@link BranchBlock} blocks
	 *
	 * @param tree
	 */
	public static void regModel(Family tree) {
		BranchBlock branchBlock = tree.getDynamicBranch();
		ModelResourceLocation modelLocation = getBranchModelResourceLocation(branchBlock);

		setGenericStateMapper(branchBlock, modelLocation);

		SurfaceRootBlock surfaceRoot = tree.getSurfaceRoot();
		if(surfaceRoot != null) {
	//		ModelLoader.setCustomStateMapper(surfaceRoot, new StateMap.Builder().ignore(surfaceRoot.getIgnorableProperties()).build());
		}
	}

	private static ModelResourceLocation getBranchModelResourceLocation(BranchBlock branchBlock) {
		ResourceLocation family = branchBlock.getFamily().getRegistryName();
		ResourceLocation resloc = new ResourceLocation(family.getNamespace(), family.getPath() + "branch");
		return new ModelResourceLocation(resloc, null);
	}

	public static void setGenericStateMapper(Block block, ModelResourceLocation modelLocation) {
//		ModelLoader.setCustomStateMapper(block, state -> {
//			return block.getStateContainer().getValidStates().stream().collect(Collectors.toMap(b -> b, b -> modelLocation));
//		});
	}

	public static void regModel(Block block) {
		if(block != Blocks.AIR) {
			regModel(Item.byBlock(block));
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
//			ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(customResourceLocation, "inventory"));
		}
	}

	public static void regColorHandler(Block block, IBlockColor blockColor) {
		Minecraft.getInstance().getBlockColors().register(blockColor, block);
	}

	public static void regColorHandler(Item item, IItemColor itemColor) {
		Minecraft.getInstance().getItemColors().register(itemColor, new Item[] {item});
	}

}
