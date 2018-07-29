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
	};
	abstract public Class<? extends Annotation> annotationClassOfThisType();
	abstract public int size();
}