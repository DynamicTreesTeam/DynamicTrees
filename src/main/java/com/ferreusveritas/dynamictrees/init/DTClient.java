package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.client.ModelHelper;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.*;
import com.ferreusveritas.dynamictrees.client.BlockColorMultipliers;
import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.client.TextureUtils;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.event.ModelBakeEventListener;
import com.ferreusveritas.dynamictrees.event.TextureGenerationHandler;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.particles.*;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.FoliageColors;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DTClient {

    public static void setup() {
        registerJsonColorMultipliers();
        registerClientEventHandlers();
        registerEntityRenderers();

        registerColorHandlers();
//        MinecraftForge.EVENT_BUS.register(BlockBreakAnimationClientHandler.instance);

        discoverWoodColors();
        LeavesPropertiesJson.postInitClient();
    }

    private static void discoverWoodColors() {
//
//        Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter = location -> Minecraft.getInstance().getTextureMap().getAtlasSprite(location.toString());
//
//        for(TreeFamily family : Species.REGISTRY.getValues().stream().map(s -> s.getFamily()).distinct().collect(Collectors.toList())) {
//            family.woodColor = 0xFFF1AE;//For roots
//            if(family != TreeFamily.NULLFAMILY) {
//                BlockState state = family.getPrimitiveLog();
//                if(state.getBlock() != Blocks.AIR) {
//                    IModel model = QuadManipulator.getModelManager().getModel(state);
//                    ResourceLocation resloc = QuadManipulator.getModelTexture(model, bakedTextureGetter, state, Direction.UP);
//                    if(resloc != null) {
//                        TextureUtils.PixelBuffer pixbuf = new TextureUtils.PixelBuffer(bakedTextureGetter.apply(resloc));
//                        int u = pixbuf.w / 16;
//                        TextureUtils.PixelBuffer center = new TextureUtils.PixelBuffer(u * 8, u * 8);
//                        pixbuf.blit(center, u * -8, u * -8);
//
//                        family.woodColor = center.averageColor();
//                    }
//                }
//            }
//        }
//
    }

    public void cleanUp() {
        BlockColorMultipliers.cleanUp();
    }

    private static boolean isValid(IBlockReader access, BlockPos pos) {
        return access != null && pos != null;
    }

    public static void registerColorHandlers() {

        final int white = 0xFFFFFFFF;
        final int magenta = 0x00FF00FF;//for errors.. because magenta sucks.

        //BLOCKS

        final BlockColors blockColors = Minecraft.getInstance().getBlockColors();

        //Register Rooty Colorizers
//        blockColors.register((state, world, pos, tintIndex) -> {
//                    switch(tintIndex) {
//                        case 0: return blockColors.getColor(DTRegistries.blockStates.grass, world, pos, tintIndex);
//                        case 1: return state.getBlock() instanceof BlockRooty ? ((BlockRooty) state.getBlock()).rootColor(state, world, pos) : white;
//                        default: return white;
//                    }
//                },
//                DTRegistries.blockRootyDirt, DTRegistries.blockRootyDirtSpecies, DTRegistries.blockRootySand, DTRegistries.blockRootyDirtFake);

//        //Register Sapling Colorizer
//        ModelHelper.regColorHandler(DTRegistries.blockDynamicSapling, (state, access, pos, tintIndex) -> {
//            return isValid(access, pos) ? DTRegistries.blockDynamicSapling.getSpecies(access, pos, state).saplingColorMultiplier(state, access, pos, tintIndex) : white;
//        });

        //Register Bonsai Pot Colorizer
//        ModelHelper.regColorHandler(DTRegistries.blockBonsaiPot, (state, access, pos, tintIndex) -> isValid(access, pos) && (state.getBlock() instanceof BlockBonsaiPot)
//                ? DTRegistries.blockBonsaiPot.getSpecies((World) access, pos).saplingColorMultiplier(state, access, pos, tintIndex) : white);

        //ITEMS

//        //Register Potion Colorizer
//        ModelHelper.regColorHandler(DTRegistries.dendroPotion, (stack, tint) -> DTRegistries.dendroPotion.getColor(stack, tint));

        //Register Woodland Staff Mesher and Colorizer
        ModelHelper.regColorHandler(DTRegistries.treeStaff, (stack, tint) -> DTRegistries.treeStaff.getColor(stack, tint));

        //TREE PARTS

        //Register GrowingLeavesBlocks Colorizers
//        for(BlockDynamicLeaves leaves: LeavesPaging.getLeavesMapForModId(DynamicTrees.MODID).values()) {
//            ModelHelper.regColorHandler(leaves, (state, worldIn, pos, tintIndex) ->
//                    TreeHelper.isLeaves(state.getBlock()) ? ((BlockDynamicLeaves) state.getBlock()).getProperties(state).foliageColorMultiplier(state, worldIn, pos) : magenta
//            );
//        }

    }

    public static void registerJsonColorMultipliers() {
        //Register programmable custom block color providers for LeavesPropertiesJson
        BlockColorMultipliers.register("birch", (state, worldIn,  pos, tintIndex) -> FoliageColors.getBirch() );
        BlockColorMultipliers.register("spruce", (state, worldIn,  pos, tintIndex) -> FoliageColors.getSpruce());
    }

    public static void registerClientEventHandlers() {
        MinecraftForge.EVENT_BUS.register(new ModelBakeEventListener());
        MinecraftForge.EVENT_BUS.register(TextureGenerationHandler.class);
    }

    public static void registerEntityRenderers() {
//        RenderingRegistry.registerEntityRenderingHandler(EntityFallingTree.class, new RenderFallingTree.Factory());
    }

    public int getFoliageColor(ILeavesProperties leavesProperties, World world, BlockState blockState, BlockPos pos) {
        return leavesProperties.foliageColorMultiplier(blockState, world, pos);
    }

    ///////////////////////////////////////////
    // PARTICLES
    ///////////////////////////////////////////

    public void addDustParticle(World world, double fx, double fy, double fz, double mx, double my, double mz, BlockState blockState, float r, float g, float b) {
        if(world.isRemote) {
            Particle particle = Minecraft.getInstance().particles.addParticle(new BlockParticleData(ParticleTypes.BLOCK, blockState), fx, fy, fz, mx, my, mz);
            assert particle != null;
            particle.setColor(r, g, b);
        }
    }

    /** Not strictly necessary. But adds a little more isolation to the server for particle effects */
    public void spawnParticle(World world, BasicParticleType particleType, double x, double y, double z, double mx, double my, double mz) {
        if(world.isRemote) {
            world.addParticle(particleType, x, y, z, mx, my, mz);
        }
    }

    public void crushLeavesBlock(World world, BlockPos pos, BlockState blockState, Entity entity) {
        if(world.isRemote) {
            Random random = world.rand;
            ITreePart treePart = TreeHelper.getTreePart(blockState);
            if(treePart instanceof BlockDynamicLeaves) {
                BlockDynamicLeaves leaves = (BlockDynamicLeaves) treePart;
                ILeavesProperties leavesProperties = leaves.getProperties(blockState);
                int color = getFoliageColor(leavesProperties, world, blockState, pos);
                float r = (color >> 16 & 255) / 255.0F;
                float g = (color >> 8 & 255) / 255.0F;
                float b = (color & 255) / 255.0F;
                for(int dz = 0; dz < 8; dz++) {
                    for(int dy = 0; dy < 8; dy++) {
                        for(int dx = 0; dx < 8; dx++) {
                            if(random.nextInt(8) == 0) {
                                double fx = pos.getX() + dx / 8.0;
                                double fy = pos.getY() + dy / 8.0;
                                double fz = pos.getZ() + dz / 8.0;
                                addDustParticle(world, fx, fy, fz, 0, random.nextFloat() * entity.getMotion().y, 0, blockState, r, g, b);
                            }
                        }
                    }
                }
            }
        }
    }

}
