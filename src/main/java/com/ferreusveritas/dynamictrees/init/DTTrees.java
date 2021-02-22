package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKits;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.trees.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegistryBuilder;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.Collections;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTTrees {
	
	public static final String NULL = "null";

	public static VanillaTreeFamily OAK;
	public static VanillaTreeFamily BIRCH;
	public static VanillaTreeFamily SPRUCE;
	public static VanillaTreeFamily JUNGLE;
	public static VanillaTreeFamily DARK_OAK;
	public static VanillaTreeFamily ACACIA;
	public static VanillaTreeFamily CRIMSON;
	public static VanillaTreeFamily WARPED;
	
	public static final String CONIFER = "conifer";
	
	public static ArrayList<VanillaTreeFamily> baseFamilies = new ArrayList<>();

	/**
	 * Pay Attn! This should be run after the Dynamic Trees Mod
	 * has created it's Blocks and Items.  These trees depend
	 * on the Dynamic Sapling
	 */
	public static void setupTrees() {
		Species.REGISTRY.register(Species.NULL_SPECIES.setRegistryName(new ResourceLocation(DynamicTrees.MOD_ID, "null")));

		OAK = new OakTree();
		BIRCH = new BirchTree();
		SPRUCE = new SpruceTree();
		JUNGLE = new JungleTree();
		DARK_OAK = new DarkOakTree();
		ACACIA = new AcaciaTree();
		CRIMSON = new CrimsonFungus();
		WARPED = new WarpedFungus();

		Collections.addAll(baseFamilies, OAK, BIRCH, SPRUCE, JUNGLE, DARK_OAK, ACACIA, CRIMSON, WARPED);

		baseFamilies.forEach(TreeFamily.REGISTRY::register);
		baseFamilies.forEach(family -> family.registerSpecies(Species.REGISTRY));

		// Registers a fake species for generating mushrooms
		Species.REGISTRY.register(new Mushroom(true));
		Species.REGISTRY.register(new Mushroom(false));
	}
	
	@SubscribeEvent
	public static void newRegistry(RegistryEvent.NewRegistry event) {
		GrowthLogicKit.REGISTRY = new RegistryBuilder<GrowthLogicKit>()
				.setName(resLoc("growth_logic_kit"))
				.setDefaultKey(resLoc("null"))
				.disableSaving()
				.setType(GrowthLogicKit.class)
				.setIDRange(0, Integer.MAX_VALUE - 1)
				.create();

		TreeFamily.REGISTRY = new RegistryBuilder<TreeFamily>()
				.setName(resLoc("tree_family"))
				.setDefaultKey(resLoc("null"))
				.disableSaving()
				.setType(TreeFamily.class)
				.setIDRange(0, Integer.MAX_VALUE - 1)
				.create();

		Species.REGISTRY = new RegistryBuilder<Species>()
				.setName(resLoc( "species"))
				.setDefaultKey(resLoc( "null"))
				.disableSaving()
				.setType(Species.class)
				.setIDRange(0, Integer.MAX_VALUE - 1)
				.create();

		GenFeature.REGISTRY = new RegistryBuilder<GenFeature>()
				.setName(resLoc( "gen_feature"))
				.setDefaultKey(resLoc( "null"))
				.disableSaving()
				.setType(GenFeature.class)
				.setIDRange(0, Integer.MAX_VALUE - 1)
				.create();
		
		setupTrees();
	}

	private static ResourceLocation resLoc (final String path) {
		return new ResourceLocation(DynamicTrees.MOD_ID, path);
	}

}
