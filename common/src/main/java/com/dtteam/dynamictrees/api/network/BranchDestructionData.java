package com.dtteam.dynamictrees.api.network;

import com.dtteam.dynamictrees.api.voxmap.BlockPosBounds;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.systems.nodemapper.NetVolumeNode;
import com.dtteam.dynamictrees.tree.species.Species;
import com.google.common.collect.AbstractIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

public class BranchDestructionData {
    public final Species species; // The species of the tree that was harvested
    public final int[] destroyedBranchesRadiusPosition; // Encoded branch radius and relative positions
    public final int[] destroyedBranchesConnections; // Encoded branch shapes
    public final int[] destroyedBranchesBlockIndex; // Encoded valid branch block index for family
    public final int[] destroyedLeaves; // Encoded leaves relative positions
    public final int[] destroyedLeavesBlockIndex; // Encoded valid leaves block index for species
    public final List<BranchBlock.ItemStackPos> leavesDrops; // A list of itemstacks and their spawn positions.  Not used on the client.
    public final int[] endPoints; // Encoded endpoint relative positions
    public final NetVolumeNode.Volume woodVolume; // A summation of all of the wood voxels that was harvested
    public final Direction cutDir; // The face that was connected to the remaining body of the tree or the rooty block
    public final Direction toolDir; // The face that was pounded on when breaking the block at cutPos
    public final BlockPos cutPos; // The absolute(world) position of the block that was cut
    public final BlockPos basePos; // The absolute(world) position of base for the tree entity
    public final int trunkHeight;
    public final Pair<Identifier, Integer> soilState;

    public static final BlockPosBounds bounds = new BlockPosBounds(new BlockPos(-64, -64, -64), new BlockPos(64, 64, 64));

    public BranchDestructionData() {
        this.species = Species.NULL_SPECIES;
        this.destroyedBranchesConnections = new int[0];
        this.destroyedBranchesRadiusPosition = new int[0];
        this.destroyedBranchesBlockIndex = new int[0];
        this.destroyedLeaves = new int[0];
        this.destroyedLeavesBlockIndex = new int[0];
        this.leavesDrops = new ArrayList<>(0);
        this.endPoints = new int[0];
        this.woodVolume = new NetVolumeNode.Volume();
        this.cutDir = Direction.DOWN;
        this.toolDir = Direction.DOWN;
        this.cutPos = BlockPos.ZERO;
        this.basePos = BlockPos.ZERO;
        this.trunkHeight = 0;
        this.soilState = null;
    }

    private Map<BlockPos, BranchConnectionData> unencodedBranches;
    private Map<BlockPos, BlockState> unencodedLeaves;
    private List<BlockPos> unencodedEnds;
    public BranchDestructionData(Species species, Map<BlockPos, BranchConnectionData> branches, Map<BlockPos, BlockState> leaves, List<BranchBlock.ItemStackPos> leavesDrops, List<BlockPos> ends, NetVolumeNode.Volume volume, BlockPos cutPos, BlockPos basePos, Direction cutDir, Direction toolDir, int trunkHeight, @Nullable Pair<Identifier, Integer> soilState) {
        this.species = species;
        int[][] encodedBranchData = convertBranchesToIntArrays(branches);
        this.destroyedBranchesRadiusPosition = encodedBranchData[0];
        this.destroyedBranchesConnections = encodedBranchData[1];
        this.destroyedBranchesBlockIndex = encodedBranchData[2];
        int[][] encodedLeavesData = convertLeavesToIntArray(leaves, species);
        this.destroyedLeaves = encodedLeavesData[0];
        this.destroyedLeavesBlockIndex = encodedLeavesData[1];
        this.leavesDrops = leavesDrops;
        this.endPoints = convertEndPointsToIntArray(ends);
        this.woodVolume = volume;
        this.cutPos = cutPos;
        this.basePos = basePos;
        this.cutDir = cutDir;
        this.toolDir = toolDir;
        this.trunkHeight = trunkHeight;
        this.soilState = soilState;
        //these are only used for merging destructionData
        unencodedBranches = branches;
        unencodedLeaves = leaves;
        unencodedEnds = ends;
    }

