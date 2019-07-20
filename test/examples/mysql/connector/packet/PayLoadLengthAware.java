package examples.mysql.connector.packet;

public interface PayLoadLengthAware {
    void setPayLoadLength(int payLoadLength);
    int getPayLoadLength();
}
