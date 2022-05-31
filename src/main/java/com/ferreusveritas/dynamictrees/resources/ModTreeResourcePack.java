package com.ferreusveritas.dynamictrees.resources;

import net.minecraftforge.forgespi.locating.IModFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link FlatTreeResourcePack} for loading resources packaged with mods. This
 * will load resources from the {@code trees} folder under their resources, given that it exists.
 *
 * @author Harley O'Connor
 */
public final class ModTreeResourcePack extends FlatTreeResourcePack {

    private final IModFile modFile;

    public ModTreeResourcePack(final Path path, final IModFile modFile) {
        super(path);
        this.modFile = modFile;
    }

    @Override
    protected Path getPath(String... paths) {
        final List<String> pathsList = new ArrayList<>(Arrays.asList(paths));
        pathsList.add(0, Resources.TREES);
        return this.modFile.findResource(pathsList.toArray(new String[0]))
                .toAbsolutePath();
    }

}
