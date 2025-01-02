package com.dtteam.dynamictrees.treepack;

import java.nio.file.Path;
import java.util.Optional;

public abstract class ModFileContainer {

    public abstract Optional<Path> findResource(String strings);

    public abstract String getModId();

}
