package com.dtteam.dynamictrees.worldgen.structure;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

/**
 * @author Harley O'Connor
 */
public interface TemplatePoolModifier {
    TemplatePoolModifier replaceTemplate(int index, StructurePoolElement element);

    TemplatePoolModifier removeTemplate(int index);

    void removeAllTemplates();

    void registerPool(BootstrapContext<StructureTemplatePool> context);
}
