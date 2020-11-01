package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.github.zhtmf.ConversionException;

/**
 * Wrapper class for eliminating branches when converting List values
 * 
 * @author dzh
 */
@SuppressWarnings("rawtypes")
class ListConverter extends AbstractListConverter implements Converter<List> {

    @Override
    public void serialize(List value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        @SuppressWarnings("unchecked")
        List<Object> listValue = (List<Object>)value;
        //validity check is done in ClassInfo
        int length = lengthForSerialize(listValue, dest, ctx, self);
        byte[] markerArray = ctx.listEndsWithArray;
        try {
            for(int i=0;i<length;++i) {
                Object elem = listValue.get(i);
                if(elem==null) {
                    throw new ExtendedConversionException(
                            self.getClass(),ctx.name,
                            "list contains null value")
                            .withSiteAndOrdinal(ListConverter.class, -1);
                }
                @SuppressWarnings("unchecked")
                Converter<Object> cv = (Converter<Object>)ctx.innerConverter;
                cv.serialize(elem, dest, ctx, self);
            }
            if(markerArray != null) {
                StreamUtils.writeBytes(dest, markerArray);
            }
        } catch(ConversionException e) {
            throw e;
        } catch (Exception e) {
            throw new ExtendedConversionException(self.getClass(),ctx.name,e)
                            .withSiteAndOrdinal(ListConverter.class, 4);
        }
    }

    @Override
    public List deserialize(java.io.InputStream in, FieldInfo ctx, Object self) throws IOException, ConversionException {
        int length = lengthForDeserialize(in, ctx, self);
        List<Object> tmp = null;
        //cv cannot be null as we lifted checking for validity of DataType <--> JavaType mapping
        //to constructor of FieldInfo
        @SuppressWarnings("unchecked")
        Converter<Object> cv = (Converter<Object>)ctx.innerConverter;
        try {
            if (length >= 0) {
        		tmp = new ArrayList<>(length);
        		while(length-->0) {
        			tmp.add(cv.deserialize(in, ctx, self));
        		}
        	}else {
        		tmp = new ArrayList<Object>();
        		DelegateModifierHandler<Boolean> listTerminationHandler = ctx.listTerminationHandler;
        		DelegateModifierHandler.context.set(tmp);
        		while(listTerminationHandler.handleDeserialize0(ctx.name, self, in) == false){
        			tmp.add(cv.deserialize(in, ctx, self));
        		}
        	}
        } catch(ConversionException e) {
            throw e;
        } catch (Exception e) {
            throw new ExtendedConversionException(self.getClass(),ctx.name,e)
                    .withSiteAndOrdinal(ListConverter.class, 14);
        } finally {
        	DelegateModifierHandler.context.remove();
        }
        return tmp;
    }
}