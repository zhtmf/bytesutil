package examples.mysql.connector;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import examples.mysql.connector.auxiliary.CapabilityFlags;
import examples.mysql.connector.datatypes.le.LEInt8;
import examples.mysql.connector.datatypes.string.LengthEncodedString;
import examples.mysql.connector.packet.HandshakeResponse41;
import examples.mysql.connector.packet.HandshakeResponse41.KeyValue;
import examples.mysql.connector.packet.HandshakeV10;
import examples.mysql.connector.packet.MySQLPacket;

public class Test {
    public static void main(String[] args) throws Exception {
        try(Socket sk = new Socket("127.0.0.1",3306,(InetAddress)null,0)){
            int seq = 0;
            InputStream in = sk.getInputStream();
            OutputStream os = sk.getOutputStream();
            
            //receive server handshake
            MySQLPacket handshake = new MySQLPacket();
            handshake.deserialize(in);
            System.out.println(handshake);
            
            HandshakeV10 v10 = (HandshakeV10)handshake.payload;
            int serverFlags = v10.capFlags2 << 16 | v10.capFlags1;
            System.out.println("server support CLIENT_PROTOCOL_41? "+((serverFlags & CapabilityFlags.CLIENT_PROTOCOL_41)!=0));
            System.out.println("server support CLIENT_CONNECT_WITH_DB? "+((serverFlags & CapabilityFlags.CLIENT_CONNECT_WITH_DB)!=0));
            System.out.println("server support CLIENT_CONNECT_ATTRS? "+((serverFlags & CapabilityFlags.CLIENT_CONNECT_ATTRS)!=0));
            System.out.println("server support CLIENT_PLUGIN_AUTH? "+((serverFlags & CapabilityFlags.CLIENT_PLUGIN_AUTH)!=0));
            int clientFlags = 0;
            clientFlags |= CapabilityFlags.CLIENT_PROTOCOL_41;
            clientFlags |= CapabilityFlags.CLIENT_CONNECT_WITH_DB;
            clientFlags |= CapabilityFlags.CLIENT_CONNECT_ATTRS;
            clientFlags |= CapabilityFlags.CLIENT_PLUGIN_AUTH;
            
            seq = handshake.sequenceId;
            
            //send response, client capabilities etc.
            HandshakeResponse41 resp = new HandshakeResponse41();
            resp.clientFlag = clientFlags;
            resp.maxPacketSize = 0xFFFFFF;
            resp.charSet = v10.charSet;
            resp.username = "dzh";
            resp.authResponseWithLength = "dzh1234";
            resp.database = "sakila";
            resp.clientPluginName = "mysql_native_password";
            resp.attrs = new HandshakeResponse41.ClientConnectAttrs();
            resp.attrs.keyValue = new ArrayList<>();
            resp.attrs.keyValue.add(new KeyValue("connectTimeout","20000"));
            resp.attrs.keyValue.add(new KeyValue("socketTimeout","20000"));
            LEInt8 int8 = new LEInt8();
            int8.value = BigInteger.valueOf(resp.attrs.keyValue.size());
            resp.attrs.lengthAllkeyValues = int8;
            resp.authResponse = new LengthEncodedString();//TODO: delete this
            
            {
                MySQLPacket packet = new MySQLPacket();
                packet.payload = resp;
                packet.payloadLength = resp.length();
                packet.sequenceId = (byte) (seq == 255 ? (seq = 0) : ++seq);
                packet.serialize(os);
            }
            
            //receive server response, if success, handshake phase is over
            {
                MySQLPacket packet = new MySQLPacket();
                packet.capabilitiesFlag = clientFlags;
                packet.phase = 1;
                packet.deserialize(in);
                System.out.println(packet);
            }
        }
    }
}
