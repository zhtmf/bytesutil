package org.dzh.bytesutil.converters.auxiliary;

import java.lang.annotation.Annotation;

public enum DataType{
	BYTE {
		@Override
		public Class<? extends Annotation> annotationClassOfThisType() {
			return org.dzh.bytesutil.annotations.types.BYTE.class;
		}
	},
	SHORT {
		@Override
		public Class<? extends Annotation> annotationClassOfThisType() {
			return org.dzh.bytesutil.annotations.types.SHORT.class;
		}
	},
	INT {
		@Override
		public Class<? extends Annotation> annotationClassOfThisType() {
			return org.dzh.bytesutil.annotations.types.INT.class;
		}
	},
	BCD {
		@Override
		public Class<? extends Annotation> annotationClassOfThisType() {
			return org.dzh.bytesutil.annotations.types.BCD.class;
		}
	},
	RAW{

		@Override
		public Class<? extends Annotation> annotationClassOfThisType() {
			return org.dzh.bytesutil.annotations.types.RAW.class;
		}
		
	},
	CHAR{

		@Override
		public Class<? extends Annotation> annotationClassOfThisType() {
			return org.dzh.bytesutil.annotations.types.CHAR.class;
		}
		
	};
	abstract public Class<? extends Annotation> annotationClassOfThisType();
}