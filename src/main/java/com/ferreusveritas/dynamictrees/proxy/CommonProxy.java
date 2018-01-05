package com.ferreusveritas.dynamictrees.proxy;


import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.event.CircleEventHandler;
import com.ferreusveritas.dynamictrees.event.CommonEventHandler;
import com.ferreusveritas.dynamictrees.event.DropEventHandler;
import com.ferreusveritas.dynamictrees.event.VanillaSaplingEventHandler;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.worldgen.DecorateEventHandler;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {

	public void registerTileEntities() {}

	public void preInit() {
		registerCommonEventHandlers();
	}

	public void init() {}

	public void registerModels() {}
	
	public void registerColorHandlers() {}

	public void registerCommonEventHandlers() {
		//Common Events.. unused at the moment
		MinecraftForge.EVENT_BUS.register(new CommonEventHandler());
		if(ModConfigs.worldGen) {
			MinecraftForge.EVENT_BUS.register(new DropEventHandler());
		}

		//An event for dealing with Vanilla Saplings
		if(ModConfigs.replaceVanillaSapling) {
			MinecraftForge.EVENT_BUS.register(new VanillaSaplingEventHandler());
		}
		
		//Conveniently accessible disaster(Optional World Generation)
		if(WorldGenRegistry.isWorldGenEnabled()) {
			GameRegistry.registerWorldGenerator(TreeGenerator.getTreeGenerator(), 20);
			MinecraftForge.TERRAIN_GEN_BUS.register(new DecorateEventHandler());
			MinecraftForge.EVENT_BUS.register(new CircleEventHandler());
		}
	}
	
	public int getTreeFoliageColor(DynamicTree tree, World world, IBlockState blockState, BlockPos pos) {
		return 0x00FF00FF;//Magenta shading as error indicator
	}

	///////////////////////////////////////////
	// PARTICLES
	///////////////////////////////////////////

	public void addDustParticle(World world, double fx, double fy, double fz, double mx, double my, double mz, IBlockState blockState, float r, float g, float b) {}
	
	/**
	 * Not strictly necessary. But adds a little more isolation to the server for particle effects
	 */
	public void spawnParticle(World world, EnumParticleTypes particleType, double x, double y, double z, double mx, double my, double mz) {}

	
	public void crushLeavesBlock(World world, BlockPos pos, IBlockState blockState, Entity entity) {}
	
}
