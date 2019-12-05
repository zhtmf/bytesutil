package examples.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.zhtmf.DataPacket;
import io.github.zhtmf.annotations.modifiers.BigEndian;
import io.github.zhtmf.annotations.modifiers.Conditional;
import io.github.zhtmf.annotations.modifiers.Length;
import io.github.zhtmf.annotations.modifiers.Order;
import io.github.zhtmf.annotations.modifiers.Unsigned;
import io.github.zhtmf.annotations.types.Bit;
import io.github.zhtmf.annotations.types.LONG;
import io.github.zhtmf.annotations.types.RAW;
import io.github.zhtmf.annotations.types.SHORT;
import io.github.zhtmf.converters.auxiliary.ModifierHandler;

import static examples.websocket.OpCodes.*;

@BigEndian
@Unsigned
public class WSFrame extends DataPacket{
    /**
     * Length of extension data determined in handshake phase.
     * It should be known both to server and client prior to 
     * start of packet exchanging.
     */
    public int extensionLength;

    /**
     * FIN:  1 bit
       Indicates that this is the final fragment in a message.  The first
       fragment MAY also be the final fragment.
     */
    @Order(1)
    @Bit
    private boolean fin;
    
    /**
     * RSV1, RSV2, RSV3:  1 bit each
       MUST be 0 unless an extension is negotiated that defines meanings
       for non-zero values.  If a nonzero value is received and none of
       the negotiated extensions defines the meaning of such a nonzero
       value, the receiving endpoint MUST _Fail the WebSocket
       Connection_.
     */
    @Order(2)
    @Bit
    @Length(3)
    private List<Boolean> rsvs = new ArrayList<Boolean>(Arrays.asList(false,false,false)); 
    
    /**
     * Opcode: 4 bits
     * 
     * Defines the interpretation of the "Payload data". If an unknown opcode is
     * received, the receiving endpoint MUST _Fail the WebSocket Connection_. The
     * following values are defined.
     * 
     * %x0 denotes a continuation frame
     * 
     * %x1 denotes a text frame
     * 
     * %x2 denotes a binary frame
     * 
     * %x3-7 are reserved for further non-control frames
     * 
     * %x8 denotes a connection close
     * 
     * %x9 denotes a ping
     * 
     * %xA denotes a pong
     * 
     * %xB-F are reserved for further control frames
     */
    @Order(3)
    @Bit(4)
    private OpCodes opcode;
    
    /**
     * Mask: 1 bit
     * 
     * Defines whether the "Payload data" is masked. If set to 1, a masking key is
     * present in masking-key, and this is used to unmask the "Payload data" as per
     * Section 5.3. All frames sent from client to server have this bit set to 1.
     * 
     */
    @Order(4)
    @Bit
    private boolean masked;
    
    /**
     * Payload length: 7 bits, 7+16 bits, or 7+64 bits
     * 
     * The length of the "Payload data", in bytes: if 0-125, that is the payload
     * length. If 126, the following 2 bytes interpreted as a 16-bit unsigned
     * integer are the payload length. If 127, the following 8 bytes interpreted as
     * a 64-bit unsigned integer (the most significant bit MUST be 0) are the
     * payload length. Multibyte length quantities are expressed in network byte
     * order. Note that in all cases, the minimal number of bytes MUST be used to
     * encode the length, for example, the length of a 124-byte-long string can't be
     * encoded as the sequence 126, 0, 124. The payload length is the length of the
     * "Extension data" + the length of the "Application data". The length of the
     * "Extension data" may be zero, in which case the payload length is the length
     * of the "Application data".
     */
    @Order(5)
    @Bit(7)
    private byte payloadLength;
    @Order(6)
    @SHORT
    @Conditional(Conditional1.class)
    private int extendedPayloadLength1;
    @Order(7)
    @LONG
    @Conditional(Conditional1.class)
    private BigInteger extendedPayloadLength2;
    
    /**
     * Masking-key: 0 or 4 bytes
     * 
     * All frames sent from the client to the server are masked by a 32-bit value
     * that is contained within the frame. This field is present if the mask bit is
     * set to 1 and is absent if the mask bit is set to 0. See Section 5.3 for
     * further information on client- to-server masking.
     */
    @Order(8)
    @RAW
    @Length(handler = MaskingKeyHandler.class)
    private byte[] maskingKey = new byte[0];
    
