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
import net.minecraftforge.event.world.WorldEvent;
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
			}
		}
		
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
		event.getWorld().addEventListener(new BlockBreakAnimationClientHandler(event.getWorld(), Minecraft.getMinecraft()));
	}
	

	private Minecraft mc;
	private World world;
	
	public BlockBreakAnimationClientHandler(World world, Minecraft mc) {
		this.mc = mc;
		this.world = world;
	}
	
	@Override
	public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
		mc.renderGlobal.notifyBlockUpdate(worldIn, pos, oldState, newState, flags);
	}

	@Override
	public void notifyLightSet(BlockPos pos) {
		mc.renderGlobal.notifyLightSet(pos);
	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
		mc.renderGlobal.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
	}

	@Override
	public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x,
			double y, double z, float volume, float pitch) {
		mc.renderGlobal.playSoundToAllNearExcept(player, soundIn, category, x, y, z, volume, pitch);
	}

	@Override
	public void playRecord(SoundEvent soundIn, BlockPos pos) {
		mc.renderGlobal.playRecord(soundIn, pos);
	}

	@Override
	public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord,
			double xSpeed, double ySpeed, double zSpeed, int... parameters) {
		mc.renderGlobal.spawnParticle(particleID, ignoreRange, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
	}

	@Override
	public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed, int... parameters) {
		mc.renderGlobal.spawnParticle(id, ignoreRange, p_190570_3_, x, y, z, xSpeed, ySpeed, zSpeed, parameters);
	}

	@Override
	public void onEntityAdded(Entity entityIn) {
		mc.renderGlobal.onEntityAdded(entityIn);
	}

	@Override
	public void onEntityRemoved(Entity entityIn) {
		mc.renderGlobal.onEntityRemoved(entityIn);
	}

	@Override
	public void broadcastSound(int soundID, BlockPos pos, int data) {
		mc.renderGlobal.broadcastSound(soundID, pos, data);
	}

	@Override
	public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
		mc.renderGlobal.playEvent(player, type, blockPosIn, data);
	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof BlockBranchThick) {
			// TODO
		} else {
			mc.renderGlobal.sendBlockBreakProgress(breakerId, pos, progress);
		}
	}

}
