package org.dzh.bytesutil.converters.auxiliary;

import java.lang.annotation.Annotation;

public enum DataType{
	BYTE {
		@Override
		public Class<? extends Annotation> annotationClassOfThisType() {
			return org.dzh.bytesutil.annotations.types.BYTE.class;
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		public String checkRange(long val, boolean unsigned) {
			if(unsigned) {
				if(val<0 || val>((long)Byte.MAX_VALUE*2+1)) {
					return "val " + val +" cannot be stored as unsigned 1-byte integer value";
				}
			}else {
				if(val<Byte.MIN_VALUE || val>Byte.MAX_VALUE) {
					return "val " + val + " cannot be stored as signed 1-byte integer value";
				}
			}
			return null;
		}

		@Override
		boolean supports(Class<?> javaType) {
			return javaType == byte.class
				|| javaType == short.class
				|| javaType == int.class
				|| javaType == long.class
				|| javaType == Byte.class
				|| javaType == Short.class
				|| javaType == Integer.class
				|| javaType == Long.class
				/*
				 * checking for validity of this enum class 
				 * is done in EnumFieldInfo, not here.
				 */
				|| javaType.isEnum();
		}
	},
	SHORT {
		@Override
		public Class<? extends Annotation> annotationClassOfThisType() {
			return org.dzh.bytesutil.annotations.types.SHORT.class;
		}

		@Override
		public int size() {
			return 2;
		}

		@Override
		public String checkRange(long val, boolean unsigned) {
			if(unsigned) {
				if(val<0 || val>((long)Short.MAX_VALUE*2+1)) {
					return String.format("val [%d] cannot be stored as unsigned 2-byte integer value",val);
				}
			}else {
				if(val<Short.MIN_VALUE || val>Short.MAX_VALUE) {
					return String.format("val [%d] cannot be stored as signed 2-byte integer value",val);
				}
			}
			return null;
		}

		@Override
		boolean supports(Class<?> javaType) {
			return javaType == short.class
				|| javaType == int.class
				|| javaType == long.class
				|| javaType == Short.class
				|| javaType == Integer.class
				|| javaType == Long.class
				|| javaType.isEnum();
		}
	},
	INT {
		@Override
		public Class<? extends Annotation> annotationClassOfThisType() {
			return org.dzh.bytesutil.annotations.types.INT.class;
		}

		@Override
		public int size() {
			return 4;
		}

		@Override
		public String checkRange(long val, boolean unsigned) {
			if(unsigned) {
				if(val<0 || val>((long)Integer.MAX_VALUE*2+1)) {
					return String.format("val [%d] cannot be stored as unsigned 4-byte integer value",val);
				}
			}else {
				if(val<Integer.MIN_VALUE || val>Integer.MAX_VALUE) {
					return String.format("val [%d] cannot be stored as signed 4-byte integer value",val);
				}
			}
			return null;
		}

		@Override
		boolean supports(Class<?> javaType) {
			return javaType == int.class
				|| javaType == long.class
				|| javaType == Integer.class
				|| javaType == Long.class
				|| javaType.isEnum();
		}
	},
	BCD {
		@Override
		public Class<? extends Annotation> annotationClassOfThisType() {
			return org.dzh.bytesutil.annotations.types.BCD.class;
		}

		@Override
		boolean supports(Class<?> javaType) {
			return javaType == byte.class
				|| javaType == short.class
				|| javaType == int.class
				|| javaType == long.class
				|| javaType == Byte.class
				|| javaType == Short.class 
				|| javaType == Integer.class
				|| javaType == Long.class
				|| javaType == String.class
				|| javaType == java.util.Date.class
				;
		}
	},
	RAW{

		@Override
		public Class<? extends Annotation> annotationClassOfThisType() {
			return org.dzh.bytesutil.annotations.types.RAW.class;
		}
		
		@Override
		boolean supports(Class<?> javaType) {
			return javaType == byte[].class
				|| javaType == int[].class
				;
		}
	},
	CHAR{

		@Override
		public Class<? extends Annotation> annotationClassOfThisType() {
			return org.dzh.bytesutil.annotations.types.CHAR.class;
		}
		
		@Override
		boolean supports(Class<?> javaType) {
			return javaType == byte.class
					|| javaType == short.class
					|| javaType == int.class
					|| javaType == long.class
					|| javaType == Byte.class
					|| javaType == Short.class
					|| javaType == Integer.class
					|| javaType == Long.class
					|| javaType == String.class
					|| javaType == char.class
					|| javaType == Character.class
					|| javaType == java.util.Date.class
					|| javaType.isEnum();
		}
	};
	abstract public Class<? extends Annotation> annotationClassOfThisType();
	public int size() {throw new UnsupportedOperationException();}
	public String checkRange(long val, boolean unsigned) {throw new UnsupportedOperationException();}
	abstract boolean supports(Class<?> javaType);
}