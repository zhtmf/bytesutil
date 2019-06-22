package examples.mysql.connector.auxiliary;

public class CapabilityFlags {
    public static final int CLIENT_PLUGIN_AUTH = 1<<19;
    public static final int CLIENT_PROTOCOL_41 = 512;
    public static final int CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA = 1<<21;
    public static final int CLIENT_CONNECT_WITH_DB = 8;
    public static final int CLIENT_CONNECT_ATTRS = 1<<20;
    public static final int CLIENT_TRANSACTIONS = 8192;
    public static final int CLIENT_SESSION_TRACK = 1<<23;
    public static final int SERVER_SESSION_STATE_CHANGED = 1<<14;
}
