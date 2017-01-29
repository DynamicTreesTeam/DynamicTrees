package com.ferreusveritas.growingtrees.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;

public class CommonEventHandler {

    @SubscribeEvent 
    public void onChuckDataLoad(ChunkDataEvent.Load event) 
    { 
        //Chunk read from disk 
//        NBTTagCompound compound = event.getData().getCompoundTag("Biota"); 
//        BioSystemHandler.onChunkLoaded(event.world, event.getChunk(), compound); 
    } 
 
    @SubscribeEvent 
    public void onChuckLoad(ChunkEvent.Load event) 
    { 
 //       if (!event.getChunk().worldObj.isRemote) 
 //       { 
 //           BioSystemHandler.onChunkLoaded(event.world, event.getChunk()); 
 //       } 
    } 
 
    @SubscribeEvent 
    public void onChuckDataSave(ChunkDataEvent.Save event) {
    	//System.out.println("onChunkDataSave");
//        //Chunk saved to disk 
//        BioSystem bioSystem = BioSystemHandler.getBioSystem(event.world, event.getChunk()); 
//        if (bioSystem != null) 
//        { 
//            NBTTagCompound compound = new NBTTagCompound(); 
//            bioSystem.saveToNBT(compound); 
//            event.getData().setTag("Biota", compound); 
//        } 
//        if (!event.getChunk().isChunkLoaded) 
//            BioSystemHandler.onChunkUnload(event.world, event.getChunk()); 
    } 
	
}
