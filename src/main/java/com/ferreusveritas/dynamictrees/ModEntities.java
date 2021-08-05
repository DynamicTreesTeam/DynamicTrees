package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

@Mod.EventBusSubscriber(modid = ModConstants.MODID)
public class ModEntities {

	public final static String FALLING_TREE = "falling_tree";

	@SubscribeEvent
	public static void register(RegistryEvent.Register<EntityEntry> event) {
		int id = 0;
		EntityRegistry.registerModEntity(new ResourceLocation(ModConstants.MODID, FALLING_TREE), EntityFallingTree.class, FALLING_TREE, id++, ModConstants.MODID, 512, Integer.MAX_VALUE, true);
	}

}
