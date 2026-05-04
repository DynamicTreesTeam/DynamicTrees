package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.AerialRootsFamily;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class FamilyLangGenerator implements Generator<DTDataProvider.Language, Family> {
    DTDataProvider.Language provider;

    @Override
    public void generate(DTDataProvider.Language prov, Family input, Dependencies dependencies) {
        this.provider = prov;
        input.getBranch().ifPresent(branch ->
                treeLang(branch, input, input.getLangOverride("branch"))
        );
        input.getBranchItem().ifPresent(branch ->
                treeLang(branch, input.getLangOverride("branch_item"))
        );
        if(input instanceof AerialRootsFamily rootsFamily){
            rootsFamily.getRoots().ifPresent(root ->
                    treeLang(root, input, input.getLangOverride("roots"))
            );
            rootsFamily.getRootsItem().ifPresent(root ->
                    treeLang(root, input.getLangOverride("roots_item"))
            );
        }
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies();
    }

    protected void treeLang(Block entry, Family family, Optional<String> override) {
        provider.addBlock(() -> entry,
                override.orElse(checkReplace(
                        family.getRegistryName().getPath()+ "_tree"
                ))
        );
    }
    protected void treeLang(Item entry, Optional<String> override) {
        provider.addItem(() -> entry,
                override.orElse(checkReplace(
                        BuiltInRegistries.ITEM.getKey(entry).getPath()
                ))
        );
    }

    protected String checkReplace(String path) {
        return Arrays.stream(path.split("_"))
                .map(StringUtils::capitalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
    }
}