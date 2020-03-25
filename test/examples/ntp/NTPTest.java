package examples.ntp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.junit.Test;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.converters.TestUtils;

/**
 * <p>
 * An example that queries several NTP servers and print their responses.
 * <p>
 * Mainly for demonstrating usage of the {@link Fixed} annotation.
 * 
 * @author dzh
 */
public class NTPTest {
    
    @Test
    public void testNTP() throws Exception {
        queryServer("ntp.jst.mfeed.ad.jp");
        queryServer("jp.pool.ntp.org");
        queryServer("ntp.nict.jp");
        queryServer("centos.pool.ntp.org");
    }
    
    private void queryServer(String host) throws Exception{
        NTPPacket request = new NTPPacket();
        request.LI = LeapIndicator.CLOCK_UNSYNCHRONIZED;
        request.mode = Mode.CLIENT;
        request.Stratum = 16;
        request.ReferenceIdentifier = new byte[4];
        request.OriginateTimestamp = System.currentTimeMillis()*1.0/1000;
        
        DatagramSocket ds = new DatagramSocket(11451);
        send(ds, request, "ntp.jst.mfeed.ad.jp");
        NTPPacket response = receive(ds);
        
        double t4 = System.currentTimeMillis()*1.0/1000;
        double t1 = request.OriginateTimestamp;
        double t2 = response.ReceiveTimestamp;
        double t3 = response.TransmitTimestamp;
        
        System.out.println("result from server: "+host);
        System.out.println("response: " + response);
        System.out.println("offset: " + (((t2 - t1) + (t3 - t4)) / 2));
        System.out.println("delay: " + ((t4 - t1) - (t3 - t2)));
        
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
