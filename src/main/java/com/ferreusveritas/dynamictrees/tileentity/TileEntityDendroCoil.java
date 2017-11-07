package com.ferreusveritas.dynamictrees.tileentity;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.blocks.BlockDendroCoil;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

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
		createStaff,
		testPoisson,
		testPoisson2
	}
	
	private class CachedCommand {
		ComputerMethod method;
		Object[] arguments;

		public CachedCommand(int method, Object[] args) {
			this.method = ComputerMethod.values()[method];
			this.arguments = args;
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

		synchronized(this) {
			treeName = new String(dendroCoil.getTree(world, pos));
			soilLife = dendroCoil.getSoilLife(world, pos);
		}

		//Run commands that are cached that shouldn't be in the lua thread
		synchronized(cachedCommands) {
			if(cachedCommands.size() > 0) { 
				if(dendroCoil != null) {
					for(CachedCommand command:  cachedCommands) {
						switch(command.method) {
							case growPulse: dendroCoil.growPulse(world, pos); break;
							case killTree: dendroCoil.killTree(world, pos); break;
							case plantTree: dendroCoil.plantTree(world, pos, (String)command.arguments[0]); break;
							case setCode: dendroCoil.setCode(world, pos, (String)command.arguments[0], (String)command.arguments[1]); break;
							case setSoilLife: dendroCoil.setSoilLife(world, pos, ((Double)command.arguments[0]).intValue()); break;
							case createStaff: dendroCoil.createStaff(world, pos, (String)command.arguments[0], (String)command.arguments[1], (String)command.arguments[2],(Boolean)command.arguments[3]); break;
							case testPoisson: dendroCoil.testPoisson(world, pos, ((Double)command.arguments[0]).intValue(), ((Double)command.arguments[1]).intValue(), (Double)command.arguments[2]); break;
							case testPoisson2: dendroCoil.testPoisson2(world, pos, ((Double)command.arguments[0]).intValue(), ((Double)command.arguments[1]).intValue(), (Double)command.arguments[2], ((Double)command.arguments[3]).intValue()); break;
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

		if(!world.isRemote && dendroCoil != null) {
			switch(ComputerMethod.values()[method]) {
				case getCode:
					return new Object[]{ dendroCoil.getCode(world, pos) };
				case getSoilLife:
					synchronized(this) {
						return new Object[]{soilLife};
					}
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
				case setSoilLife:
					if(arguments.length >= 1 &&
						arguments[0] instanceof Double) {
						cacheCommand(method, arguments);
					} else {
						throw new LuaException("Expected: " + methodNames[method] + " life<Number>");
					}
					break;
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
						throw new LuaException("Expected: " + methodNames[method] + " radius1<Number>, radius2<Number>, angle<Number>, radius3<Number>");
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
