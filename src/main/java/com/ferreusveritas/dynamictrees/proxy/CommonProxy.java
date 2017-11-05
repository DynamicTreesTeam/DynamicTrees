package com.ferreusveritas.dynamictrees.proxy;

import com.ferreusveritas.dynamictrees.ConfigHandler;
import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.event.CircleEventHandler;
import com.ferreusveritas.dynamictrees.event.CommonEventHandler;
import com.ferreusveritas.dynamictrees.event.VanillaSaplingEventHandler;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.worldgen.DecorateEventHandler;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {

	public void registerTileEntities() {}

	public void preInit() {}

	public void init() {}

	public void registerModels() {}
	
	public EntityPlayer getClientPlayer() {
		return null;
	}

	public World getClientWorld() {
		return null;
	}

	public void registerEventHandlers() {
		//Common Events.. unused at the moment
		MinecraftForge.EVENT_BUS.register(new CommonEventHandler());

		//An event for dealing with Vanilla Saplings
		if(ConfigHandler.replaceVanillaSapling) {
			MinecraftForge.EVENT_BUS.register(new VanillaSaplingEventHandler());
		}
		
		//Conveniently accessible disaster(Optional World Generation)
		if(WorldGenRegistry.isWorldGenEnabled()) {
			GameRegistry.registerWorldGenerator(DynamicTrees.treeGenerator, 20);
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

	public void addDustParticle(double fx, double fy, double fz, double mx, double my, double mz, IBlockState blockState, float r, float g, float b) {}
	
	/**
	 * Not strictly necessary. But adds a little more isolation to the server for particle effects
	 */
	public void spawnParticle(World world, EnumParticleTypes particleType, double x, double y, double z, double mx, double my, double mz) {}

}
