package me.ely.shadowsocks.crypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Ely on 30/11/2016.
 */
public class ShadowsocksKey implements SecretKey {

    private static final Logger logger = LoggerFactory.getLogger(ShadowsocksKey.class);

    private static final int KEY_LENGTH = 32;

    private byte[] key;
    private int length;

    public ShadowsocksKey(String password, int length) {
        this.length = length;
        this.key = generateKey(password);
    }

    public byte[] generateKey(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            byte[] rawBytes = password.getBytes("UTF-8");
            byte[] keys = new byte[KEY_LENGTH];
            byte[] hash = null;
            byte[] temp = null;
            int i = 0;

            while (i < keys.length) {
                if (i == 0) {
                    hash = messageDigest.digest(rawBytes);
                    temp = new byte[rawBytes.length + hash.length];
                } else {
                    System.arraycopy(hash, 0, temp, 0, hash.length);
                    System.arraycopy(rawBytes, 0, temp, hash.length, rawBytes.length);
                    hash = messageDigest.digest(temp);
                }
                System.arraycopy(hash, 0, keys, i, hash.length);
                i += hash.length;
            }

            if (length != KEY_LENGTH) {
                byte[] key1 = new byte[length];
                System.arraycopy(keys, 0, key1, 0, length);
                return key1;
            }
            return keys;
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String getAlgorithm() {
        return "shadowsocks";
    }

    @Override
    public String getFormat() {
        return "raw";
    }

    @Override
    public byte[] getEncoded() {
        return key;
    }
}
