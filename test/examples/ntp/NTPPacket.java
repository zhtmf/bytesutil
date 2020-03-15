package examples.ntp;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Signed;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.BYTE;
import io.github.zhtmf.annotations.types.Bit;
import io.github.zhtmf.annotations.types.RAW;

public class NTPPacket extends DataPacket{

    /**
     * Leap Indicator (2 bits)
     */
    @Bit(2)
    @Order(0)
    public LeapIndicator LI;
    
    /**
     * NTP Version Number (3 bits) (current version is 4).
     */
    @Bit(3)
    @Order(1)
    public byte VN = 4;
    
    @Bit(3)
    @Order(2)
    public Mode mode;
    
    /**
     * Stratum level of the time source (8 bits) The values of the Stratum field
     * follow: 
     * 0: Unspecified or invalid 
     * 1: Primary server 
     * 2â€“15: Secondary server
     * 16: Unsynchronized 
     * 17â€“255: Reserved
     */
    @Unsigned
    @BYTE
    @Order(3)
    public int Stratum;
    
    /**
     * Poll interval (8-bit signed integer)2 
     * value of the maximum interval between successive NTP messages, in seconds.
     */
    @Signed
    @BYTE
    @Order(4)
    public int Poll;
    
    /**
     *  Clock precision (8-bit signed integer)
     *  The precision of the system clock, in log2 seconds.
     */
    @Signed
    @BYTE
    @Order(5)
    public int Precision;
    
    /**
     * The total round-trip delay from the server to the primary reference sourced
     */
    @Order(6)
    public NTPShort RootDelay = new NTPShort();
    
    /**
     * The maximum error due to clock frequency tolerance.
     */
    @Order(7)
    public NTPShort RootDispersion = new NTPShort();
    
    /**
     * For stratum 1 servers this value is a four-character ASCII code that
     * describes the external reference source (refer to Figure 2). For secondary
     * servers this value is the 32-bit IPv4 address of the synchronization source,
     * or the first 32 bits of the Message Digest Algorithm 5 (MD5) hash of the IPv6
     * address of the synchronization source.
     */
    @RAW(4)
    @Order(8)
    public byte[] ReferenceIdentifier = new byte[4];
    
    /**
     * his field is the time the system clock was last set or corrected, in 64-bit time-stamp format.
     */
    @Order(9)
    public NTPTimestamp ReferenceTimestamp;
    /**
     * This value is the time at which the request departed the client for the server, in 64-bit time-stamp format.
     */
    @Order(10)
    public NTPTimestamp OriginateTimestamp;
    /**
     * This value is the time at which the client request arrived at the server in 64-bit time-stamp format.
     */
    @Order(11)
    public NTPTimestamp ReceiveTimestamp = new NTPTimestamp();
    /**
     * This value is the time at which the server reply departed the server, in 64-bit time-stamp format.
     */
    @Order(12)
    public NTPTimestamp TransmitTimestamp = new NTPTimestamp();
    
    public String getReferenceIdentifier() {
        if(this.Stratum == 1) {
            return new String(ReferenceIdentifier);
        }else if(this.Stratum >=2 && this.Stratum <=15) {
            StringBuilder ret = new StringBuilder();
            for(byte b:this.ReferenceIdentifier) {
                ret.append(b>=0 ? b : -b*2-1);
            }
            return ret.toString();
        }else if(Stratum == 0){
            /*
             * If the Stratum field is 0, which implies unspecified or invalid, the
             * Reference Identifier field can be used to convey messages useful for status
             * reporting and access control.
             */
            return new String(ReferenceIdentifier);
        }else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "NTPPacket [LI=" + LI + ", VN=" + VN + ", mode=" + mode + ", Stratum=" + Stratum + ", Poll=" + Poll
                + ", Precision=" + Precision + ", RootDelay=" + RootDelay + ", RootDispersion=" + RootDispersion
                + ", ReferenceIdentifier=" + getReferenceIdentifier() + ", ReferenceTimestamp="
                + ReferenceTimestamp + ", OriginateTimestamp=" + OriginateTimestamp + ", ReceiveTimestamp="
                + ReceiveTimestamp + ", TransmitTimestamp=" + TransmitTimestamp + "]";
    }
}
