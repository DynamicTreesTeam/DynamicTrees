//package com.dtteam.dynamictrees.model.baked;
//
//import com.dtteam.dynamictrees.block.leaves.PalmLeavesProperties;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.block.model.BakedQuad;
//import net.minecraft.client.renderer.block.model.BlockModel;
//import net.minecraft.client.renderer.block.model.ItemOverrides;
//import net.minecraft.client.renderer.block.model.ItemTransforms;
//import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//import net.minecraft.client.resources.model.BakedModel;
//import net.minecraft.client.resources.model.Material;
//import net.minecraft.core.Direction;
//import net.minecraft.resources.Identifier;
//import net.minecraft.util.RandomSource;
//import net.minecraft.world.inventory.InventoryMenu;
//import net.minecraft.world.level.block.state.BlockState;
//import net.neoforged.neoforge.client.ChunkRenderTypeSet;
//import net.neoforged.neoforge.client.RenderTypeGroup;
//import net.neoforged.neoforge.client.model.IDynamicBakedModel;
//import net.neoforged.neoforge.client.model.data.ModelData;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.*;
//import java.util.function.Function;
//
//
//public abstract class PalmLeavesBakedModel implements IDynamicBakedModel {
//
//    protected RenderTypeGroup renderGroup = new RenderTypeGroup(RenderType.cutout(), RenderType.cutout());
//    protected final BlockModel blockModel;
//
//    TextureAtlasSprite frondsTexture;
//
//    protected final BakedModel[] bakedFronds = new BakedModel[8]; // 8 = Number of surrounding blocks
//
//    public PalmLeavesBakedModel(Identifier frondsResLoc, Function<Material, TextureAtlasSprite> spriteGetter){
//        this.blockModel = new BlockModel(null, new ArrayList<>(), new HashMap<>(), false, BlockModel.GuiLight.FRONT, ItemTransforms.NO_TRANSFORMS, new ArrayList<>());
//        frondsTexture = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, frondsResLoc));
//        initModels();
//    }
//
//    //This method defines the model and shapes of the fronds. Each implementation must define its own.
//    public abstract void initModels();
//
//    @NotNull
//    @Override
//    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
//        if (state == null || side != null)
//            return Collections.emptyList();
//
//        LinkedList<BakedQuad> quads = new LinkedList<>();
//
//        int direction = state.getValue(PalmLeavesProperties.DynamicPalmLeavesBlock.DIRECTION);
//
//        if (direction != 0)
//            quads.addAll(bakedFronds[direction-1].getQuads(state, null, rand, extraData, renderType));
//
//
//        return quads;
//    }
//
//    @Override
//    public boolean useAmbientOcclusion() {
//        return false;
//    }
//
//    @Override
//    public boolean isGui3d() {
//        return false;
//    }
//
//    @Override
//    public boolean usesBlockLight() {
//        return false;
//    }
//
//    @Override
//    public boolean isCustomRenderer() {
//        return true;
//    }
//
//    @NotNull
//    @Override
//    public TextureAtlasSprite getParticleIcon() {
//        return frondsTexture;
//    }
//
//    @NotNull
//    @Override
//    public ItemOverrides getOverrides() {
//        return ItemOverrides.EMPTY;
//    }
//
//    @Override
//    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data)
//    {
//        return ChunkRenderTypeSet.of(RenderType.cutoutMipped());
//    }
//
//    public static class BlockVertexData {
//
//        public float x;
//        public float y;
//        public float z;
//        public int color;
//        public float u;
//        public float v;
//
//        public BlockVertexData(float x, float y, float z, float u, float v) {
//            this.x = x;
//            this.y = y;
//            this.z = z;
//            this.u = u;
//            this.v = v;
//            color = 0xFFFFFFFF;
//        }
//
//        public BlockVertexData(BakedQuad quad, int vIndex) {
//            this(quad.getVertices(), vIndex);
//        }
//
//        public BlockVertexData(int[] data, int vIndex) {
//            vIndex *= 8;
//            x = Float.intBitsToFloat(data[vIndex++]);
//            y = Float.intBitsToFloat(data[vIndex++]);
//            z = Float.intBitsToFloat(data[vIndex++]);
//            color = data[vIndex++];
//            u = Float.intBitsToFloat(data[vIndex++]);
//            v = Float.intBitsToFloat(data[vIndex]);
//        }
//
//        public int[] toInts() {
//            return new int[] { Float.floatToRawIntBits(x), Float.floatToRawIntBits(y), Float.floatToRawIntBits(z),
//                    color, Float.floatToRawIntBits(u), Float.floatToRawIntBits(v), 0, 0 };
//        }
//
//        protected int[] toInts(TextureAtlasSprite texture) {
//            return new int[] {
//                    Float.floatToRawIntBits(x), Float.floatToRawIntBits(y), Float.floatToRawIntBits(z),
//                    color,
//                    Float.floatToRawIntBits(texture.getU(u)), Float.floatToRawIntBits(texture.getV(v)),
//                    0, 0
//            };
//        }
//
//    }
//
//}
