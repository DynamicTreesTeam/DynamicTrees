package com.dtteam.dynamictrees.block;

import net.minecraft.util.StringRepresentable;

public enum CreakingHeartBranchState implements StringRepresentable {
    DORMANT("dormant"),
    AWAKE("awake");

    private final String name;

    CreakingHeartBranchState(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public String getSerializedName() {
        return this.name;
    }

    public boolean isAwake(){
        return this.equals(AWAKE);
    }
}