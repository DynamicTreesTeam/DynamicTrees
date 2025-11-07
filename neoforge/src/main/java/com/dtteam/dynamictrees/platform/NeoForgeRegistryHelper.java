package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlockEntity;
import com.dtteam.dynamictrees.platform.services.IRegistryHelper;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryHandler;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryLoader;
import com.dtteam.dynamictrees.registry.PottedSaplingBlockEntityNF;
import com.dtteam.dynamictrees.registry.RegistryLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

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
    public BlockEntityType.BlockEntitySupplier<PottedSaplingBlockEntity> getPottedSaplingBlockEntity() {
        return PottedSaplingBlockEntityNF::new;
    }

    @Override
    public PottedSaplingBlockEntity newPottedSaplingBlockEntity(BlockPos pPos, BlockState pState) {
        return new PottedSaplingBlockEntityNF(pPos, pState);
    }

}