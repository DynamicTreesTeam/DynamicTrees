package com.dtteam.dynamictrees.api.treedata;

public record BranchShapeState(byte down, byte up, byte north, byte south, byte west, byte east, byte core){

    public int toIndex() {
        int index = down;
        index = index * 9 + up;
        index = index * 9 + north;
        index = index * 9 + south;
        index = index * 9 + west;
        index = index * 9 + east;
        index = index * 8 + core - 1;
        return index;
    }

    public static BranchShapeState fromIntArray(int[] radii){
        return new BranchShapeState((byte) radii[0], (byte) radii[1], (byte) radii[2], (byte) radii[3], (byte) radii[4], (byte) radii[5], (byte) radii[6]);
    }

}