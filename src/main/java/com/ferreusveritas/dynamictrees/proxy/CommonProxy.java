package com.ferreusveritas.dynamictrees.proxy;

import com.ferreusveritas.dynamictrees.event.CommonEventHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityBlockDustFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {

	public void registerTileEntities() {
	}

	public void preInit() {
	}

	public void init() {
	}

	public EntityPlayer getClientPlayer() {
		return null;
	}

	public World getClientWorld() {
		return null;
	}

	public void registerEventHandlers() {
		CommonEventHandler ev = new CommonEventHandler();
		FMLCommonHandler.instance().bus().register(ev);
		MinecraftForge.EVENT_BUS.register(ev);
	}

	public void addDustParticle(World world, double fx, double fy, double fz, double mx, double my, double mz, int x, int y, int z, Block block, int metadata) {
	}
	
	/**
	 * Not strictly necessary. But adds a little more isolation to the server for particle effects
	 */
	public void spawnParticle(World world, String particleName, double x, double y, double z, double mx, double my, double mz) {
	}
	
}
