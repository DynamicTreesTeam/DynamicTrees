package com.dtteam.dynamictrees.treepack;

import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class NeoForgeModFileContainer extends ModFileContainer {

    public final IModInfo modInfo;
    public final IModFile modFile;

    public NeoForgeModFileContainer(IModInfo modInfo) {
        this.modInfo = modInfo;
        this.modFile = modInfo.getOwningFile().getFile();
    }

    //TODO: not sure if this works
    @Override
    public @NotNull Optional<Path> findResource(String strings) {
        Collection<Path> rootPaths = modFile.getContents().getContentRoots();
        for (Path rootPath : rootPaths) {
            Path resourcePath = rootPath.resolve(strings);
            if (Files.exists(resourcePath)) {
                return Optional.of(resourcePath);
            }
        }
        return Optional.empty();
    }

    @Override
    public @NotNull String getModId() {
        return modInfo.getModId();
    }
}
