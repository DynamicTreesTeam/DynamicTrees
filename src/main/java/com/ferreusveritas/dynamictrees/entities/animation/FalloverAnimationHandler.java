package com.ferreusveritas.dynamictrees.entities.animation;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
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

public class FalloverAnimationHandler implements IAnimationHandler {

	@Override public String getName() { return "fallover"; };

	class HandlerData extends DataAnimationHandler {
		float fallSpeed = 0;
		int bounces = 0;
		HashSet<LivingEntity> entitiesHit = new HashSet<>();//A record of the entities that have taken damage to ensure they are only damaged a single time
	}

	HandlerData getData(EntityFallingTree entity) {
		return entity.dataAnimationHandler != null ? (HandlerData) entity.dataAnimationHandler : new HandlerData();
	}

	@Override
	public void initMotion(EntityFallingTree entity) {
		entity.dataAnimationHandler = new HandlerData();
		EntityFallingTree.standardDropLeavesPayLoad(entity);//Seeds and stuff fall out of the tree before it falls over

		BlockPos belowBlock = entity.getDestroyData().cutPos.down();
		if(entity.world.getBlockState(belowBlock).isSolidSide(entity.world, belowBlock, Direction.UP)) {
			entity.setOnGround(true);
			return;
		}
	}

	@Override
	public void handleMotion(EntityFallingTree entity) {

		float fallSpeed = getData(entity).fallSpeed;

		if(entity.isOnGround()) {
			float height = (float) entity.getMassCenter().y * 2;
			fallSpeed += (0.2 / height);
			addRotation(entity, fallSpeed);
		}

		entity.setMotion(entity.getMotion().x, entity.getMotion().y - AnimationConstants.TREE_GRAVITY, entity.getMotion().z);
		entity.setPosition(entity.getPosX(), entity.getPosY() + entity.getMotion().y, entity.getPosZ());

		{//Handle entire entity falling and collisions with it's base and the ground
			World world = entity.world;
			int radius = 8;
			BlockState state = entity.getDestroyData().getBranchBlockState(0);
			if(TreeHelper.isBranch(state)) {
				radius = ((BranchBlock)state.getBlock()).getRadius(state);
			}
			AxisAlignedBB fallBox = new AxisAlignedBB(entity.getPosX() - radius, entity.getPosY(), entity.getPosZ() - radius, entity.getPosX() + radius, entity.getPosY() + 1.0, entity.getPosZ() + radius);
			BlockPos pos = new BlockPos(entity.getPosX(), entity.getPosY(), entity.getPosZ());
			BlockState collState = world.getBlockState(pos);

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
				entity.setOnGround(true);
			}
		}

		if(fallSpeed > 0 && testCollision(entity)) {
			addRotation(entity, -fallSpeed);//pull back to before the collision
			getData(entity).bounces++;
			fallSpeed *= -AnimationConstants.TREE_ELASTICITY;//bounce with elasticity
			entity.landed = Math.abs(fallSpeed) < 0.02f;//The entity has landed if after a bounce it has little velocity
		}

		//Crush living things with clumsy dead trees
		World world = entity.world;
		if(DTConfigs.enableFallingTreeDamage.get() && !world.isRemote) {
			List<LivingEntity> elist = testEntityCollision(entity);
			for(LivingEntity living: elist) {
				if(!getData(entity).entitiesHit.contains(living)) {
					getData(entity).entitiesHit.add(living);
					float damage = entity.getDestroyData().woodVolume.getVolume() * Math.abs(fallSpeed) * 3f;
					if(getData(entity).bounces == 0 && damage > 2) {
						//System.out.println("damage: " + damage);
						living.setMotion(
								living.getMotion().x + (world.rand.nextFloat() * entity.getDestroyData().toolDir.getOpposite().getXOffset() * damage * 0.2f),
								living.getMotion().y + (world.rand.nextFloat() * fallSpeed * 0.25f),
								living.getMotion().z + (world.rand.nextFloat() * entity.getDestroyData().toolDir.getOpposite().getZOffset() * damage * 0.2f));
						living.setMotion(living.getMotion().x + (world.rand.nextFloat() - 0.5), living.getMotion().y, living.getMotion().z + (world.rand.nextFloat() - 0.5));
						damage *= DTConfigs.fallingTreeDamageMultiplier.get();
						//System.out.println("Tree Falling Damage: " + damage + "/" + living.getHealth());
						living.attackEntityFrom(AnimationConstants.TREE_DAMAGE, damage);
					}
				}
			}
		}

