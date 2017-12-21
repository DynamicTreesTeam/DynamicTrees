package com.ferreusveritas.dynamictrees.proxy;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumParticleTypes;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.event.CircleEventHandler;
import com.ferreusveritas.dynamictrees.event.CommonEventHandler;
import com.ferreusveritas.dynamictrees.event.VanillaSaplingEventHandler;
import com.ferreusveritas.dynamictrees.worldgen.DecorateEventHandler;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;

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
		CommonEventHandler ev = new CommonEventHandler();
		FMLCommonHandler.instance().bus().register(ev);
		MinecraftForge.EVENT_BUS.register(ev);

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
	
	//1.7.10 Does not register BlockColorHandlers

	///////////////////////////////////////////
	// PARTICLES
	///////////////////////////////////////////

	public void addDustParticle(World world, double fx, double fy, double fz, double mx, double my, double mz, int x, int y, int z, Block block, int metadata) {}
	
	/**
	 * Not strictly necessary. But adds a little more isolation to the server for particle effects
	 */
	public void spawnParticle(World world, EnumParticleTypes particleType, double x, double y, double z, double mx, double my, double mz) {}

	
	public void crushLeavesBlock(World world, BlockPos pos, IBlockState blockState, Entity entity) {}
	
}
