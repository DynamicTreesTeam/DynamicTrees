package com.ferreusveritas.dynamictrees.resources.loader;

import com.ferreusveritas.dynamictrees.api.applier.ApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.api.resource.loading.preparation.JsonRegistryResourceLoader;
import com.ferreusveritas.dynamictrees.block.GrowableBlock;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.deserialisation.JsonHelper;
import com.ferreusveritas.dynamictrees.deserialisation.ResourceLocationDeserialiser;
import com.ferreusveritas.dynamictrees.systems.pod.Pod;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.Null;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * @author Harley O'Connor
 */
public final class PodResourceLoader extends JsonRegistryResourceLoader<Pod> {

    public PodResourceLoader() {
        super(Pod.REGISTRY, ApplierRegistryEvent.PODS);
    }

    @Override
    public void registerAppliers() {
        this.loadAppliers
                .register("max_age", Integer.class, Pod::setMaxAge)
                .register("minimum_radius", Integer.class, Pod::setMinRadius)
                .register("maximum_radius", Integer.class, Pod::setMaxRadius);

        this.commonAppliers.register("block_shapes", JsonObject.class, this::readBlockShapes);

        // Item is needed on datagen and setup
        this.gatherDataAppliers.register("item_stack", Item.class, (pod, item) -> pod.setItemStack(new ItemStack(item)));
        this.setupAppliers.register("item_stack", Item.class, (pod, item) -> pod.setItemStack(new ItemStack(item)));

        this.reloadAppliers
                .register("item_stack", ItemStack.class, Pod::setItemStack)
                .register("can_bone_meal", Boolean.class, Pod::setCanBoneMeal)
                .register("growth_chance", Float.class, Pod::setGrowthChance)
                .register("season_offset", Float.class, Pod::setSeasonOffset)
                .register("flower_hold_period_length", Float.class, Pod::setFlowerHoldPeriodLength)
                .register("min_production_factor", Float.class, Pod::setMinProductionFactor)
                .register("mature_action", GrowableBlock.MatureAction.class, Pod::setMatureAction);
    }

    private void readBlockShapes(Pod pod, JsonObject json) {
        Direction.Plane.HORIZONTAL.stream().forEach(facing -> {
            JsonElement shapeArrayElement = json.get(facing.getName().toLowerCase(Locale.ROOT));
            if (!shapeArrayElement.isJsonArray()) {
                return;
            }
            List<VoxelShape> shapes = new LinkedList<>();
            shapeArrayElement.getAsJsonArray().forEach(shapeElement ->
                    JsonDeserialisers.VOXEL_SHAPE.deserialise(shapeElement).ifSuccess(shapes::add)
            );
            pod.setBlockShapes(facing, shapes.toArray(new VoxelShape[0]));
        });
    }

    @Override
    protected void applyLoadAppliers(JsonRegistryResourceLoader<Pod>.LoadData loadData, JsonObject json) {
        super.applyLoadAppliers(loadData, json);
        final JsonObject propertiesJson = getBlockPropertiesJson(json);
        if (propertiesJson == null) {
            this.createBlock(loadData.getResource(), json);
        } else {
            this.createBlock(loadData.getResource(), json, propertiesJson);
        }
    }

    @Nullable
    private JsonObject getBlockPropertiesJson(JsonObject json) {
        return Null.applyIfNonnull(json.get("block_properties"), element ->
                JsonDeserialisers.JSON_OBJECT.deserialise(element).orElse(null)
        );
    }

    private void createBlock(Pod pod, JsonObject json) {
        pod.createBlock(getBlockRegistryName(pod, json), pod.getDefaultBlockProperties());
    }

    private void createBlock(Pod pod, JsonObject json, JsonObject propertiesJson) {
        final BlockBehaviour.Properties blockProperties = JsonHelper.getBlockProperties(
                propertiesJson,
                pod.getDefaultMapColor(),
                pod::getDefaultBlockProperties,
                error -> this.logError(pod.getRegistryName(), error),
                warning -> this.logWarning(pod.getRegistryName(), warning)
        );
        pod.createBlock(getBlockRegistryName(pod, json), blockProperties);
    }

    /**
     * @return the registry name to set for the pod block, or {@code null} if it was not set (which defaults to the
     * using the pod's registry name)
     */
    @Nullable
    private ResourceLocation getBlockRegistryName(Pod pod, JsonObject json) {
        return Null.applyIfNonnull(json.get("block_registry_name"), element ->
                ResourceLocationDeserialiser.create(pod.getRegistryName().getNamespace())
                        .deserialise(element)
                        .orElse(null)
        );
    }

}
