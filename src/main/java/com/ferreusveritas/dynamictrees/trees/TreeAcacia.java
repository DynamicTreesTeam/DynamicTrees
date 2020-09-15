package com.ferreusveritas.dynamictrees.trees;

import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockPlanks;
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
		super(BlockPlanks.EnumType.ACACIA);
		addConnectableVanillaLeaves((state) -> { return state.getBlock() instanceof BlockNewLeaf && (state.getValue(BlockNewLeaf.VARIANT) == BlockPlanks.EnumType.ACACIA); });
	}
	
	@Override
	public void createSpecies() {
		setCommonSpecies(new SpeciesAcacia(this));
	}
	
}
