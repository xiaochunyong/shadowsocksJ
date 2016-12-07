package me.ely.shadowsocks.crypt;

import java.io.ByteArrayOutputStream;

/**
 * Created by Ely on 29/11/2016.
 */
public interface ICrypt {

    void encrypt(byte[] data, int length, ByteArrayOutputStream stream);

    void encrypt(byte[] data, ByteArrayOutputStream stream);

    void decrypt(byte[] data, int length, ByteArrayOutputStream stream);

    void decrypt(byte[] data, ByteArrayOutputStream stream);

    int getIVLength();

    int getKeyLength();

}
