package com.ferreusveritas.dynamictrees.tileentity;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.BlockDendroCoil;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;

public class TileEntityDendroCoil extends TileEntity implements IPeripheral, ITickable {

	public enum ComputerMethod {
		growPulse("", true),
		getCode("", false),
		setCode("ss", true, "treeName", "joCode"),
		getTree("", false),
		plantTree("s", true, "treeName"),
		killTree("", true),
		getSoilLife("", false),
		setSoilLife("n", true, "life"),
		getSpeciesList("", false),
		createStaff("sssb", true, "treeName", "joCode", "rgbColor", "readOnly"),
		testPoisson("nnn", true, "radius1", "radius2", "angle"),
		testPoisson2("nnnn", true, "radius1", "radius2", "angle", "radius3", "onlyTight"),
		testPoisson3("nnnnnb", true, "radius1", "delX", "delZ", "radius2", "radius3", "onlyTight");
		
		private final String argTypes;
		private final String args[];
		private final boolean cached;
		
		private ComputerMethod(String argTypes, boolean cached, String ... args) {
			this.argTypes = argTypes;
			this.args = args;
			this.cached = cached;
		}
		
		public boolean isCached() {
			return cached;
		}
		
		public boolean isValidArguments(Object[] arguments) {
			if(arguments.length >= argTypes.length()) {
				for (int i = 0; i < argTypes.length(); i++){
					if(!CCDataType.byIdent(argTypes.charAt(i)).isInstance(arguments[i])) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
		
		public boolean validateArguments(Object[] arguments) throws LuaException {
			if(isValidArguments(arguments)) {
				return true;
			}
			throw new LuaException(invalidArgumentsError());
		}
		
		public String invalidArgumentsError() {
			String error = "Expected: " + this.toString();
			for (int i = 0; i < argTypes.length(); i++){
				error += " " + args[i] + "<" + CCDataType.byIdent(argTypes.charAt(i)).name + ">";
			}
			return error;
		}
	}
	
	private class CachedCommand {
		ComputerMethod method;
		Object[] arguments;
		int argRead = 0;
		
		public CachedCommand(int method, Object[] args) {
			this.method = ComputerMethod.values()[method];
			this.arguments = args;
		}
		
		public double d() {
			return ((Double)arguments[argRead++]).doubleValue();
		}
		
		public int i() {
			return ((Double)arguments[argRead++]).intValue();
		}
		
		public String s() {
			return ((String)arguments[argRead++]);
		}
		
		public boolean b() {
			return ((Boolean)arguments[argRead++]).booleanValue();
		}
	}
	
	private ArrayList<CachedCommand> cachedCommands = new ArrayList<CachedCommand>(1);
	private String treeName;
	private int soilLife;
	
	public static final int numMethods = ComputerMethod.values().length;
	public static final String[] methodNames = new String[numMethods]; 
	static {
		for(ComputerMethod method : ComputerMethod.values()) { 
			methodNames[method.ordinal()] = method.toString(); 
		}
	}
	
	public void cacheCommand(int method, Object[] args) {
		synchronized (cachedCommands) {
			cachedCommands.add(new CachedCommand(method, args));
		}
	}
	
	@Override
	public void update() {
		
		BlockDendroCoil dendroCoil = (BlockDendroCoil)getBlockType();
		World world = getWorld();
		
		synchronized(this) {
			treeName = new String(dendroCoil.getSpecies(world, getPos()));
			soilLife = dendroCoil.getSoilLife(world, getPos());
		}
		
		//Run commands that are cached that shouldn't be in the lua thread
		synchronized(cachedCommands) {
			if(cachedCommands.size() > 0) { 
				if(dendroCoil != null) {
					for(CachedCommand cmd:  cachedCommands) {
						switch(cmd.method) {
							case growPulse: dendroCoil.growPulse(world, getPos()); break;
							case killTree: dendroCoil.killTree(world, getPos()); break;
							case plantTree: dendroCoil.plantTree(world, getPos(), cmd.s()); break;
							case setCode: dendroCoil.setCode(world, getPos(), cmd.s(), cmd.s()); break;
							case setSoilLife: dendroCoil.setSoilLife(world, getPos(), cmd.i()); break;
							case createStaff: dendroCoil.createStaff(world, getPos(), cmd.s(), cmd.s(), cmd.s(), cmd.b()); break;
							case testPoisson: dendroCoil.testPoisson(world, getPos(), cmd.i(), cmd.i(), cmd.d(), cmd.b()); break;
							case testPoisson2: dendroCoil.testPoisson2(world, getPos(), cmd.i(), cmd.i(), cmd.d(), cmd.i(), cmd.b()); break;
							case testPoisson3: dendroCoil.testPoisson3(world, getPos(), cmd.i(), getPos().add(cmd.i(), 0, cmd.i()), cmd.i(), cmd.i()); break;
							default: break;
						}
					}
					cachedCommands.clear();
				}
			}
		}
	}
	
	@Override
	public String getType() {
		return "dendrocoil";
	}
	
	@Override
	public String[] getMethodNames() {
		return methodNames;
	}
	
	/**
	* I hear ya Dan!  Make the function threadsafe by caching the commmands to run in the main world server thread and not the lua thread.
	*/
	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int methodNum, Object[] arguments) throws LuaException {
		if(methodNum < 0 || methodNum >= numMethods) {
			throw new IllegalArgumentException("Invalid method number");
		}
		
		BlockDendroCoil dendroCoil = (BlockDendroCoil)getBlockType();
		World world = getWorld();
		
		if(!world.isRemote && dendroCoil != null) {
			ComputerMethod method = ComputerMethod.values()[methodNum];

			if(method.validateArguments(arguments)) {
				switch(method) {
					case getCode:
						return new Object[]{ dendroCoil.getCode(world, getPos()) };
					case getTree:
						synchronized(this) {
							return new Object[]{treeName};
						}
					case getSoilLife:
						synchronized(this) {
							return new Object[]{soilLife};
						}
					case getSpeciesList:
						ArrayList<String> species = new ArrayList<String>();
						TreeRegistry.getSpeciesDirectory().forEach(r -> species.add(r.toString()));
						return species.toArray();
					default:
						if(method.isCached()) {
							cacheCommand(methodNum, arguments);
						}
				}
			}
		}
		
		return null;
	}
	
	@Override
	public void attach(IComputerAccess computer) {
	}
	
	@Override
	public void detach(IComputerAccess computer) {
	}
	
	@Override
	public boolean equals(IPeripheral other) {
		return this == other;
	}
	
}
