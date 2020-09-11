package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.RootyBlockHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.trees.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTTrees {

	public static final String NULL = "null";
	public static final String OAK = "oak";
	public static final String BIRCH = "birch";
	public static final String SPRUCE = "spruce";
	public static final String JUNGLE = "jungle";
	public static final String DARKOAK = "darkoak";
	public static final String ACACIA = "acacia";

	public static final String CONIFER = "conifer";

	public static ArrayList<TreeFamilyVanilla> baseFamilies = new ArrayList<>();
	// keeping the cactus 'tree' out of baseTrees prevents automatic registration of seed/sapling conversion recipes, transformation potion recipes, and models
    public static TreeCactus dynamicCactus;

	/**
	 * Pay Attn! This should be run after the Dynamic Trees Mod
	 * has created it's Blocks and Items.  These trees depend
	 * on the Dynamic Sapling
	 */
	public static void setupTrees() {
		Species.REGISTRY.register(Species.NULLSPECIES.setRegistryName(new ResourceLocation(DynamicTrees.MODID, "null")));
		Collections.addAll(baseFamilies, new TreeOak(), new TreeSpruce(), new TreeBirch(), new TreeJungle(), new TreeAcacia(), new TreeDarkOak());
		baseFamilies.forEach(tree -> tree.registerSpecies(Species.REGISTRY));
        dynamicCactus = new TreeCactus();
        dynamicCactus.registerSpecies(Species.REGISTRY);

		//Registers a fake species for generating mushrooms
        Species.REGISTRY.register(new Mushroom(true));
        Species.REGISTRY.register(new Mushroom(false));

        setupVanillaRootyBlocks();
	}

	private static void setupVanillaRootyBlocks(){
		// We add rooty dirt separately to give it special properties.
		// In this case, it turns to grass or mycelium like normal dirt
		RootyBlockHelper.addToRootyBlocksMap(Blocks.DIRT, new BlockRooty(Blocks.DIRT){
			@Override
			public void randomTick(BlockState state, World world, BlockPos pos, Random random) {
				super.randomTick(state, world, pos, random);

				Block rootyGrass = RootyBlockHelper.getRootyBlocksMap().get(Blocks.GRASS_BLOCK);
				Block rootyMycelium = RootyBlockHelper.getRootyBlocksMap().get(Blocks.MYCELIUM);

				//this is a similar behaviour to vanilla grass spreading but inverted to be handled by the dirt block
				if (!world.isRemote)
				{
					if (!world.isAreaLoaded(pos, 3)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light and spreading
					if (world.getLight(pos.up()) >= 9)
					{
						for (int i = 0; i < 4; ++i)
						{
							BlockPos thatPos = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);

							if (thatPos.getY() >= 0 && thatPos.getY() < 256 && !world.isBlockLoaded(thatPos)) return;

							BlockState thatStateUp = world.getBlockState(thatPos.up());
							BlockState thatState = world.getBlockState(thatPos);

							if ((thatState.getBlock() == Blocks.GRASS_BLOCK || thatState.getBlock() == rootyGrass) && world.getLight(pos.up()) >= 9 && thatStateUp.getOpacity(world, thatPos.up()) <= 2)
							{
								world.setBlockState(pos, rootyGrass.getDefaultState().with(FERTILITY, world.getBlockState(pos).get(FERTILITY)));
							}
							else if ((thatState.getBlock() == Blocks.MYCELIUM || thatState.getBlock() == rootyMycelium) && world.getLight(pos.up()) >= 9 && thatStateUp.getOpacity(world, thatPos.up()) <= 2)
							{
								world.setBlockState(pos, rootyMycelium.getDefaultState().with(FERTILITY, world.getBlockState(pos).get(FERTILITY)));
							}
						}
					}

				}

			}
		});

		// We excempt farmland from having a custom rooty block and default to Dirt's rooty block.
		RootyBlockHelper.excemptBlock(Blocks.FARMLAND, Blocks.DIRT);

		// Rooty Dirt blocks are created for each allowed soil in the registry (except the previously added and excempt ones)
		setupRootyBlocks(Species.REGISTRY.getValues());

	}

	/**
	 * This method must be called by any addon that adds new allowed soils that arent vanilla
	 * @param speciesList list of species with new allowed soils to be rootified
	 */
	public static void setupRootyBlocks (Collection<Species> speciesList){
		for(Species species: speciesList) {
			for (Block soil : species.getAcceptableSoils()){
				if (!RootyBlockHelper.getRootyBlocksMap().containsKey(soil)){
					RootyBlockHelper.addToRootyBlocksMap(soil);
				}
			}
		}
	}

	@SubscribeEvent
	public static void newRegistry(RegistryEvent.NewRegistry event) {
		Species.REGISTRY = new RegistryBuilder<Species>()
				.setName(new ResourceLocation(DynamicTrees.MODID, "species"))
				.setDefaultKey(new ResourceLocation(DynamicTrees.MODID, "null"))
				.disableSaving()
				.setType(Species.class)
				.setIDRange(0, Integer.MAX_VALUE - 1)
				.create();

		setupTrees();
	}

	public static void setupExtraSoils() {
		Collection<Item> sandItems = ItemTags.SAND.getAllElements();

		Species cactus = dynamicCactus.getCommonSpecies();

		for (Item item : sandItems) {
			if (item instanceof BlockItem) {
				BlockItem itemBlock = (BlockItem) item;
				Block sandBlock = itemBlock.getBlock();
				cactus.addAcceptableSoil(sandBlock);
			}
		}
	}
	
}
