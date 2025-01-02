package com.dtteam.dynamictrees.event;

import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;

import java.util.List;

/**
 * This event fires after a tree has been created and decorated. Use this to add things like beehives, bird nests, extra
 * vines, hanging lights, etc.
 *
 * @author ferreusveritas
 */
public class SpeciesPostGenerationEvent extends Event {

    private final LevelAccessor level;
    private final Species species;
    private final BlockPos rootPos;
    private final List<BlockPos> endPoints;
    private final BlockState initialDirtState;

    public SpeciesPostGenerationEvent(LevelAccessor level, Species species, BlockPos rootPos, List<BlockPos> endPoints, BlockState initialDirtState) {
        this.level = level;
        this.species = species;
        this.rootPos = rootPos;
        this.endPoints = endPoints;
        this.initialDirtState = initialDirtState;
    }

    public LevelAccessor getLevel() {
        return level;
    }

    public Species getSpecies() {
        return species;
    }

    public BlockPos getRootPos() {
        return rootPos;
    }

    /**
     * An endpoint is a {@link BlockPos} of the end of each branch in a tree.
     *
     * @return Endpoint list
     */
    public List<BlockPos> getEndPoints() {
        return endPoints;
    }

    /**
     * Safebounds offers a method to test if a block is about to be put in an unloaded chunk.
     *
     * @return The current safe bounds for placing blocks
     */
    public boolean isWorldGen() {
        return true;
    }

    /**
     * Get the state of the dirt before a tree was planted there
     *
     * @return The initial state of the dirt block before it was changed
     */
    public BlockState getInitialDirtState() {
        return initialDirtState;
    }
}
