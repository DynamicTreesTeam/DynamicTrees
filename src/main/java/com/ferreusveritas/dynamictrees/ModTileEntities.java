package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.tileentity.TileEntityBonsai;
import com.ferreusveritas.dynamictrees.tileentity.TileEntitySpecies;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModTileEntities {

	public static void preInit() {
		//In 1.13 these will need to change to the proper Dynamic Trees domain but unfortunately for now it'll have
		//to stay in the minecraft domain for backwards compatibility with existing worldsaves.
		GameRegistry.registerTileEntity(TileEntitySpecies.class, new ResourceLocation("minecraft", "species_tile_entity"));
		GameRegistry.registerTileEntity(TileEntityBonsai.class, new ResourceLocation("minecraft", "bonsai_tile_entity"));
	}

}
