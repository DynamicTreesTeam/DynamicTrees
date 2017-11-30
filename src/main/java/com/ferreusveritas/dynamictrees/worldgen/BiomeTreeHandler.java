package com.ferreusveritas.dynamictrees.worldgen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDensityProvider;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeTreeSelector;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.util.MathHelper;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

/**
* Selects a tree appropriate for a biome
* 
* @author ferreusveritas
*/
public class BiomeTreeHandler implements IBiomeDensityProvider, IBiomeTreeSelector {

	private ArrayList<IBiomeTreeSelector> biomeTreeSelectors = new ArrayList<IBiomeTreeSelector>();
	private ArrayList<IBiomeDensityProvider> biomeDensityProvider = new ArrayList<IBiomeDensityProvider>();
	
	public BiomeTreeHandler() {
		addTreeSelector(new DefaultBiomeTreeSelector());
		addDensityProvider(new DefaultBiomeDensityProvider());
	}
	
	public void addTreeSelector(IBiomeTreeSelector treeSelector) {
		biomeTreeSelectors.add(treeSelector);
		biomeTreeSelectors.sort(new Comparator<IBiomeTreeSelector>() {
			@Override
			public int compare(IBiomeTreeSelector sel1, IBiomeTreeSelector sel2) {
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
		for(IBiomeTreeSelector selector : biomeTreeSelectors) {
			selector.init();
		}
	}
	
	@Override
	public Decision getTree(World world, BiomeGenBase biome, BlockPos pos, IBlockState dirt, Random random) {
		
		for(IBiomeTreeSelector selector : biomeTreeSelectors) {
			Decision decision = selector.getTree(world, biome, pos, dirt, random);
			if(decision != null && decision.isHandled()) {
				return decision;
			}
		}
		
		return new Decision(null);//No tree at all
	}
	
	@Override
	public double getDensity(BiomeGenBase biome, double noiseDensity, Random random) {
		
		for(IBiomeDensityProvider provider : biomeDensityProvider) {
			double density = provider.getDensity(biome, noiseDensity, random);
			if(density >= 0) {
				return MathHelper.clamp(density, 0.0, 1.0);
			}
		}
		
		return noiseDensity;
	}
	
	@Override
	public EnumChance chance(BiomeGenBase biome, DynamicTree tree, int radius, Random random) {
				
		for(IBiomeDensityProvider provider : biomeDensityProvider) {
			EnumChance c = provider.chance(biome, tree, radius, random);
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
	public String getName() {
		return "core";
	}
}