    public BranchDestructionData merge (BranchDestructionData other){
        //All the positions are relative to the cutPos, so when merging they must all be offset by their difference
        final BlockPos offset = other.cutPos.subtract(cutPos);
        //Merge and offset branches
        Map<BlockPos, BranchConnectionData> newBranches = new HashMap<>(unencodedBranches);
        other.unencodedBranches.forEach((key, value) -> newBranches.put(key.offset(offset), value));
        //Merge and offset leaves
        Map<BlockPos, BlockState> newLeaves = new HashMap<>(unencodedLeaves);
        other.unencodedLeaves.forEach((key, value) -> newLeaves.put(key.offset(offset), value));
        //Merge and offset leaves drops
        List<BranchBlock.ItemStackPos> newLeavesDrops = new LinkedList<>(leavesDrops);
        newLeavesDrops.addAll(other.leavesDrops.stream().map(a->new BranchBlock.ItemStackPos(a.stack, a.pos.offset(offset))).toList());
        //Merge and offset ends
        List<BlockPos> newEnds = new LinkedList<>(unencodedEnds);
        newEnds.addAll(other.unencodedEnds.stream().map(e->e.offset(offset)).toList());
        //Merge volumes
        NetVolumeNode.Volume newVolume = new NetVolumeNode.Volume(woodVolume.getRawVolumesArray());
        newVolume.addVolume(other.woodVolume);

        //The base position for the tree is the minimum between the two destructionData
        BlockPos newBasePos = basePos.getY() < other.basePos.getY() ? basePos : other.basePos;
        //We find the highest point to re-calculate the trunk height based on the new positions
        int maxY = Math.max(basePos.getY()+trunkHeight, other.basePos.getY()+other.trunkHeight);
        int newHeight = maxY - newBasePos.getY();

        //If the other brings soil accept it
        Pair<Identifier, Integer> soil = soilState == null ? other.soilState : soilState;

        //Finally the new destructionData is generated.
        // All other parameters use the values from the first destructionData (this).
        return new BranchDestructionData(
                species, newBranches, newLeaves, newLeavesDrops, newEnds, newVolume,
                cutPos, newBasePos, cutDir, toolDir, newHeight, soil
        );
    }

