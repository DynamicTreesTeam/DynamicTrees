package com.ferreusveritas.dynamictrees.tree.species;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.block.rooty.SoilHelper;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.family.MangroveFamily;
import com.ferreusveritas.dynamictrees.worldgen.GenerationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.security.InvalidParameterException;

public class MangroveSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(MangroveSpecies::new);

    private int worldGenHeightOffset = 4;

    public void setWorldGenHeightOffset(int worldGenHeightOffset) {
        this.worldGenHeightOffset = worldGenHeightOffset;
    }

    public MangroveSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
        if (!(family instanceof MangroveFamily)) {
            throw new InvalidParameterException("Family " + family.toString() + " for mangrove species " + name + "is not of type "+ MangroveFamily.class);
        }
    }

    @Override
    public ResourceLocation getSaplingSmartModelLocation() {
        return DynamicTrees.location("block/smartmodel/water_sapling_thin");
    }

    public MangroveFamily getFamily() {
        return (MangroveFamily) family;
    }

    public boolean placeRootyDirtBlock(LevelAccessor level, BlockPos rootPos, int fertility) {
        BlockState dirtState = level.getBlockState(rootPos);
        Block dirt = dirtState.getBlock();

        if (!SoilHelper.isSoilRegistered(dirt) && !(dirt instanceof RootyBlock)) {
            //soil is not valid so we place default roots
            level.setBlock(rootPos, getFamily().getDefaultSoil().getSoilState(dirtState, fertility, this.doesRequireTileEntity(level, rootPos)), 3);
            return true;
        }

        return super.placeRootyDirtBlock(level, rootPos, fertility);
    }

    @Override
    public boolean generate(GenerationContext context) {
        context.rootPos().move(Direction.UP, worldGenHeightOffset);
        return super.generate(context);
    }

}
