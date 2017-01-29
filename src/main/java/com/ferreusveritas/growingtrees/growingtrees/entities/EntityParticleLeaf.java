package com.ferreusveritas.growingtrees.entities;

import net.minecraft.client.particle.EntityAuraFX;
import net.minecraft.world.World;

public class EntityParticleLeaf extends EntityAuraFX {

	public EntityParticleLeaf(World world, double x, double y, double z, double velx, double vely, double velz) {
		super(world, x, y, z, velx, vely, velz);
		motionY = vely;
		setParticleTextureIndex(82);
		particleScale = 1.0f;
		setRBGColorF(1.0f, 1.0f, 1.0f);
	}
	
	@Override
	public void onUpdate(){
		super.onUpdate();
	}

}
