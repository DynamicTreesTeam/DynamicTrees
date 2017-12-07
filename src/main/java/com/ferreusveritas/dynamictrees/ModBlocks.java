package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSaplingVanilla;
import com.ferreusveritas.dynamictrees.blocks.BlockFruitCocoa;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = ModConstants.MODID)
public class ModBlocks {
	
	public static BlockRootyDirt blockRootyDirt;
	public static BlockDynamicSapling blockDynamicSapling;
	public static BlockFruitCocoa blockFruitCocoa;
	public static BlockBonsaiPot blockBonsaiPot;
	
	public static void preInit() {
	
		//Dirt
		blockRootyDirt = new BlockRootyDirt();
		
		//Dynamic version of a Vanilla sapling
		blockDynamicSapling = new BlockDynamicSaplingVanilla("sapling");
		
		//Bonsai Pot
		blockBonsaiPot = new BlockBonsaiPot();
		
		//Fruit
		blockFruitCocoa = new BlockFruitCocoa();
	
	}
	
	@SubscribeEvent
	public static void registerBlocks(final RegistryEvent.Register<Block> event) {
		final IForgeRegistry<Block> registry = event.getRegistry();

		registry.register(ModBlocks.blockRootyDirt);
		registry.register(ModBlocks.blockDynamicSapling);
		registry.register(ModBlocks.blockBonsaiPot);
		registry.register(ModBlocks.blockFruitCocoa);

		ArrayList<Block> treeBlocks = new ArrayList<Block>();

		for(DynamicTree tree: ModTrees.baseTrees) {
			tree.getRegisterableBlocks(treeBlocks);
		}

		for(Block block: treeBlocks) {
			registry.register(block);
		}
		
		for(BlockDynamicLeaves leavesBlock: TreeHelper.getLeavesMapForModId(ModConstants.MODID).values()) {
			registry.register(leavesBlock);
		}

		DynamicTrees.compatProxy.registerBlocks(event);
	}
	
}
