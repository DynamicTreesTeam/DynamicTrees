package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class BiomeSuitabilityEvent extends Event {

    protected Level level;
    protected Biome biome;
    protected Species species;
    protected BlockPos pos;
    protected float suitability = 1.0f;
    protected boolean handled = false;

    public BiomeSuitabilityEvent(Level level, Biome biome, Species species, BlockPos pos) {
        this.level = level;
        this.biome = biome;
        this.species = species;
        this.pos = pos;
    }

    public Level getLevel() {
        return level;
    }

    public Biome getBiome() {
        return biome;
    }

    public Species getSpecies() {
        return species;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void setSuitability(float suitability) {
        this.suitability = suitability;
        handled = true;
    }

    public float getSuitability() {
        return suitability;
    }

    public boolean isHandled() {
        return handled;
    }

}
