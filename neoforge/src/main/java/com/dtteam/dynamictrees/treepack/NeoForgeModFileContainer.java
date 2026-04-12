package com.dtteam.dynamictrees.treepack;

import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
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
        return Optional.of(Path.of(modFile.getFilePath().toString(), strings));
    }

    @Override
    public @NotNull String getModId() {
        return modInfo.getModId();
    }
}
