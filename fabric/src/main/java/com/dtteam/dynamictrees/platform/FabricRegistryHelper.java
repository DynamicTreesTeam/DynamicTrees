package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlockEntity;
import com.dtteam.dynamictrees.registry.FabricRegistryHandler;
import com.dtteam.dynamictrees.registry.FabricRegistryLoader;
import com.dtteam.dynamictrees.registry.RegistryLoader;
import com.dtteam.dynamictrees.platform.services.IRegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

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
        return new FabricRegistryHandler();
    }

    @Override
    public RegistryHandler newRegistryHandler(String modId) {
        return new FabricRegistryHandler(modId);
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<PottedSaplingBlockEntity> getPottedSaplingBlockEntity() {
        return PottedSaplingBlockEntity::new;
    }

    @Override
    public PottedSaplingBlockEntity newPottedSaplingBlockEntity(BlockPos pPos, BlockState pState) {
        return new PottedSaplingBlockEntity(pPos, pState);
    }

}