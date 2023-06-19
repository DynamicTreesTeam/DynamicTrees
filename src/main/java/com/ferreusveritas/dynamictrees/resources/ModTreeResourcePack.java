package com.ferreusveritas.dynamictrees.resources;

import net.minecraftforge.forgespi.locating.IModFile;

import java.nio.file.Path;

/**
 * A {@link FlatTreeResourcePack} for loading resources packaged with mods. This
 * will load resources from the {@code trees} folder under their resources, given that it exists.
 *
 * @author Harley O'Connor
 */
public final class ModTreeResourcePack extends FlatTreeResourcePack {
    private final IModFile modFile;

    public ModTreeResourcePack(String packId, boolean isBuiltin, final Path path, final IModFile modFile) {
        super(packId, isBuiltin, path);
        this.modFile = modFile;
    }

    @Override
    protected Path resolve(String... paths) {
        String[] newPaths = new String[paths.length + 1];
        newPaths[0] = Resources.TREES;
        System.arraycopy(paths, 0, newPaths, 1, paths.length);
        return this.modFile.findResource(newPaths).toAbsolutePath();
    }
}
