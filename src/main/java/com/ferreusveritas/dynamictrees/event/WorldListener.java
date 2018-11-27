package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.blocks.BlockTrunkShell;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

public class WorldListener implements IWorldEventListener {
	
	private final MinecraftServer mcServer;
	World world;

	public WorldListener(World world, MinecraftServer server) {
		this.mcServer = server;
		this.world = world;
	}
	
	@Override
	public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {}
	
	@Override
	public void notifyLightSet(BlockPos pos) {}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {}

	@Override
	public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {}

	@Override
	public void playRecord(SoundEvent soundIn, BlockPos pos) {}

	@Override
	public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {}

	@Override
	public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {}

	@Override
	public void onEntityAdded(Entity entityIn) {}

	@Override
	public void onEntityRemoved(Entity entityIn) {}

	@Override
	public void broadcastSound(int soundID, BlockPos pos, int data) {}

	@Override
	public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
		if (mcServer == null) return;
		
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof BlockTrunkShell) {
			//BlockTrunkShell shell = (BlockTrunkShell) state.getBlock();
			
			for (EntityPlayerMP entityplayermp : this.mcServer.getPlayerList().getPlayers()) {
	            if (entityplayermp != null && entityplayermp.world == this.world) {
	                double d0 = (double) pos.getX() - entityplayermp.posX;
	                double d1 = (double) pos.getY() - entityplayermp.posY;
	                double d2 = (double) pos.getZ() - entityplayermp.posZ;

	                if (d0 * d0 + d1 * d1 + d2 * d2 < 1024.0D) {
	                	BlockPos centerPos = pos.add(state.getValue(BlockTrunkShell.COREDIR).getOffset());
	                    entityplayermp.connection.sendPacket(new SPacketBlockBreakAnim((-breakerId) - 1, centerPos, progress));
	                }
	            }
	        }
		}
	}

}
