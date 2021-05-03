package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;

/**
 * Wrapper class for eliminating branches in {@link DataPacket} when converting
 * DataPacket values
 * 
 * @author dzh
 */
class DataPacketConverter implements Converter<DataPacket> {

    @Override
    public void serialize(DataPacket value, OutputStream dest, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        ((DataPacket)value).serialize(dest);
    }

    @Override
    public DataPacket deserialize(InputStream in, FieldInfo ctx, Object self)
            throws IOException, ConversionException {
        DataPacket object = null;
        try {
            object = ctx.entityForDeserializing(self, in);
        } catch (Exception e) {
            throw new ExtendedConversionException(
                    self.getClass(),ctx.name,
                    String.format("field value is null and"
                            + " entity class [%s] cannot be instantiated"
                            , ctx.isEntityList ? ctx.listComponentClass : ctx.getFieldType()),e)
                    .withSiteAndOrdinal(DataPacketConverter.class, 11);
        }
        ClassInfo.getClassInfo(object).injectAdditionalFields(self, object);
        object.deserialize(in);
        return object;
    }
}
