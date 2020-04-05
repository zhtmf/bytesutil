package examples.ntp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import io.github.zhtmf.ConversionException;
import io.github.zhtmf.annotations.types.Fixed;
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
    
    private static final long GAP;
    static {
        Timestamp epoch = Timestamp.valueOf(LocalDateTime.of(1970, 1, 1, 0, 0));
        Timestamp ntpEpoch = Timestamp.valueOf(LocalDateTime.of(1900, 1, 1, 0, 0));
        GAP = epoch.getTime() - ntpEpoch.getTime();
    }
    
    public static void main(String[] args) throws Exception {
        queryServer("ntp.jst.mfeed.ad.jp");
        queryServer("jp.pool.ntp.org");
        queryServer("ntp.nict.jp");
        queryServer("centos.pool.ntp.org");
    }
    
    private static void queryServer(String host) throws SocketTimeoutException, Exception{
        DatagramSocket ds = null;
        try {
            
            System.out.println("server "+host+" ======================");
            
            NTPPacket request = new NTPPacket();
            request.LI = LeapIndicator.CLOCK_UNSYNCHRONIZED;
            request.mode = Mode.CLIENT;
            request.Stratum = 16;
            request.ReferenceIdentifier = new byte[4];
            request.OriginateTimestamp = (System.currentTimeMillis() + GAP)*1.0/1000;
            
            ds = new DatagramSocket(11451);
            ds.setSoTimeout(2000);
            send(ds, request, "ntp.jst.mfeed.ad.jp");
            NTPPacket response = receive(ds);
            
            double t4 = (System.currentTimeMillis() + GAP)*1.0/1000;
            double t1 = request.OriginateTimestamp;
            double t2 = response.ReceiveTimestamp;
            double t3 = response.TransmitTimestamp;
            
            System.out.println("result: "+host);
            System.out.println("response: " + response);
            System.out.println("offset: " + (((t2 - t1) + (t3 - t4)) / 2));
            System.out.println("delay: " + ((t4 - t1) - (t3 - t2)));
            
            ds.close();
        } catch (SocketTimeoutException e) {
            System.out.println("server: "+host+" timed out");
        } finally {
            if(ds != null)
                ds.close();
        }
    }

    private static void send(DatagramSocket socket, NTPPacket packet, String addr) throws IllegalArgumentException, ConversionException, IOException {
        byte[] bytes = TestUtils.serializeAndGetBytes(packet);
        DatagramPacket out = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(addr), 123);
        socket.send(out);
    }
    
    private static NTPPacket receive(DatagramSocket socket) throws IllegalArgumentException, ConversionException, IOException {
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
