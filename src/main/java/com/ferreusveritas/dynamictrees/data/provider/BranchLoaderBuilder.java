package com.ferreusveritas.dynamictrees.data.provider;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.event.handlers.BakedModelEventHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import static com.ferreusveritas.dynamictrees.util.ResourceLocationUtils.prefix;
import static com.ferreusveritas.dynamictrees.util.ResourceLocationUtils.suffix;

/**
 * @author Harley O'Connor
 */
public final class BranchLoaderBuilder extends CustomLoaderBuilder<BlockModelBuilder> {

    private final Map<String, String> textures = new LinkedHashMap<>();

    public BranchLoaderBuilder(ResourceLocation loaderId, BlockModelBuilder parent, ExistingFileHelper existingFileHelper) {
        super(loaderId, parent, existingFileHelper);
    }

    public BranchLoaderBuilder texture(String key, ResourceLocation location) {
        this.textures.put(key, location.toString());
        return this;
    }

    public BranchLoaderBuilder texturesFor(Block primitiveLog) {
        texturesFor(primitiveLog, this::texture);
        return this;
    }

    public static void texturesFor(Block primitiveLog, BiConsumer<String, ResourceLocation> textureConsumer) {
        final ResourceLocation barkTextureLocation = prefix(
                Objects.requireNonNull(primitiveLog.getRegistryName()),
                "block/"
        );
        textureConsumer.accept("bark", barkTextureLocation);
        textureConsumer.accept("rings", suffix(barkTextureLocation, "_top"));
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);

        final JsonObject textures = new JsonObject();
        this.textures.forEach((key, location) ->
                textures.add(key, new JsonPrimitive(location)));
        json.add("textures", textures);

        return json;
    }

    public static BranchLoaderBuilder branch(BlockModelBuilder parent, ExistingFileHelper existingFileHelper) {
        return new BranchLoaderBuilder(BakedModelEventHandler.BRANCH, parent, existingFileHelper);
    }

    public static BranchLoaderBuilder root(BlockModelBuilder parent, ExistingFileHelper existingFileHelper) {
        return new BranchLoaderBuilder(BakedModelEventHandler.ROOT, parent, existingFileHelper);
    }

}
