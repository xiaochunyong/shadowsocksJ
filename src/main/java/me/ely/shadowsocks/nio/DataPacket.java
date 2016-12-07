package me.ely.shadowsocks.nio;

/**
 * Created by Ely on 02/12/2016.
 */
public class DataPacket {

    public byte[] data;
    public boolean isEncrypt;

    public DataPacket() {
    }

    public DataPacket(byte[] data, boolean isEncrypt) {
        this.data = data;
        this.isEncrypt = isEncrypt;
    }
}
