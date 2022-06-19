package com.ferreusveritas.dynamictrees.worldgen.structure;

import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Blocks;
import net.minecraft.block.JigsawBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.JigsawTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.IJigsawDeserializer;
import net.minecraft.world.gen.feature.jigsaw.JigsawOrientation;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Random;

/**
 * @author Harley O'Connor
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class TreeJigsawPiece extends JigsawPiece {

    public static final Codec<TreeJigsawPiece> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Species.CODEC.fieldOf("species").forGetter(TreeJigsawPiece::getSpecies),
                    BlockPos.CODEC.fieldOf("offset").forGetter(TreeJigsawPiece::getOffset),
                    projectionCodec())
            .apply(instance, TreeJigsawPiece::new));

    public static final IJigsawDeserializer<TreeJigsawPiece> TREE_POOL_ELEMENT = IJigsawDeserializer.register(
            "tree_pool_element", CODEC
    );

    private final Species species;

    private final BlockPos offset;
    private final CompoundNBT defaultJigsawNBT;

    public TreeJigsawPiece(Species species, JigsawPattern.PlacementBehaviour projection) {
        this(species, BlockPos.ZERO, projection);
    }

    public TreeJigsawPiece(Species species, BlockPos offset, JigsawPattern.PlacementBehaviour projection) {
        super(projection);
        this.species = species;
        this.offset = offset;
        this.defaultJigsawNBT = this.fillDefaultJigsawNBT();
    }

    private CompoundNBT fillDefaultJigsawNBT() {
        CompoundNBT compoundnbt = new CompoundNBT();
        compoundnbt.putString("name", "minecraft:bottom");
        compoundnbt.putString("final_state", "minecraft:air");
        compoundnbt.putString("pool", "minecraft:empty");
        compoundnbt.putString("target", "minecraft:empty");
        compoundnbt.putString("joint", JigsawTileEntity.OrientationType.ROLLABLE.getSerializedName());
        return compoundnbt;
    }

    public List<Template.BlockInfo> getShuffledJigsawBlocks(TemplateManager templateManager, BlockPos pos,
                                                            Rotation rotation, Random random) {
        List<Template.BlockInfo> list = Lists.newArrayList();
        list.add(new Template.BlockInfo(pos,
                Blocks.JIGSAW.defaultBlockState().setValue(JigsawBlock.ORIENTATION, JigsawOrientation.fromFrontAndTop(
                        Direction.DOWN, Direction.SOUTH)), this.defaultJigsawNBT));
        return list;
    }

    @Override
    public MutableBoundingBox getBoundingBox(TemplateManager templateManager, BlockPos pos, Rotation rotation) {
        return new MutableBoundingBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean place(TemplateManager templateManager, ISeedReader world, StructureManager structureManager,
                         ChunkGenerator generator, BlockPos pos, BlockPos p_230378_6_, Rotation rotation,
                         MutableBoundingBox box, Random random, boolean keepJigsaws) {
        final Seed seed = species.getSeed().orElse(null);
        if (seed == null) {
            return false;
        }

        final ItemStack seedStack = new ItemStack(seed);
        final CompoundNBT tag = new CompoundNBT();
        tag.putBoolean(Seed.FORCE_PLANT_KEY, true);
        tag.putInt(Seed.CODE_RADIUS_KEY, random.nextInt(7) + 2);
        tag.putInt(Seed.LIFESPAN_KEY, 0);
        seedStack.setTag(tag);

        final int posX = pos.getX() + getOffsetX(rotation);
        final int posY = pos.getY() + getOffsetY(rotation);
        final int posZ = pos.getZ() + getOffsetZ(rotation);
        world.addFreshEntity(new ItemEntity(world.getLevel(), posX, posY, posZ, seedStack));
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
    public IJigsawDeserializer<?> getType() {
        return TREE_POOL_ELEMENT;
    }

    public Species getSpecies() {
        return species;
    }

    private BlockPos getOffset() {
        return offset;
    }

}
