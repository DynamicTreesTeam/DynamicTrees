package com.ferreusveritas.dynamictrees.entities.animation;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PhysicsAnimationHandler implements IAnimationHandler {
	@Override public String getName() { return "physics"; };
	
	class HandlerData extends DataAnimationHandler {
		float rotYaw = 0;
		float rotPit = 0;
	}
	
	HandlerData getData(EntityFallingTree entity) {
		return entity.dataAnimationHandler instanceof HandlerData ? (HandlerData) entity.dataAnimationHandler : new HandlerData();
	}
	
	@Override
	public void initMotion(EntityFallingTree entity) {
		entity.dataAnimationHandler = new HandlerData();
		
		BlockPos cutPos = entity.getDestroyData().cutPos;
		
		long seed = entity.world.rand.nextLong();
		Random random = new Random(seed ^ (((long)cutPos.getX()) << 32 | ((long)cutPos.getZ())) );
		float mass = entity.getDestroyData().woodVolume.getTotalVolume();
		float inertialMass = MathHelper.clamp(mass, 1, 3);
		entity.setVelocity(entity.getMotion().x/inertialMass, entity.getMotion().y/inertialMass, entity.getMotion().z/inertialMass);
		
		getData(entity).rotPit = (random.nextFloat() - 0.5f) * 4 / inertialMass;
		getData(entity).rotYaw = (random.nextFloat() - 0.5f) * 4 / inertialMass;

		Direction cutDir = entity.getDestroyData().cutDir;
		entity.addVelocity(cutDir.getOpposite().getXOffset() * 0.1,cutDir.getOpposite().getXOffset() * 0.1,cutDir.getOpposite().getXOffset() * 0.1);
		
		EntityFallingTree.standardDropLeavesPayLoad(entity);//Seeds and stuff fall out of the tree before it falls over
	}
	
	@Override
	public void handleMotion(EntityFallingTree entity) {
		
		if(entity.landed) {
			return;
		}

		entity.setMotion(entity.getMotion().x, entity.getMotion().y - AnimationConstants.TREE_GRAVITY, entity.getMotion().z);

		//Create drag in air
		entity.setMotion(entity.getMotion().x * 0.98f, entity.getMotion().y * 0.98f, entity.getMotion().z * 0.98f);
		getData(entity).rotYaw *= 0.98f;
		getData(entity).rotPit *= 0.98f;

		//Apply motion

		entity.setPosition(entity.getPosX() + entity.getMotion().x, entity.getPosY() + entity.getMotion().y, entity.getPosZ() + entity.getMotion().z);
		entity.rotationPitch = MathHelper.wrapDegrees(entity.rotationPitch + getData(entity).rotPit);
		entity.rotationYaw = MathHelper.wrapDegrees(entity.rotationYaw + getData(entity).rotYaw);

		int radius = 8;
		BlockState state = entity.getDestroyData().getBranchBlockState(0);
		if(TreeHelper.isBranch(state)) {
			radius = ((BranchBlock)state.getBlock()).getRadius(state);
		}
		World world = entity.world;
		AxisAlignedBB fallBox = new AxisAlignedBB(entity.getPosX() - radius, entity.getPosY(), entity.getPosZ() - radius, entity.getPosX() + radius, entity.getPosY() + 1.0, entity.getPosZ() + radius);
		BlockPos pos = new BlockPos(entity.getPosX(), entity.getPosY(), entity.getPosZ());
		BlockState collState = world.getBlockState(pos);

		if(!TreeHelper.isLeaves(collState) && !TreeHelper.isBranch(collState) && collState.getBlock() != DTRegistries.trunkShellBlock) {
			if(collState.getBlock() instanceof FlowingFluidBlock) {
				//Undo the gravity
				entity.setMotion(entity.getMotion().x, entity.getMotion().y + (AnimationConstants.TREE_GRAVITY), entity.getMotion().z);
				//Create drag in liquid
				entity.setMotion(entity.getMotion().x * 0.8f, entity.getMotion().y * 0.8f, entity.getMotion().z * 0.8f);
				getData(entity).rotYaw *= 0.8f;
				getData(entity).rotPit *= 0.8f;
				//Add a little buoyancy
				entity.setMotion(entity.getMotion().x, entity.getMotion().y + 0.01, entity.getMotion().z);
				entity.onFire = false;
			} else {
				VoxelShape shape = collState.getCollisionShape(world, pos);
				AxisAlignedBB collBox = new AxisAlignedBB(0,0,0,0,0,0);
				if (!shape.isEmpty()){
					collBox = collState.getCollisionShape(world, pos).getBoundingBox();
				}

				collBox = collBox.offset(pos);
				if(fallBox.intersects(collBox)) {
					entity.setMotion(entity.getMotion().x, 0, entity.getMotion().z);
					entity.setPosition(entity.getPosX(), collBox.maxY, entity.getPosZ());
					entity.prevPosY = entity.getPosY();
					entity.landed = true;
					entity.setOnGround(true);
					if(entity.onFire) {
						if(entity.world.isAirBlock(pos.up())) {
							entity.world.setBlockState(pos.up(), Blocks.FIRE.getDefaultState());
						}
					}
				}
			}
		}
		
	}
	
	@Override
	public void dropPayload(EntityFallingTree entity) {
		World world = entity.world;
		entity.getPayload().forEach(i -> Block.spawnAsEntity(world, new BlockPos(entity.getPosX(), entity.getPosY(), entity.getPosZ()), i));
		entity.getDestroyData().leavesDrops.forEach(bis -> Block.spawnAsEntity(world, entity.getDestroyData().cutPos.add(bis.pos), bis.stack));
	}
	
	public boolean shouldDie(EntityFallingTree entity) {
		boolean dead = entity.landed || entity.ticksExisted > 120;
		
		if(dead) {
			entity.cleanupRootyDirt();
		}
		
		return dead;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderTransform(EntityFallingTree entity, float entityYaw, float partialTicks, MatrixStack matrixStack) {
		float yaw = MathHelper.wrapDegrees(com.ferreusveritas.dynamictrees.util.MathHelper.angleDegreesInterpolate(entity.prevRotationYaw, entity.rotationYaw, partialTicks));
		float pit = MathHelper.wrapDegrees(com.ferreusveritas.dynamictrees.util.MathHelper.angleDegreesInterpolate(entity.prevRotationPitch, entity.rotationPitch, partialTicks));

		Vector3d mc = entity.getMassCenter();
		matrixStack.translate(mc.x, mc.y, mc.z);
		matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), -yaw, true));
		matrixStack.rotate(new Quaternion(new Vector3f(1, 0, 0), pit, true));
		matrixStack.translate(-mc.x - 0.5, -mc.y, -mc.z - 0.);

	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean shouldRender(EntityFallingTree entity) {
		return true;
	}
}
