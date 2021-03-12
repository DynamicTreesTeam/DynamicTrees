package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.leaves.WartProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.resources.DTResourceRegistries;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.trees.*;
import com.ferreusveritas.dynamictrees.util.Registry;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;
import net.minecraftforge.registries.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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

	@SubscribeEvent
	public static void registerSpecies (final com.ferreusveritas.dynamictrees.util.RegistryEvent<Species> event) {
		// Registers fake species for generating mushrooms.
		event.getRegistry().registerAll(new Mushroom(true), new Mushroom(false));
	}

	public static final ResourceLocation NULL = resLoc("null");

	public static final ResourceLocation CELL_KIT = resLoc("cell_kit");
	public static final ResourceLocation GROWTH_LOGIC_KIT = resLoc("growth_logic_kit");
	public static final ResourceLocation SPECIES = resLoc("species");
	public static final ResourceLocation GEN_FEATURE = resLoc("gen_feature");

	@SubscribeEvent
	public static void newRegistry(RegistryEvent.NewRegistry event) {
		CellKit.REGISTRY = createRegistry(CellKit.class, CELL_KIT);
		GrowthLogicKit.REGISTRY = createRegistry(GrowthLogicKit.class, GROWTH_LOGIC_KIT);
		GenFeature.REGISTRY = createRegistry(GenFeature.class, GEN_FEATURE);

		// Fire custom registry events.
		ModLoader.get().postEvent(new CellKitRegistryEvent());

		LeavesProperties.REGISTRY.registerType(DynamicTrees.resLoc("wart"), new WartProperties.Type());
		Family.REGISTRY.registerType(DynamicTrees.resLoc("fungus"), new FungusFamily.Type());
		Species.REGISTRY.registerType(DynamicTrees.resLoc("fungus"), new FungusSpecies.Type());
		Species.REGISTRY.registerType(DynamicTrees.resLoc("dark_oak"), new DarkOakSpecies.Type());

		DTResourceRegistries.setupTreesResourceManager();
		JsonObjectGetters.registerRegistryEntryGetters();
		DTResourceRegistries.TREES_RESOURCE_MANAGER.load();

		Arrays.asList(LeavesProperties.REGISTRY, Family.REGISTRY, Species.REGISTRY).forEach(registry -> {
			registry.postRegistryEvent();
			registry.lock();
		});
	}

	/**
	 * Custom registry event so that certain registries are created before blocks and items
	 * are registered.
	 *
	 * @param <T> The registry entry {@link Class}.
	 */
	public static class CustomRegistryEvent<T extends IForgeRegistryEntry<T>> extends Event implements IModBusEvent {
		private final IForgeRegistry<T> registry;

		public CustomRegistryEvent(IForgeRegistry<T> registry) {
			this.registry = registry;
		}

		public IForgeRegistry<T> getRegistry() {
			return registry;
		}
	}

	public static final class CellKitRegistryEvent extends CustomRegistryEvent<CellKit> {
		public CellKitRegistryEvent() {
			super(CellKit.REGISTRY);
		}
	}

	private static <T extends ForgeRegistryEntry<T>> IForgeRegistry<T> createRegistry (final Class<T> type, final ResourceLocation name) {
		return new RegistryBuilder<T>().setName(name).setDefaultKey(NULL).disableSaving().setType(type).setIDRange(0, Integer.MAX_VALUE - 1).create();
	}

	private static ResourceLocation resLoc (final String path) {
		return DynamicTrees.resLoc(path);
	}

}
