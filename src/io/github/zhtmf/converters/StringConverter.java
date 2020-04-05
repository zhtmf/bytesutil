package io.github.zhtmf.converters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.annotations.types.BCD;

import static io.github.zhtmf.converters.StreamUtils.*;

class StringConverter implements Converter<String> {

    @Override
    public void serialize(String value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case CHAR:
            serializeAsCHAR(value, dest, ctx, self);
            break;
        case BCD:{
            serializeBCD(value, dest, ctx, self);
            break;
        }
        default:throw new Error("should not reach here");
        }
    }

    @Override
    public String deserialize(InputStream in, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case CHAR:{
            Charset cs = ctx.charsetForDeserializingCHAR(self, in);
            int length = ctx.lengthForDeserializingCHAR(self, in);
            if(length<0 && ctx.endsWith==null) {
                length = StreamUtils.readIntegerOfType(in, ctx);
                return new String(StreamUtils.readBytes(in, length),cs);
            }
            if(length>=0) {
                return new String(StreamUtils.readBytes(in, length),cs);
            }
            
            //EndsWith
            byte[] found = KMPSearch(ctx.endsWith,ctx.endingArrayAux,in);
            if(found!=null) {
                return new String(found,0,found.length-ctx.endsWith.length+1,cs);
            }
            throw new ExtendedConversionException(ctx.enclosingEntityClass, ctx.name,
                        "cannot find ending array in the stream")
                        .withSiteAndOrdinal(StringConverter.class, 1);
        }
        case BCD:
            return StreamUtils.readStringBCD(in, ctx.localAnnotation(BCD.class).value());
        default:throw new Error("should not reach here");
        }
    }
    
    private static byte[] KMPSearch(byte[] ending, int[] aux, InputStream txt) throws IOException 
    { 
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len1 = ending.length;
        int j = 0;
        int b = txt.read();
        while (b!=-1) { 
            if (ending[j] == b) { 
                if(++j==len1) {
                    return baos.toByteArray();
                }
                baos.write(b);
                b = txt.read();
            }else{ 
                if (j != 0) {
                    j = aux[j - 1]; 
                }else {
                    baos.write(b);
                    b = txt.read();
                }
            } 
        } 
        return null;
    } 
}
