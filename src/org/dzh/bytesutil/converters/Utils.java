package org.dzh.bytesutil.converters;

import java.io.BufferedInputStream;

import org.dzh.bytesutil.annotations.types.CHAR;
import org.dzh.bytesutil.converters.auxiliary.Context;

class Utils {
	public static int lengthForSerializingCHAR(Context ctx,Object self) {
		int length = ctx.annotation(CHAR.class).value();
		if(length<0) {
			length = ctx.length;
			if(length<0 && ctx.lengthHandler!=null) {
				length = (Integer)ctx.lengthHandler.handleSerialize(ctx.name, self);
				if(length<0) {
					throw new IllegalArgumentException("should return non-negative value from length handler");
				}
			}
		}
		return length;
	}
	
	public static int lengthForDeserializingCHAR(Context ctx,Object self, BufferedInputStream bis) {
		int length = ctx.annotation(CHAR.class).value();
		if(length<0) {
			length = ctx.length;
			if(length<0 && ctx.lengthHandler!=null) {
				length = (Integer)ctx.lengthHandler.handleDeserialize(ctx.name, self, bis);
				if(length<0) {
					throw new IllegalArgumentException("should return non-negative value from length handler");
				}
			}
		}
		return length;
	}
}
