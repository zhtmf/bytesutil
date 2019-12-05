package examples.websocket;

import java.io.IOException;
import java.util.Arrays;

/**
 * An example demonstrating features of this library (mainly the Bit type) by
 * implementing a simple WebSocket server.
 * <p>
 * Aside from boilerplate codes, users should focus on codes in this class, the
 * later half of {@link Server.WorkerThread#run()} and {@link WSFrame} for a
 * reference about how this library is used to implement the protocol.
 * <p>
 * The easiest way to test this server is using javascript in the browser
 * developer tool. The script in this folder should suffice for this purpose.
 * 
 * @author dzh
 */
public class WebSocketTest {
    public static void main(String[] args) throws IOException {
        new Server(30024).start().registerCallback(new WSFrameCallback() {

            @Override
            public WSFrame onText(String clientAddress, String text) {
                System.out.println("received text... " + text);
                WSFrame response = new WSFrame();
                response.setFin(true);
                response.setOpcode(OpCodes.TEXT);
                response.setApplicationData((text + text + text).getBytes());
                response.setCalculatedPayLoadLength(response.getApplicationData().length);
                return response;
            }

            @Override
            public void onPing(String clientAddress) {
            }

            @Override
            public void onClose(String clientAddress, int statusCode, String reason) {
            }

            @Override
            public WSFrame onBinary(String clientAddress, byte[] bytes) {
                System.out.println("received binary... " + Arrays.toString(bytes));
                return null;
            }
        });
        System.in.read();
    }
}
