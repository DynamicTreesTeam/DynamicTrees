package com.ferreusveritas.dynamictrees.event;

//import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
//import com.ferreusveritas.dynamictrees.blocks.BlockBranchThick;
//import com.ferreusveritas.dynamictrees.models.ICustomDamageModel;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import net.minecraft.block.state.BlockState;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.entity.EntityPlayerSP;
//import net.minecraft.client.renderer.*;
//import net.minecraft.client.renderer.block.model.BakedQuad;
//import net.minecraft.client.renderer.block.model.BakedQuadRetextured;
//import net.minecraft.client.renderer.block.model.IBakedModel;
//import net.minecraft.client.renderer.block.model.SimpleBakedModel;
//import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//import net.minecraft.client.renderer.texture.TextureManager;
//import net.minecraft.client.renderer.texture.TextureMap;
//import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
//import net.minecraft.client.resources.IReloadableResourceManager;
//import net.minecraft.client.resources.IResourceManager;
//import net.minecraft.client.resources.IResourceManagerReloadListener;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.util.EnumBlockRenderType;
//import net.minecraft.util.Direction;
//import net.minecraft.util.SoundCategory;
//import net.minecraft.util.SoundEvent;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.MathHelper;
//import net.minecraft.world.IBlockAccess;
//import net.minecraft.world.IWorldEventListener;
//import net.minecraft.world.World;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//import net.minecraftforge.client.event.RenderWorldLastEvent;
//import net.minecraftforge.event.entity.EntityJoinWorldEvent;
//import net.minecraftforge.event.world.WorldEvent;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.relauncher.ReflectionHelper;
//import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.SideOnly;
//
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//
//@OnlyIn(Dist.CLIENT)
//public class BlockBreakAnimationClientHandler implements IResourceManagerReloadListener {
//
//	public static final BlockBreakAnimationClientHandler instance = new BlockBreakAnimationClientHandler(Minecraft.getInstance());
//	private static final Map<Integer, DestroyBlockProgress> damagedBranches = Maps.<Integer, DestroyBlockProgress>newHashMap();
//
//	private final TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];
//
//	private BlockBreakAnimationClientHandler(Minecraft mc) {
//		((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(this);
//	}
//
//	@SubscribeEvent
//	public void onPlayerJoinWorldEvent(EntityJoinWorldEvent event) {
//		if (event.getEntity() instanceof EntityPlayerSP && Minecraft.getInstance().player != null) {
//			if (Minecraft.getInstance().player.getEntityId() == event.getEntity().getEntityId()) {
//				event.getWorld().removeEventListener(Minecraft.getInstance().renderGlobal);
//				List<IWorldEventListener> listeners = ReflectionHelper.getPrivateValue(World.class, event.getWorld(), "eventListeners", "field_73021_x");
//				if (listeners.stream().noneMatch((el) -> el instanceof RenderGlobalWrapper)) {
//					event.getWorld().addEventListener(new RenderGlobalWrapper(event.getWorld()));
//				}
//			}
//		}
//	}
//
//	@SubscribeEvent
//	public void worldUnload(WorldEvent.Unload event) {
//		BlockBreakAnimationClientHandler.damagedBranches.clear();
//	}
//
//	@SubscribeEvent
//	public void worldLoad(WorldEvent.Load event) {
//		BlockBreakAnimationClientHandler.damagedBranches.clear();
//	}
//
//	@SubscribeEvent
//	public void renderBlockBreakAnim(RenderWorldLastEvent event) {
//		Minecraft mc = Minecraft.getInstance();
//		TextureManager textureManager = mc.getTextureManager();
//
//		GlStateManager.enableBlend();
//		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
//		this.drawBlockDamageTexture(mc, textureManager, Tessellator.getInstance(), Tessellator.getInstance().getBuffer(), mc.getRenderViewEntity(), event.getPartialTicks());
//		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
//		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//		GlStateManager.disableBlend();
//	}
//
//	@Override
//	public void onResourceManagerReload(IResourceManager resourceManager) {
//		TextureMap texturemap = Minecraft.getInstance().getTextureMapBlocks();
//
//		for (int i = 0; i < this.destroyBlockIcons.length; ++i) {
//			this.destroyBlockIcons[i] = texturemap.getAtlasSprite("minecraft:blocks/destroy_stage_" + i);
//		}
//	}
//
//	private void cleanupExtraDamagedBlocks() {
//		Iterator<Entry<Integer, DestroyBlockProgress>> iter = BlockBreakAnimationClientHandler.damagedBranches.entrySet().iterator();
//
//		while(iter.hasNext()) {
//			DestroyBlockProgress destroyblockprogress = iter.next().getValue();//entry.getValue();
//			int tick = destroyblockprogress.getCreationCloudUpdateTick();
//
//			if (Minecraft.getInstance().world.getWorldTime() - tick > 400) {
//				iter.remove();
//			}
//		}
//	}
//
//	public void sendThickBranchBreakProgress(int breakerId, BlockPos pos, int progress) {
//		if (progress >= 0 && progress < 10) {
//			DestroyBlockProgress destroyblockprogress = BlockBreakAnimationClientHandler.damagedBranches.get(Integer.valueOf(breakerId));
//
//			if (destroyblockprogress == null || destroyblockprogress.getPosition().getX() != pos.getX() || destroyblockprogress.getPosition().getY() != pos.getY() || destroyblockprogress.getPosition().getZ() != pos.getZ()) {
//				destroyblockprogress = new DestroyBlockProgress(breakerId, pos);
//				BlockBreakAnimationClientHandler.damagedBranches.put(Integer.valueOf(breakerId), destroyblockprogress);
//			}
//
//			destroyblockprogress.setPartialBlockDamage(progress);
//			destroyblockprogress.setCloudUpdateTick((int) Minecraft.getInstance().world.getWorldTime());
//		} else {
//			BlockBreakAnimationClientHandler.damagedBranches.remove(breakerId);
//		}
//	}
//
//	private void preRenderDamagedBlocks() {
//		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//		GlStateManager.enableBlend();
//		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
//		GlStateManager.doPolygonOffset(-3.0F, -3.0F);
//		GlStateManager.enablePolygonOffset();
//		GlStateManager.alphaFunc(516, 0.1F);
//		GlStateManager.enableAlpha();
//		GlStateManager.pushMatrix();
//	}
//
//	private void postRenderDamagedBlocks() {
//		GlStateManager.disableAlpha();
//		GlStateManager.doPolygonOffset(0.0F, 0.0F);
//		GlStateManager.disablePolygonOffset();
//		GlStateManager.enableAlpha();
//		GlStateManager.depthMask(true);
//		GlStateManager.popMatrix();
//	}
//
//	private void drawBlockDamageTexture(Minecraft mc, TextureManager renderEngine, Tessellator tessellatorIn, BufferBuilder bufferBuilderIn, Entity entityIn, float partialTicks) {
//		double posX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
//		double posY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
//		double posZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
//
//		if (mc.world.getWorldTime() % 20 == 0) {
//			this.cleanupExtraDamagedBlocks();
//		}
//
//		if (!BlockBreakAnimationClientHandler.damagedBranches.isEmpty()) {
//			renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
//			this.preRenderDamagedBlocks();
//			bufferBuilderIn.begin(7, DefaultVertexFormats.BLOCK);
//			bufferBuilderIn.setTranslation(-posX, -posY, -posZ);
//			bufferBuilderIn.noColor();
//
//			for (Entry<Integer, DestroyBlockProgress> entry : BlockBreakAnimationClientHandler.damagedBranches.entrySet()) {
//				DestroyBlockProgress destroyblockprogress = entry.getValue();
//				BlockPos pos = destroyblockprogress.getPosition();
//				double delX = (double) pos.getX() - posX;
//				double delY = (double) pos.getY() - posY;
//				double delZ = (double) pos.getZ() - posZ;
//
//				if (delX * delX + delY * delY + delZ * delZ > 16384) {
//					BlockBreakAnimationClientHandler.damagedBranches.remove(entry.getKey());
//				} else {
//					BlockState state = mc.world.getBlockState(pos);
//					if(state.getBlock() instanceof BlockBranch) {
//						int k1 = destroyblockprogress.getPartialBlockDamage();
//						TextureAtlasSprite textureatlassprite = this.destroyBlockIcons[k1];
//						BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
//						if (state.getRenderType() == EnumBlockRenderType.MODEL) {
//							state = state.getActualState(mc.world, pos);
//							IBakedModel baseModel = blockrendererdispatcher.getBlockModelShapes().getModelForState(state);
//							IBakedModel damageModel = getDamageModel(baseModel, textureatlassprite, state, mc.world, pos);
//							blockrendererdispatcher.getBlockModelRenderer().renderModel(mc.world, damageModel, state, pos, bufferBuilderIn, true);
//						}
//					}
//				}
//			}
//
//			tessellatorIn.draw();
//			bufferBuilderIn.setTranslation(0.0D, 0.0D, 0.0D);
//			this.postRenderDamagedBlocks();
//		}
//	}
//
//	private IBakedModel getDamageModel(IBakedModel baseModel, TextureAtlasSprite texture, BlockState state, IBlockAccess world, BlockPos pos) {
//		state = state.getBlock().getExtendedState(state, world, pos);
//
//		if (baseModel instanceof ICustomDamageModel) {
//			ICustomDamageModel customDamageModel = (ICustomDamageModel) baseModel;
//			long rand = MathHelper.getPositionRandom(pos);
//
//			List<BakedQuad> generalQuads = Lists.<BakedQuad>newArrayList();
//			Map<Direction, List<BakedQuad>> faceQuads = Maps.newEnumMap(Direction.class);
//
//			for (Direction facing : Direction.values()) {
//				List<BakedQuad> quadList = Lists.newArrayList();
//				for (BakedQuad quad : customDamageModel.getCustomDamageQuads(state, facing, rand)) {
//					quadList.add(new BakedQuadRetextured(quad, texture));
//				}
//				faceQuads.put(facing, quadList);
//			}
//			for (BakedQuad quad : customDamageModel.getCustomDamageQuads(state, null, rand)) {
//				generalQuads.add(new BakedQuadRetextured(quad, texture));
//			}
//
//			return new SimpleBakedModel(generalQuads, faceQuads, baseModel.isAmbientOcclusion(state), baseModel.isGui3d(), baseModel.getParticleTexture(), baseModel.getItemCameraTransforms(), baseModel.getOverrides());
//		}
//
//		return (new SimpleBakedModel.Builder(state, baseModel, texture, pos)).makeBakedModel();
//	}
//
//
//	private static class RenderGlobalWrapper implements IWorldEventListener {
//
//		private World world;
//
//		public RenderGlobalWrapper(World world) {
//			this.world = world;
//		}
//
//		@Override
//		public void notifyBlockUpdate(World worldIn, BlockPos pos, BlockState oldState, BlockState newState, int flags) {
//			Minecraft.getInstance().renderGlobal.notifyBlockUpdate(worldIn, pos, oldState, newState, flags);
//		}
//
//		@Override
//		public void notifyLightSet(BlockPos pos) {
//			Minecraft.getInstance().renderGlobal.notifyLightSet(pos);
//		}
//
//		@Override
//		public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
//			Minecraft.getInstance().renderGlobal.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
//		}
//
//		@Override
//		public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x,
//                                             double y, double z, float volume, float pitch) {
//			Minecraft.getInstance().renderGlobal.playSoundToAllNearExcept(player, soundIn, category, x, y, z, volume, pitch);
//		}
//
//		@Override
//		public void playRecord(SoundEvent soundIn, BlockPos pos) {
//			Minecraft.getInstance().renderGlobal.playRecord(soundIn, pos);
//		}
//
//		@Override
//		public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord,
//				double xSpeed, double ySpeed, double zSpeed, int... parameters) {
//			Minecraft.getInstance().renderGlobal.spawnParticle(particleID, ignoreRange, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
//		}
//
//		@Override
//		public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z,
//				double xSpeed, double ySpeed, double zSpeed, int... parameters) {
//			Minecraft.getInstance().renderGlobal.spawnParticle(id, ignoreRange, p_190570_3_, x, y, z, xSpeed, ySpeed, zSpeed, parameters);
//		}
//
//		@Override
//		public void onEntityAdded(Entity entityIn) {
//			Minecraft.getInstance().renderGlobal.onEntityAdded(entityIn);
//		}
//
//		@Override
//		public void onEntityRemoved(Entity entityIn) {
//			Minecraft.getInstance().renderGlobal.onEntityRemoved(entityIn);
//		}
//
//		@Override
//		public void broadcastSound(int soundID, BlockPos pos, int data) {
//			Minecraft.getInstance().renderGlobal.broadcastSound(soundID, pos, data);
//		}
//
//		@Override
//		public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
//			Minecraft.getInstance().renderGlobal.playEvent(player, type, blockPosIn, data);
//		}
//
//		@Override
//		public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
//			BlockState state = world.getBlockState(pos);
//			if (state.getBlock() instanceof BlockBranchThick) {
//				BlockBreakAnimationClientHandler.instance.sendThickBranchBreakProgress(breakerId, pos, progress);
//			} else {
//				Minecraft.getInstance().renderGlobal.sendBlockBreakProgress(breakerId, pos, progress);
//			}
//		}
//
//	}
//
//}
