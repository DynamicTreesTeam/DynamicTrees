package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesPaging;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.resources.DTResourceRegistries;
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

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTTrees {

	public static ResourceLocation OAK = DynamicTrees.resLoc("oak");
	public static ResourceLocation BIRCH = DynamicTrees.resLoc("birch");
	public static ResourceLocation SPRUCE = DynamicTrees.resLoc("spruce");
	public static ResourceLocation JUNGLE = DynamicTrees.resLoc("jungle");
	public static ResourceLocation DARK_OAK = DynamicTrees.resLoc("dark_oak");
	public static ResourceLocation ACACIA = DynamicTrees.resLoc("acacia");
	public static ResourceLocation CRIMSON = DynamicTrees.resLoc("crimson");
	public static ResourceLocation WARPED = DynamicTrees.resLoc("warped");

	public static void setupLeavesProperties() {
		LeavesPaging.register(resLoc("leaves/common.json"));
	}

	public static void setupTrees() {
		DTResourceRegistries.setupTreesResourceManager();
		DTResourceRegistries.TREES_RESOURCE_MANAGER.load();

		TreeFamily.REGISTRY.register(TreeFamily.NULL_FAMILY);
	}

	@SubscribeEvent
	public static void registerSpecies (final RegistryEvent.Register<Species> event) {
		final IForgeRegistry<Species> registry = event.getRegistry();

		registry.register(Species.NULL_SPECIES.setRegistryName(NULL));

		TreeFamily.REGISTRY.forEach(family -> family.registerSpecies(registry));

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
		return DynamicTrees.resLoc(path);
	}

}
