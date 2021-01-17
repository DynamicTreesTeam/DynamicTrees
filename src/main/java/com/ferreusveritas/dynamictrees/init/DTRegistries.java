package com.ferreusveritas.dynamictrees.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.RootyBlockHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.BonsaiPotBlock;
import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import com.ferreusveritas.dynamictrees.blocks.CocoaFruitBlock;
import com.ferreusveritas.dynamictrees.blocks.RootyBlock;
import com.ferreusveritas.dynamictrees.blocks.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.blocks.LeavesPaging;
import com.ferreusveritas.dynamictrees.blocks.LeavesProperties;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.items.DirtBucket;
import com.ferreusveritas.dynamictrees.items.Staff;
import com.ferreusveritas.dynamictrees.tileentity.BonsaiTileEntity;
import com.ferreusveritas.dynamictrees.tileentity.SpeciesTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTRegistries {
	
	///////////////////////////////////////////
	// BLOCKS
	///////////////////////////////////////////

	public static FruitBlock blockApple;
	public static CocoaFruitBlock cocoaFruitBlock;
	public static BonsaiPotBlock bonsaiPotBlock;
	public static TrunkShellBlock trunkShellBlock;
	
	public static Map<String, ILeavesProperties> leaves = new HashMap<>();
	
	public static final CommonBlockStates blockStates = new CommonBlockStates();
	
	public static void setupBlocks() {
		bonsaiPotBlock = new BonsaiPotBlock();//Bonsai Pot
		cocoaFruitBlock = new CocoaFruitBlock();//Modified Cocoa pods
		blockApple = new FruitBlock().setDroppedItem(new ItemStack(Items.APPLE));//Apple
		trunkShellBlock = new TrunkShellBlock();
		
		setupLeavesProperties();
	}
	
	public static void setupLeavesProperties() {
		leaves = LeavesPaging.build(new ResourceLocation(DynamicTrees.MODID, "leaves/common.json"));
		leaves.put("cactus", new LeavesProperties(null, ItemStack.EMPTY, TreeRegistry.findCellKit("bare")));//Explicitly unbuilt since there's no leaves
	}
	
	@SubscribeEvent
	public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
		IForgeRegistry<Block> registry = blockRegistryEvent.getRegistry();
		
		ArrayList<Block> treeBlocks = new ArrayList<Block>();
		DTTrees.baseFamilies.forEach(tree -> tree.getRegisterableBlocks(treeBlocks));
		DTTrees.dynamicCactus.getRegisterableBlocks(treeBlocks);
		treeBlocks.addAll(LeavesPaging.getLeavesListForModId(DynamicTrees.MODID));
		
		for (RootyBlock rooty : RootyBlockHelper.generateListForRegistry(false)){
			registry.register(rooty);
		}
		
		registry.registerAll(bonsaiPotBlock, cocoaFruitBlock, blockApple, trunkShellBlock);
		
		registry.registerAll(treeBlocks.toArray(new Block[0]));
		
	}
	
	///////////////////////////////////////////
	// ITEMS
	///////////////////////////////////////////
	
	public static DendroPotion dendroPotion;
	public static DirtBucket dirtBucket;
	public static Staff treeStaff;
	
	public static void setupItems() {
		dendroPotion = new DendroPotion();//Potions
		dirtBucket = new DirtBucket();//Dirt Bucket
		treeStaff = new Staff();//Creative Mode Staff
	}
	
	@SubscribeEvent
	public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
		IForgeRegistry<Item> registry = itemRegistryEvent.getRegistry();
		
		ArrayList<Item> treeItems = new ArrayList<>();
		DTTrees.baseFamilies.forEach(tree -> tree.getRegisterableItems(treeItems));
		DTTrees.dynamicCactus.getRegisterableItems(treeItems);
		
		registry.registerAll(dendroPotion, dirtBucket, treeStaff);
		registry.registerAll(treeItems.toArray(new Item[0]));
	}
	
	///////////////////////////////////////////
	// ENTITIES
	///////////////////////////////////////////
	
	public final static String FALLING_TREE = "falling_tree";
	
	public static EntityType<EntityFallingTree> fallingTree;
	
	public static void setupEntities() {
		fallingTree = EntityType.Builder.create(EntityFallingTree::new, EntityClassification.MISC)
				.setShouldReceiveVelocityUpdates(true)
				.setTrackingRange(512)
				.setUpdateInterval(Integer.MAX_VALUE)
				.setCustomClientFactory((spawnEntity, world) -> new EntityFallingTree(fallingTree, world))
				.build(FALLING_TREE);
	}
	
	@SubscribeEvent
	public static void onEntitiesRegistry(final RegistryEvent.Register<EntityType<?>> entityRegistryEvent) {
		setupEntities();
		
		IForgeRegistry<EntityType<?>> registry = entityRegistryEvent.getRegistry();
		
		registry.register(fallingTree.setRegistryName(new ResourceLocation(DynamicTrees.MODID, FALLING_TREE)));
	}
	
	///////////////////////////////////////////
	// TILE ENTITIES
	///////////////////////////////////////////
	
	public static TileEntityType<SpeciesTileEntity> speciesTE;
	public static TileEntityType<BonsaiTileEntity> bonsaiTE;
	
	public static void setupTileEntities() {
		LinkedList<RootyBlock> rootyDirts = RootyBlockHelper.generateListForRegistry(false);
		System.out.println(rootyDirts.get(0));
		speciesTE = TileEntityType.Builder.create(SpeciesTileEntity::new, rootyDirts.toArray(new RootyBlock[0])).build(null);
		bonsaiTE = TileEntityType.Builder.create(BonsaiTileEntity::new, bonsaiPotBlock).build(null);
	}
	
	@SubscribeEvent
	public static void onTileEntitiesRegistry(final RegistryEvent.Register<TileEntityType<?>> tileEntityRegistryEvent) {
		setupTileEntities();
		
		tileEntityRegistryEvent.getRegistry().register(bonsaiTE.setRegistryName(bonsaiPotBlock.getRegistryName()));
		tileEntityRegistryEvent.getRegistry().register(speciesTE.setRegistryName(new ResourceLocation(DynamicTrees.MODID, "tile_entity_species")));
	}
	
	///////////////////////////////////////////
	// MISC
	///////////////////////////////////////////

	/** This is the creative tab that holds all DT items */
	public static final ItemGroup dynamicTreesTab = new ItemGroup("dynamictrees") {
		@Override
		public ItemStack createIcon() {
			return TreeRegistry.findSpeciesSloppy(DynamicTrees.VanillaWoodTypes.oak.toString()).getSeedStack(1);
		}
	};
	
	public static final class CommonBlockStates {
		public final BlockState air = Blocks.AIR.getDefaultState();
		public final BlockState dirt = Blocks.DIRT.getDefaultState();
		public final BlockState coarsedirt = Blocks.COARSE_DIRT.getDefaultState();
		public final BlockState sand = Blocks.SAND.getDefaultState();
		public final BlockState grass = Blocks.GRASS.getDefaultState();
		public final BlockState podzol = Blocks.PODZOL.getDefaultState();
		public final BlockState redMushroom = Blocks.RED_MUSHROOM.getDefaultState();
		public final BlockState brownMushroom = Blocks.BROWN_MUSHROOM.getDefaultState();
	}
	
}
