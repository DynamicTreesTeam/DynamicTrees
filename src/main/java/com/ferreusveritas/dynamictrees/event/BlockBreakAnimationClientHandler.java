package com.ferreusveritas.dynamictrees.event;

import java.util.Map;
import java.util.Map.Entry;

import com.ferreusveritas.dynamictrees.blocks.BlockBranchThick;
import com.google.common.collect.Maps;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockBreakAnimationClientHandler implements IResourceManagerReloadListener {
	
	public static final BlockBreakAnimationClientHandler instance = new BlockBreakAnimationClientHandler(Minecraft.getMinecraft());
	private static final Map<Integer, DestroyBlockProgress> damagedBranches = Maps.<Integer, DestroyBlockProgress>newHashMap();
	
	private final TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];
	
	private BlockBreakAnimationClientHandler(Minecraft mc) {
		((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(this);
	}
	
	@SubscribeEvent
	public void onPlayerJoinWorldEvent(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityPlayerSP && Minecraft.getMinecraft().player != null) {
			if (Minecraft.getMinecraft().player.getEntityId() == event.getEntity().getEntityId()) {
				event.getWorld().removeEventListener(Minecraft.getMinecraft().renderGlobal);
				event.getWorld().addEventListener(new RenderGlobalWrapper(event.getWorld()));
			}
		}
	}
	
	@SubscribeEvent
	public void worldUnload(WorldEvent.Unload event) {
		this.damagedBranches.clear();
	}
	
	@SubscribeEvent
	public void worldLoad(WorldEvent.Load event) {
		this.damagedBranches.clear();
	}
	
	@SubscribeEvent
	public void renderBlockBreakAnim(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		TextureManager textureManager = mc.getTextureManager();
		
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		//this.drawBlockDamageTexture(Tessellator.getInstance(), Tessellator.getInstance().getBuffer(), mc.getRenderViewEntity(), event.getPartialTicks());
		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableBlend();
	}
	
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		TextureMap texturemap = Minecraft.getMinecraft().getTextureMapBlocks();

        for (int i = 0; i < this.destroyBlockIcons.length; ++i) {
            this.destroyBlockIcons[i] = texturemap.getAtlasSprite("minecraft:blocks/destroy_stage_" + i);
        }
	}
	
	private void cleanupExtraDamagedBlocks() {
        for (Entry<Integer, DestroyBlockProgress> entry : this.damagedBranches.entrySet()) {
        	DestroyBlockProgress destroyblockprogress = entry.getValue();
            int k1 = destroyblockprogress.getCreationCloudUpdateTick();
            
            if (Minecraft.getMinecraft().world.getWorldTime() - k1 > 400) {
                this.damagedBranches.remove(entry.getKey());
            }
        }
    }
	
	public void sendThickBranchBreakProgress(int breakerId, BlockPos pos, int progress) {
		if (progress >= 0 && progress < 10) {
			DestroyBlockProgress destroyblockprogress = this.damagedBranches.get(Integer.valueOf(breakerId));
			
			if (destroyblockprogress == null || destroyblockprogress.getPosition().getX() != pos.getX() || destroyblockprogress.getPosition().getY() != pos.getY() || destroyblockprogress.getPosition().getZ() != pos.getZ()) {
				destroyblockprogress = new DestroyBlockProgress(breakerId, pos);
                this.damagedBranches.put(Integer.valueOf(breakerId), destroyblockprogress);
			}
			
			destroyblockprogress.setPartialBlockDamage(progress);
            destroyblockprogress.setCloudUpdateTick((int) Minecraft.getMinecraft().world.getWorldTime());
		} else {
			this.damagedBranches.remove(breakerId);
		}
	}
	
	private void preRenderDamagedBlocks() {
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.enableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
		GlStateManager.doPolygonOffset(-3.0F, -3.0F);
		GlStateManager.enablePolygonOffset();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableAlpha();
		GlStateManager.pushMatrix();
	}
	
	private void postRenderDamagedBlocks() {
		GlStateManager.disableAlpha();
		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.disablePolygonOffset();
		GlStateManager.enableAlpha();
		GlStateManager.depthMask(true);
		GlStateManager.popMatrix();
	}
	
	
	
	
	private static class RenderGlobalWrapper implements IWorldEventListener {
		
		private World world;
		
		public RenderGlobalWrapper(World world) {
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

}
