package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.platform.services.IPlatformHelper;
import com.dtteam.dynamictrees.treepack.FabricModFileContainer;
import com.dtteam.dynamictrees.treepack.ModFileContainer;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;
import java.util.stream.Collectors;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public List<ModFileContainer> getMods() {
        return FabricLoader.getInstance().getAllMods().stream().map(FabricModFileContainer::new).collect(Collectors.toList());
    }

}
