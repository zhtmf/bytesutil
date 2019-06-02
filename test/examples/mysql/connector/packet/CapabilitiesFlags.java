package examples.mysql.connector.packet;

public class CapabilitiesFlags {
    public static final long CLIENT_PROTOCOL_41 = 512;
    public static final long CLIENT_TRANSACTIONS = 8192;   
    public static final long CLIENT_SESSION_TRACK = ((long)1)<<23;
}
