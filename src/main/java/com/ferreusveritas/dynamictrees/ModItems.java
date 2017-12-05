package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.items.DirtBucket;
import com.ferreusveritas.dynamictrees.items.Staff;

public class ModItems {
	
	public static Staff treeStaff;
	public static DendroPotion dendroPotion;
	public static DirtBucket dirtBucket;
	
	public static void preInit() {
		
		//Potions
		dendroPotion = new DendroPotion();
		
		//Dirt Bucket
		dirtBucket = new DirtBucket();
		
		//Creative Mode Staff
		treeStaff = new Staff();
	}
	
}
