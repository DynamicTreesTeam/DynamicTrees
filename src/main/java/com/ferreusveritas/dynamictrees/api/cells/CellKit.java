package com.ferreusveritas.dynamictrees.api.cells;

import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.SimpleRegistry;
import com.ferreusveritas.dynamictrees.cell.LeafClusters;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import net.minecraft.resources.ResourceLocation;

public abstract class CellKit extends RegistryEntry<CellKit> {

    public static final CellSolver NULL_CELL_SOLVER = cells -> 0;

    public static final CellKit NULL_CELL_KIT = new CellKit(DTTrees.NULL) {
        @Override
        public Cell getCellForLeaves(int hydro) {
            return CellNull.NULL_CELL;
        }

        @Override
        public Cell getCellForBranch(int radius, int meta) {
            return CellNull.NULL_CELL;
        }

        @Override
        public CellSolver getCellSolver() {
            return NULL_CELL_SOLVER;
        }

        @Override
        public SimpleVoxmap getLeafCluster() {
            return LeafClusters.NULL_MAP;
        }

        @Override
        public int getDefaultHydration() {
            return 0;
        }
    };

    /**
     * Central registry for all {@link CellKit} objects.
     */
    public static final SimpleRegistry<CellKit> REGISTRY = new SimpleRegistry<>(CellKit.class, NULL_CELL_KIT);

    public CellKit(final ResourceLocation registryName) {
        this.setRegistryName(registryName);
    }

    public abstract Cell getCellForLeaves(int distance);

    public abstract Cell getCellForBranch(int radius, int meta);

    public abstract CellSolver getCellSolver();

    /**
     * A voxel map of leaves blocks that are "stamped" on to the tree during generation
     */
    public abstract SimpleVoxmap getLeafCluster();

    /**
     * The default hydration level of a newly created leaf block [default = 4]
     **/
    public abstract int getDefaultHydration();

}
