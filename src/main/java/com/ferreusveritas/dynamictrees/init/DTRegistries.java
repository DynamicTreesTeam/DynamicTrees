package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.BonsaiPotBlock;
import com.ferreusveritas.dynamictrees.blocks.CocoaFruitBlock;
import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyWaterBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SpreadableRootyBlock;
import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.entities.LingeringEffectorEntity;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKits;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.items.DirtBucket;
import com.ferreusveritas.dynamictrees.items.Staff;
import com.ferreusveritas.dynamictrees.systems.BranchConnectables;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.RootyBlockHelper;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import com.ferreusveritas.dynamictrees.systems.substances.GrowthSubstance;
import com.ferreusveritas.dynamictrees.tileentity.BonsaiTileEntity;
import com.ferreusveritas.dynamictrees.tileentity.SpeciesTileEntity;
import com.ferreusveritas.dynamictrees.worldgen.DynamicTreeFeature;
import com.google.common.collect.Sets;
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
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.LinkedList;
import java.util.Set;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTRegistries {
	
	///////////////////////////////////////////
	// BLOCKS
	///////////////////////////////////////////

	public static FruitBlock appleBlock;
	public static CocoaFruitBlock cocoaFruitBlock;
	public static BonsaiPotBlock bonsaiPotBlock;
	public static TrunkShellBlock trunkShellBlock;

	public static final CommonBlockStates blockStates = new CommonBlockStates();
	
	public static void setupBlocks() {
		bonsaiPotBlock = new BonsaiPotBlock();//Bonsai Pot
		cocoaFruitBlock = new CocoaFruitBlock();//Modified Cocoa pods
		appleBlock = new FruitBlock().setDroppedItem(new ItemStack(Items.APPLE));//Apple
		trunkShellBlock = new TrunkShellBlock();

		setUpSoils();
		setupConnectables();
	}

	private static void setUpSoils(){
		DirtHelper.registerSoil(Blocks.GRASS_BLOCK, DirtHelper.DIRT_LIKE);
		DirtHelper.registerSoil(Blocks.MYCELIUM, DirtHelper.DIRT_LIKE);
		DirtHelper.registerSoil(Blocks.DIRT, DirtHelper.DIRT_LIKE, new SpreadableRootyBlock(Blocks.DIRT, 9, Blocks.GRASS_BLOCK, Blocks.MYCELIUM));
		DirtHelper.registerSoil(Blocks.COARSE_DIRT, DirtHelper.DIRT_LIKE);
		DirtHelper.registerSoil(Blocks.PODZOL, DirtHelper.DIRT_LIKE);
		DirtHelper.registerSoil(Blocks.FARMLAND, DirtHelper.DIRT_LIKE, Blocks.DIRT);
		DirtHelper.registerSoil(Blocks.SAND, DirtHelper.SAND_LIKE);
		DirtHelper.registerSoil(Blocks.RED_SAND, DirtHelper.SAND_LIKE);
		DirtHelper.registerSoil(Blocks.GRAVEL, DirtHelper.GRAVEL_LIKE);
		DirtHelper.registerSoil(Blocks.WATER, DirtHelper.WATER_LIKE, new RootyWaterBlock(Blocks.WATER));
		DirtHelper.registerSoil(Blocks.MYCELIUM, DirtHelper.FUNGUS_LIKE);
		DirtHelper.registerSoil(Blocks.CRIMSON_NYLIUM, DirtHelper.FUNGUS_LIKE);
		DirtHelper.registerSoil(Blocks.WARPED_NYLIUM, DirtHelper.FUNGUS_LIKE);
		DirtHelper.registerSoil(Blocks.NETHERRACK, DirtHelper.NETHER_LIKE, new SpreadableRootyBlock(Blocks.NETHERRACK, Items.BONE_MEAL, Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM));
		DirtHelper.registerSoil(Blocks.SOUL_SAND, DirtHelper.NETHER_LIKE);
		DirtHelper.registerSoil(Blocks.SOUL_SOIL, DirtHelper.NETHER_LIKE);
		DirtHelper.registerSoil(Blocks.CRIMSON_NYLIUM, DirtHelper.NETHER_LIKE);
		DirtHelper.registerSoil(Blocks.WARPED_NYLIUM, DirtHelper.NETHER_LIKE);
		DirtHelper.registerSoil(Blocks.SOUL_SOIL, DirtHelper.NETHER_SOIL_LIKE);
		DirtHelper.registerSoil(Blocks.CRIMSON_NYLIUM, DirtHelper.NETHER_SOIL_LIKE);
		DirtHelper.registerSoil(Blocks.WARPED_NYLIUM, DirtHelper.NETHER_SOIL_LIKE);
		DirtHelper.registerSoil(Blocks.END_STONE, DirtHelper.END_LIKE);
	}

	public static void setupConnectables(){
		BranchConnectables.makeBlockConnectable(Blocks.BEE_NEST, (state,world,pos,side)->{
			if (side == Direction.DOWN) return 1;
			return 0;
		});
		BranchConnectables.makeBlockConnectable(Blocks.SHROOMLIGHT, (state,world,pos,side)->{
			if (side == Direction.DOWN){
				BlockState branchState = world.getBlockState(pos.offset(Direction.UP));
				BranchBlock branch = TreeHelper.getBranch(branchState);
				if (branch != null){
					return MathHelper.clamp(branch.getRadius(branchState) - 1, 1, 8);
				} else {
					return 8;
				}
			}
			return 0;
		});
	}

	@SubscribeEvent
	public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
		IForgeRegistry<Block> registry = blockRegistryEvent.getRegistry();
		
		Set<Block> treeBlocks = Sets.newHashSet();
		DTTrees.FAMILIES.forEach(tree -> tree.getRegisterableBlocks(treeBlocks));

		registry.registerAll(bonsaiPotBlock, cocoaFruitBlock, appleBlock, trunkShellBlock);
		
		registry.registerAll(treeBlocks.toArray(new Block[0]));

		for (RootyBlock rooty : RootyBlockHelper.generateListForRegistry(false, DynamicTrees.MOD_ID)){
			registry.register(rooty);
		}
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
		
		Set<Item> treeItems = Sets.newHashSet();
		DTTrees.FAMILIES.forEach(tree -> tree.getRegisterableItems(treeItems));

		registry.registerAll(dendroPotion, dirtBucket, treeStaff);
		registry.registerAll(treeItems.toArray(new Item[0]));
	}
	
	///////////////////////////////////////////
	// ENTITIES
	///////////////////////////////////////////
	
	public final static String FALLING_TREE = "falling_tree";
	public final static String LINGERING_EFFECTOR = "lingering_effector";
	
	public static EntityType<FallingTreeEntity> fallingTree;
	public static EntityType<LingeringEffectorEntity> lingeringEffector;

	public static void setupEntities() {
		fallingTree = EntityType.Builder.create(FallingTreeEntity::new, EntityClassification.MISC)
				.setShouldReceiveVelocityUpdates(true)
				.setTrackingRange(512)
				.setUpdateInterval(Integer.MAX_VALUE)
				.setCustomClientFactory((spawnEntity, world) -> new FallingTreeEntity(fallingTree, world))
				.build(FALLING_TREE);

		lingeringEffector = EntityType.Builder.<LingeringEffectorEntity>create(LingeringEffectorEntity::new, EntityClassification.MISC)
				// Giving it growth substance works for now as it's the only lingering substance, however in the future this should be changed in the future.
				.setCustomClientFactory((spawnEntity, world) ->
						new LingeringEffectorEntity(world, new BlockPos(spawnEntity.getPosX(), spawnEntity.getPosY(), spawnEntity.getPosZ()), new GrowthSubstance()))
				.build(LINGERING_EFFECTOR);
	}
	
	@SubscribeEvent
	public static void onEntitiesRegistry(final RegistryEvent.Register<EntityType<?>> entityRegistryEvent) {
		setupEntities();
		
		IForgeRegistry<EntityType<?>> registry = entityRegistryEvent.getRegistry();
		
		registry.registerAll(fallingTree.setRegistryName(new ResourceLocation(DynamicTrees.MOD_ID, FALLING_TREE)),
				lingeringEffector.setRegistryName(new ResourceLocation(DynamicTrees.MOD_ID, LINGERING_EFFECTOR)));
	}
	
	///////////////////////////////////////////
	// TILE ENTITIES
	///////////////////////////////////////////
	
	public static TileEntityType<SpeciesTileEntity> speciesTE;
	public static TileEntityType<BonsaiTileEntity> bonsaiTE;
	
	public static void setupTileEntities() {
		LinkedList<RootyBlock> rootyDirts = RootyBlockHelper.generateListForRegistry(false);
		speciesTE = TileEntityType.Builder.create(SpeciesTileEntity::new, rootyDirts.toArray(new RootyBlock[0])).build(null);
		bonsaiTE = TileEntityType.Builder.create(BonsaiTileEntity::new, bonsaiPotBlock).build(null);
	}
	
	@SubscribeEvent
	public static void onTileEntitiesRegistry(final RegistryEvent.Register<TileEntityType<?>> tileEntityRegistryEvent) {
		setupTileEntities();
		
		tileEntityRegistryEvent.getRegistry().register(bonsaiTE.setRegistryName(bonsaiPotBlock.getRegistryName()));
		tileEntityRegistryEvent.getRegistry().register(speciesTE.setRegistryName(new ResourceLocation(DynamicTrees.MOD_ID, "tile_entity_species")));
	}
	
	///////////////////////////////////////////
	// MISC
	///////////////////////////////////////////

	/** This is the creative tab that holds all DT items */
	public static final ItemGroup dynamicTreesTab = new ItemGroup(DynamicTrees.MOD_ID) {
		@Override
		public ItemStack createIcon() {
			return TreeRegistry.findSpeciesSloppy(DynamicTrees.VanillaWoodTypes.oak.toString()).getSeedStack(1);
		}
	};
	
	public static final class CommonBlockStates {
		public final BlockState AIR = Blocks.AIR.getDefaultState();
		public final BlockState DIRT = Blocks.DIRT.getDefaultState();
		public final BlockState COARSE_DIRT = Blocks.COARSE_DIRT.getDefaultState();
		public final BlockState SAND = Blocks.SAND.getDefaultState();
		public final BlockState GRASS = Blocks.GRASS.getDefaultState();
		public final BlockState PODZOL = Blocks.PODZOL.getDefaultState();
		public final BlockState RED_MUSHROOM = Blocks.RED_MUSHROOM.getDefaultState();
		public final BlockState BROWN_MUSHROOM = Blocks.BROWN_MUSHROOM.getDefaultState();
	}

	public static final DynamicTreeFeature DYNAMIC_TREE_FEATURE = new DynamicTreeFeature();

	@SubscribeEvent
	public static void onFeatureRegistry (final RegistryEvent.Register<Feature<?>> event) {
		event.getRegistry().register(DYNAMIC_TREE_FEATURE);
	}

	@SubscribeEvent
	public static void onGrowthLogicKitRegistry (final RegistryEvent.Register<GrowthLogicKit> event) {
		GrowthLogicKits.register(event.getRegistry());
	}

	@SubscribeEvent
	public static void onGenFeatureRegistry (final RegistryEvent.Register<GenFeature> event) {
		GenFeatures.register(event.getRegistry());
	}

}
