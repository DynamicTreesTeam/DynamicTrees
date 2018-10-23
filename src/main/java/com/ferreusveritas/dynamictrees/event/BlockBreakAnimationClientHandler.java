package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.blocks.BlockBranchThick;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockBreakAnimationClientHandler implements IWorldEventListener {
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onPlayerJoinWorldEvent(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityPlayerSP && Minecraft.getMinecraft().player != null) {
			if (Minecraft.getMinecraft().player.getEntityId() == event.getEntity().getEntityId()) {
				event.getWorld().removeEventListener(Minecraft.getMinecraft().renderGlobal);
				event.getWorld().addEventListener(new BlockBreakAnimationClientHandler(event.getWorld()));
			}
		}
		
	}

	private World world;
	
	public BlockBreakAnimationClientHandler(World world) {
		this.world = world;
	}
	
	@Override
	public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
		Minecraft.getMinecraft().renderGlobal.notifyBlockUpdate(worldIn, pos, oldState, newState, flags);
	}

	@Override
	public void notifyLightSet(BlockPos pos) {
		Minecraft.getMinecraft().renderGlobal.notifyLightSet(pos);
	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
		Minecraft.getMinecraft().renderGlobal.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
	}

	@Override
	public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x,
			double y, double z, float volume, float pitch) {
		Minecraft.getMinecraft().renderGlobal.playSoundToAllNearExcept(player, soundIn, category, x, y, z, volume, pitch);
	}

	@Override
	public void playRecord(SoundEvent soundIn, BlockPos pos) {
		Minecraft.getMinecraft().renderGlobal.playRecord(soundIn, pos);
	}

	@Override
	public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord,
			double xSpeed, double ySpeed, double zSpeed, int... parameters) {
		Minecraft.getMinecraft().renderGlobal.spawnParticle(particleID, ignoreRange, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
	}

	@Override
	public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed, int... parameters) {
		Minecraft.getMinecraft().renderGlobal.spawnParticle(id, ignoreRange, p_190570_3_, x, y, z, xSpeed, ySpeed, zSpeed, parameters);
	}

	@Override
	public void onEntityAdded(Entity entityIn) {
		Minecraft.getMinecraft().renderGlobal.onEntityAdded(entityIn);
	}

	@Override
	public void onEntityRemoved(Entity entityIn) {
		Minecraft.getMinecraft().renderGlobal.onEntityRemoved(entityIn);
	}

	@Override
	public void broadcastSound(int soundID, BlockPos pos, int data) {
		Minecraft.getMinecraft().renderGlobal.broadcastSound(soundID, pos, data);
	}

	@Override
	public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
		Minecraft.getMinecraft().renderGlobal.playEvent(player, type, blockPosIn, data);
	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof BlockBranchThick) {
			// TODO
		} else {
			Minecraft.getMinecraft().renderGlobal.sendBlockBreakProgress(breakerId, pos, progress);
		}
	}

}
