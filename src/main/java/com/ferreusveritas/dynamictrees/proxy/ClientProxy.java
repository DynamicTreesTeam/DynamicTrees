package com.ferreusveritas.dynamictrees.proxy;

import com.ferreusveritas.dynamictrees.event.ClientEventHandler;
import com.ferreusveritas.dynamictrees.renderers.RendererBranch;
import com.ferreusveritas.dynamictrees.renderers.RendererRootyDirt;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityBlockDustFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
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
		super.registerEventHandlers();//Registers Common Handlers
		ClientEventHandler ev = new ClientEventHandler();
		FMLCommonHandler.instance().bus().register(ev);
		MinecraftForge.EVENT_BUS.register(ev);
	}

	@Override
	public void addDustParticle(World world, double fx, double fy, double fz, double mx, double my, double mz, int x, int y, int z, Block block, int metadata) {
		EntityFX entityfx = (new EntityBlockDustFX(world, fx, fy, fz, mx, my, mz, block, metadata)).applyColourMultiplier(x, y, z);
		Minecraft.getMinecraft().effectRenderer.addEffect(entityfx);
	}
	
	/**
	 * Not strictly necessary. But adds a little more isolation to the server for particle effects
	 */
	@Override
	public void spawnParticle(World world, String particleName, double x, double y, double z, double mx, double my, double mz) {
		world.spawnParticle(particleName, x, y, z, mx, my, mz);
	}

	
}
