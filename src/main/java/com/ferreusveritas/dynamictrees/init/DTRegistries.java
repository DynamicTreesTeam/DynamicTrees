package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.blocks.BonsaiPotBlock;
import com.ferreusveritas.dynamictrees.blocks.CocoaFruitBlock;
import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyWaterBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SpreadableRootyBlock;
import com.ferreusveritas.dynamictrees.cells.CellKits;
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
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.worldgen.DynamicTreeFeature;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.LinkedList;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTRegistries {
	
	///////////////////////////////////////////
	// BLOCKS
	///////////////////////////////////////////

	/** An apple fruit block. */
	public static final FruitBlock APPLE_FRUIT = new FruitBlock().setDroppedItem(new ItemStack(Items.APPLE));

	/** A modified cocoa fruit block (for dynamic trees). */
	public static final CocoaFruitBlock COCOA_FRUIT = new CocoaFruitBlock();

	/** A bonsai pot block, which is a normal pot but for dynamic saplings. */
	public static final BonsaiPotBlock BONSAI_POT = new BonsaiPotBlock();

	/** A trunk shell block, which is the outer block for thick branches. */
	public static final TrunkShellBlock TRUNK_SHELL = new TrunkShellBlock();

	public static final CommonBlockStates BLOCK_STATES = new CommonBlockStates();
	
	public static void setup() {
		setupBlocks();
		setUpSoils();
		setupConnectables();
		setupItems();
		setupEntities();
	}

	public static void setupBlocks() {
		RegistryHandler.addBlock(DynamicTrees.resLoc("apple_fruit"), APPLE_FRUIT);
		RegistryHandler.addBlock(DynamicTrees.resLoc("cocoa_fruit"), COCOA_FRUIT);
		RegistryHandler.addBlock(BonsaiPotBlock.REG_NAME, BONSAI_POT);
		RegistryHandler.addBlock(DynamicTrees.resLoc("trunk_shell"), TRUNK_SHELL);
	}

	private static void setUpSoils() {
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

	public static void setupConnectables() {
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
	public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
		final Species appleOak = Species.REGISTRY.get(DynamicTrees.resLoc("apple_oak"));

		if (appleOak.isValid())
			APPLE_FRUIT.setSpecies(appleOak);

		for (RootyBlock rooty : RootyBlockHelper.generateListForRegistry(false, DynamicTrees.MOD_ID)){
			event.getRegistry().register(rooty);
		}
	}
	
	///////////////////////////////////////////
	// ITEMS
	///////////////////////////////////////////

	/** A custom potion called the Dendro Potion, houses all tree potions. */
	public static final DendroPotion DENDRO_POTION = new DendroPotion();

	/** A bucket of dirt item, for crafting saplings into seeds and vice versa. */
	public static final DirtBucket DIRT_BUCKET = new DirtBucket();

	/** A staff, a creative tool for copying and pasting tree shapes. */
	public static final Staff STAFF = new Staff();

	public static void setupItems() {
		RegistryHandler.addItem(DynamicTrees.resLoc("dendro_potion"), DENDRO_POTION);
		RegistryHandler.addItem(DynamicTrees.resLoc("dirt_bucket"), DIRT_BUCKET);
		RegistryHandler.addItem(DynamicTrees.resLoc("staff"), STAFF);
	}

	///////////////////////////////////////////
	// ENTITIES
	///////////////////////////////////////////
	
	public final static String FALLING_TREE_ID = "falling_tree";
	public final static String LINGERING_EFFECTOR_ID = "lingering_effector";
	
	public static EntityType<FallingTreeEntity> fallingTree;
	public static EntityType<LingeringEffectorEntity> lingeringEffector;

	private static void setupEntities() {
		fallingTree = EntityType.Builder.create(FallingTreeEntity::new, EntityClassification.MISC)
				.setShouldReceiveVelocityUpdates(true)
				.setTrackingRange(512)
				.setUpdateInterval(Integer.MAX_VALUE)
				.setCustomClientFactory((spawnEntity, world) -> new FallingTreeEntity(fallingTree, world))
				.build(FALLING_TREE_ID);

		lingeringEffector = EntityType.Builder.<LingeringEffectorEntity>create(LingeringEffectorEntity::new, EntityClassification.MISC)
				// Giving it growth substance works for now as it's the only lingering substance, however this should be changed in the future.
				.setCustomClientFactory((spawnEntity, world) ->
						new LingeringEffectorEntity(world, new BlockPos(spawnEntity.getPosX(), spawnEntity.getPosY(), spawnEntity.getPosZ()), new GrowthSubstance()))
				.build(LINGERING_EFFECTOR_ID);
	}
	
	@SubscribeEvent
	public static void onEntitiesRegistry(final RegistryEvent.Register<EntityType<?>> entityRegistryEvent) {
		IForgeRegistry<EntityType<?>> registry = entityRegistryEvent.getRegistry();
		
		registry.registerAll(fallingTree.setRegistryName(DynamicTrees.resLoc(FALLING_TREE_ID)),
				lingeringEffector.setRegistryName(DynamicTrees.resLoc(LINGERING_EFFECTOR_ID)));
	}
	
	///////////////////////////////////////////
	// TILE ENTITIES
	///////////////////////////////////////////
	
	public static TileEntityType<SpeciesTileEntity> speciesTE;
	public static TileEntityType<BonsaiTileEntity> bonsaiTE;
	
	public static void setupTileEntities() {
		LinkedList<RootyBlock> rootyDirts = RootyBlockHelper.generateListForRegistry(false);
		speciesTE = TileEntityType.Builder.create(SpeciesTileEntity::new, rootyDirts.toArray(new RootyBlock[0])).build(null);
		bonsaiTE = TileEntityType.Builder.create(BonsaiTileEntity::new, BONSAI_POT).build(null);
	}
	
	@SubscribeEvent
	public static void onTileEntitiesRegistry(final RegistryEvent.Register<TileEntityType<?>> tileEntityRegistryEvent) {
		setupTileEntities();
		
		tileEntityRegistryEvent.getRegistry().register(bonsaiTE.setRegistryName(BonsaiPotBlock.REG_NAME));
		tileEntityRegistryEvent.getRegistry().register(speciesTE.setRegistryName(DynamicTrees.resLoc("tile_entity_species")));
	}
	
	///////////////////////////////////////////
	// MISC
	///////////////////////////////////////////

	/** This is the creative tab that holds all DT items */
	public static final ItemGroup dynamicTreesTab = new ItemGroup(DynamicTrees.MOD_ID) {
		@Override
		public ItemStack createIcon() {
			return TreeRegistry.findSpecies(DTTrees.OAK).getSeedStack(1);
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
	public static void onCellKitRegistry (final com.ferreusveritas.dynamictrees.util.RegistryEvent<CellKit> event) {
		CellKits.register(event.getRegistry());
	}

	@SubscribeEvent
	public static void onGrowthLogicKitRegistry (final com.ferreusveritas.dynamictrees.util.RegistryEvent<GrowthLogicKit> event) {
		GrowthLogicKits.register(event.getRegistry());
	}

	@SubscribeEvent
	public static void onGenFeatureRegistry (final com.ferreusveritas.dynamictrees.util.RegistryEvent<GenFeature> event) {
		GenFeatures.register(event.getRegistry());
	}

}
