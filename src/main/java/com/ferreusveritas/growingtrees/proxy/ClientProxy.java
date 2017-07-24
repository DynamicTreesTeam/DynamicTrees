package com.ferreusveritas.growingtrees.proxy;

import com.ferreusveritas.growingtrees.event.ClientEventHandler;
import com.ferreusveritas.growingtrees.renderers.RendererBranch;
import com.ferreusveritas.growingtrees.renderers.RendererRootyDirt;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
	@Override
	public void preInit() {
	}

	@Override
	public void init() {
		RenderingRegistry.registerBlockHandler(new RendererBranch());
		RenderingRegistry.registerBlockHandler(new RendererRootyDirt());
	}

	@Override
	public EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().thePlayer;
	}

	@Override
	public World getClientWorld() {
		return Minecraft.getMinecraft().theWorld;
	}

	@Override 
	public void registerEventHandlers() {
		super.registerEventHandlers();
		ClientEventHandler ev = new ClientEventHandler();
		FMLCommonHandler.instance().bus().register(ev);
		MinecraftForge.EVENT_BUS.register(ev);
	}

}
