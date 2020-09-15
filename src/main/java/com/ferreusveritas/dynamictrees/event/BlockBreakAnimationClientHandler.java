package com.ferreusveritas.dynamictrees.event;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.lwjgl.opengl.GL11;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranchThick;
import com.ferreusveritas.dynamictrees.models.ICustomDamageModel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuadRetextured;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockBreakAnimationClientHandler implements ISelectiveResourceReloadListener {
	
	public static final BlockBreakAnimationClientHandler instance = new BlockBreakAnimationClientHandler(Minecraft.getMinecraft());
	private static final Map<Integer, DestroyBlockProgress> damagedBranches = new ConcurrentHashMap<Integer, DestroyBlockProgress>();
	
	private final TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];
	
	private BlockBreakAnimationClientHandler(Minecraft mc) {
		((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(this);
	}
	
	@SubscribeEvent
	public void onPlayerJoinWorldEvent(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityPlayerSP && Minecraft.getMinecraft().player != null) {
			if (Minecraft.getMinecraft().player.getEntityId() == event.getEntity().getEntityId()) {
				event.getWorld().removeEventListener(Minecraft.getMinecraft().renderGlobal);
				List<IWorldEventListener> listeners = ReflectionHelper.getPrivateValue(World.class, event.getWorld(), "eventListeners", "field_73021_x");
				if (listeners.stream().noneMatch((el) -> el instanceof RenderGlobalWrapper)) {
					event.getWorld().addEventListener(new RenderGlobalWrapper(event.getWorld()));
				}
			}
		}
	}
	
	@SubscribeEvent
	public void worldUnload(WorldEvent.Unload event) {
		BlockBreakAnimationClientHandler.damagedBranches.clear();
	}
	
	@SubscribeEvent
	public void worldLoad(WorldEvent.Load event) {
		BlockBreakAnimationClientHandler.damagedBranches.clear();
	}
	
	@SubscribeEvent
	public void renderBlockBreakAnim(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		TextureManager textureManager = mc.getTextureManager();
		
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE, SourceFactor.ONE, DestFactor.ZERO);
		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		this.drawBlockDamageTexture(mc, textureManager, Tessellator.getInstance(), Tessellator.getInstance().getBuffer(), mc.getRenderViewEntity(), event.getPartialTicks());
		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
		GlStateManager.disableBlend();
	}
	
	private void cleanupExtraDamagedBlocks() {
		Iterator<Entry<Integer, DestroyBlockProgress>> iter = BlockBreakAnimationClientHandler.damagedBranches.entrySet().iterator();
		
		while(iter.hasNext()) {
			DestroyBlockProgress destroyblockprogress = iter.next().getValue();//entry.getValue();
			int tick = destroyblockprogress.getCreationCloudUpdateTick();
			
			if (Minecraft.getMinecraft().world.getWorldTime() - tick > 400) {
				iter.remove();
			}
		}
	}
	
	public void sendThickBranchBreakProgress(int breakerId, BlockPos pos, int progress) {
		if (progress >= 0 && progress < 10) {
			DestroyBlockProgress destroyblockprogress = BlockBreakAnimationClientHandler.damagedBranches.get(Integer.valueOf(breakerId));
			
			if (destroyblockprogress == null || destroyblockprogress.getPosition().getX() != pos.getX() || destroyblockprogress.getPosition().getY() != pos.getY() || destroyblockprogress.getPosition().getZ() != pos.getZ()) {
				destroyblockprogress = new DestroyBlockProgress(breakerId, pos);
				BlockBreakAnimationClientHandler.damagedBranches.put(Integer.valueOf(breakerId), destroyblockprogress);
			}
			
			destroyblockprogress.setPartialBlockDamage(progress);
			destroyblockprogress.setCloudUpdateTick((int) Minecraft.getMinecraft().world.getWorldTime());
		} else {
			BlockBreakAnimationClientHandler.damagedBranches.remove(breakerId);
		}
	}
	
	
	//Optifine patches the method in net.minecraft.client.renderer.RenderGlobal with special boilerplate code
	//for shaders.  The best way to handle this is to simply grant ourselves access to those private methods and
	//call the vanilla functions.  If they were patched then we get to run the patched code too.
	static Method preRenderMethod;
	static Method postRenderMethod;
	
	static {
		preRenderMethod = ReflectionHelper.findMethod(RenderGlobal.class, "preRenderDamagedBlocks", "func_180443_s");
		preRenderMethod.setAccessible(true);
		
		postRenderMethod = ReflectionHelper.findMethod(RenderGlobal.class, "postRenderDamagedBlocks", "func_174969_t");
		postRenderMethod.setAccessible(true);
	}

	
	private void preRenderDamagedBlocks() {
		try { preRenderMethod.invoke(Minecraft.getMinecraft().renderGlobal, new Object[] {}); } catch (Exception e) { }
	}
	
	private void postRenderDamagedBlocks() {
		try { postRenderMethod.invoke(Minecraft.getMinecraft().renderGlobal, new Object[] {}); } catch (Exception e) { }
	}
	
	private void drawBlockDamageTexture(Minecraft mc, TextureManager renderEngine, Tessellator tessellatorIn, BufferBuilder bufferBuilderIn, Entity entityIn, float partialTicks) {
		double posX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
		double posY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
		double posZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
		
		if (mc.world.getWorldTime() % 20 == 0) {
			cleanupExtraDamagedBlocks();
		}
		
		if (!BlockBreakAnimationClientHandler.damagedBranches.isEmpty()) {
			renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			this.preRenderDamagedBlocks();
			bufferBuilderIn.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			bufferBuilderIn.setTranslation(-posX, -posY, -posZ);
			bufferBuilderIn.noColor();
			
			for (Entry<Integer, DestroyBlockProgress> entry : BlockBreakAnimationClientHandler.damagedBranches.entrySet()) {
				DestroyBlockProgress destroyblockprogress = entry.getValue();
				BlockPos pos = destroyblockprogress.getPosition();
				double delX = (double) pos.getX() - posX;
				double delY = (double) pos.getY() - posY;
				double delZ = (double) pos.getZ() - posZ;
				
				if (delX * delX + delY * delY + delZ * delZ > 16384) {
					BlockBreakAnimationClientHandler.damagedBranches.remove(entry.getKey());
				} else {
					IBlockState state = mc.world.getBlockState(pos);
					if(state.getBlock() instanceof BlockBranch) {
						int k1 = destroyblockprogress.getPartialBlockDamage();
						TextureAtlasSprite textureatlassprite = this.destroyBlockIcons[k1];
						BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
						if (state.getRenderType() == EnumBlockRenderType.MODEL) {
							state = state.getActualState(mc.world, pos);
							IBakedModel baseModel = blockrendererdispatcher.getBlockModelShapes().getModelForState(state);
							IBakedModel damageModel = getDamageModel(baseModel, textureatlassprite, state, mc.world, pos);
							blockrendererdispatcher.getBlockModelRenderer().renderModel(mc.world, damageModel, state, pos, bufferBuilderIn, true);
						}
					} else {
						BlockBreakAnimationClientHandler.damagedBranches.remove(entry.getKey());
					}
				}
			}
			
			tessellatorIn.draw();
			bufferBuilderIn.setTranslation(0.0D, 0.0D, 0.0D);
			this.postRenderDamagedBlocks();
		}
	}
	
	
	private IBakedModel getDamageModel(IBakedModel baseModel, TextureAtlasSprite texture, IBlockState state, IBlockAccess world, BlockPos pos) {
		state = state.getBlock().getExtendedState(state, world, pos);
		
		if (baseModel instanceof ICustomDamageModel) {
			
			ICustomDamageModel customDamageModel = (ICustomDamageModel) baseModel;
			long rand = MathHelper.getPositionRandom(pos);
			
			List<BakedQuad> generalQuads = Lists.<BakedQuad>newArrayList();
			Map<EnumFacing, List<BakedQuad>> faceQuads = Maps.newEnumMap(EnumFacing.class);
			
			for (EnumFacing facing : EnumFacing.values()) {
				List<BakedQuad> quadList = Lists.newArrayList();
				for (BakedQuad quad : customDamageModel.getCustomDamageQuads(state, facing, rand)) {
					quadList.add(new BakedQuadRetextured(quad, texture));
				}
				faceQuads.put(facing, quadList);
			}
			for (BakedQuad quad : customDamageModel.getCustomDamageQuads(state, null, rand)) {
				generalQuads.add(new BakedQuadRetextured(quad, texture));
			}
			
			return new SimpleBakedModel(generalQuads, faceQuads, baseModel.isAmbientOcclusion(state), baseModel.isGui3d(), baseModel.getParticleTexture(), baseModel.getItemCameraTransforms(), baseModel.getOverrides());
		}
		
		
		return (new SimpleBakedModel.Builder(state, baseModel, texture, pos)).makeBakedModel();
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
				BlockBreakAnimationClientHandler.instance.sendThickBranchBreakProgress(breakerId, pos, progress);
			} else {
				Minecraft.getMinecraft().renderGlobal.sendBlockBreakProgress(breakerId, pos, progress);
			}
		}
		
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
		if(resourcePredicate.test(VanillaResourceType.TEXTURES)) {
			TextureMap texturemap = Minecraft.getMinecraft().getTextureMapBlocks();
			
			for (int i = 0; i < this.destroyBlockIcons.length; ++i) {
				this.destroyBlockIcons[i] = texturemap.getAtlasSprite("minecraft:blocks/destroy_stage_" + i);
			}
		}
	}
	
}
