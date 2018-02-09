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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityDendroCoil extends TileEntity implements IPeripheral, ITickable {

	public enum ComputerMethod {
		growPulse,
		getCode,
		setCode,
		getTree,
		plantTree,
		killTree,
		getSoilLife,
		setSoilLife,
		getSpeciesList,
		createStaff,
		testPoisson,
		testPoisson2,
		testPoisson3
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
			return d(argRead++);
		}

		public int i() {
			return i(argRead++);
		}

		public String s() {
			return s(argRead++);
		}

		public boolean b() {
			return b(argRead++);
		}
		
		public double d(int arg) {
			return ((Double)arguments[arg]).doubleValue();
		}
		
		public int i(int arg) {
			return ((Double)arguments[arg]).intValue();
		}
		
		public String s(int arg) {
			return ((String)arguments[arg]);
		}
		
		public boolean b(int arg) {
			return ((Boolean)arguments[arg]).booleanValue();
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

	public BlockPos getPos() {
		return pos;
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
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException {
		if(method < 0 || method >= numMethods) {
			throw new IllegalArgumentException("Invalid method number");
		}

		BlockDendroCoil dendroCoil = (BlockDendroCoil)getBlockType();
		World world = getWorld();
		
		if(!world.isRemote && dendroCoil != null) {
			switch(ComputerMethod.values()[method]) {
				case getCode:
					return new Object[]{ dendroCoil.getCode(world, getPos()) };
				case getTree:
					synchronized(this) {
						return new Object[]{treeName};
					}
				case plantTree:
					if(arguments.length >= 1 &&
						arguments[0] instanceof String) {
						cacheCommand(method, arguments);
					} else {
						throw new LuaException("Expected: " + methodNames[method] + " treeName<String>");
					}
					break;
				case getSoilLife:
					synchronized(this) {
						return new Object[]{soilLife};
					}
				case setSoilLife:
					if(arguments.length >= 1 &&
						arguments[0] instanceof Double) {
						cacheCommand(method, arguments);
					} else {
						throw new LuaException("Expected: " + methodNames[method] + " life<Number>");
					}
					break;
				case getSpeciesList:
					ArrayList<String> species = new ArrayList<String>();
					TreeRegistry.getSpeciesDirectory().forEach(r -> species.add(r.toString()));
					return species.toArray();
				case createStaff:
					if(arguments.length >= 4 &&
						arguments[0] instanceof String &&
						arguments[1] instanceof String &&
						arguments[2] instanceof String &&
						arguments[3] instanceof Boolean) {
						cacheCommand(method, arguments);
					} else {
						throw new LuaException("Expected: " + methodNames[method] + " treeName<String>, joCode<String>, rgbColor<String>, readOnly<Boolean>");
					}
					break;
				case setCode:
					if(arguments.length >= 2 && 
						arguments[0] instanceof String &&
						arguments[1] instanceof String) {
						cacheCommand(method, arguments);
					} else {
						throw new LuaException("Expected: " + methodNames[method] + " treeName<String>, joCode<String>");
					}
					break;
				case growPulse:
					cacheCommand(method, arguments);
					break;
				case killTree:
					cacheCommand(method, arguments);
					break;
				case testPoisson:
					if(arguments.length >= 3 &&
						arguments[0] instanceof Double &&
						arguments[1] instanceof Double &&
						arguments[2] instanceof Double) {
						cacheCommand(method, arguments);
					} else {
						throw new LuaException("Expected: " + methodNames[method] + " radius1<Number>, radius2<Number>, angle<Number>");
					}
					break;
				case testPoisson2:
					if(arguments.length >= 4 &&
						arguments[0] instanceof Double &&
						arguments[1] instanceof Double &&
						arguments[2] instanceof Double &&
						arguments[3] instanceof Double) {
						cacheCommand(method, arguments);
					} else {
						throw new LuaException("Expected: " + methodNames[method] + " radius1<Number>, radius2<Number>, angle<Number>, radius3<Number>, onlyTight<Boolean>");
					}
				case testPoisson3:
					if(arguments.length >= 4 &&
						arguments[0] instanceof Double &&
						arguments[1] instanceof Double &&
						arguments[2] instanceof Double &&
						arguments[3] instanceof Double &&
						arguments[4] instanceof Double) {
						cacheCommand(method, arguments);
					} else {
						throw new LuaException("Expected: " + methodNames[method] + " radius1<Number>, delX<Number>, delZ<Number>, radius2<Number>, radius3<Number>, onlyTight<Boolean>");
					}
					break;
				default:
					break;
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
