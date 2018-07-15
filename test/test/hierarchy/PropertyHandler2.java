package test.hierarchy;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.dzh.bytesutil.converters.auxiliary.ModifierHandler;

import test.entity.MyEntity;

public class PropertyHandler2 extends ModifierHandler<Charset> {

	@Override
	public Charset handleDeserialize0(String fieldName,Object entity, InputStream is){
		MyEntity sb = (MyEntity)entity;
		return sb.a>0 ? Charset.forName("UTF-8") : Charset.forName("GBK");
	}

	@Override
	public Charset handleSerialize0(String fieldName,Object entity){
		MyEntity sb = (MyEntity)entity;
		return sb.a>0 ? Charset.forName("UTF-8") : Charset.forName("GBK");
	}

}
