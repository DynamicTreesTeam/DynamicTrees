package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class LeavesPropertiesLangGenerator implements Generator<DTDataProvider.Language, LeavesProperties> {
    DTDataProvider.Language provider;

    @Override
    public void generate(DTDataProvider.Language prov, LeavesProperties input, Dependencies dependencies) {
        this.provider = prov;
        //input.getDynamicLeavesBlock().ifPresent(leaves -> blockLang(leaves, input.getLangOverride("leaves")));
    }

    @Override
    public Dependencies gatherDependencies(LeavesProperties input) {
        return new Dependencies();
    }

//    protected void itemLang(Item entry, Optional<String> override) {
//        if (!(entry instanceof BlockItem) || entry instanceof ItemNameBlockItem) {
//            provider.addItem(() -> entry, override.orElse(checkReplace(BuiltInRegistries.ITEM.getKey(entry))));
//        }
//    }

    protected void speciesLang(Species entry, Optional<String> override) {
        provider.add(entry.getLocalizedName(), override.orElse(checkReplace(entry.getRegistryName())));
    }

    protected void blockLang(Block entry, Optional<String> blah) {
        provider.addBlock(() -> entry, blah.orElse(checkReplace(BuiltInRegistries.BLOCK.getKey(entry))));
    }

    protected String checkReplace(Identifier registryObject) {
        return Arrays.stream(registryObject.getPath().split("_"))
                .map(StringUtils::capitalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
    }
}
