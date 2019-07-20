package examples.mysql.connector.packet.common;

public interface PayLoadLengthAware {
    void setPayLoadLength(int payLoadLength);
    int getPayLoadLength();
}
