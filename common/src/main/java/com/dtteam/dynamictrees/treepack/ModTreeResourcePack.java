//package com.dtteam.dynamictrees.treepack;
//
//import net.minecraft.server.packs.PackLocationInfo;
//
//import java.nio.file.Path;
//
///**
// * A {@link FlatTreeResourcePack} for loading resources packaged with mods. This
// * will load resources from the {@code trees} folder under their resources, given that it exists.
// *
// * @author Harley O'Connor
// */
//public final class ModTreeResourcePack extends FlatTreeResourcePack {
//
//    private final ModFileContainer modFile;
//    public ModTreeResourcePack(PackLocationInfo location, Path root, ModFileContainer modFile) {
//        super(location, root);
//        this.modFile = modFile;
//    }
//
//    @Override
//    protected Path resolve(String paths) {
//        String[] newPaths = new String[paths.length + 1];
//        newPaths[0] = Resources.TREES;
//        System.arraycopy(paths, 0, newPaths, 1, paths.length);
//        return modFile.resourceFinder.apply(newPaths).toAbsolutePath();
//    }
//}