    public BranchDestructionData(CompoundTag nbt) {
        this.species = Species.findSpecies(Identifier.parse(nbt.getStringOr("species", "")));
        this.destroyedBranchesRadiusPosition = nbt.getIntArray("branchpos").orElseGet(() -> new int[0]);
        this.destroyedBranchesConnections = nbt.getIntArray("branchcon").orElseGet(() -> new int[0]);
        this.destroyedBranchesBlockIndex = nbt.getIntArray("branchblock").orElseGet(() -> new int[0]);
        this.destroyedLeaves = nbt.getIntArray("leavespos").orElseGet(() -> new int[0]);
        this.destroyedLeavesBlockIndex = nbt.getIntArray("leavesblock").orElseGet(() -> new int[0]);
        this.leavesDrops = new ArrayList<>();
        this.endPoints = nbt.getIntArray("ends").orElseGet(() -> new int[0]);
        this.woodVolume = new NetVolumeNode.Volume(nbt.getIntArray("volume").orElseGet(() -> new int[0]));
        this.cutPos = new BlockPos(nbt.getIntOr("cutx", 0), nbt.getIntOr("cuty", 0), nbt.getIntOr("cutz", 0));
        this.basePos = new BlockPos(nbt.getIntOr("basex", 0), nbt.getIntOr("basey", 0), nbt.getIntOr("basez", 0));
        this.cutDir = Direction.values()[Mth.clamp(nbt.getIntOr("cutdir", 0), 0, Direction.values().length - 1)];
        this.toolDir = Direction.values()[Mth.clamp(nbt.getIntOr("tooldir", 0), 0, Direction.values().length - 1)];
        this.trunkHeight = nbt.getIntOr("trunkheight", 0);
        this.soilState = nbt.contains("soilblock") ?
                Pair.of(Identifier.parse(nbt.getStringOr("soilblock", "")), nbt.getIntOr("soilstateid", 0))
                : null;
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        tag.putString("species", species.getRegistryName().toString());
        tag.putIntArray("branchpos", destroyedBranchesRadiusPosition);
        tag.putIntArray("branchcon", destroyedBranchesConnections);
        tag.putIntArray("branchblock", destroyedBranchesBlockIndex);
        tag.putIntArray("leavespos", destroyedLeaves);
        tag.putIntArray("leavesblock", destroyedLeavesBlockIndex);
        tag.putIntArray("ends", endPoints);
        tag.putIntArray("volume", woodVolume.getRawVolumesArray());
        tag.putInt("cutx", cutPos.getX());
        tag.putInt("cuty", cutPos.getY());
        tag.putInt("cutz", cutPos.getZ());
        tag.putInt("basex", basePos.getX());
        tag.putInt("basey", basePos.getY());
        tag.putInt("basez", basePos.getZ());
        tag.putInt("cutdir", cutDir.get3DDataValue());
        tag.putInt("tooldir", toolDir.get3DDataValue());
        tag.putInt("trunkheight", trunkHeight);
        if (soilState != null) {
            tag.putString("soilblock", soilState.getLeft().toString());
            tag.putInt("soilstateid", soilState.getRight());
        }
        return tag;
    }

    ///////////////////////////////////////////////////////////
    // Branches
    ///////////////////////////////////////////////////////////

    private int[][] convertBranchesToIntArrays(Map<BlockPos, BranchConnectionData> branchList) {
        int[] radPosData = new int[branchList.size()];
        int[] connectionData = new int[branchList.size()];
        int[] blockIndexData = new int[branchList.size()];
        int index = 0;

        //Ensure the origin block is at the first index
        BranchConnectionData origConnData = branchList.get(BlockPos.ZERO);
        if (origConnData != null) {
            BlockState origState = origConnData.getBlockState();
            radPosData[index] = encodeBranchesRadiusPos(BlockPos.ZERO, (BranchBlock) origState.getBlock(), origState);
            connectionData[index] = encodeBranchesConnections(origConnData.getConnections());
            blockIndexData[index++] = encodeBranchBlocks((BranchBlock) origState.getBlock());
        }

        //Encode the remaining blocks
        for (Entry<BlockPos, BranchConnectionData> set : branchList.entrySet()) {
            if (set.getKey().equals(BlockPos.ZERO)) continue;
            BlockPos relPos = set.getKey();
            BranchConnectionData connData = set.getValue();
            BlockState state = connData.getBlockState();
            Block block = state.getBlock();

            if (block instanceof BranchBlock && bounds.inBounds(relPos)) { //Place comfortable limits on the system
                radPosData[index] = encodeBranchesRadiusPos(relPos, (BranchBlock) block, state);
                connectionData[index] = encodeBranchesConnections(connData.getConnections());
                blockIndexData[index++] = encodeBranchBlocks((BranchBlock) block);
            }
        }

        //Shrink down the arrays
        radPosData = Arrays.copyOf(radPosData, index);
        connectionData = Arrays.copyOf(connectionData, index);
        blockIndexData = Arrays.copyOf(blockIndexData, index);

        return new int[][]{radPosData, connectionData, blockIndexData};
    }

    private int encodeBranchesRadiusPos(BlockPos relPos, BranchBlock branchBlock, BlockState state) {
        return ((branchBlock.getRadius(state) & 0x1F) << 24) | //Radius 0 - 31
                encodeRelBlockPos(relPos);
    }

