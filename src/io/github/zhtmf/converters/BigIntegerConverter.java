package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import io.github.zhtmf.ConversionException;

import static io.github.zhtmf.converters.StreamUtils.*;

class BigIntegerConverter implements Converter<BigInteger>{
    
    @Override
    public void serialize(BigInteger value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case LONG:
            String error = null;
            if((error = DataTypeOperations.LONG.checkRange(value, ctx))!=null) {
                throw new ExtendedConversionException(ctx.enclosingEntityClass, ctx.name, error)
                        .withSiteAndOrdinal(BigIntegerConverter.class, 1);
            }
            StreamUtils.writeLong(dest, value.longValue(), ctx.bigEndian);
            return;
        case CHAR:
            serializeAsCHAR(value, dest, ctx, self);
            return;
        case VARINT:
            StreamUtils.writeUnsignedVarint(dest, value, ctx.bigEndian);
            return;
        case NUMBER:
            if(value.compareTo(BigInteger.ZERO) < 0) {
                throw new ExtendedConversionException(ctx.enclosingEntityClass, ctx.name, "NUMBER type can only encode unsigned values")
                    .withSiteAndOrdinal(BigIntegerConverter.class, 3);
            }
            int length = ctx.lengthForSerializingNUMBER(self);
            if(length == 0) {
                return;
            }
            byte[] raw = value.toByteArray();
            int from = 0;
            if(raw.length > 1) {
                while(raw[from] == 0)++from;
                if(ctx.littleEndian) {
                    StreamUtils.reverse(raw, from, raw.length);
                }
            }
            //in such encoding, the sender can use more bytes than minimum necessary to represent a number,
            //so the defined length value should be respected and the actual byte sequence be padded if needed.
            int gap = length - raw.length + from;
            if(ctx.bigEndian) {
                for(int i=0;i<gap;++i) {
                    StreamUtils.writeBYTE(dest, (byte)0);
                }
            }
            StreamUtils.writeBytes(dest, raw, from);
            if(ctx.littleEndian) {
                for(int i=0;i<gap;++i) {
                    StreamUtils.writeBYTE(dest, (byte)0);
                }
            }
            return;
        default: throw new Error("should not reach here");
        }
    }

    @Override
    public BigInteger deserialize(InputStream in, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case LONG:
            BigInteger ret = null;
            if(ctx.signed) {
                ret = BigInteger.valueOf(StreamUtils.readLong(in, ctx.bigEndian));
            }else {
                ret = StreamUtils.readUnsignedLong(in, ctx.bigEndian);
            }
            String error = null;
            if((error = DataTypeOperations.LONG.checkRange(ret, ctx))!=null) {
                throw new ExtendedConversionException(ctx.enclosingEntityClass, ctx.name, error)
                        .withSiteAndOrdinal(BigIntegerConverter.class, 2);
            }
            return ret;
        case CHAR:
            return deserializeBigIntegerAsCHAR(in, ctx, self, ctx.dataType);
        case VARINT:
            return readVarint((MarkableInputStream)in, ctx.bigEndian);
        case NUMBER:
            byte[] raw = StreamUtils.readBytes(in, ctx.lengthForDeserializingNUMBER(self, in));
            if(raw.length == 0) {
                return null;
            }
            if(ctx.littleEndian) {
                StreamUtils.reverse(raw);
            }
            return new BigInteger(1, raw);
        default: throw new Error("should not reach here");
        }
    }
}