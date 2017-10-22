package com.ferreusveritas.dynamictrees.worldgen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDensityProvider;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeTreeSelector;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

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
			public int compare(IBiomeDensityProvider sel2, IBiomeDensityProvider sel1) {
				return sel2.getPriority() - sel1.getPriority();//Sort backwards so higher values are on top.
			}
		});
	}
	
	@Override
	public Decision getTree(World world, Biome biome, BlockPos pos, IBlockState dirt) {
		
		for(IBiomeTreeSelector selector : biomeTreeSelectors) {
			Decision decision = selector.getTree(world, biome, pos, dirt);
			if(decision.isHandled()) {
				return decision;
			}
		}
		
		return new Decision(null);//No tree at all
	}
	
	@Override
	public double getDensity(Biome biome, double noiseDensity, Random random) {
		
		for(IBiomeDensityProvider provider : biomeDensityProvider) {
			double density = provider.getDensity(biome, noiseDensity, random);
			if(density >= 0) {
				return MathHelper.clamp_double(density, 0.0, 1.0);
			}
		}
		
		return 0.0;
	}
	
	@Override
	public EnumChance chance(Biome biome, DynamicTree tree, int radius, Random random) {
		
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
