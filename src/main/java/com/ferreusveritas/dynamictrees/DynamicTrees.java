package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesPropertiesJson;
import com.ferreusveritas.dynamictrees.cells.CellKits;
import com.ferreusveritas.dynamictrees.client.thickrings.ThickRingSpriteUploader;
import com.ferreusveritas.dynamictrees.client.thickrings.ThickRingTextureManager;
import com.ferreusveritas.dynamictrees.compat.CompatHandler;
import com.ferreusveritas.dynamictrees.event.handlers.*;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKits;
import com.ferreusveritas.dynamictrees.init.DTClient;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;
import com.ferreusveritas.dynamictrees.worldgen.WorldGenEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("dynamictrees")
public class DynamicTrees {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public static final String MOD_ID = "dynamictrees";
	public static final String NAME = "Dynamic Trees";
	public static final String VERSION = "1.16.5-9999.9999.9999z";//Maxed out version to satisfy dependencies during dev, Assigned from gradle during build, do not change
	
	public static final String OPT_AFTER = "after:";
	public static final String OPT_BEFORE = "before:";
	public static final String REQ_AFTER = "required-after:";
	public static final String REQ_BEFORE = "required-before:";
	public static final String NEXT = ";";
	public static final String AT = "@[";
	public static final String GREATER_THAN = "@(";
	public static final String OR_GREATER = ",)";

	public static final String SERENE_SEASONS = "sereneseasons";

	// Other mods can use this string to depend on the latest version of Dynamic Trees
	public static final String DT_LATEST = MOD_ID + AT + VERSION + OR_GREATER;

	public static final String MINECRAFT_ID = "minecraft";

	//	//Other Add-on Mods
	//	public static final String DYNAMICTREESBOP = "dynamictreesbop";
	//	public static final String DYNAMICTREESTC = "dynamictreestc";
	//	public static final String DYNAMICTREESPHC = "dynamictreesphc";
	//	public static final String DYNAMICTREESTRAVERSE = "dttraverse";
	//	public static final String DYNAMICTREESHNC = "dynamictreeshnc";
	//	public static final String RUSTIC = "rustic";
	//
	//	//Other Mod Versions.. These have been added to avoid the whole "Duh.. How come my mod is crashing?" bullshit bug reports.
	//	public static final String DYNAMICTREESBOP_VER = GREATERTHAN + "1.4.1d" + ORGREATER;
	//	public static final String DYNAMICTREESTC_VER =	 GREATERTHAN + "1.4.1d" + ORGREATER;
	//	public static final String DYNAMICTREESPHC_VER = GREATERTHAN + "1.4.1e" + ORGREATER;
	//	public static final String DYNAMICTREESTRAVERSE_VER =  GREATERTHAN + "1.4" + ORGREATER;//Traverse will need a new build. Display an error rather than crash.
	//	public static final String DYNAMICTREESHNC_VER =  GREATERTHAN + "1.1" + ORGREATER;//Heat and Climate Add-on has not be updated in a while and the latest 1.1 is not longer supported
	//	public static final String RUSTIC_VER = GREATERTHAN + "1.0.14" + ORGREATER;
	//	public static final String RECURRENT_COMPLEX = "reccomplex";//Load after recurrent complex to allow it to generate it's content first
	//
	//	//Forge
	//	private static final String FORGE = "forge";
	//	public static final String FORGE_VER = FORGE + AT + "14.23.5.2768" + ORGREATER;
	//
	//	public static final String DEPENDENCIES
	//			= REQAFTER + FORGE_VER
	//			+ NEXT
	//			+ OPTBEFORE + RUSTIC + RUSTIC_VER
	//			+ NEXT
	//			+ OPTBEFORE + DYNAMICTREESBOP + DYNAMICTREESBOP_VER
	//			+ NEXT
	//			+ OPTBEFORE + DYNAMICTREESTC + DYNAMICTREESTC_VER
	//			+ NEXT
	//			+ OPTBEFORE + DYNAMICTREESTC + DYNAMICTREESPHC_VER
	//			+ NEXT
	//			+ OPTBEFORE + DYNAMICTREESTRAVERSE + DYNAMICTREESTRAVERSE_VER
	//			+ NEXT
	//			+ OPTBEFORE + DYNAMICTREESHNC + DYNAMICTREESHNC_VER
	//			+ NEXT
	//			+ OPTAFTER + RECURRENT_COMPLEX
	//			;
	//
	
	public enum EnumAxeDamage {
		VANILLA,
		THICKNESS,
		VOLUME
	}
	
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
		SETRADIUS,
		HARVEST,
		ROT,
		OVERFLOW
	}
	
	public DynamicTrees() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, DTConfigs.SERVER_CONFIG);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DTConfigs.COMMON_CONFIG);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DTConfigs.CLIENT_CONFIG);

		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		DistExecutor.runWhenOn(Dist.CLIENT, ()->()-> clientStart(modEventBus));

		CellKits.setup();
		GrowthLogicKits.setup();
		TreeGenerator.setup();
		
		DTRegistries.setupBlocks();
		DTRegistries.setupItems();
		
		DTRegistries.setupEntities();
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
		
		registerCommonEventHandlers();
		
		MinecraftForge.EVENT_BUS.register(this);

		CompatHandler.init();
//		DTTrees.setupExtraSoils(); // TODO: Should this be called here? Where is post-init in this version?!
	}

	//TODO: thick rings are loading before the resourcelocations are added, need to find an event that fires then.
	private static void clientStart(IEventBus modEventBus) {
//		modEventBus.addListener(EventPriority.NORMAL, false, ColorHandlerEvent.Block.class, setupEvent -> {
//			IResourceManager manager = Minecraft.getInstance().getResourceManager();
//			if (manager instanceof IReloadableResourceManager){
//				ThickRingSpriteUploader uploader = new ThickRingSpriteUploader(Minecraft.getInstance().textureManager);
//				((IReloadableResourceManager) manager).addReloadListener(uploader);
//			}
//		});
	}

	@SuppressWarnings("deprecation")
	private void commonSetup(final FMLCommonSetupEvent event) {
//		LeavesPropertiesJson.resolveAll();
		DeferredWorkQueue.runLater(this::registerDendroRecipes);
//		cleanUp();
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		DTClient.setup();
	}
	
	private void registerDendroRecipes () {
		DTRegistries.dendroPotion.registerRecipes();
	}
	
	public void cleanUp() {
		LeavesPropertiesJson.cleanUp();
		TreeRegistry.cleanupCellKit();
		TreeRegistry.cleanupGrowthLogicKit();
	}
	
	public void registerCommonEventHandlers() {
		MinecraftForge.EVENT_BUS.register(new CommonEventHandler());
		MinecraftForge.EVENT_BUS.register(new ServerEventHandler());
		if(DTConfigs.worldGen.get()) {
			MinecraftForge.EVENT_BUS.register(new WorldGenEvents());
		}
		
		if (ModList.get().isLoaded("fastleafdecay")) {
			MinecraftForge.EVENT_BUS.register(new LeafUpdateEventHandler());
		}
		
		//An event for dealing with Vanilla Saplings
		if(DTConfigs.replaceVanillaSapling.get()) {
			MinecraftForge.EVENT_BUS.register(new VanillaSaplingEventHandler());
		}

//		MinecraftForge.EVENT_BUS.register(new SafeChunkEvents());

		//Conveniently accessible disaster(Optional World Generation)
		if(WorldGenRegistry.isWorldGenEnabled()) {
			MinecraftForge.EVENT_BUS.register(new PoissonDiscEventHandler());
		}
	}

	public static Logger getLogger() {
		return LOGGER;
	}
}
