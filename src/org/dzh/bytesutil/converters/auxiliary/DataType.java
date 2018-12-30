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
		Class<?> correspondingJavaClass() {
			return Byte.class;
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
		Class<?> correspondingJavaClass() {
			return Short.class;
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
		Class<?> correspondingJavaClass() {
			return Integer.class;
		}
	},
	BCD {
		@Override
		public Class<? extends Annotation> annotationClassOfThisType() {
			return org.dzh.bytesutil.annotations.types.BCD.class;
		}

		@Override
		public int size() {
			throw new UnsupportedOperationException();
		}

		@Override
		Class<?> correspondingJavaClass() {
			throw new UnsupportedOperationException();
		}
	},
	RAW{

		@Override
		public Class<? extends Annotation> annotationClassOfThisType() {
			return org.dzh.bytesutil.annotations.types.RAW.class;
		}
		
		@Override
		public int size() {
			throw new UnsupportedOperationException();
		}

		@Override
		Class<?> correspondingJavaClass() {
			throw new UnsupportedOperationException();
		}
	},
	CHAR{

		@Override
		public Class<? extends Annotation> annotationClassOfThisType() {
			return org.dzh.bytesutil.annotations.types.CHAR.class;
		}
		
		@Override
		public int size() {
			throw new UnsupportedOperationException();
		}

		@Override
		Class<?> correspondingJavaClass() {
			throw new UnsupportedOperationException();
		}
	};
	abstract public Class<? extends Annotation> annotationClassOfThisType();
	abstract public int size();
	abstract Class<?> correspondingJavaClass();
}