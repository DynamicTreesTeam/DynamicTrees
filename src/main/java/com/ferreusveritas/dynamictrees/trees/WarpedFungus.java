package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import net.minecraft.block.Blocks;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class WarpedFungus extends CrimsonFungus {

	public class WarpedSpecies extends CrimsonFungus.CrimsonSpecies {

		WarpedSpecies(TreeFamily treeFamily) {
			super(treeFamily);
		}

	}

	public WarpedFungus() {
		super(DynamicTrees.VanillaWoodTypes.warped);
		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.WARPED_WART_BLOCK);
	}
	
	@Override
	public void createSpecies() {
		setCommonSpecies(new WarpedSpecies(this));
	}

}
