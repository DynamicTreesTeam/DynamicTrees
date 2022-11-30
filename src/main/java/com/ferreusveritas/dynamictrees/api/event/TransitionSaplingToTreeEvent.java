package com.ferreusveritas.dynamictrees.api.event;

import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired when {@link Species#transitionToTree(Level, BlockPos)} is invoked, before placement checks and logic are done.
 * <p>
 * This event is {@link Cancelable}.
 * <p>
 * This event does not {@linkplain Event.HasResult have a result}.
 * <p>
 * This event is fired on the {@linkplain net.minecraftforge.common.MinecraftForge#EVENT_BUS Forge bus}.
 *
 * @author Harley O'Connor
 */
@Cancelable
public class TransitionSaplingToTreeEvent extends Event {

    /** The species to transition to. */
    private final Species species;
    private final Level level;
    /** Position of sapling block. */
    private final BlockPos pos;

    public TransitionSaplingToTreeEvent(Species species, Level level, BlockPos pos) {
        this.species = species;
        this.level = level;
        this.pos = pos;
    }

    public Species getSpecies() {
        return species;
    }

    public Level getLevel() {
        return level;
    }

    public BlockPos getPos() {
        return pos;
    }

}
