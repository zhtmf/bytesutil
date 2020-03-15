package examples.ntp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.converters.TestUtils;

public class TestNTP {
    
    /*
     * .支持>8个的bit
     * .支持通用二进制浮点数表示和转换
     * .支持bit转换到bitset, byte[]
     */

    @Test
    public void testNTP() throws IllegalArgumentException, ConversionException, IOException {
        
        NTPPacket packet = new NTPPacket();
        packet.LI = LeapIndicator.CLOCK_UNSYNCHRONIZED;
        packet.mode = Mode.CLIENT;
        packet.Stratum = 16;
        packet.ReferenceIdentifier = new byte[4];
        packet.ReferenceTimestamp = new NTPTimestamp();
        packet.OriginateTimestamp = new NTPTimestamp();
        
        DatagramSocket ds = new DatagramSocket(11451);
        send(ds, packet, "ntp.jst.mfeed.ad.jp");
        packet = receive(ds);
        
        System.out.println(packet);
        
        ds.close();
    }

    private void send(DatagramSocket socket, NTPPacket packet, String addr) throws IllegalArgumentException, ConversionException, IOException {
        byte[] bytes = TestUtils.serializeAndGetBytes(packet);
        DatagramPacket out = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(addr), 123);
        socket.send(out);
    }
    
    private NTPPacket receive(DatagramSocket socket) throws IllegalArgumentException, ConversionException, IOException {
        ByteArrayOutputStream received = new ByteArrayOutputStream();
        DatagramPacket in = new DatagramPacket(new byte[1024], 1024);
        for(;;) {
            socket.receive(in);
            received.write(in.getData(), 0, in.getLength());
            if(in.getLength() < 1024) {
                break;
            }
        }
        NTPPacket packet = new NTPPacket();
        packet.deserialize(TestUtils.newInputStream(received.toByteArray()));
        return packet;
    }
}
