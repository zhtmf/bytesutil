package examples.websocket;

public interface WSFrameCallback {
    WSFrame onText(String clientAddress, String text);
    WSFrame onBinary(String clientAddress, byte[] bytes);
    void onClose(String clientAddress, int statusCode, String reason);
    void onPing(String clientAddress);
}
