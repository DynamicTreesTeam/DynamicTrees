package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.compat.CompatHandler;
import com.ferreusveritas.dynamictrees.event.handlers.EventHandlers;
import com.ferreusveritas.dynamictrees.init.DTClient;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DynamicTrees.MOD_ID)
public class DynamicTrees {

	public static final String MOD_ID = "dynamictrees";
	public static final String NAME = "Dynamic Trees";

	public static final String MINECRAFT = "minecraft";
	public static final String FORGE = "forge";
	public static final String SERENE_SEASONS = "sereneseasons";
	public static final String FAST_LEAF_DECAY = "fastleafdecay";

	public enum EnumAxeDamage {
		VANILLA,
		THICKNESS,
		VOLUME
	}

	@Deprecated
	public enum VanillaWoodTypes {
		oak,
		spruce,
		birch,
		jungle,
		dark_oak,
		acacia,
		warped,
		crimson;

		public Block getLog() {
			switch(this) {
				default:
				case oak: return Blocks.OAK_LOG;
				case birch: return Blocks.BIRCH_LOG;
				case spruce: return Blocks.SPRUCE_LOG;
				case jungle: return Blocks.JUNGLE_LOG;
				case dark_oak: return Blocks.DARK_OAK_LOG;
				case acacia: return Blocks.ACACIA_LOG;
				case warped: return Blocks.WARPED_STEM;
				case crimson: return Blocks.CRIMSON_STEM;
			}
		}

		public Block getStrippedLog() {
			switch (this) {
				default:
				case oak: return Blocks.STRIPPED_OAK_LOG;
				case birch: return Blocks.STRIPPED_BIRCH_LOG;
				case spruce: return Blocks.STRIPPED_SPRUCE_LOG;
				case jungle: return Blocks.STRIPPED_JUNGLE_LOG;
				case dark_oak: return Blocks.STRIPPED_DARK_OAK_LOG;
				case acacia: return Blocks.STRIPPED_ACACIA_LOG;
				case warped: return Blocks.STRIPPED_WARPED_STEM;
				case crimson: return Blocks.STRIPPED_CRIMSON_STEM;
			}
		}
	}
	
	public enum EnumDestroyMode {
		SLOPPY,
		SET_RADIUS,
		HARVEST,
		ROT,
		OVERFLOW
	}

	public DynamicTrees() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		final ModLoadingContext loadingContext = ModLoadingContext.get();

		loadingContext.registerConfig(ModConfig.Type.SERVER, DTConfigs.SERVER_CONFIG);
		loadingContext.registerConfig(ModConfig.Type.COMMON, DTConfigs.COMMON_CONFIG);
		loadingContext.registerConfig(ModConfig.Type.CLIENT, DTConfigs.CLIENT_CONFIG);

		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> DTClient::clientStart);

		TreeGenerator.setup();
		
		DTRegistries.setupBlocks();
		DTRegistries.setupItems();
		
		DTRegistries.setupEntities();
		
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::clientSetup);
		modEventBus.addListener(this::parallelDispatch);

		EventHandlers.registerCommon();
		CompatHandler.init();
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		DTClient.setup();
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		// TODO: Fix this for new Json system.
		for (Species species : Species.REGISTRY) {
			final BlockState primitiveSaplingState = species.getPrimitiveSapling();

			if (primitiveSaplingState != null)
				TreeRegistry.registerSaplingReplacer(primitiveSaplingState, species);
		}
	}

	private void parallelDispatch(final ParallelDispatchEvent event) {
		event.enqueueWork(() -> DTRegistries.dendroPotion.registerRecipes());
	}

	public static ResourceLocation resLoc (final String path) {
		return new ResourceLocation(MOD_ID, path);
	}

}
