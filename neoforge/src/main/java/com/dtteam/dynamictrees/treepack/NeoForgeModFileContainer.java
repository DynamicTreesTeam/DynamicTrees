package com.dtteam.dynamictrees.treepack;

import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class NeoForgeModFileContainer extends ModFileContainer {

    public final IModInfo modInfo;
    public final IModFile modFile;

    public NeoForgeModFileContainer(IModInfo modInfo) {
        this.modInfo = modInfo;
        this.modFile = modInfo.getOwningFile().getFile();
    }

    @Override
    public @NotNull Optional<Path> findResource(String strings) {
        Path filePath = modFile.getFilePath();
        if (Files.isDirectory(filePath)) {
            Path resolved = filePath.resolve(strings);
            return Files.exists(resolved) ? Optional.of(resolved) : Optional.empty();
        }
        try {
            URI uri = URI.create("jar:" + filePath.toUri());
            FileSystem fs;
            try {
                fs = FileSystems.getFileSystem(uri);
            } catch (FileSystemNotFoundException ignored) {
                fs = FileSystems.newFileSystem(uri, Map.of());
            }
            Path resolved = fs.getPath(strings);
            return Files.exists(resolved) ? Optional.of(resolved) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public @NotNull String getModId() {
        return modInfo.getModId();
    }
}
