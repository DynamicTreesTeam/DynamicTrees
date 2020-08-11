package com.ferreusveritas.dynamictrees.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class TESTBLOCK extends Block {

    public TESTBLOCK() {
        super(Properties.create(Material.ANVIL).sound(SoundType.CORAL));
        setRegistryName("test");
    }

}
