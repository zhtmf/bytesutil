package examples.ntp;

/**
 * NTP packet mode (3 bits)
 * 0: Reserved
 * 1: Symmetric active
 * 2: Symmetric passive
 * 3: Client
 * 4: Server
 * 5: Broadcast
 * 6: NTP control message
 * 7: Reserved for private use
 * @author dzh
 */
public enum Mode {
    RESERVED,
    SYMMETRIC_ACTIVE,
    SYMMETRIC_PASSIVE,
    CLIENT,
    SERVER,
    BROADCAST,
    CONTROL,
    PRIVATE
}
