package com.ferreusveritas.dynamictrees.worldgen.structure;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;
import java.util.function.Function;

public class DTCancelVanillaTreePoolElement extends StructurePoolElement {
    public static final Codec<DTCancelVanillaTreePoolElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StructurePoolElement.CODEC.fieldOf("enabled").forGetter(provider -> provider.enabled),
            StructurePoolElement.CODEC.fieldOf("disabled").forGetter(provider -> provider.disabled)
    ).apply(instance, DTCancelVanillaTreePoolElement::new));
    public final StructurePoolElement enabled;
    public final StructurePoolElement disabled;

    protected DTCancelVanillaTreePoolElement(StructurePoolElement enabled, StructurePoolElement disabled) {
        //noinspection DataFlowIssue
        super(null);
        this.enabled = enabled;
        this.disabled = disabled;
    }

    public static Function<StructureTemplatePool.Projection, DTCancelVanillaTreePoolElement> create(Function<StructureTemplatePool.Projection, ? extends StructurePoolElement> enabled,
            Function<StructureTemplatePool.Projection, ? extends StructurePoolElement> disabled) {
        return projection -> new DTCancelVanillaTreePoolElement(enabled.apply(projection), disabled.apply(projection));
    }

    private boolean isEnabled() {
        return DTConfigs.CANCEL_VANILLA_VILLAGE_TREES.get();
    }

    @Override
    public Vec3i getSize(StructureTemplateManager structureTemplateManager, Rotation rotation) {
        return this.isEnabled() ? this.enabled.getSize(structureTemplateManager, rotation) : this.disabled.getSize(structureTemplateManager, rotation);
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager structureTemplateManager, BlockPos pos, Rotation rotation, RandomSource random) {
        return this.isEnabled()
                ? this.enabled.getShuffledJigsawBlocks(structureTemplateManager, pos, rotation, random)
                : this.disabled.getShuffledJigsawBlocks(structureTemplateManager, pos, rotation, random);
    }

    @Override
    public BoundingBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos pos, Rotation rotation) {
        return this.isEnabled()
                ? this.enabled.getBoundingBox(structureTemplateManager, pos, rotation)
                : this.disabled.getBoundingBox(structureTemplateManager, pos, rotation);
    }

    @Override
    public boolean place(StructureTemplateManager structureTemplateManager, WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, BlockPos p_227340_,
            BlockPos p_227341_, Rotation rotation, BoundingBox box, RandomSource random, boolean p_227345_) {
        return this.isEnabled()
                ? this.enabled.place(structureTemplateManager, level, structureManager, generator, p_227340_, p_227341_, rotation, box, random, p_227345_)
                : this.disabled.place(structureTemplateManager, level, structureManager, generator, p_227340_, p_227341_, rotation, box, random, p_227345_);
    }

    @Override
    public StructureTemplatePool.Projection getProjection() {
        return this.isEnabled() ? this.enabled.getProjection() : this.disabled.getProjection();
    }

    @Override
    public StructurePoolElement setProjection(StructureTemplatePool.Projection projection) {
        this.enabled.setProjection(projection);
        this.disabled.setProjection(projection);
        return this;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return DTRegistries.CANCEL_VANILLA_VILLAGE_TREE_STRUCTURE_POOL_ELEMENT_TYPE.get();
    }
}