    private int encodeBranchesConnections(Connections exState) {
        int result = 0;
        int[] radii = exState.getAllRadii();
        for (Direction face : Direction.values()) {
            int faceIndex = face.get3DDataValue();
            int rad = radii[faceIndex];
            result |= (rad & 0x1F) << (faceIndex * 5);//5 bits per face * 6 faces = 30bits
        }
        return result;
    }

    private int encodeBranchBlocks(BranchBlock branch) {
        return branch.getFamily().getBranchBlockIndex(branch);
    }

    public int getNumBranches() {
        return destroyedBranchesRadiusPosition.length;
    }

    public BlockPos getBranchRelPos(int index) {
        BlockPos pos = decodeRelPos(destroyedBranchesRadiusPosition[index]);
        if (basePos != cutPos){ //When a root system is involved, the relative positions are moved down
            return pos.offset(getRelativeCutPos());
        }
        return pos;
    }

    public BlockPos getRelativeCutPos(){
        return cutPos.subtract(basePos);
    }

    public int getBranchRadius(int index) {
        return decodeBranchRadius(destroyedBranchesRadiusPosition[index]);
    }

    private int decodeBranchRadius(int encoded) {
        return (encoded >> 24) & 0x1F;
    }

    @Nullable
    public BlockState getBranchBlockState(int index) {
        if (destroyedBranchesBlockIndex.length > 0) {
            BranchBlock branch = species.getFamily().getValidBranchBlock(destroyedBranchesBlockIndex[index]);
            if (branch != null) {
                int radius = decodeBranchRadius(destroyedBranchesRadiusPosition[index]);
                return branch.getStateForRadius(radius);
            }
        }
        return null;
    }

    public void getConnections(int index, int[] connections) {
        int encodedConnections = destroyedBranchesConnections[index];

        for (Direction face : Direction.values()) {
            connections[face.get3DDataValue()] = (encodedConnections >> (face.get3DDataValue() * 5) & 0x1F);
        }
    }

    ///////////////////////////////////////////////////////////
    // Leaves
    ///////////////////////////////////////////////////////////

    private int[][] convertLeavesToIntArray(Map<BlockPos, BlockState> leavesList, Species species) {
        int[] posData = new int[leavesList.size()];
        int[] blockIndexData = new int[leavesList.size()];
        int index = 0;

        //Encode the remaining blocks
        for (Entry<BlockPos, BlockState> set : leavesList.entrySet()) {
            BlockPos relPos = set.getKey();
            BlockState state = set.getValue();
            Block block = state.getBlock();

            if (species.canEncodeLeavesBlocks(relPos, state, block, this) && bounds.inBounds(relPos)) { //Place comfortable limits on the system
                posData[index] = species.encodeLeavesPos(relPos, state, block, this);
                blockIndexData[index++] = species.encodeLeavesBlocks(relPos, state, block, this);
            }
        }
        posData = Arrays.copyOf(posData, index); //Shrink down the array
        blockIndexData = Arrays.copyOf(blockIndexData, index);

        return new int[][]{posData, blockIndexData};
    }

    public int getNumLeaves() {
        return destroyedLeaves.length;
    }

    public BlockPos getLeavesRelPos(int index) {
        BlockPos pos = decodeLeavesRelPos(destroyedLeaves[index]);
        if (basePos != cutPos){ //When a root system is involved, the relative positions are moved down
            return pos.offset(getRelativeCutPos());
        }
        return pos;
    }

    private BlockPos decodeLeavesRelPos(int encoded) {
        return decodeRelPos(encoded);
    }

    public int getLeavesHydro(int index) {
        return decodeLeavesHydro(destroyedLeaves[index]);
    }

    private int decodeLeavesHydro(int encoded) {
        return (encoded >> 24) & 0x0F;
    }

    public LeavesProperties getLeavesProperties(int index) {
        return this.species.getValidLeavesProperties(this.destroyedLeavesBlockIndex[index]);
    }

