package com.ferreusveritas.growingtrees.proxy;

import com.ferreusveritas.growingtrees.renderers.RendererBranch;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class ClientProxy extends CommonProxy {
	@Override
	public void preInit() {
	}

	@Override
	public void init() {
		RenderingRegistry.registerBlockHandler(new RendererBranch());
	}
	
	@Override
	public EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().thePlayer;
	}

	@Override
	public World getClientWorld() {
		return Minecraft.getMinecraft().theWorld;
	}
}
