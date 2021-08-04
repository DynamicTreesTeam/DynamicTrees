package com.ferreusveritas.dynamictrees.resources;

import net.minecraftforge.forgespi.locating.IModFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sub-class of {@link TreeResourcePack} for {@link IModFile} so tree resources are automatically loaded from other
 * mods.
 *
 * @author Harley O'Connor
 */
public final class ModTreeResourcePack extends TreeResourcePack {

    private final IModFile modFile;

    public ModTreeResourcePack(final Path path, final IModFile modFile) {
        super(path);
        this.modFile = modFile;
    }

    @Override
    protected Path getPath(String... paths) {
        final List<String> pathsList = new ArrayList<>(Arrays.asList(paths));
        pathsList.add(0, DTResourceRegistries.TREES);
        return this.modFile.getLocator().findPath(this.modFile, pathsList.toArray(new String[0])).toAbsolutePath();
    }

}
