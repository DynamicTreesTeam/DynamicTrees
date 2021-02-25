package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesPaging;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.trees.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.ArrayList;
import java.util.Collections;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTTrees {

	public static TreeFamily OAK;
	public static TreeFamily BIRCH;
	public static TreeFamily SPRUCE;
	public static TreeFamily JUNGLE;
	public static TreeFamily DARK_OAK;
	public static TreeFamily ACACIA;
	public static TreeFamily CRIMSON;
	public static TreeFamily WARPED;

	public static final ArrayList<TreeFamily> FAMILIES = new ArrayList<>();

	public static void setupLeavesProperties() {
		LeavesPaging.register(new ResourceLocation(DynamicTrees.MOD_ID, "leaves/common.json"));
	}

	public static void setupTrees() {
		OAK = new OakTree();
		BIRCH = new BirchTree();
		SPRUCE = new SpruceTree();
		JUNGLE = new JungleTree();
		DARK_OAK = new DarkOakTree();
		ACACIA = new AcaciaTree();
		CRIMSON = new CrimsonFungus();
		WARPED = new WarpedFungus();

		Collections.addAll(FAMILIES, OAK, BIRCH, SPRUCE, JUNGLE, DARK_OAK, ACACIA, CRIMSON, WARPED);

		FAMILIES.forEach(TreeFamily.REGISTRY::register);
	}

	@SubscribeEvent
	public static void registerSpecies (final RegistryEvent.Register<Species> event) {
		final IForgeRegistry<Species> registry = event.getRegistry();

		registry.register(Species.NULL_SPECIES.setRegistryName(new ResourceLocation(DynamicTrees.MOD_ID, "null")));

		FAMILIES.forEach(family -> family.registerSpecies(registry));

		// Registers a fake species for generating mushrooms
		registry.registerAll(new Mushroom(true), new Mushroom(false));
	}

	public static final ResourceLocation NULL = resLoc("null");

	public static final ResourceLocation LEAVES_PROPERTIES = resLoc("leaves_properties");
	public static final ResourceLocation GROWTH_LOGIC_KIT = resLoc("growth_logic_kit");
	public static final ResourceLocation TREE_FAMILY = resLoc("tree_family");
	public static final ResourceLocation SPECIES = resLoc("species");
	public static final ResourceLocation GEN_FEATURE = resLoc("gen_feature");

	@SubscribeEvent
	public static void newRegistry(RegistryEvent.NewRegistry event) {
		LeavesProperties.REGISTRY = createRegistry(LeavesProperties.class, LEAVES_PROPERTIES);
		GrowthLogicKit.REGISTRY = createRegistry(GrowthLogicKit.class, GROWTH_LOGIC_KIT);
		TreeFamily.REGISTRY = createRegistry(TreeFamily.class, TREE_FAMILY);
		Species.REGISTRY = createRegistry(Species.class, SPECIES);
		GenFeature.REGISTRY = createRegistry(GenFeature.class, GEN_FEATURE);

		setupLeavesProperties();
		setupTrees();
	}

	private static <T extends ForgeRegistryEntry<T>> IForgeRegistry<T> createRegistry (final Class<T> type, final ResourceLocation name) {
		return new RegistryBuilder<T>().setName(name).setDefaultKey(NULL).disableSaving().setType(type).setIDRange(0, Integer.MAX_VALUE - 1).create();
	}

	private static ResourceLocation resLoc (final String path) {
		return new ResourceLocation(DynamicTrees.MOD_ID, path);
	}

}
