package com.ferreusveritas.dynamictrees.worldgen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDensityProvider;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeSpeciesSelector;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.MathHelper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

/**
* Selects a tree appropriate for a biome
* 
* @author ferreusveritas
*/
public class BiomeSpeciesHandler implements IBiomeDensityProvider, IBiomeSpeciesSelector {

	private ArrayList<IBiomeSpeciesSelector> biomeTreeSelectors = new ArrayList<IBiomeSpeciesSelector>();
	private ArrayList<IBiomeDensityProvider> biomeDensityProvider = new ArrayList<IBiomeDensityProvider>();
	
	public final DefaultBiomeDensityProvider defaultDensityProvider;
	
	public BiomeSpeciesHandler() {
		defaultDensityProvider = new DefaultBiomeDensityProvider();
		
		addTreeSelector(new DefaultBiomeSpeciesSelector());
		addDensityProvider(defaultDensityProvider);
	}
	
	public void addTreeSelector(IBiomeSpeciesSelector treeSelector) {
		biomeTreeSelectors.add(treeSelector);
		biomeTreeSelectors.sort(new Comparator<IBiomeSpeciesSelector>() {
			@Override
			public int compare(IBiomeSpeciesSelector sel1, IBiomeSpeciesSelector sel2) {
				return sel2.getPriority() - sel1.getPriority();//Sort backwards so higher values are on top.
			}
		});
	}
	
	public void addDensityProvider(IBiomeDensityProvider densityProvider) {
		biomeDensityProvider.add(densityProvider);
		biomeDensityProvider.sort(new Comparator<IBiomeDensityProvider>() {
			@Override
			public int compare(IBiomeDensityProvider sel1, IBiomeDensityProvider sel2) {
				return sel2.getPriority() - sel1.getPriority();//Sort backwards so higher values are on top.
			}
		});
	}
		
	@Override
	public void init() {
		biomeTreeSelectors.forEach(s -> s.init());
	}
	
	@Override
	public Decision getSpecies(World world, Biome biome, BlockPos pos, IBlockState dirt, Random random) {
		
		for(IBiomeSpeciesSelector selector : biomeTreeSelectors) {
			Decision decision = selector.getSpecies(world, biome, pos, dirt, random);
			if(decision != null && decision.isHandled()) {
				return decision;
			}
		}
		
		return new Decision(null);//No species at all
	}
	
	@Override
	public double density(Biome biome, double noiseDensity, Random random) {
		
		for(IBiomeDensityProvider provider : biomeDensityProvider) {
			double density = provider.density(biome, noiseDensity, random);
			if(density >= 0) {
				return MathHelper.clamp(density, 0.0, 1.0);
			}
		}
		
		return noiseDensity;
	}
	
	@Override
	public EnumChance chance(Biome biome, Species species, int radius, Random random) {
		
		for(IBiomeDensityProvider provider : biomeDensityProvider) {
			EnumChance c = provider.chance(biome, species, radius, random);
			if(c != EnumChance.UNHANDLED) {
				return c;
			}
		}
				
		return EnumChance.OK;
	}
	
	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public ResourceLocation getName() {
		return new ResourceLocation(ModConstants.MODID, "core");
	}
}
