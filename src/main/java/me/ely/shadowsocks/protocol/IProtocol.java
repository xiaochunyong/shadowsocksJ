package me.ely.shadowsocks.protocol;

import java.util.List;

/**
 * Created by Ely on 29/11/2016.
 */
public interface IProtocol {

    enum TYPE {SOCKS5, HTTP, AUTO}

    boolean isReady();

    TYPE getType();

    byte[] getResponse(byte[] data);

    List<byte[]> getRemoteResponse(byte[] data);

    boolean isMine(byte[] data);

}
