package examples.websocket;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import io.github.zhtmf.ConversionException;

public class Server {
    private int port;
    private ServerSocket socket;
    private Thread acceptThread;
    private ConcurrentHashMap<Client, Thread> clientThreads = 
            new ConcurrentHashMap<Server.Client, Thread>();
    private List<WSFrameCallback> callbacks = new CopyOnWriteArrayList<>();
    public Server(int port) {
        this.port = port;
    }
    public Server start() throws IOException {
        final ServerSocket ss = new ServerSocket(port);
        this.socket = ss;
        Thread acceptThread = new Thread() {
            public void run() {
                try {
                    for(;;) {
                        Socket socket = ss.accept();
                        Client client = new Client(socket);
                        WorkerThread worker = new WorkerThread(client);
                        worker.start();
                        clientThreads.put(client, worker);
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    try {
                        ss.close();
                    } catch (IOException e1) {
                    }
                    for(Thread thread:clientThreads.values()) {
                        thread.interrupt();
                    }
                    socket = null;
                    return;
                }
            };
        };
        this.acceptThread = acceptThread;
        acceptThread.start();
        System.out.println("=== server started on port"+port+" ====");
        return this;
    }
    public Server registerCallback(WSFrameCallback callback) {
        this.callbacks.add(callback);
        return this;
    }
    public void shutdown() {
        acceptThread.interrupt();
    }
    public boolean closed() {
        return socket == null;
    }
    
    private static class Client{
        private Socket clientSocket;
        private String remoteAddress;
        public Client(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.remoteAddress = clientSocket.getRemoteSocketAddress().toString();
        }
        public Socket getClientSocket() {
            return clientSocket;
        }
        public String getAddress() {
            return remoteAddress;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((remoteAddress == null) ? 0 : remoteAddress.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Client other = (Client) obj;
            if (remoteAddress == null) {
                if (other.remoteAddress != null)
                    return false;
            } else if (!remoteAddress.equals(other.remoteAddress))
                return false;
            return true;
        }
    }
    
    public class WorkerThread extends Thread{
        private Client client;
        private InputStream in;
        private OutputStream os;
        public WorkerThread(Client client) throws IOException {
            this.client = client;
            this.in = client.getClientSocket().getInputStream();
            this.os = client.getClientSocket().getOutputStream();
        }
        @Override
        public void run() {
            System.out.println("===== reading handshake ====");
            Map<String,String> headers = null;
            try {
                headers = receiveHandshake(in);
            } catch (IOException e) {
                System.out.println("bad handshake:"+e.getMessage());
                System.out.println("===== bad handshake, exiting ====");
                exit();
                return;
            }
            System.out.println("headers received:"+headers);
            String key = headers.get("sec-websocket-key");
            /*
             * concatenate this with the  Globally Unique Identifier (GUID, [RFC4122])
             * "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
             */
            key += "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            /*
             * A SHA-1 hash (160 bits) [FIPS.180-3], base64-encoded (see Section 4 of
             * [RFC4648]), of this concatenation is then returned in the server's handshake.
             */
            byte[] sha1Result = DigestUtils.sha1(key.getBytes());
            String ret = Base64.encodeBase64String(sha1Result);
            StringBuilder response = new StringBuilder();
            //response
            response.append("HTTP/1.1 101 Switching Protocols\r\n");
            response.append("Upgrade: websocket\r\n");
            response.append("Connection: Upgrade\r\n");
            response.append("Sec-WebSocket-Accept: "+ret+"\r\n");
            response.append("\r\n");
            try {
                os.write(response.toString().getBytes());
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("===== bad handshake, exiting ====");
                exit();
                return;
            }
            
            System.out.println("===== handshake successful ====");
            
            // ============== exchanging packets ==============================
            
            ByteArrayOutputStream fragmentedData = new ByteArrayOutputStream();
            boolean continuation = false;
            OpCodes fragmentedFrameType = null;
            for(;;) {
                WSFrame frame = new WSFrame();
                try {
                    frame.deserialize(in);
                    switch (frame.getOpcode()) {
                    case CLOSE:
                        System.out.println("===== closed by client ==== "+frame);
                        CLOSE_FRAME.serialize(os);
                        exit();
                        for(WSFrameCallback callback:callbacks) {
                            callback.onClose(this.client.getAddress(), frame.getStatusCode(), new String(frame.getUnmaskedApplicationData()));
                        }
                        return;
                    case PING:
                        System.out.println("received ping:"+frame);
                        PONG_FRAME.serialize(os);
                        for(WSFrameCallback callback:callbacks) {
                            callback.onPing(this.client.getAddress());
                        }
                        break;
                    case PONG:
                        System.out.println("received pong:"+frame);
                        break;
                    case CONTINUATION:
                        if(!continuation) {
                            System.out.println("===== unexpected continuation frame, closing... ====");
                            CLOSE_FRAME.serialize(os);
                            exit();
                            return;
                        }
                        try {
                            fragmentedData.write(frame.getUnmaskedApplicationData());
                        } catch (IOException e1) {
                        }
                        // final frame in a sequence
                        if(frame.isFin()) {
                            continuation = false;
                            if(fragmentedFrameType == OpCodes.BINARY) {
                                for(WSFrameCallback callback:callbacks) {
                                    WSFrame callbackResponse = callback.onBinary(this.client.getAddress(),fragmentedData.toByteArray());
                                    if(callbackResponse!=null)
                                        callbackResponse.serialize(os);
                                }
                            }else {
                                for(WSFrameCallback callback:callbacks) {
                                    WSFrame callbackResponse = callback.onText(this.client.getAddress(),new String(fragmentedData.toByteArray()));
                                    if(callbackResponse!=null)
                                        callbackResponse.serialize(os);
                                }
                            }
                        }
                        break;
                    case TEXT:
                    case BINARY:
                        /*
                         * A fragmented message consists of a single frame with the FIN bit
                           clear and an opcode other than 0, followed by zero or more frames
                           with the FIN bit clear and the opcode set to 0, and terminated by
                           a single frame with the FIN bit set and an opcode of 0
                           
                           Control frames (see Section 5.5) MAY be injected in the middle of
                           a fragmented message.
                         */
                        if(frame.isFin()) {
                            if(frame.getOpcode() == OpCodes.TEXT) {
                                PING_FRAME.serialize(os);
                                for(WSFrameCallback callback:callbacks) {
                                    WSFrame callbackResponse = callback.onText(this.client.getAddress(), new String(frame.getUnmaskedApplicationData()));
                                    if(callbackResponse!=null) {
                                        //For testing purposes
                                        List<WSFrame> fragmentedFrames = callbackResponse.fragmentize(3);
                                        for(WSFrame fragment:fragmentedFrames) {
                                            fragment.serialize(os);
                                        }
                                    }
                                }
                            }else {
                                PING_FRAME.serialize(os);
                                for(WSFrameCallback callback:callbacks) {
                                    WSFrame callbackResponse = callback.onBinary(this.client.getAddress(),frame.getUnmaskedApplicationData());
                                    if(callbackResponse!=null) callbackResponse.serialize(os);
                                }
                            }
                        }else {
                            fragmentedData.reset();
                            try {
                                fragmentedData.write(frame.getUnmaskedApplicationData());
                            } catch (IOException e) {
                                throw new Error(e);
                            }
                            fragmentedFrameType = frame.getOpcode();
                            continuation = true;
                        }
                        break;
                    }
                } catch (ConversionException e) {
                    e.printStackTrace();
                    exit();
                    return;
                }
            }
        }
        @Override
        public String toString() {
            return "Worker["+client.getAddress()+"]";
        }
        private void exit() {
            try {
                client.getClientSocket().close();
                Server.this.clientThreads.remove(client);
            } catch (IOException e) {
            }
        }
    }
    
    private static Map<String,String> receiveHandshake(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
        String line = null;
        String key = null;
        Map<String,String> headers = new HashMap<>();
        for(;;) {
            line = reader.readLine();
            if(line.length()==0) {
                break;
            }
            if(line.startsWith("GET ")) {
                System.out.println("Request URI:"+line);
                continue;
            }
            //normal headers
            if(line.indexOf(':')<=0) {
                throw new IOException("illegal header line:"+line);
            }
            String[] components = line.split(":");
            String name = components[0].trim().toLowerCase();
            String value = components[1].trim();
            if(name.equals("host")) {
                System.out.println("Host:" + value);
                continue;
            }
            if(name.equals("upgrade")) {
                if(!value.equals("websocket")) {
                    throw new IOException("not an websocket handshake:"+line);
                }
            }
            if(name.equals("origin")) {
                System.out.println("Origin:"+value);
                continue;
            }
            if(name.equals("sec-websocket-key")) {
                key = value;
            }
            headers.put(name, value);
        }
        if(key==null) {
            throw new IOException("client does not specify a websocket key:"+headers);
        }
        return headers;
    }
    
    private static final WSFrame CLOSE_FRAME = new WSFrame();
    private static final WSFrame PONG_FRAME = new WSFrame();
    private static final WSFrame PING_FRAME = new WSFrame();
    static {
        CLOSE_FRAME.setFin(true);
        CLOSE_FRAME.setOpcode(OpCodes.CLOSE);
        CLOSE_FRAME.setMasked(false);
        CLOSE_FRAME.setStatusCode(1001);
        CLOSE_FRAME.setApplicationData("GOING AWAY...".getBytes());
        CLOSE_FRAME.setCalculatedPayLoadLength((byte)
                (CLOSE_FRAME.getApplicationData().length));
        PONG_FRAME.setFin(true);
        PONG_FRAME.setOpcode(OpCodes.PONG);
        PONG_FRAME.setMasked(false);
        PONG_FRAME.setApplicationData("PONG!".getBytes());
        PONG_FRAME.setCalculatedPayLoadLength((byte)
                (PONG_FRAME.getApplicationData().length));
        PING_FRAME.setFin(true);
        PING_FRAME.setOpcode(OpCodes.PING);
        PING_FRAME.setMasked(false);
        PING_FRAME.setApplicationData("PING!".getBytes());
        PING_FRAME.setCalculatedPayLoadLength(
                (byte) PING_FRAME.getApplicationData().length);
    }
}
