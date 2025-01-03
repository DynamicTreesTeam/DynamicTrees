package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlockEntity;
import com.dtteam.dynamictrees.config.FabricRegistryLoader;
import com.dtteam.dynamictrees.registry.RegistryLoader;
import com.dtteam.dynamictrees.platform.services.IRegistryHelper;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class FabricRegistryHelper implements IRegistryHelper {

    private static RegistryLoader registriesInstance;
    @Override
    public RegistryLoader getRegistryLoader() {
        if (registriesInstance == null){
            registriesInstance = new FabricRegistryLoader();
        }
        return registriesInstance;
    }

    @Override
    public RegistryHandler newRegistryHandler() {
        return null;
    }

    @Override
    public RegistryHandler newRegistryHandler(String modId) {
        return null;
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<PottedSaplingBlockEntity> getPottedSaplingBlockEntity() {
        return PottedSaplingBlockEntity::new;
    }

}