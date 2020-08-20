package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class TreeAcacia extends TreeFamilyVanilla {
	
	public class SpeciesAcacia extends Species {
		
		SpeciesAcacia(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily);
			
			//Acacia Trees are short, very slowly growing trees
			setBasicGrowingParameters(0.15f, 12.0f, 0, 3, 0.7f);
			
			envFactor(Type.COLD, 0.25f);
			envFactor(Type.NETHER, 0.75f);
			envFactor(Type.WET, 0.75f);
			
			setupStandardSeedDropping();
		}
		
		@Override
		public boolean isBiomePerfect(Biome biome) {
			return BiomeDictionary.hasType(biome, Type.SAVANNA);
		}
		
	}
	
	public TreeAcacia() {
		super(DynamicTrees.VanillaWoodTypes.acacia);
		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.ACACIA_LEAVES);
	}
	
	@Override
	public void createSpecies() {
		setCommonSpecies(new SpeciesAcacia(this));
	}
	
	@Override
	public boolean autoCreateBranch() {
		return true;
	}
}
