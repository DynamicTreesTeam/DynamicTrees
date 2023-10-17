package com.ferreusveritas.dynamictrees.worldgen.structure;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

/**
 * @author Harley O'Connor
 */
public interface TemplatePoolModifier {
    TemplatePoolModifier replaceTemplate(int index, StructurePoolElement element);

    TemplatePoolModifier removeTemplate(int index);

    void removeAllTemplates();

    void registerPool(BootstapContext<StructureTemplatePool> context);
}
