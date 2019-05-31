package io.github.zhtmf.converters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.annotations.types.BCD;
import io.github.zhtmf.converters.auxiliary.FieldInfo;
import io.github.zhtmf.converters.auxiliary.MarkableInputStream;
import io.github.zhtmf.converters.auxiliary.StreamUtils;
import io.github.zhtmf.converters.auxiliary.Utils;
import io.github.zhtmf.converters.auxiliary.exceptions.ExtendedConversionException;

public class StringConverter implements Converter<String> {

    @Override
    public void serialize(String value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case CHAR:
            Utils.serializeAsCHAR(value, dest, ctx, self);
            break;
        case BCD:{
            Utils.serializeBCD(value, dest, ctx, self);
            break;
        }
        default:throw new Error("cannot happen");
        }
    }

    @Override
    public String deserialize(MarkableInputStream is, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        switch(ctx.dataType) {
        case CHAR:{
            Charset cs = Utils.charsetForDeserializingCHAR(ctx, self, is);
            int length = Utils.lengthForDeserializingCHAR(ctx, self, is);
            if(length<0 && ctx.endsWith==null) {
                length = StreamUtils.readIntegerOfType(is, ctx.lengthType(), ctx.bigEndian);
                return new String(StreamUtils.readBytes(is, length),cs);
            }
            if(length>=0) {
                return new String(StreamUtils.readBytes(is, length),cs);
            }
            
            //EndsWith
            byte[] found = KMPSearch(ctx.endsWith,ctx.endingArrayAux,is);
            if(found!=null) {
                return new String(found,0,found.length-ctx.endsWith.length+1,cs);
            }
            throw new ExtendedConversionException(ctx.enclosingEntityClass, ctx.name,
                        "cannot find ending array in the stream")
                        .withSiteAndOrdinal(StringConverter.class, 1);
        }
        case BCD:
            return StreamUtils.readStringBCD(is, ctx.localAnnotation(BCD.class).value());
        default:throw new Error("cannot happen");
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