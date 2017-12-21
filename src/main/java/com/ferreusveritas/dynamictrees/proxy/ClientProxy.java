package com.ferreusveritas.dynamictrees.proxy;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumParticleTypes;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.renderers.RendererBonsai;
import com.ferreusveritas.dynamictrees.renderers.RendererBranch;
import com.ferreusveritas.dynamictrees.renderers.RendererRootyDirt;
import com.ferreusveritas.dynamictrees.renderers.RendererSapling;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityBlockDustFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;

public class ClientProxy extends CommonProxy {
	
	@Override
	public void preInit() {
		super.preInit();
		registerClientEventHandlers();
	}
	
	@Override
	public void init() {
		super.init();
		RenderingRegistry.registerBlockHandler(new RendererBonsai());
		RenderingRegistry.registerBlockHandler(new RendererBranch());
		RenderingRegistry.registerBlockHandler(new RendererRootyDirt());
		RenderingRegistry.registerBlockHandler(new RendererSapling());
	}
	
	@Override
	public void registerModels() {
		//1.7.10 Does not register models
	}
	
	public void makePlantsBlue() {
		//1.7.10 Does not register BlockColorHandlers
	}
 
	public void registerClientEventHandlers() {
		//There are currently no Client Side events to handle
	}
	
	//1.7.10 Does not register BlockColorHandlers
	
	///////////////////////////////////////////
	// PARTICLES
	///////////////////////////////////////////
	
	@Override
	public void addDustParticle(World world, double fx, double fy, double fz, double mx, double my, double mz, int x, int y, int z, Block block, int metadata) {
		EntityFX entityfx = (new EntityBlockDustFX(world.real(), fx, fy, fz, mx, my, mz, block, metadata)).applyColourMultiplier(x, y, z);
		Minecraft.getMinecraft().effectRenderer.addEffect(entityfx);
	}
	
	/** Not strictly necessary. But adds a little more isolation to the server for particle effects */
	@Override
	public void spawnParticle(World world, EnumParticleTypes particleType, double x, double y, double z, double mx, double my, double mz) {
		world.real().spawnParticle(particleType.getName(), x, y, z, mx, my, mz);
	}

	public void crushLeavesBlock(World world, BlockPos pos, IBlockState blockState, Entity entity) {
		Random random = world.rand;
		ITreePart treePart = TreeHelper.getTreePart(blockState);
		if(treePart instanceof BlockDynamicLeaves) {
			BlockDynamicLeaves leaves = (BlockDynamicLeaves) treePart;
			DynamicTree tree = leaves.getTree(blockState);
			if(tree != null) {
				for(int dz = 0; dz < 8; dz++) {
					for(int dy = 0; dy < 8; dy++) {
						for(int dx = 0; dx < 8; dx++) {
							if(random.nextInt(8) == 0) {
								double fx = pos.getX() + dx / 8.0;
								double fy = pos.getY() + dy / 8.0;
								double fz = pos.getZ() + dz / 8.0;
								addDustParticle(world, fx, fy, fz, 0, random.nextFloat() * entity.motionY, 0, pos.getX(), pos.getY(), pos.getZ(), blockState.getBlock(), blockState.getMeta());
							}
						}
					}
				}
			}
		}
	}
	
}
