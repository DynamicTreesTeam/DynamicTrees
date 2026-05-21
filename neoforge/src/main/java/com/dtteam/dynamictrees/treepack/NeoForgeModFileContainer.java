package com.dtteam.dynamictrees.treepack;

import com.dtteam.dynamictrees.DynamicTrees;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
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
    public @NotNull Optional<Path> findResource(String subPath) {
        Collection<Path> rootPaths = modFile.getContents().getContentRoots();
        for (Path rootPath : rootPaths) {
            Path resourcePath;
            if (Files.isDirectory(rootPath)){
                //folder is as a plan regular directory
                resourcePath = rootPath.resolve(subPath);
            } else {
                //folder is inside a zip file
                URI jarUri = (URI.create("jar:" + rootPath.toUri()));
                try {
                    try {
                        resourcePath = FileSystems.newFileSystem(jarUri, Map.of()).getPath(subPath);
                    } catch (FileSystemAlreadyExistsException e){
                        resourcePath = FileSystems.getFileSystem(jarUri).getPath(subPath);
                    }
                } catch (IOException e){
                    DynamicTrees.LOG.error("{}", e.getMessage());
                    return Optional.empty();
                }
            }
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
