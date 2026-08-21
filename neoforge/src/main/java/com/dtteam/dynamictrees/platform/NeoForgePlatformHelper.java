package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.platform.services.IPlatformHelper;
import com.dtteam.dynamictrees.treepack.ModFileContainer;
import com.dtteam.dynamictrees.treepack.NeoForgeModFileContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

import java.util.List;
import java.util.stream.Collectors;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.getCurrent().isProduction();
    }

    public List<ModFileContainer> getMods(){
        return ModList.get().getMods().stream().map((NeoForgeModFileContainer::new)).collect(Collectors.toList());
    }
}