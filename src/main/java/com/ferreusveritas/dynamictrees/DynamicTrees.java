package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.command.DTCommand;
import com.ferreusveritas.dynamictrees.compat.CompatHandler;
import com.ferreusveritas.dynamictrees.proxy.CommonProxy;
import com.ferreusveritas.dynamictrees.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.worldgen.WorldGeneratorTrees;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Logger;

/**
* 2016-2018 Ferreusveritas
*/
@Mod(modid = ModConstants.MODID, name = ModConstants.NAME, version=ModConstants.VERSION, dependencies=ModConstants.DEPENDENCIES, updateJSON = ModConstants.UPDATE_CHECKER)
public class DynamicTrees {
	
	@Mod.Instance(ModConstants.MODID)
	public static DynamicTrees instance;
	
	@SidedProxy(clientSide = ModConstants.CLIENT_PROXY, serverSide = ModConstants.COMMON_PROXY)
	public static CommonProxy proxy;
	
	public static Logger log;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		log = event.getModLog();
		proxy.preInit(event);
		CompatHandler.preInit();
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit();
		proxy.cleanUp();
	}
	
	@Mod.EventHandler
	public static void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new DTCommand());
		SeasonHelper.getSeasonManager().flushMappings();
	}
	
	@Mod.EventHandler
	public static void serverStopped(FMLServerStoppedEvent event) {
		WorldGeneratorTrees.clearFlatWorldCache();
	}
	
}
