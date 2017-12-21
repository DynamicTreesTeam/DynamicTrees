package com.ferreusveritas.dynamictrees.api.backport;

public class PropertyInteger implements IProperty {

	public enum Bits {
		B000X(0x01, 0, 1),
		B00X0(0x02, 1, 1),
		B0X00(0x04, 2, 1),
		BX000(0x08, 3, 1),
		B00XX(0x03, 0, 3),
		B0XX0(0x06, 1, 3),
		BXX00(0x0C, 2, 3),
		B0XXX(0x07, 0, 7),
		BXXX0(0x0E, 1, 7),
		BXXXX(0x0F, 0, 15);
		
		private final int mask;
		private final int shift;
		private final int maxInt;
		
		private Bits(int mask, int shift, int maxInt) {
			this.mask = mask;
			this.shift = shift;
			this.maxInt = maxInt;
		}
		
		public int extract(int meta) {
			return (meta & mask) >> shift;
		}

		public int encode(int data, int meta) {
			return (meta & ~mask) | (data << shift) & mask;
		}

		public int getMaxInt() {
			return maxInt;
		}
		
	}
	
	private String name;
	private int min = 0;
	private int max = 0;
	private Bits bits = Bits.BXXXX;
	
	int value = 0;
	
	public static PropertyInteger create(String name, int min, int max, Bits bits) {
		try {
			return new PropertyInteger(name, min, max, bits);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	protected PropertyInteger(String name, int min, int max, Bits bits) throws Exception {
		this.name = name;
		this.min = min;
		this.max = max;
		this.bits = bits;
		
		if(max - min > bits.maxInt) {
			throw new Exception("Range " + min + " - " + max + " to large for available bits:" + bits.maxInt + " in property " + this.name);
		}
	}
	
	@Override
	public int apply(int input, int meta) {
		if(input >= min && input <= max) {
			int value = input - min;
			meta = bits.encode(value, meta);
		} else {
			try {
				throw new Exception("Value out of range in property " + this.name);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return meta;
	}
	
	public int read(int meta) {
		return min + bits.extract(meta);
	}
	
}
