package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;
import java.util.Collections;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.Mushroom;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeAcacia;
import com.ferreusveritas.dynamictrees.trees.TreeBirch;
import com.ferreusveritas.dynamictrees.trees.TreeCactus;
import com.ferreusveritas.dynamictrees.trees.TreeDarkOak;
import com.ferreusveritas.dynamictrees.trees.TreeFamilyVanilla;
import com.ferreusveritas.dynamictrees.trees.TreeJungle;
import com.ferreusveritas.dynamictrees.trees.TreeOak;
import com.ferreusveritas.dynamictrees.trees.TreeSpruce;

import net.minecraft.block.BlockSapling;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

public class ModTrees {
	
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
	public static void preInit() {
		Species.REGISTRY.register(Species.NULLSPECIES.setRegistryName(new ResourceLocation(ModConstants.MODID, "null")));
		Collections.addAll(baseFamilies, new TreeOak(), new TreeSpruce(), new TreeBirch(), new TreeJungle(), new TreeAcacia(), new TreeDarkOak());
		baseFamilies.forEach(tree -> tree.registerSpecies(Species.REGISTRY));
		dynamicCactus = new TreeCactus();
		dynamicCactus.registerSpecies(Species.REGISTRY);
		
		//Registers a fake species for generating mushrooms
		Species.REGISTRY.register(new Mushroom(true));
		Species.REGISTRY.register(new Mushroom(false));
		
		for(TreeFamilyVanilla vanillaFamily: baseFamilies) {
			TreeRegistry.registerSaplingReplacer(Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, vanillaFamily.woodType), vanillaFamily.getCommonSpecies());
		}

	}
	
}