		getData(entity).fallSpeed = fallSpeed;
	}

	/**
	 * This tests a bounding box cube for each block of the trunk.
	 * Processing is approximately equivalent to the same number of {@link ItemEntity}s in the world.
	 *
	 * @param entity
	 * @return true if collision is detected
	 */
	private boolean testCollision(EntityFallingTree entity) {
		Direction toolDir = entity.getDestroyData().toolDir;

		float actingAngle = toolDir.getAxis() == Direction.Axis.X ? entity.rotationYaw : entity.rotationPitch;

		int offsetX = toolDir.getXOffset();
		int offsetZ = toolDir.getZOffset();
		float h = MathHelper.sin((float) Math.toRadians(actingAngle)) * (offsetX | offsetZ);
		float v = MathHelper.cos((float) Math.toRadians(actingAngle));
		float xbase = (float) (entity.getPosX() + offsetX * ( - (0.5f) + (v * 0.5f) + (h * 0.5f) ) );
		float ybase = (float) (entity.getPosY() - (h * 0.5f) + (v * 0.5f));
		float zbase = (float) (entity.getPosZ() + offsetZ * ( - (0.5f) + (v * 0.5f) + (h * 0.5f) ) );

		int trunkHeight = entity.getDestroyData().trunkHeight;
		float maxRadius = entity.getDestroyData().getBranchRadius(0) / 16.0f;

		trunkHeight = Math.min(trunkHeight, 24);

		for(int segment = 0; segment < trunkHeight; segment++) {
			float segX = xbase + h * segment * offsetX;
			float segY = ybase + v * segment;
			float segZ = zbase + h * segment * offsetZ;
			float tex = 0.0625f;
			float half = MathHelper.clamp(tex * (segment + 1) * 2, tex, maxRadius);
			AxisAlignedBB testBB = new AxisAlignedBB(segX - half, segY - half, segZ - half, segX + half, segY + half, segZ + half);

			if(!entity.world.hasNoCollisions(entity, testBB)) {
				return true;
			}
		}

		return false;
	}

	private void addRotation(EntityFallingTree entity, float delta) {
		Direction toolDir = entity.getDestroyData().toolDir;

		switch(toolDir) {
			case NORTH: entity.rotationPitch += delta; break;
			case SOUTH: entity.rotationPitch -= delta; break;
			case WEST: entity.rotationYaw += delta; break;
			case EAST: entity.rotationYaw -= delta; break;
			default: break;
		}

		entity.rotationPitch = MathHelper.wrapDegrees(entity.rotationPitch);
		entity.rotationYaw = MathHelper.wrapDegrees(entity.rotationYaw);
	}

	public List<LivingEntity> testEntityCollision(EntityFallingTree entity) {

		World world = entity.world;

		Direction toolDir = entity.getDestroyData().toolDir;

		float actingAngle = toolDir.getAxis() == Direction.Axis.X ? entity.rotationYaw : entity.rotationPitch;

		int offsetX = toolDir.getXOffset();
		int offsetZ = toolDir.getZOffset();
		float h = MathHelper.sin((float) Math.toRadians(actingAngle)) * (offsetX | offsetZ);
		float v = MathHelper.cos((float) Math.toRadians(actingAngle));
		float xbase = (float) (entity.getPosX() + offsetX * ( - (0.5f) + (v * 0.5f) + (h * 0.5f) ) );
		float ybase = (float) (entity.getPosY() - (h * 0.5f) + (v * 0.5f));
		float zbase = (float) (entity.getPosZ() + offsetZ * ( - (0.5f) + (v * 0.5f) + (h * 0.5f) ) );
		int trunkHeight = entity.getDestroyData().trunkHeight;
		float segX = xbase + h * (trunkHeight - 1) * offsetX;
		float segY = ybase + v * (trunkHeight - 1);
		float segZ = zbase + h * (trunkHeight - 1) * offsetZ;

		float maxRadius = entity.getDestroyData().getBranchRadius(0) / 16.0f;

		Vector3d vec3d1 = new Vector3d(xbase, ybase, zbase);
		Vector3d vec3d2 = new Vector3d(segX, segY, segZ);

		return world.getEntitiesInAABBexcluding(entity, new AxisAlignedBB(vec3d1.x, vec3d1.y, vec3d1.z, vec3d2.x, vec3d2.y, vec3d2.z),
				entity1 -> {
					if(entity1 instanceof LivingEntity && entity1.canBeCollidedWith()) {
						AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow(maxRadius);
						return axisalignedbb.contains(vec3d1) || axisalignedbb.intersects(vec3d1, vec3d2);
					}
					return false;
				}
		).stream().map( a -> (LivingEntity)a ).collect(Collectors.toList());

	}

	@Override
	public void dropPayload(EntityFallingTree entity) {
		World world = entity.world;
		BlockPos cutPos = entity.getDestroyData().cutPos;
		entity.getPayload().forEach(i -> Block.spawnAsEntity(world, cutPos, i));
	}

	@Override
	public boolean shouldDie(EntityFallingTree entity) {

		boolean dead =
			Math.abs(entity.rotationPitch) >= 160 ||
			Math.abs(entity.rotationYaw) >= 160 ||
			entity.landed ||
			entity.ticksExisted > 120 + (entity.getDestroyData().trunkHeight);

		//Force the Rooty Dirt to update if it's there.  Turning it back to dirt.
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

		//Vec3d mc = entity.getMassCenter();

		int radius = entity.getDestroyData().getBranchRadius(0);

		Direction toolDir = entity.getDestroyData().toolDir;
		Vector3d toolVec = new Vector3d(toolDir.getXOffset(), toolDir.getYOffset(), toolDir.getZOffset()).scale(radius / 16.0f);

		matrixStack.translate(-toolVec.x, -toolVec.y, -toolVec.z);
		matrixStack.rotate(new Quaternion(new Vector3f(0, 0, 1), -yaw, true));
		matrixStack.rotate(new Quaternion(new Vector3f(1, 0, 0), pit, true));
		matrixStack.translate(toolVec.x, toolVec.y, toolVec.z);

		matrixStack.translate(-0.5, 0, -0.5);

	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean shouldRender(EntityFallingTree entity) {
		return true;
	}

}
