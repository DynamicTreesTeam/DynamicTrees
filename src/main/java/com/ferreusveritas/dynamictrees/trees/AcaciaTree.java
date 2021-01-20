package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.block.Blocks;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class AcaciaTree extends VanillaTreeFamily {
	
	public class AcaciaSpecies extends Species {
		
		AcaciaSpecies(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily);
			
			//Acacia Trees are short, very slowly growing trees
			setBasicGrowingParameters(0.15f, 12.0f, 0, 3, 0.7f);
			
			envFactor(Type.COLD, 0.25f);
			envFactor(Type.NETHER, 0.75f);
			envFactor(Type.WET, 0.75f);
			
			setupStandardSeedDropping();
		}
		
		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) {
			return BiomeDictionary.hasType(biome, Type.SAVANNA);
		}
		
	}
	
	public AcaciaTree() {
		super(DynamicTrees.VanillaWoodTypes.acacia);
		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.ACACIA_LEAVES);
	}
	
	@Override
	public void createSpecies() {
		setCommonSpecies(new AcaciaSpecies(this));
	}

}
