package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.block.Blocks;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class AcaciaTree extends VanillaTreeFamily {
	
	public static class AcaciaSpecies extends Species {
		
		AcaciaSpecies(Family family) {
			super(family.getRegistryName(), family);
			
			//Acacia Trees are short, very slowly growing trees
			setBasicGrowingParameters(0.15f, 12.0f, 0, 3, 0.7f);
			
			envFactor(Type.COLD, 0.25f);
			envFactor(Type.NETHER, 0.75f);
			envFactor(Type.WET, 0.75f);
			
			setupStandardSeedDropping();
			setupStandardStickDropping();

			this.setPrimitiveSapling(Blocks.ACACIA_SAPLING);
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

}
