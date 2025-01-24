package com.dtteam.dynamictrees.worldgen.structure;

import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.species.Species;
import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
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

/**
 * @author Harley O'Connor
 */
@MethodsReturnNonnullByDefault
public final class TreePoolElement extends StructurePoolElement {

    public static final MapCodec<TreePoolElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Species.CODEC.fieldOf("species").forGetter(TreePoolElement::getSpecies),
                    BlockPos.CODEC.optionalFieldOf("offset", BlockPos.ZERO).forGetter(TreePoolElement::getOffset),
                    projectionCodec())
            .apply(instance, TreePoolElement::new));

    private final Species species;

    private final BlockPos offset;
    private final CompoundTag defaultJigsawNBT;

    public TreePoolElement(Species species, StructureTemplatePool.Projection projection) {
        this(species, BlockPos.ZERO, projection);
    }

    public TreePoolElement(Species species, BlockPos offset, StructureTemplatePool.Projection projection) {
        super(projection);
        this.species = species;
        this.offset = offset;
        this.defaultJigsawNBT = this.fillDefaultJigsawNBT();
    }

    public static Function<StructureTemplatePool.Projection, TreePoolElement> create(Species species) {
        return projection -> new TreePoolElement(species, projection);
    }

    public static Function<StructureTemplatePool.Projection, TreePoolElement> create(Species species, BlockPos offset) {
        return projection -> new TreePoolElement(species, offset, projection);
    }

    private CompoundTag fillDefaultJigsawNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", "minecraft:bottom");
        tag.putString("final_state", "minecraft:air");
        tag.putString("pool", "minecraft:empty");
        tag.putString("target", "minecraft:empty");
        tag.putString("joint", JigsawBlockEntity.JointType.ROLLABLE.getSerializedName());
        return tag;
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager structureManager, BlockPos pos, Rotation rotation, RandomSource random) {
        return Lists.newArrayList(
                new StructureTemplate.StructureBlockInfo(pos, Blocks.JIGSAW.defaultBlockState().setValue(JigsawBlock.ORIENTATION, FrontAndTop.fromFrontAndTop(Direction.DOWN, Direction.SOUTH)),
                        this.defaultJigsawNBT)
        );
    }


    @Override
    public BoundingBox getBoundingBox(StructureTemplateManager structureManager, BlockPos pos, Rotation rotation) {
        return new BoundingBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public Vec3i getSize(StructureTemplateManager pStructureManager, Rotation pRotation) {
        return Vec3i.ZERO;
    }

    @Override
    public boolean place(StructureTemplateManager structureTemplateManager, WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos1, Rotation rotation, BoundingBox boundingBox, RandomSource randomSource, LiquidSettings liquidSettings, boolean b) {
        species.plantSapling(level, blockPos, true);
        return true;
    }

    private int getOffsetX(Rotation rotation) {
        return offset.getX() * (rotation.rotation().inverts(Direction.Axis.X) ? -1 : 1);
    }

    private int getOffsetY(Rotation rotation) {
        return offset.getY();
    }

    private int getOffsetZ(Rotation rotation) {
        return offset.getZ() * (rotation.rotation().inverts(Direction.Axis.Z) ? -1 : 1);
    }


    @Override
    public StructurePoolElementType<?> getType() {
        return DTRegistries.TREE_STRUCTURE_POOL_ELEMENT_TYPE.get();
    }

    public Species getSpecies() {
        return species;
    }

    private BlockPos getOffset() {
        return offset;
    }

}
