package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

@Cancelable
public class VoluntarySeedDropEvent extends Event {

    private final Level level;
    private final BlockPos rootPos;
    private final Species species;
    private final List<ItemStack> dropList;

    public VoluntarySeedDropEvent(Level level, BlockPos rootPos, Species species, List<ItemStack> dropList) {
        this.level = level;
        this.rootPos = rootPos;
        this.species = species;
        this.dropList = dropList;
    }

    public Level getLevel() {
        return level;
    }

    public BlockPos getRootPos() {
        return rootPos;
    }

    public Species getSpecies() {
        return species;
    }

    public List<ItemStack> getDropList() {
        return dropList;
    }

}
