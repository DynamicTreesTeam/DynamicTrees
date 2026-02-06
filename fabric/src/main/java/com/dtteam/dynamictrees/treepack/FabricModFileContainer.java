package com.dtteam.dynamictrees.treepack;

import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class FabricModFileContainer extends ModFileContainer {

    private final ModContainer modContainer;

    public FabricModFileContainer(ModContainer modContainer) {
        this.modContainer = modContainer;
    }

    @Override
    public Optional<Path> findResource(String strings) {
        List<Path> rootPaths = modContainer.getRootPaths();
        for (Path rootPath : rootPaths) {
            Path resourcePath = rootPath.resolve(strings);
            if (Files.exists(resourcePath)) {
                return Optional.of(resourcePath);
            }
        }
        return Optional.empty();
    }

    @Override
    public String getModId() {
        return modContainer.getMetadata().getId();
    }
}
