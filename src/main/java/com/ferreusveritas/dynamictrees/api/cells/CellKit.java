package com.ferreusveritas.dynamictrees.api.cells;

import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.cells.LeafClusters;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import net.minecraft.util.ResourceLocation;

public abstract class CellKit extends RegistryEntry<CellKit> {

    public static final ICellSolver NULL_CELL_SOLVER = cells -> 0;

    public static final CellKit NULL_CELL_KIT = new CellKit(DTTrees.NULL) {
        @Override
        public ICell getCellForLeaves(int hydro) {
            return CellNull.NULL_CELL;
        }

        @Override
        public ICell getCellForBranch(int radius, int meta) {
            return CellNull.NULL_CELL;
        }

        @Override
        public ICellSolver getCellSolver() {
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
    public static final Registry<CellKit> REGISTRY = new Registry<>(CellKit.class, NULL_CELL_KIT);

    public CellKit(final ResourceLocation registryName) {
        this.setRegistryName(registryName);
    }

    public abstract ICell getCellForLeaves(int distance);

    public abstract ICell getCellForBranch(int radius, int meta);

    public abstract ICellSolver getCellSolver();

    /**
     * A voxel map of leaves blocks that are "stamped" on to the tree during generation
     */
    public abstract SimpleVoxmap getLeafCluster();

    /**
     * The default hydration level of a newly created leaf block [default = 4]
     **/
    public abstract int getDefaultHydration();

}
