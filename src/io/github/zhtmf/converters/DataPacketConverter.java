package io.github.zhtmf.converters;

import java.io.IOException;
import java.io.OutputStream;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.DataPacket;
import io.github.zhtmf.converters.auxiliary.FieldInfo;
import io.github.zhtmf.converters.auxiliary.MarkableInputStream;
import io.github.zhtmf.converters.auxiliary.exceptions.ExtendedConversionException;

/**
 * Wrapper class for eliminating branches in {@link DataPacket} when converting
 * DataPacket values
 * 
 * @author dzh
 */
public class DataPacketConverter implements Converter<DataPacket> {

    @Override
    public void serialize(DataPacket value, OutputStream dest, FieldInfo fi, Object self)
            throws IOException, ConversionException {
        ((DataPacket)value).serialize(dest);
    }

    @Override
    public DataPacket deserialize(MarkableInputStream is, FieldInfo fi, Object self)
            throws IOException, ConversionException {
        DataPacket object = null;
        try {
            object = fi.entityCreator.handleDeserialize(fi.name, self, is);
        } catch (Exception e) {
            throw new ExtendedConversionException(
                    self.getClass(),fi.name,
                    String.format("field value is null and"
                            + " entity class [%s] cannot be instantiated"
                            , fi.isEntityList ? fi.listComponentClass : fi.getFieldType()),e)
                    .withSiteAndOrdinal(DataPacketConverter.class, 11);
        }
        object.deserialize(is);
        return object;
    }
}
