package com.ferreusveritas.dynamictrees.worldgen.structure;

import com.ferreusveritas.dynamictrees.item.Seed;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author Harley O'Connor
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class TreePoolElement extends StructurePoolElement {

    public static final Codec<TreePoolElement> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Species.CODEC.fieldOf("species").forGetter(TreePoolElement::getSpecies),
                    BlockPos.CODEC.fieldOf("offset").forGetter(TreePoolElement::getOffset),
                    projectionCodec())
            .apply(instance, TreePoolElement::new));

    public static final StructurePoolElementType<TreePoolElement> TREE_POOL_ELEMENT = StructurePoolElementType.register(
            "dynamictrees:tree_pool_element", CODEC
    );

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
                new StructureTemplate.StructureBlockInfo(pos, Blocks.JIGSAW.defaultBlockState().setValue(JigsawBlock.ORIENTATION, FrontAndTop.fromFrontAndTop(Direction.DOWN, Direction.SOUTH)), this.defaultJigsawNBT)
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
    public boolean place(StructureTemplateManager structureManager, WorldGenLevel level, StructureManager structureFeatureManager, ChunkGenerator chunkGenerator, BlockPos pos, BlockPos p_210488_, Rotation rotation, BoundingBox box, RandomSource random, boolean keepJigsaws) {
        final Seed seed = species.getSeed().orElse(null);
        if (seed == null) {
            return false;
        }

        final ItemStack seedStack = new ItemStack(seed);
        final CompoundTag tag = new CompoundTag();
        tag.putBoolean(Seed.FORCE_PLANT_KEY, true);
        tag.putInt(Seed.CODE_KEY, random.nextInt(7) + 2);
        tag.putInt(Seed.LIFESPAN_KEY, 0);
        seedStack.setTag(tag);

        final int posX = pos.getX() + getOffsetX(rotation);
        final int posY = pos.getY() + getOffsetY(rotation);
        final int posZ = pos.getZ() + getOffsetZ(rotation);
        level.addFreshEntity(new ItemEntity(level.getLevel(), posX, posY, posZ, seedStack));
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
        return TREE_POOL_ELEMENT;
    }

    public Species getSpecies() {
        return species;
    }

    private BlockPos getOffset() {
        return offset;
    }

}
