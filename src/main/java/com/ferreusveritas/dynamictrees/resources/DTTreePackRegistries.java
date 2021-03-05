package com.ferreusveritas.dynamictrees.resources;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.trees.TreeFamilyManager;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.google.common.collect.Lists;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * @author Harley O'Connor
 */
public final class DTTreePackRegistries {

    public static DTTreePackRegistries INSTANCE;

    private final TreesResourceManager treesResourceManager = new TreesResourceManager();
    private TreeFamilyManager treeFamilyManager;

    public DTTreePackRegistries() {
        INSTANCE = this;
    }

    public void setupTreesResources () {
        this.treeFamilyManager = new TreeFamilyManager();
        this.treesResourceManager.addLoadListener(this.treeFamilyManager);

        final AddTreesResourcePackEvent addTreesResourcePackEvent = new AddTreesResourcePackEvent();
        MinecraftForge.EVENT_BUS.post(addTreesResourcePackEvent);
        addTreesResourcePackEvent.treeResourcePacks.forEach(this.treesResourceManager::addResourcePack);

        // Add dynamic trees last so other add-ons take priority.
        this.treesResourceManager.addResourcePack(new TreeResourcePack(JsonHelper.getDirectory(JsonHelper.ResourceFolder.TREES)));

        final AddTreesLoadListenerEvent addLoadListenerEvent = new AddTreesLoadListenerEvent();
        MinecraftForge.EVENT_BUS.post(addLoadListenerEvent);
        addLoadListenerEvent.loadListeners.forEach(this.treesResourceManager::addLoadListener);
    }

    public TreesResourceManager getTreesResourceManager() {
        return treesResourceManager;
    }

    public static final class AddTreesLoadListenerEvent extends Event {

        private final List<ILoadListener> loadListeners = Lists.newArrayList();

        public void addLoadListener (final ILoadListener loadListener) {
            loadListeners.add(loadListener);
        }

    }

    public static final class AddTreesResourcePackEvent extends Event {

        private final List<TreeResourcePack> treeResourcePacks = Lists.newArrayList();

        public void addResourcePack (final TreeResourcePack treeResourcePack) {
            this.treeResourcePacks.add(treeResourcePack);
        }

    }

}
