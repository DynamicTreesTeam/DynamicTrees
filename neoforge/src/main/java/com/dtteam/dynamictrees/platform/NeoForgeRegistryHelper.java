package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.api.registry.*;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlockEntity;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.registry.*;
import com.dtteam.dynamictrees.platform.services.IRegistryHelper;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class NeoForgeRegistryHelper implements IRegistryHelper {

    private static RegistryLoader registriesInstance;
    @Override
    public RegistryLoader getRegistryLoader() {
        if (registriesInstance == null){
            registriesInstance = new NeoForgeRegistryLoader();
        }
        return registriesInstance;
    }

    @Override
    public RegistryHandler newRegistryHandler() {
        return new NeoForgeRegistryHandler();
    }
    @Override
    public RegistryHandler newRegistryHandler(String modId) {
        return new NeoForgeRegistryHandler(modId);
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<PottedSaplingBlockEntity> getPottedSaplingBlockEntity() {
        return PottedSaplingBlockEntityNF::new;
    }

}