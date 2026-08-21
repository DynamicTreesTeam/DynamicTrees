package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.SurfaceRootBlock;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.model.baked.BasicBranchBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.BasicRootsBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.BakedModelBlockPottedSapling;
import com.dtteam.dynamictrees.model.baked.BreakingOverlayModel;
import com.dtteam.dynamictrees.model.baked.PalmFrondGeometry;
import com.dtteam.dynamictrees.model.baked.PalmLeavesBakedModel;
import com.dtteam.dynamictrees.model.baked.SurfaceRootBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.ThickBranchBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.WinterLeavesBlockStateModel;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.UndergroundRootsFamily;
import com.google.common.collect.Interners;
import com.google.common.collect.Interner;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector3fc;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public final class NeoForgeModelPlugin {

    public static final Identifier POTTED_SAPLING_MODEL = DynamicTrees.location("potted_sapling");
    public static final Identifier WINTER_LEAVES_MODEL = DynamicTrees.location("block/winter_leaves");
    private static final Identifier FALLBACK_BARK = Identifier.withDefaultNamespace("block/oak_log");
    private static final Identifier FALLBACK_RINGS = Identifier.withDefaultNamespace("block/oak_log_top");

    private static final Map<Identifier, BlockStateModel> BRANCH_MODEL_CACHE = new HashMap<>();
    private static final Map<Identifier, BlockStateModel> ROOT_MODEL_CACHE = new HashMap<>();
    private static final Map<Identifier, BlockStateModel> UNDERGROUND_ROOTS_MODEL_CACHE = new HashMap<>();
    private static final Map<Identifier, BlockStateModel> PALM_MODEL_CACHE = new HashMap<>();
    private static boolean modelsInitialized;
    private static SimpleModelWrapper winterLeavesPart;

    private NeoForgeModelPlugin() {}

    public static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
        modelsInitialized = false;
        BRANCH_MODEL_CACHE.clear();
        ROOT_MODEL_CACHE.clear();
        UNDERGROUND_ROOTS_MODEL_CACHE.clear();
        PALM_MODEL_CACHE.clear();
        winterLeavesPart = null;

        ModelBaker baker = bakerFromEvent(event);
        Map<BlockState, BlockStateModel> models = event.getBakingResult().blockStateModels();
        models.replaceAll((state, model) -> wrap(state, model, baker));
    }

    private static ModelBaker bakerFromEvent(ModelEvent.ModifyBakingResult event) {
        ModelBakery bakery = event.getModelBakery();
        TextureAtlasSprite missingSprite = event.getTextureGetter().apply(MissingTextureAtlasSprite.getLocation());
        MaterialBaker materials = new MaterialBaker(missingSprite) {
            @Override
            protected Material.Baked bake(Material material) {
                TextureAtlasSprite sprite = event.getTextureGetter().apply(material.sprite());
                return sprite != null ? new Material.Baked(sprite, material.forceTranslucent()) : null;
            }
        };
        BakerInterner intern = new BakerInterner();
        BlockStateModelPart missingPart = event.getBakingResult().missingModels().blockPart();
        return new ModelBaker() {
            @Override
            public ResolvedModel getModel(Identifier location) {
                ResolvedModel resolved = bakery.resolvedModels.get(location);
                return resolved != null ? resolved : bakery.missingModel;
            }

            @Override
            public BlockStateModelPart missingBlockModelPart() {
                return missingPart;
            }

            @Override
            public MaterialBaker materials() {
                return materials;
            }

            @Override
            public ModelBaker.Interner interner() {
                return intern;
            }

            @Override
            public <T> T compute(SharedOperationKey<T> key) {
                return key.compute(this);
            }
        };
    }

    private static final class BakerInterner implements ModelBaker.Interner {
        private final Interner<Vector3fc> vectors = Interners.newStrongInterner();
        private final Interner<BakedQuad.MaterialInfo> materialInfos = Interners.newStrongInterner();
        private final Interner<BakedNormals> normals = Interners.newStrongInterner();
        private final Interner<BakedColors> colors = Interners.newStrongInterner();

        @Override
        public Vector3fc vector(Vector3fc vector) {
            return this.vectors.intern(vector);
        }

        @Override
        public BakedQuad.MaterialInfo materialInfo(BakedQuad.MaterialInfo material) {
            return this.materialInfos.intern(material);
        }

        @Override
        public BakedNormals normals(BakedNormals normals) {
            return this.normals.intern(normals);
        }

        @Override
        public BakedColors colors(BakedColors colors) {
            return this.colors.intern(colors);
        }
    }

    private static BlockStateModel wrap(BlockState state, BlockStateModel model, ModelBaker baker) {
        Block block = state.getBlock();
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);

        if (blockId.equals(POTTED_SAPLING_MODEL)) {
            return new BakedModelBlockPottedSapling(model);
        }

        if (block instanceof BasicRootsBlock) {
            initBranchModels(baker);
            if (state.hasProperty(BasicRootsBlock.LAYER)) {
                BasicRootsBlock.Layer layer = state.getValue(BasicRootsBlock.LAYER);
                if (layer == BasicRootsBlock.Layer.COVERED) {
                    return model;
                }
                Identifier cacheKey = blockId.withSuffix(layer == BasicRootsBlock.Layer.FILLED ? "_filled" : "_exposed");
                BlockStateModel rootsModel = UNDERGROUND_ROOTS_MODEL_CACHE.get(cacheKey);
                if (rootsModel instanceof BasicBranchBlockBakedModel baked) {
                    return new BreakingOverlayModel(baked, baked.collectQuads(state, new int[6], null));
                }
            }
            return model;
        }

        if (block instanceof SurfaceRootBlock) {
            initBranchModels(baker);
            BlockStateModel rootModel = ROOT_MODEL_CACHE.get(blockId);
            if (rootModel instanceof SurfaceRootBlockBakedModel baked) {
                return new BreakingOverlayModel(baked, baked.collectBreakingQuads(state));
            }
            return rootModel != null ? rootModel : model;
        }

        if (block instanceof BranchBlock) {
            initBranchModels(baker);
            BlockStateModel branchModel = BRANCH_MODEL_CACHE.get(blockId);
            if (branchModel instanceof BasicBranchBlockBakedModel baked) {
                return new BreakingOverlayModel(baked, baked.collectQuads(state, new int[6], null));
            }
        }

        BlockStateModel palmWrapped = PalmFrondGeometry.wrap(state, model, baker, PALM_MODEL_CACHE, PalmLeavesBakedModel::new);
        if (palmWrapped != model) {
            return palmWrapped;
        }

        if (block instanceof DynamicLeavesBlock leaves && leaves.getLeavesProperties().leavesPerishInWinter()) {
            SimpleModelWrapper winter = bakeWinterLeaves(baker);
            if (winter != null) {
                return new WinterLeavesBlockStateModel(model, winter);
            }
        }

        return model;
    }

    private static void initBranchModels(ModelBaker baker) {
        if (modelsInitialized) {
            return;
        }
        modelsInitialized = true;
        ModelDebugName debugName = () -> "dynamictrees:branch";

        for (Family family : Family.REGISTRY.getAll()) {
            if (!family.isValid()) {
                continue;
            }

            family.getPrimitiveLog().ifPresent(primitiveLog -> {
                Identifier primitiveLogId = BuiltInRegistries.BLOCK.getKey(primitiveLog);
                Identifier barkTexture = Identifier.fromNamespaceAndPath(primitiveLogId.getNamespace(), "block/" + primitiveLogId.getPath());
                Identifier ringsTexture = barkTexture.withSuffix("_top");
                AtomicReference<Identifier> barkRef = new AtomicReference<>(barkTexture);
                AtomicReference<Identifier> ringsRef = new AtomicReference<>(ringsTexture);
                family.getTexturePath(Family.BRANCH).ifPresent(barkRef::set);
                family.getTexturePath(Family.BRANCH_TOP).ifPresent(ringsRef::set);
                boolean isThick = family.isThick();
                family.getBranch().ifPresent(branch -> {
                    Identifier blockId = BuiltInRegistries.BLOCK.getKey(branch);
                    BRANCH_MODEL_CACHE.put(blockId, createBranchModel(baker, debugName, barkRef.get(), ringsRef.get(), isThick));
                });
            });

            family.getPrimitiveStrippedLog().ifPresent(strippedLog -> {
                Identifier strippedLogId = BuiltInRegistries.BLOCK.getKey(strippedLog);
                Identifier strippedBarkTexture = Identifier.fromNamespaceAndPath(strippedLogId.getNamespace(), "block/" + strippedLogId.getPath());
                Identifier strippedRingsTexture = strippedBarkTexture.withSuffix("_top");
                AtomicReference<Identifier> barkRef = new AtomicReference<>(strippedBarkTexture);
                AtomicReference<Identifier> ringsRef = new AtomicReference<>(strippedRingsTexture);
                family.getTexturePath(Family.STRIPPED_BRANCH).ifPresent(barkRef::set);
                family.getTexturePath(Family.STRIPPED_BRANCH_TOP).ifPresent(ringsRef::set);
                boolean isThick = family.isThick();
                family.getStrippedBranch().ifPresent(strippedBranch -> {
                    Identifier blockId = BuiltInRegistries.BLOCK.getKey(strippedBranch);
                    BRANCH_MODEL_CACHE.put(blockId, createBranchModel(baker, debugName, barkRef.get(), ringsRef.get(), isThick));
                });
            });

            family.getSurfaceRoot().ifPresent(surfaceRoot -> family.getPrimitiveLog().ifPresent(primitiveLog -> {
                Identifier primitiveLogId = BuiltInRegistries.BLOCK.getKey(primitiveLog);
                Identifier barkTexture = Identifier.fromNamespaceAndPath(primitiveLogId.getNamespace(), "block/" + primitiveLogId.getPath());
                AtomicReference<Identifier> barkRef = new AtomicReference<>(barkTexture);
                family.getTexturePath(Family.BRANCH).ifPresent(barkRef::set);
                Identifier blockId = BuiltInRegistries.BLOCK.getKey(surfaceRoot);
                Material.Baked bark = baker.materials().get(new Material(barkRef.get()), debugName);
                ROOT_MODEL_CACHE.put(blockId, new SurfaceRootBlockBakedModel(baker, bark));
            }));

            if (family instanceof UndergroundRootsFamily undergroundFamily) {
                undergroundFamily.getRoots().ifPresent(roots -> {
                    Identifier blockId = BuiltInRegistries.BLOCK.getKey(roots);
                    undergroundFamily.getPrimitiveRoots().ifPresent(primitiveRoots -> {
                        Identifier primitiveRootsId = BuiltInRegistries.BLOCK.getKey(primitiveRoots);
                        Identifier barkTexture = Identifier.fromNamespaceAndPath(primitiveRootsId.getNamespace(), "block/" + primitiveRootsId.getPath() + "_side");
                        Identifier ringsTexture = Identifier.fromNamespaceAndPath(primitiveRootsId.getNamespace(), "block/" + primitiveRootsId.getPath() + "_top");
                        AtomicReference<Identifier> barkRef = new AtomicReference<>(barkTexture);
                        AtomicReference<Identifier> ringsRef = new AtomicReference<>(ringsTexture);
                        family.getTexturePath(Family.ROOTS_SIDE).ifPresent(barkRef::set);
                        family.getTexturePath(Family.ROOTS_TOP).ifPresent(ringsRef::set);
                        UNDERGROUND_ROOTS_MODEL_CACHE.put(blockId.withSuffix("_exposed"),
                                createRootsBlockModel(baker, debugName, barkRef.get(), ringsRef.get()));
                    });
                    undergroundFamily.getPrimitiveFilledRoots().ifPresent(primitiveFilledRoots -> {
                        Identifier primitiveFilledRootsId = BuiltInRegistries.BLOCK.getKey(primitiveFilledRoots);
                        Identifier barkTexture = Identifier.fromNamespaceAndPath(primitiveFilledRootsId.getNamespace(), "block/" + primitiveFilledRootsId.getPath() + "_side");
                        Identifier ringsTexture = Identifier.fromNamespaceAndPath(primitiveFilledRootsId.getNamespace(), "block/" + primitiveFilledRootsId.getPath() + "_top");
                        UNDERGROUND_ROOTS_MODEL_CACHE.put(blockId.withSuffix("_filled"),
                                createRootsBlockModel(baker, debugName, barkTexture, ringsTexture));
                    });
                });
            }
        }
    }

    private static BlockStateModel createBranchModel(ModelBaker baker, ModelDebugName debugName,
                                                     Identifier barkTexture, Identifier ringsTexture, boolean isThick) {
        Material.Baked bark = materialOrFallback(baker, debugName, barkTexture, FALLBACK_BARK);
        Material.Baked rings = materialOrFallback(baker, debugName, ringsTexture, FALLBACK_RINGS);
        if (isThick) {
            Material.Baked thickRings = materialOrFallback(baker, debugName, ringsTexture.withSuffix("_thick"), FALLBACK_RINGS);
            return new ThickBranchBlockBakedModel(baker, bark, rings, thickRings);
        }
        return new BasicBranchBlockBakedModel(baker, bark, rings);
    }

    private static BlockStateModel createRootsBlockModel(ModelBaker baker, ModelDebugName debugName,
                                                         Identifier barkTexture, Identifier ringsTexture) {
        Material.Baked bark = materialOrFallback(baker, debugName, barkTexture, FALLBACK_BARK);
        Material.Baked rings = materialOrFallback(baker, debugName, ringsTexture, FALLBACK_RINGS);
        return new BasicRootsBlockBakedModel(baker, bark, rings);
    }

    private static Material.Baked materialOrFallback(ModelBaker baker, ModelDebugName debugName,
                                                     Identifier texture, Identifier fallback) {
        Material.Baked baked = baker.materials().get(new Material(texture), debugName);
        if (baked != null && baked.sprite() != null && !isMissingSprite(baked.sprite())) {
            return baked;
        }
        return baker.materials().get(new Material(fallback), debugName);
    }

    private static boolean isMissingSprite(TextureAtlasSprite sprite) {
        Identifier id = sprite.contents().name();
        return id.getPath().contains("missingno") || id.getPath().equals("missingno");
    }

    private static SimpleModelWrapper bakeWinterLeaves(ModelBaker baker) {
        if (winterLeavesPart != null) {
            return winterLeavesPart;
        }
        try {
            ResolvedModel winter = baker.getModel(WINTER_LEAVES_MODEL);
            var textures = winter.getTopTextureSlots();
            Material.Baked particle = winter.resolveParticleMaterial(textures, baker);
            winterLeavesPart = new SimpleModelWrapper(
                    winter.bakeTopGeometry(textures, baker, new ModelState() {}),
                    winter.getTopAmbientOcclusion(),
                    particle
            );
            return winterLeavesPart;
        } catch (Exception e) {
            DynamicTrees.LOG.warn("Failed to bake winter leaves model: {}", e.getMessage());
            return null;
        }
    }
}
