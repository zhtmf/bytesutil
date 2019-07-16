package examples.mysql.connector;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Arrays;

import examples.mysql.connector.auxiliary.CapabilityFlags;
import examples.mysql.connector.datatypes.result.ColumnDefinition;
import examples.mysql.connector.packet.AuthSwitchRequest;
import examples.mysql.connector.packet.COMInitDB;
import examples.mysql.connector.packet.COMQuery;
import examples.mysql.connector.packet.HandshakeResponse41;
import examples.mysql.connector.packet.HandshakeV10;
import examples.mysql.connector.packet.MySQLPacket;
import examples.mysql.connector.packet.OKPacket;
import examples.mysql.connector.packet.PasswordResponsePacket;
import examples.mysql.connector.packet.TextResultSet;

public class Test {
    
    private static final class Seq{
        private int seq;
        public byte addAndGet() {
            return (byte) (seq == 255 ? (seq = 0) : ++seq);
        }
        public byte get() {
            return (byte)seq;
        }
        public void reset(int seq) {
            this.seq = seq;
        }
        public void reset() {
            reset(0);
        }
    }
    
    private static void checkBody(MySQLPacket resp) {
        if(! ( resp.payload instanceof OKPacket)) {
            System.err.println("[err ]unexpected response from server");
            System.exit(1);
        }
    }
    
    public static void main(String[] args) throws Exception {
        try(Socket sk = new Socket("localhost",3306,(InetAddress)null,0)){
            
            final String username = "dzh";
            final String password = "dzh1234";
            
            Seq seq = new Seq();
            InputStream in = sk.getInputStream();
            OutputStream os = sk.getOutputStream();
            
            //################receive initial server handshake###################
            
            MySQLPacket initialHandshake = new MySQLPacket(0);
            initialHandshake.phase = MySQLPacket.INITIAL_HANDSHAKE;
            initialHandshake.deserialize(in);
            System.out.println("[recv]initial server handshake:"+initialHandshake);
            
            if(!(initialHandshake.payload instanceof HandshakeV10)) {
                System.err.println("[err ]unexpected initial handshake from server:"+initialHandshake.payload);
                System.exit(1);
            }
            
            HandshakeV10 handshakeV10 = (HandshakeV10)initialHandshake.payload;
            int serverFlags = handshakeV10.capFlags2 << 16 | handshakeV10.capFlags1;
            System.out.println("server support CLIENT_PROTOCOL_41? "+((serverFlags & CapabilityFlags.CLIENT_PROTOCOL_41)!=0));
            System.out.println("server support CLIENT_CONNECT_WITH_DB? "+((serverFlags & CapabilityFlags.CLIENT_CONNECT_WITH_DB)!=0));
            System.out.println("server support CLIENT_CONNECT_ATTRS? "+((serverFlags & CapabilityFlags.CLIENT_CONNECT_ATTRS)!=0));
            System.out.println("server support CLIENT_PLUGIN_AUTH? "+((serverFlags & CapabilityFlags.CLIENT_PLUGIN_AUTH)!=0));
            
            if((serverFlags & CapabilityFlags.CLIENT_PROTOCOL_41) == 0) {
                System.err.println("server does not support 4.1 protocol");
                System.exit(1);
            }
            
            if( ! "mysql_native_password".equals(handshakeV10.authPluginName)) {
                System.err.println("this example does not support auth method other than 'mysql_native_password'");
                System.exit(1);
            }
            
            int clientFlags = 0;
            clientFlags |= CapabilityFlags.CLIENT_PROTOCOL_41;
            clientFlags |= CapabilityFlags.CLIENT_PLUGIN_AUTH;
            
            seq.reset(initialHandshake.sequenceId);
            
            //################send client flags and client capabilities################
            
            {
                HandshakeResponse41 resp = new HandshakeResponse41();
                resp.clientFlag = clientFlags;
                resp.maxPacketSize = 0xFFFF;
                resp.charSet = handshakeV10.charSet;
                resp.username = username;
                resp.authResponseWithLength = password;
                resp.clientPluginName = handshakeV10.authPluginName;
                
                MySQLPacket packet = new MySQLPacket(resp,seq.addAndGet());
                System.out.println("[send]response to initial handshake:"+packet);
                packet.serialize(os);
            }
            
            //################receive server response################
            
            {
                MySQLPacket packet = new MySQLPacket(clientFlags);
                packet.deserialize(in);
                System.out.println("[recv]server handshake response:"+packet);
                
                seq.reset(packet.sequenceId);
                
                //normally the server should send back a OKPacket, however it could also 
                //be a AuthSwitchRequest which we should response with encrypted password
                if(packet.payload instanceof AuthSwitchRequest) {
                    AuthSwitchRequest payload = (AuthSwitchRequest)packet.payload;
                    MessageDigest digest = MessageDigest.getInstance("SHA-1");
                    //SHA1( password ) XOR SHA1( "20-bytes random data from server" <concat> SHA1( SHA1( password ) ) )
                    byte[] part1 = digest.digest(password.getBytes());
                    byte[] part3 = digest.digest(digest.digest(password.getBytes()));
                    byte[] bytes20 = payload.pluginProvidedData.substring(
                            0, payload.pluginProvidedData.length()-1).getBytes();//remove trailing \0
                    byte[] concatenated = Arrays.copyOf(bytes20, bytes20.length+part3.length);
                    System.arraycopy(part3, 0, concatenated, bytes20.length, part3.length);
                    byte[] part2 = digest.digest(concatenated);
                    for(int i=0;i<part1.length;++i) {
                        part1[i] = (byte) (part1[i] ^ part2[i]);
                    }
                    
                    //another packet which does not comply with pattern which uses first byte to 
                    //indicate the type, the packet itself is merely a byte array
                    PasswordResponsePacket resp = new PasswordResponsePacket();
                    resp.pwd = part1;
                    
                    MySQLPacket packet2 = new MySQLPacket(resp,seq.addAndGet());
                    System.out.println("[send]auth switch request response to initial handshake:"+packet2);
                    packet2.serialize(os);
                    
                    MySQLPacket packet3 = new MySQLPacket(clientFlags);
                    packet3.deserialize(in);
                    System.out.println("[recv]server response to encrypted password:"+packet3);
                    
                    checkBody(packet3);
                }
            }
            
            seq.reset();
            
            //################Init DB################
            {
                COMInitDB query = new COMInitDB();
                query.schemaName = "sakila";
                MySQLPacket packet = new MySQLPacket(query,seq.get());
                System.out.println("[send]init db:"+packet);
                packet.serialize(os);
                MySQLPacket resp = new MySQLPacket(clientFlags);
                resp.deserialize(in);
                System.out.println("[recv]init db response:"+resp);
                seq.reset(resp.sequenceId);
                checkBody(resp);
            }
            //################Run Query################
            {
                COMQuery query = new COMQuery();
                query.query = "select version(),CURRENT_TIMESTAMP;";
                MySQLPacket packet = new MySQLPacket(query,seq.addAndGet());
                System.out.println("[send]query:"+packet);
                
                MySQLPacket resp = new MySQLPacket(clientFlags);
                resp.deserialize(in);
                System.out.println("[recv]query response:"+resp);
                seq.reset(resp.sequenceId);
                
                if (packet.payload instanceof TextResultSet) {
                    TextResultSet rs = (TextResultSet) packet.payload;
                    for (ColumnDefinition def : rs.fieldMetaData) {
                        System.out.println(def);
                    }
                } else {
                    System.err.println(packet);
                }
                
            }
        }
    }
}