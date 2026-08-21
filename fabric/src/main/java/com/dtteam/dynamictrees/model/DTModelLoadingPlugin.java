package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.CreakingHeartBranchBlock;
import com.dtteam.dynamictrees.block.branch.SurfaceRootBlock;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.model.baked.BasicBranchBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.BasicRootsBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.BreakingOverlayModel;
import com.dtteam.dynamictrees.model.baked.PalmFrondGeometry;
import com.dtteam.dynamictrees.model.baked.PalmLeavesBakedModel;
import com.dtteam.dynamictrees.model.baked.SurfaceRootBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.ThickBranchBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.WinterLeavesBlockStateModel;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.UndergroundRootsFamily;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class DTModelLoadingPlugin implements ModelLoadingPlugin {

    public static final Identifier POTTED_SAPLING_MODEL = DynamicTrees.location("potted_sapling");
    public static final Identifier WINTER_LEAVES_MODEL = DynamicTrees.location("block/winter_leaves");
    private static final Map<Identifier, BlockStateModel> BRANCH_MODEL_CACHE = new HashMap<>();
    private static final Map<Identifier, BlockStateModel> ROOT_MODEL_CACHE = new HashMap<>();
    private static final Map<Identifier, BlockStateModel> UNDERGROUND_ROOTS_MODEL_CACHE = new HashMap<>();
    private static final Map<Identifier, BlockStateModel> PALM_MODEL_CACHE = new HashMap<>();
    private static boolean modelsInitialized = false;
    private static SimpleModelWrapper winterLeavesPart;

    @Override
    public void initialize(Context pluginContext) {
        modelsInitialized = false;
        BRANCH_MODEL_CACHE.clear();
        ROOT_MODEL_CACHE.clear();
        UNDERGROUND_ROOTS_MODEL_CACHE.clear();
        PALM_MODEL_CACHE.clear();
        winterLeavesPart = null;
        pluginContext.modifyModelOnLoad().register(ModelModifier.WRAP_PHASE, DTModelLoadingPlugin::assignMissingParticle);
        pluginContext.modifyBlockModelAfterBake().register(ModelModifier.WRAP_PHASE, this::modifyModelAfterBake);
    }

    private void initBranchModels(net.minecraft.client.resources.model.ModelBaker baker) {
        if (modelsInitialized) return;
        modelsInitialized = true;
        ModelDebugName debugName = () -> "dynamictrees:branch";

        for (Family family : Family.REGISTRY.getAll()) {
            if (!family.isValid()) continue;

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

            if (family instanceof com.dtteam.dynamictrees.tree.family.CreakingHeartFamily heartFamily) {
                cacheCreakingModels(baker, debugName, heartFamily);
            }

            family.getSurfaceRoot().ifPresent(surfaceRoot -> {
                family.getPrimitiveLog().ifPresent(primitiveLog -> {
                    Identifier primitiveLogId = BuiltInRegistries.BLOCK.getKey(primitiveLog);
                    Identifier barkTexture = Identifier.fromNamespaceAndPath(primitiveLogId.getNamespace(), "block/" + primitiveLogId.getPath());
                    AtomicReference<Identifier> barkRef = new AtomicReference<>(barkTexture);
                    family.getTexturePath(Family.BRANCH).ifPresent(barkRef::set);

                    Identifier blockId = BuiltInRegistries.BLOCK.getKey(surfaceRoot);
                    Material.Baked bark = baker.materials().get(new Material(barkRef.get()), debugName);
                    ROOT_MODEL_CACHE.put(blockId, new SurfaceRootBlockBakedModel(baker, bark));
                });
            });

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
                if (undergroundFamily instanceof com.dtteam.dynamictrees.tree.family.MossyAerialRootsFamily mossyFamily) {
                    mossyFamily.getMossyRoots().ifPresent(roots -> {
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
    }

    private static Identifier creakingCacheKey(Identifier blockId, net.minecraft.world.level.block.state.BlockState state) {
        if (state.hasProperty(CreakingHeartBranchBlock.HIDDEN) && state.getValue(CreakingHeartBranchBlock.HIDDEN)) {
            return blockId.withSuffix("_hidden");
        }
        if (state.hasProperty(CreakingHeartBranchBlock.STATE)) {
            return blockId.withSuffix("_" + state.getValue(CreakingHeartBranchBlock.STATE));
        }
        return blockId;
    }

    private void cacheCreakingModels(net.minecraft.client.resources.model.ModelBaker baker, ModelDebugName debugName,
                                     com.dtteam.dynamictrees.tree.family.CreakingHeartFamily family) {
        boolean isThick = family.isThick();
        family.getHeartBranch().ifPresent(heart -> {
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(heart);
            family.getPrimitiveLog().ifPresent(primitiveLog -> {
                Identifier primitiveLogId = BuiltInRegistries.BLOCK.getKey(primitiveLog);
                Identifier bark = Identifier.fromNamespaceAndPath(primitiveLogId.getNamespace(), "block/" + primitiveLogId.getPath());
                Identifier rings = bark.withSuffix("_top");
                BRANCH_MODEL_CACHE.put(blockId.withSuffix("_hidden"), createBranchModel(baker, debugName, bark, rings, isThick));
            });
            family.getPrimitiveHeartLog().ifPresent(primitiveHeart -> {
                Identifier heartId = BuiltInRegistries.BLOCK.getKey(primitiveHeart);
                Identifier base = Identifier.fromNamespaceAndPath(heartId.getNamespace(), "block/" + heartId.getPath());
                BRANCH_MODEL_CACHE.put(blockId.withSuffix("_awake"),
                        createBranchModel(baker, debugName, base.withSuffix("_awake"), base.withSuffix("_awake_top"), isThick));
                BRANCH_MODEL_CACHE.put(blockId.withSuffix("_dormant"),
                        createBranchModel(baker, debugName, base.withSuffix("_dormant"), base.withSuffix("_dormant_top"), isThick));
                BRANCH_MODEL_CACHE.put(blockId.withSuffix("_uprooted"),
                        createBranchModel(baker, debugName, base, base.withSuffix("_top"), isThick));
                BRANCH_MODEL_CACHE.put(blockId, createBranchModel(baker, debugName, base, base.withSuffix("_top"), isThick));
            });
        });
        family.getResinBranch().ifPresent(resin -> {
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(resin);
            Identifier resinId = BuiltInRegistries.BLOCK.getKey(family.getResinBlock());
            Identifier bark = Identifier.fromNamespaceAndPath(resinId.getNamespace(), "block/" + resinId.getPath());
            BRANCH_MODEL_CACHE.put(blockId, createBranchModel(baker, debugName, bark, bark, isThick));
        });
    }

    private static final Identifier FALLBACK_BARK = Identifier.withDefaultNamespace("block/oak_log");
    private static final Identifier FALLBACK_RINGS = Identifier.withDefaultNamespace("block/oak_log_top");

    private BlockStateModel createBranchModel(net.minecraft.client.resources.model.ModelBaker baker, ModelDebugName debugName,
                                              Identifier barkTexture, Identifier ringsTexture, boolean isThick) {
        Material.Baked bark = materialOrFallback(baker, debugName, barkTexture, FALLBACK_BARK);
        Material.Baked rings = materialOrFallback(baker, debugName, ringsTexture, FALLBACK_RINGS);
        if (isThick) {
            Material.Baked thickRings = materialOrFallback(baker, debugName, ringsTexture.withSuffix("_thick"), FALLBACK_RINGS);
            return new ThickBranchBlockBakedModel(baker, bark, rings, thickRings);
        }
        return new BasicBranchBlockBakedModel(baker, bark, rings);
    }

    private BlockStateModel createRootsBlockModel(net.minecraft.client.resources.model.ModelBaker baker, ModelDebugName debugName,
                                                  Identifier barkTexture, Identifier ringsTexture) {
        Material.Baked bark = materialOrFallback(baker, debugName, barkTexture, FALLBACK_BARK);
        Material.Baked rings = materialOrFallback(baker, debugName, ringsTexture, FALLBACK_RINGS);
        return new BasicRootsBlockBakedModel(baker, bark, rings);
    }

    private static Material.Baked materialOrFallback(net.minecraft.client.resources.model.ModelBaker baker, ModelDebugName debugName,
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

    private static UnbakedModel assignMissingParticle(UnbakedModel model, ModelModifier.OnLoad.Context context) {
        TextureSlots.Data data = model.textureSlots();
        Map<String, TextureSlots.SlotContents> values = data.values();
        if (values.containsKey(UnbakedModel.PARTICLE_TEXTURE_REFERENCE)) {
            return model;
        }
        TextureSlots.Data.Builder extra = new TextureSlots.Data.Builder();
        if (values.containsKey("bark")) {
            extra.addReference(UnbakedModel.PARTICLE_TEXTURE_REFERENCE, "bark");
        } else if (values.containsKey("leaves")) {
            extra.addReference(UnbakedModel.PARTICLE_TEXTURE_REFERENCE, "leaves");
        } else {
            extra.addTexture(UnbakedModel.PARTICLE_TEXTURE_REFERENCE, new net.minecraft.client.resources.model.sprite.Material(
                    Identifier.withDefaultNamespace("block/oak_log")));
        }
        Map<String, TextureSlots.SlotContents> merged = new HashMap<>(values);
        merged.putAll(extra.build().values());
        return new ParticleFallbackUnbakedModel(model, new TextureSlots.Data(merged));
    }

    private BlockStateModel modifyModelAfterBake(BlockStateModel model, ModelModifier.AfterBakeBlock.Context context) {
        BlockState state = context.state();
        Block block = state.getBlock();
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);

        if (blockId.equals(POTTED_SAPLING_MODEL)) {
            return new BakedModelBlockPottedSapling(model);
        }

        if (block instanceof BasicRootsBlock) {
            initBranchModels(context.baker());
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
            initBranchModels(context.baker());
            BlockStateModel rootModel = ROOT_MODEL_CACHE.get(blockId);
            if (rootModel instanceof SurfaceRootBlockBakedModel baked) {
                return new BreakingOverlayModel(baked, baked.collectBreakingQuads(state));
            }
            return rootModel != null ? rootModel : model;
        }

        if (block instanceof CreakingHeartBranchBlock) {
            initBranchModels(context.baker());
            Identifier cacheKey = creakingCacheKey(blockId, state);
            BlockStateModel branchModel = BRANCH_MODEL_CACHE.get(cacheKey);
            if (branchModel instanceof BasicBranchBlockBakedModel baked) {
                return new BreakingOverlayModel(baked, baked.collectQuads(state, new int[6], null));
            }
        }

        if (block instanceof BranchBlock) {
            initBranchModels(context.baker());
            BlockStateModel branchModel = BRANCH_MODEL_CACHE.get(blockId);
            if (branchModel instanceof BasicBranchBlockBakedModel baked) {
                return new BreakingOverlayModel(baked, baked.collectQuads(state, new int[6], null));
            }
        }

        BlockStateModel palmWrapped = PalmFrondGeometry.wrap(state, model, context.baker(), PALM_MODEL_CACHE, PalmLeavesBakedModel::new);
        if (palmWrapped != model) {
            return palmWrapped;
        }

        if (block instanceof DynamicLeavesBlock leaves && leaves.getLeavesProperties().leavesPerishInWinter()) {
            SimpleModelWrapper winter = bakeWinterLeaves(context.baker());
            if (winter != null) {
                return new WinterLeavesBlockStateModel(model, winter);
            }
        }

        return model;
    }

    private static SimpleModelWrapper bakeWinterLeaves(net.minecraft.client.resources.model.ModelBaker baker) {
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
