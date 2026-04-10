package com.dtteam.dynamictrees.block.leaves;

import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.utility.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;

public class ScruffyLeavesProperties extends LeavesProperties {

    public static final TypedRegistry.EntryType<LeavesProperties> TYPE = TypedRegistry.newType(ScruffyLeavesProperties::new);

    public ScruffyLeavesProperties(Identifier registryName) {
        super(registryName);
    }

    private float leafChance = 0.66f;
    private int maxHydro = 1;

    public void setLeafChance (float leafChance){
        this.leafChance = leafChance;
    }
    public void setMaxHydro (int maxHydro) {
        this.maxHydro = maxHydro;
    }

    @Override
    protected DynamicLeavesBlock createDynamicLeaves(final Block.Properties properties) {
        return new DynamicLeavesBlock(this, properties){
            public int getHydrationLevelFromNeighbors(LevelAccessor level, BlockPos pos, LeavesProperties leavesProperties) {
                int hydro = super.getHydrationLevelFromNeighbors(level, pos, leavesProperties);
                if (hydro <= maxHydro){
                    int hash = CoordUtils.coordHashCode(pos, 2) % 1000;
                    float rand = hash / 1000f;
                    if (rand >= leafChance) return 0;
                }
                return hydro;
            }
        };
    }
}
