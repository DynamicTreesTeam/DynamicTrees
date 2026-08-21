package com.dtteam.dynamictrees.worldgen.structure;

import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;
import java.util.function.Function;

public class DTCancelVanillaTreePoolElement extends StructurePoolElement {
    public static final MapCodec<DTCancelVanillaTreePoolElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
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
        return DTConfigs.COMMON.cancelVanillaVillageTrees.get();
    }

    public Vec3i getSize(StructureTemplateManager structureTemplateManager, Rotation rotation) {
        return this.isEnabled() ? this.enabled.getSize(structureTemplateManager, rotation) : this.disabled.getSize(structureTemplateManager, rotation);
    }

    public List<StructureTemplate.JigsawBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager structureTemplateManager, BlockPos pos, Rotation rotation, RandomSource random) {
        return java.util.List.of();
    }

    public BoundingBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos pos, Rotation rotation) {
        return this.isEnabled()
                ? this.enabled.getBoundingBox(structureTemplateManager, pos, rotation)
                : this.disabled.getBoundingBox(structureTemplateManager, pos, rotation);
    }

    public boolean place(StructureTemplateManager structureTemplateManager, WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, BlockPos blockPos, BlockPos blockPos1, Rotation rotation, BoundingBox box, RandomSource random, LiquidSettings liquidSettings, boolean b) {
        return this.isEnabled()
                ? this.enabled.place(structureTemplateManager, level, structureManager, generator, blockPos, blockPos1, rotation, box, random, liquidSettings, b)
                : this.disabled.place(structureTemplateManager, level, structureManager, generator, blockPos, blockPos1, rotation, box, random, liquidSettings, b);
    }

    public StructureTemplatePool.Projection getProjection() {
        return this.isEnabled() ? this.enabled.getProjection() : this.disabled.getProjection();
    }

    public StructurePoolElement setProjection(StructureTemplatePool.Projection projection) {
        this.enabled.setProjection(projection);
        this.disabled.setProjection(projection);
        return this;
    }

    public StructurePoolElementType<?> getType() {
        return DTRegistries.CANCEL_VANILLA_VILLAGE_TREE_STRUCTURE_POOL_ELEMENT_TYPE.get();
    }
}
