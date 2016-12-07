package me.ely.shadowsocks.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Ely on 29/11/2016.
 */
public class HttpProtocol implements IProtocol {

    private static final Logger logger = LoggerFactory.getLogger(HttpProtocol.class);


    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public TYPE getType() {
        return null;
    }

    @Override
    public byte[] getResponse(byte[] data) {
        return new byte[0];
    }

    @Override
    public List<byte[]> getRemoteResponse(byte[] data) {
        return null;
    }

    @Override
    public boolean isMine(byte[] data) {
        return false;
    }
}