    @Order(9)
    @RAW
    @Length(handler = ExtensionDataHandler.class)
    private byte[] extensionData = new byte[0];
    
    /**
     * The Close frame MAY contain a body (the "Application data" portion of the
     * frame) that indicates a reason for closing, such as an endpoint shutting
     * down, an endpoint having received a frame too large, or an endpoint having
     * received a frame that does not conform to the format expected by the
     * endpoint. If there is a body, the first two bytes of the body MUST be a
     * 2-byte unsigned integer (in network byte order) representing a status code
     * with value /code/ defined in Section 7.4. Following the 2-byte integer, the
     * body MAY contain UTF-8-encoded data with value /reason/,
     */
    @Order(10)
    @SHORT
    @Conditional(Conditional2.class)
    private int statusCode;
    
    @Order(11)
    @RAW
    @Length(handler = ApplicationDataHandler.class)
    private byte[] applicationData;
    
    public long calculatePayloadLength() {
        if(payloadLength<=125) {
            return payloadLength;
        }
        switch (payloadLength) {
        case 126:
            return extendedPayloadLength1;
        case 127:
            return extendedPayloadLength2.longValue();
        default:
            throw new Error();
        }
    }
    
    public void setCalculatedPayLoadLength(long length) {
        if(this.opcode == OpCodes.CLOSE) {
            length += 2;
        }
        if(length<=125) {
            payloadLength = (byte) length;
        }else if(length <= Character.MAX_VALUE) {
            payloadLength = 126;
            extendedPayloadLength1 = (int) length;
        }else {
            payloadLength = 127;
            extendedPayloadLength2 = BigInteger.valueOf(length);
        }
    }
    
    /**
     * The masking does not affect the length of the "Payload data". To convert
     * masked data into unmasked data, or vice versa, the following algorithm is
     * applied. The same algorithm applies regardless of the direction of the
     * translation, e.g., the same steps are applied to mask the data as to unmask
     * the data.
     * <p>
     * Octet i of the transformed data ("transformed-octet-i") is the XOR of octet i
     * of the original data ("original-octet-i") with octet at index i modulo 4 of
     * the masking key ("masking-key-octet-j"):
     * <p>
     * j = i MOD 4
     * <p>
     * transformed-octet-i = original-octet-i XOR masking-key-octet-j
     * 
     * @return
     */
    public byte[] getUnmaskedApplicationData() {
        byte[] applicationData = this.applicationData;
        if(applicationData.length == 0) {
            return applicationData;
        }
        if(!masked) {
            return applicationData;
        }
        byte[] ret = new byte[applicationData.length];
        byte[] maskingKey = this.maskingKey;
        for(int i=0;i<applicationData.length;++i) {
            byte mask = maskingKey[i%4];
            ret[i] = (byte) (applicationData[i] ^ mask);
        }
        return ret;
    }
    
    /**
     * Split this frame into fragmented ones.
     * <p>
     * For testing of fragmentation
     * @return
     */
    public List<WSFrame> fragmentize(int count){
        if(this.opcode != OpCodes.TEXT && this.opcode != OpCodes.BINARY)
            throw new IllegalStateException();
        byte[] applicationData = this.applicationData;
        if(applicationData.length<count)
            throw new IllegalStateException();
        int partSize = applicationData.length/count;
        int start = 0;
        List<WSFrame> ret = new ArrayList<WSFrame>();
        while(start<applicationData.length) {
            WSFrame frame = new WSFrame();
            frame.setOpcode(OpCodes.CONTINUATION);
            frame.setFin(false);
            frame.setMasked(false);
            frame.setApplicationData(Arrays.copyOfRange(
                    applicationData, start, Math.min(start+partSize, applicationData.length)));
            frame.setCalculatedPayLoadLength(frame.getApplicationData().length);
            ret.add(frame);
            start += partSize;
        }
        ret.get(0).setOpcode(this.opcode);
        ret.get(ret.size()-1).setFin(true);
        return ret;
    }
    
