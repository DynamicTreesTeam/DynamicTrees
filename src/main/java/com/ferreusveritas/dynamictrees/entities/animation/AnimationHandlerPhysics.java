package com.ferreusveritas.dynamictrees.entities.animation;

import java.util.Random;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AnimationHandlerPhysics implements IAnimationHandler {
	@Override public String getName() { return "physics"; };
	
	class HandlerData extends AnimationHandlerData {
		float rotYaw = 0;
		float rotPit = 0;
	}
	
	HandlerData getData(EntityFallingTree entity) {
		return entity.animationHandlerData instanceof HandlerData ? (HandlerData) entity.animationHandlerData : new HandlerData();
	}
	
	@Override
	public void initMotion(EntityFallingTree entity) {
		entity.animationHandlerData = new HandlerData();
		
		BlockPos cutPos = entity.getDestroyData().cutPos;
		long seed = entity.world.rand.nextLong();
		Random random = new Random(seed ^ (((long)cutPos.getX()) << 32 | ((long)cutPos.getZ())) );
		float mass = entity.getDestroyData().woodVolume;
		float inertialMass = MathHelper.clamp(mass / AnimationConstants.TREE_PARTICLES_PER_LOG, 1, 3);
		entity.motionX /= inertialMass;
		entity.motionY /= inertialMass;
		entity.motionZ /= inertialMass;
		
		getData(entity).rotPit = (random.nextFloat() - 0.5f) * 4 / inertialMass;
		getData(entity).rotYaw = (random.nextFloat() - 0.5f) * 4 / inertialMass;

		EnumFacing cutDir = entity.getDestroyData().cutDir;
		entity.motionX += cutDir.getOpposite().getFrontOffsetX() * 0.1;
		entity.motionY += cutDir.getOpposite().getFrontOffsetY() * 0.1;
		entity.motionZ += cutDir.getOpposite().getFrontOffsetZ() * 0.1;
		
		EntityFallingTree.standardDropLeavesPayLoad(entity);//Seeds and stuff fall out of the tree before it falls over
	}
	
	@Override
	public void handleMotion(EntityFallingTree entity) {
		entity.motionY -= AnimationConstants.TREE_GRAVITY;
		
		//Create drag in air
		entity.motionX *= 0.98f;
		entity.motionY *= 0.98f;
		entity.motionZ *= 0.98f;
		getData(entity).rotYaw *= 0.98f;
		getData(entity).rotPit *= 0.98f;
		
		//Apply motion
		entity.posX += entity.motionX;
		entity.posY += entity.motionY;
		entity.posZ += entity.motionZ;
		entity.rotationPitch = MathHelper.wrapDegrees(entity.rotationPitch + getData(entity).rotPit);
		entity.rotationYaw = MathHelper.wrapDegrees(entity.rotationYaw + getData(entity).rotYaw);
		
		int radius = 8;
		IBlockState state = entity.getDestroyData().getBranchBlockState(0);
		if(TreeHelper.isBranch(state)) {
			radius = ((BlockBranch)state.getBlock()).getRadius(state);
		}
		World world = entity.world;
		AxisAlignedBB fallBox = new AxisAlignedBB(entity.posX - radius, entity.posY, entity.posZ - radius, entity.posX + radius, entity.posY + 1.0, entity.posZ + radius);
		BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
		IBlockState collState = world.getBlockState(pos);
		
		if(!TreeHelper.isLeaves(collState) && !TreeHelper.isBranch(collState)) {
			if(collState.getBlock() instanceof BlockLiquid) {
				entity.motionY += AnimationConstants.TREE_GRAVITY;//Undo the gravity
				//Create drag in liquid
				entity.motionX *= 0.8f;
				entity.motionY *= 0.8f;
				entity.motionZ *= 0.8f;
				getData(entity).rotYaw *= 0.8f;
				getData(entity).rotPit *= 0.8f;
				//Add a little buoyancy
				entity.motionY += 0.01;
				entity.onFire = false;
			} else {
				AxisAlignedBB collBox = collState.getCollisionBoundingBox(world, pos);
				
				if(collBox != null) {
					collBox = collBox.offset(pos);
					if(fallBox.intersects(collBox)) {
						entity.motionY = 0;
						entity.posY = collBox.maxY;
						entity.prevPosY = entity.posY;
						entity.landed = true;
						entity.onGround = true;
						if(entity.onFire) {
							entity.world.setBlockState(pos.up(), ModBlocks.blockVerboseFire.getDefaultState());
						}
					}
				}
			}
		}
		
	}
	
	@Override
	public void dropPayload(EntityFallingTree entity) {
		World world = entity.world;
		entity.getPayload().forEach(i -> Block.spawnAsEntity(world, new BlockPos(entity.posX, entity.posY, entity.posZ), i));
		entity.getDestroyData().leavesDrops.forEach(bis -> Block.spawnAsEntity(world, entity.getDestroyData().cutPos.add(bis.pos), bis.stack));
	}
	
	public boolean shouldDie(EntityFallingTree entity) {
		return entity.landed || entity.ticksExisted > 120;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderTransform(EntityFallingTree entity, float entityYaw, float partialTicks) {
		float yaw = MathHelper.wrapDegrees(com.ferreusveritas.dynamictrees.util.MathHelper.angleDegreesInterpolate(entity.prevRotationYaw, entity.rotationYaw, partialTicks));
		float pit = MathHelper.wrapDegrees(com.ferreusveritas.dynamictrees.util.MathHelper.angleDegreesInterpolate(entity.prevRotationPitch, entity.rotationPitch, partialTicks));			
		
		Vec3d mc = entity.getMassCenter();
		GlStateManager.translate(mc.x, mc.y, mc.z);
		GlStateManager.rotate(-yaw, 0, 1, 0);
		GlStateManager.rotate(pit, 1, 0, 0);
		GlStateManager.translate(-mc.x - 0.5, -mc.y, -mc.z - 0.5);
	}
}
