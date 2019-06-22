package examples.mysql.connector;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import examples.mysql.connector.auxiliary.CapabilityFlags;
import examples.mysql.connector.packet.HandshakeResponse41;
import examples.mysql.connector.packet.HandshakeResponse41.KeyValue;
import examples.mysql.connector.packet.HandshakeV10;

public class Test {
    public static void main(String[] args) throws Exception {
        try(Socket sk = new Socket("localhost",3306)){
            InputStream in = sk.getInputStream();
            OutputStream os = sk.getOutputStream();
            HandshakeV10 v10 = new HandshakeV10();
            v10.deserialize(in);
            System.out.println(v10);
            
            HandshakeResponse41 resp = new HandshakeResponse41();
            resp.clientFlag |= CapabilityFlags.CLIENT_PROTOCOL_41;
            resp.clientFlag |= CapabilityFlags.CLIENT_CONNECT_WITH_DB;
            resp.clientFlag |= CapabilityFlags.CLIENT_CONNECT_ATTRS;
            resp.maxPacketSize = 0xFFFF;
            resp.charSet = v10.charSet;
            resp.username = "dzh";
            resp.authResponseWithLength = "dzh1234";
            resp.database = "sakila";
            resp.attrs = new HandshakeResponse41.ClientConnectAttrs();
            resp.attrs.keyValue.add(new KeyValue("connectTimeout","20000"));
            resp.attrs.keyValue.add(new KeyValue("socketTimeout","20000"));
            
            resp.serialize(os);
        }
    }
}