    public static class Conditional1 extends ModifierHandler<Boolean>{

        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream in) throws IOException {
            if("extendedPayloadLength1".equals(fieldName)) {
                return ((WSFrame)entity).payloadLength == 126;
            }else if("extendedPayloadLength2".equals(fieldName)) {
                return ((WSFrame)entity).payloadLength == 127;
            }
            return false;
        }

        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            if("extendedPayloadLength1".equals(fieldName)) {
                return ((WSFrame)entity).payloadLength == 126;
            }else if("extendedPayloadLength2".equals(fieldName)) {
                return ((WSFrame)entity).payloadLength == 127;
            }
            return false;
        }
        
    }
    
    public static class MaskingKeyHandler extends ModifierHandler<Integer>{
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream in) throws IOException {
            return ((WSFrame)entity).masked ? 4 : 0;
        }
        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            return ((WSFrame)entity).masked ? 4 : 0;
        }
    }
    
    public static class ApplicationDataHandler extends ModifierHandler<Integer>{
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream in) throws IOException {
            WSFrame frame = (WSFrame)entity;
            int len = (int) (frame.calculatePayloadLength() - (frame.extensionLength));
            if(frame.opcode == CLOSE && len>0) {
                len -= 2;
            }
            return len;
        }
        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            WSFrame frame = (WSFrame)entity;
            int len = (int) (frame.calculatePayloadLength() - (frame.extensionLength));
            if(frame.opcode == CLOSE && len>0) {
                len -= 2;
            }
            return len;
        }
    }
    
    public static class ExtensionDataHandler extends ModifierHandler<Integer>{
        @Override
        public Integer handleDeserialize0(String fieldName, Object entity, InputStream in) throws IOException {
            return ((WSFrame)entity).extensionLength;
        }
        @Override
        public Integer handleSerialize0(String fieldName, Object entity) {
            return ((WSFrame)entity).extensionLength;
        }
    }
    
    public static class Conditional2 extends ModifierHandler<Boolean>{

        @Override
        public Boolean handleDeserialize0(String fieldName, Object entity, InputStream in) throws IOException {
            return ((WSFrame)entity).opcode == CLOSE && ((WSFrame)entity).calculatePayloadLength() > 0;
        }

        @Override
        public Boolean handleSerialize0(String fieldName, Object entity) {
            return ((WSFrame)entity).opcode == CLOSE && ((WSFrame)entity).calculatePayloadLength() > 0;
        }
        
    }

    public boolean isFin() {
        return fin;
    }

    public void setFin(boolean fin) {
        this.fin = fin;
    }

    public List<Boolean> getRsvs() {
        return rsvs;
    }

    public void setRsvs(List<Boolean> rsvs) {
        this.rsvs = rsvs;
    }

    public OpCodes getOpcode() {
        return opcode;
    }

    public void setOpcode(OpCodes opcode) {
        this.opcode = opcode;
    }

    public boolean isMasked() {
        return masked;
    }

    public void setMasked(boolean masked) {
        this.masked = masked;
    }

    public int getExtendedPayloadLength1() {
        return extendedPayloadLength1;
    }

    public void setExtendedPayloadLength1(int extendedPayloadLength1) {
        this.extendedPayloadLength1 = extendedPayloadLength1;
    }

    public BigInteger getExtendedPayloadLength2() {
        return extendedPayloadLength2;
    }

    public void setExtendedPayloadLength2(BigInteger extendedPayloadLength2) {
        this.extendedPayloadLength2 = extendedPayloadLength2;
    }

    public byte[] getMaskingKey() {
        return maskingKey;
    }

    public void setMaskingKey(byte[] maskingKey) {
        this.maskingKey = maskingKey;
    }

    public byte[] getExtensionData() {
        return extensionData;
    }

    public void setExtensionData(byte[] extensionData) {
        this.extensionData = extensionData;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public byte[] getApplicationData() {
        return applicationData;
    }

    public void setApplicationData(byte[] applicationData) {
        this.applicationData = applicationData;
    }

    public void setPayloadLength(byte payloadLength) {
        this.payloadLength = payloadLength;
    }
    
    public byte getPayloadLength() {
        return payloadLength;
    }

    @Override
    public String toString() {
        return String.format("Frame[op=%s,fin=%s,masked=%s,len=%s]", opcode, fin, masked, calculatePayloadLength());
    }
}
