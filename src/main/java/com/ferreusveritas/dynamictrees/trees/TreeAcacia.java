package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary.Type;

public class TreeAcacia extends DynamicTree {
		
	public class SpeciesAcacia extends Species {

		SpeciesAcacia(DynamicTree treeFamily) {
			super(treeFamily.getName(), treeFamily, ModBlocks.acaciaLeavesProperties);
			
			//Acacia Trees are short, very slowly growing trees
			setBasicGrowingParameters(0.15f, 12.0f, 0, 3, 0.7f);
		
			envFactor(Type.COLD, 0.25f);
			envFactor(Type.NETHER, 0.75f);
			envFactor(Type.WET, 0.75f);
			
			setupStandardSeedDropping();
		}
		
		@Override
		public boolean isBiomePerfect(Biome biome) {
			return CompatHelper.biomeHasType(biome, Type.SAVANNA);
		}
		
	}
		
	public TreeAcacia() {
		super(BlockPlanks.EnumType.ACACIA);
		ModBlocks.acaciaLeavesProperties.setTree(this);
		
		addConnectableVanillaLeaves(new IConnectable() {
			@Override
			public boolean isConnectable(IBlockState blockState) {
				return blockState.getBlock() instanceof BlockNewLeaf && (blockState.getValue(BlockNewLeaf.VARIANT) == BlockPlanks.EnumType.ACACIA);
			}
		});
	}

	@Override
	public void createSpecies() {
		setCommonSpecies(new SpeciesAcacia(this));
	}
	
}
