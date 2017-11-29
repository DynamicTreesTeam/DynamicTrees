package com.ferreusveritas.dynamictrees.compat;

public class ClientProxyCompat extends CommonProxyCompat {

	public QuarkProxyBase quarkproxy;//Client side only features

	@Override
	public void preInit() {
		super.preInit();
		
		//Quark Stuff
		quarkproxy = QuarkProxyBase.hasQuark() ? new QuarkProxyActiveClient() : new QuarkProxyBase();
	}
	
	@Override
	public void init() {
		super.init();

		//Quark Stuff
		quarkproxy.init();
	}
	
}
