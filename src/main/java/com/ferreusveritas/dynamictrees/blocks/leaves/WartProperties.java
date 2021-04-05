package com.ferreusveritas.dynamictrees.blocks.leaves;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.ResourceLocation;

/**
 * @author Harley O'Connor
 */
public class WartProperties extends SolidLeavesProperties {

    public static final TypedRegistry.EntryType<LeavesProperties> TYPE = TypedRegistry.newType(WartProperties::new);

    public WartProperties(final ResourceLocation registryName) {
        super(registryName);
        this.requiresShears = false;
    }

    @Override
    protected ResourceLocation getDynamicLeavesRegName() {
        return ResourceLocationUtils.suffix(this.getRegistryName(), "_wart");
    }

    @Override
    public Material getDefaultMaterial() {
        return Material.GRASS;
    }

    @Override
    public AbstractBlock.Properties getDefaultBlockProperties(Material material, MaterialColor materialColor) {
        return AbstractBlock.Properties.of(material, materialColor).strength(1.0F).sound(SoundType.WART_BLOCK).randomTicks();
    }

}
