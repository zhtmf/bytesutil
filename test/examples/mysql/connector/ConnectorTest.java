package examples.mysql.connector;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Arrays;

import examples.mysql.connector.packet.CapabilityFlags;
import examples.mysql.connector.packet.MySQLPacket;
import examples.mysql.connector.packet.command.COMInitDB;
import examples.mysql.connector.packet.command.COMQuery;
import examples.mysql.connector.packet.command.ColumnDefinition;
import examples.mysql.connector.packet.command.QueryResultColumnCount;
import examples.mysql.connector.packet.command.TextResultsetRow;
import examples.mysql.connector.packet.common.EOFPacket;
import examples.mysql.connector.packet.common.ERRPacket;
import examples.mysql.connector.packet.common.OKPacket;
import examples.mysql.connector.packet.connection.AuthSwitchRequest;
import examples.mysql.connector.packet.connection.HandshakeResponse41;
import examples.mysql.connector.packet.connection.HandshakeV10;
import examples.mysql.connector.packet.connection.PasswordResponsePacket;
import io.github.zhtmf.ConversionException;

public class ConnectorTest {
    
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
            initialHandshake.payload = new HandshakeV10();
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
            
            //################send client flags and client clientCapabilities################
            
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
            
            
            //################Init DB################
            {
                seq.reset();
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
            //################Run Queries################
            System.out.println("####### test query ##########");
            runQuery(os,in,clientFlags,"select version(),CURRENT_TIMESTAMP,222 as cnt,NULL as null_value");
            System.out.println("####### show system variables ##########");
            runQuery(os,in,clientFlags,"show VARIABLES");
            System.out.println("####### show contents in city table ##########");
            runQuery(os,in,clientFlags,"select * from city");
        }
    }
    
    private static void runQuery(OutputStream os, InputStream in, int clientFlags, String queryString) throws IllegalArgumentException, ConversionException {
        Seq seq = new Seq();
        COMQuery query = new COMQuery();
        query.query = queryString;
        MySQLPacket packet = new MySQLPacket(query,seq.get());
        System.out.println("[send]query:"+packet);
        packet.serialize(os);
        /*
         * The column definitions part starts with a packet containing the column-count,
         * followed by as many Column Definition packets as there are columns and
         * terminated by an EOF_Packet. packet if the CLIENT_DEPRECATE_EOF capability
         * flag is not set.
         */
        MySQLPacket resp = new MySQLPacket(clientFlags);
        resp.payload = new QueryResultColumnCount();
        resp.deserialize(in);
        System.out.println("[recv]column count:"+resp);
        QueryResultColumnCount count = (QueryResultColumnCount)resp.payload;
        int columnCount = count.columnCount.getNumericValue().intValue();
        for(int i=0;i<columnCount;++i) {
            resp.payload = new ColumnDefinition();
            resp.deserialize(in);
            System.out.println("[recv]column metadata:"+resp);
        }
        resp.deserialize(in);
        System.out.println("[recv]eof marking end of column metadata:"+resp);
        /*
         * One or more ProtocolText::ResultsetRow packets, each containing column_count
         * values
         * 
         * ERR_Packet in case of error. Otherwise: If the CLIENT_DEPRECATE_EOF client
         * capability flag is set, OK_Packet; else EOF_Packet.
         */
        for(;;) {
            TextResultsetRow row = new TextResultsetRow();
            row.columnCount = columnCount;
            resp.payload = row;
            resp.deserialize(in);
            if(resp.payload instanceof ERRPacket) {
                System.err.println("[recv]error during receiving result set data:"+resp);
                System.exit(1);
            }else if(resp.payload instanceof EOFPacket) {
                System.out.println("[recv]eof marking end of result set data:"+resp);
                break;
            }else if(resp.payload instanceof TextResultsetRow){
                System.out.println("[recv]result set data:"+resp);
            }else {
                System.err.println("[recv]unexpected response during receiving result set data:"+resp);
                System.exit(1);
            }
        }
    }
}