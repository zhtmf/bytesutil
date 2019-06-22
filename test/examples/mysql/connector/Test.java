package examples.mysql.connector;

import java.io.InputStream;
import java.net.Socket;

import examples.mysql.connector.packet.HandshakeV10;

public class Test {
    public static void main(String[] args) throws Exception {
        Socket sk = new Socket("localhost",3306);
        InputStream in = sk.getInputStream();
        HandshakeV10 v10 = new HandshakeV10();
        v10.deserialize(in);
        System.out.println(v10);
        sk.close();
    }
}
