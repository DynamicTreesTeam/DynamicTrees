package com.dtteam.dynamictrees.treepack;

import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Path;
import java.util.Optional;

public class FabricModFileContainer extends ModFileContainer {

    private final ModContainer modContainer;

    public FabricModFileContainer(ModContainer modContainer) {
        this.modContainer = modContainer;
    }

    @Override
    public Optional<Path> findResource(String strings) {
        return modContainer.findPath(strings);
    }

    @Override
    public String getModId() {
        return modContainer.getMetadata().getId();
    }
}
