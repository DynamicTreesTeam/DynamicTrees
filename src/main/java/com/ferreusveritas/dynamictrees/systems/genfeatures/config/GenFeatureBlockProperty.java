package com.ferreusveritas.dynamictrees.systems.genfeatures.config;

import com.ferreusveritas.dynamictrees.util.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * An implementation of {@link GenFeatureProperty} for {@link Block} objects. Handles
 * getting the {@link Block} value from a Json file.
 *
 * @author Harley O'Connor
 */
public class GenFeatureBlockProperty extends GenFeatureProperty<Block> {

    public GenFeatureBlockProperty(String identifier) {
        super(identifier, Block.class);
    }

    @Nullable
    @Override
    public GenFeaturePropertyValue<Block> getFromJsonObject(JsonObject jsonObject) {
        JsonElement jsonElement = jsonObject.get(this.identifier);

        if (jsonElement == null || !jsonElement.isJsonPrimitive())
            return null;

        final String registryNameStr = JsonHelper.getFromPrimitive(jsonElement.getAsJsonPrimitive(), String.class);

        if (registryNameStr == null)
            return null;

        ResourceLocation registryName = ResourceLocation.tryCreate(registryNameStr);

        if (registryName == null)
            return null;

        Block block = ForgeRegistries.BLOCKS.getValue(registryName);

        return block != null ? new GenFeaturePropertyValue<>(block) : null;
    }

}