    @Nullable
    public BlockState getLeavesBlockState(int index) {
        DynamicLeavesBlock leaves = species.getValidLeafBlock(destroyedLeavesBlockIndex[index]);
        if (leaves != null) {
            return leaves.defaultBlockState();
        }
        return null;
    }

    public List<Pair<BlockPos, BlockState>> getAllLeavesWithPos(){
        List<Pair<BlockPos, BlockState>> pairs = new ArrayList<>();
        final HashMap<BlockPos, BlockState> leavesClusters = species.getFellingLeavesClusters(this);
        if (leavesClusters != null) {
            leavesClusters.forEach((key, value) -> pairs.add(Pair.of(key, value)));
        } else {
            for (int index = 0; index < getNumLeaves(); index++) {
                BlockPos relPos = getLeavesRelPos(index);
                BlockState leafState = getLeavesBlockState(index);
                pairs.add(Pair.of(relPos, leafState));
            }
        }
        return pairs;
    }

    ///////////////////////////////////////////////////////////
    // End Points
    ///////////////////////////////////////////////////////////

    private int[] convertEndPointsToIntArray(List<BlockPos> endPoints) {
        int[] data = new int[endPoints.size()];
        int index = 0;

        for (BlockPos relPos : endPoints) {
            if (bounds.inBounds(relPos)) { //Place comfortable limits on the system
                data[index++] = encodeRelBlockPos(relPos);
            }
        }

        return Arrays.copyOf(data, index);//Shrink down the array
    }

    public int getNumEndpoints() {
        return endPoints.length;
    }

    public BlockPos getEndPointRelPos(int index) {
        BlockPos pos = decodeRelPos(endPoints[index]);
        if (basePos != cutPos){ //When a root system is involved, the relative positions are moved down
            return pos.offset(getRelativeCutPos());
        }
        return pos;
    }


    ///////////////////////////////////////////////////////////
    // Position Iteration
    ///////////////////////////////////////////////////////////

    public enum PosType {
        BRANCHES,
        LEAVES,
        ENDPOINTS
    }

    /**
     * Get absolute positions of a position type
     */
    public Iterable<BlockPos> getPositions(PosType posType) {
        return getPositions(posType, true);
    }

    /**
     * Get relative or absolute positions of a position type
     */
    public Iterable<BlockPos> getPositions(PosType posType, boolean absolute) {

        final Function<Integer, BlockPos> getter;
        final int limit = switch (posType) {
            case ENDPOINTS -> {
                getter = absolute ? i -> getEndPointRelPos(i).offset(basePos) : this::getEndPointRelPos;
                yield getNumEndpoints();
            }
            case LEAVES -> {
                getter = absolute ? i -> getLeavesRelPos(i).offset(basePos) : this::getLeavesRelPos;
                yield getNumLeaves();
            }
            case BRANCHES -> {
                getter = absolute ? i -> getBranchRelPos(i).offset(basePos) : this::getBranchRelPos;
                yield getNumBranches();
            }
        };

        return new Iterable<>() {
            @NotNull
            public Iterator<BlockPos> iterator() {
                return new AbstractIterator<>() {
                    private int index = 0;

                    protected BlockPos computeNext() {
                        return index < limit ? getter.apply(index++) : this.endOfData();
                    }
                };
            }
        };
    }


    ///////////////////////////////////////////////////////////
    // Generic
    ///////////////////////////////////////////////////////////

    public static int encodeRelBlockPos(BlockPos relPos) {
        return (((relPos.getX() + 64) & 0xFF) << 16) |
                (((relPos.getY() + 64) & 0xFF) << 8) |
                (((relPos.getZ() + 64) & 0xFF));
    }

    public static BlockPos decodeRelPos(int encoded) {
        return new BlockPos(
                (((encoded >> 16) & 0xFF) - 64),
                (((encoded >> 8) & 0xFF) - 64),
                (((encoded) & 0xFF) - 64)
        );
    }

}
