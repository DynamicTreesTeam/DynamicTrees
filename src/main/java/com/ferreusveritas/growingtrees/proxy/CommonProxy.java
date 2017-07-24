package com.ferreusveritas.growingtrees.proxy;

import com.ferreusveritas.growingtrees.event.CommonEventHandler;

import cpw.mods.fml.common.FMLCommonHandler;
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

}
