package me.ely.shadowsocks.crypt;

import me.ely.shadowsocks.utils.Util;
import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.modes.OFBBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Ely on 29/11/2016.
 */
public class AESCrypt {

    private static final Logger logger = LoggerFactory.getLogger(AESCrypt.class);

    public final static String CIPHER_AES_128_CFB = "aes-128-cfb";

    public final static String CIPHER_AES_192_CFB = "aes-192-cfb";

    public final static String CIPHER_AES_256_CFB = "aes-256-cfb";

    public final static String CIPHER_AES_128_OFB = "aes-128-ofb";

    public final static String CIPHER_AES_192_OFB = "aes-192-ofb";

    public final static String CIPHER_AES_256_OFB = "aes-256-ofb";

    private String name;
    private SecretKey key;
    private ShadowsocksKey ssKey;
    private byte[] encryptIV;
    private byte[] decryptIV;
    private boolean encryptIVHasSet;
    private boolean decryptIVHasSet;
    private final Lock encLock = new ReentrantLock();
    private final Lock decLock = new ReentrantLock();
    private StreamBlockCipher encCipher;
    private StreamBlockCipher decCipher;

    public AESCrypt(String name, String password) {
        this.name = name;
        this.ssKey = new ShadowsocksKey(password, getKeyLength());
        this.key = getKey();
    }

    public StreamBlockCipher getCipher(boolean isEncrypt) {
        AESFastEngine engine = new AESFastEngine();
        StreamBlockCipher cipher = null;
        if (CIPHER_AES_128_CFB.equals(this.name) || CIPHER_AES_192_CFB.equals(this.name) || CIPHER_AES_256_CFB.equals(this.name)) {
            cipher = new CFBBlockCipher(engine, getIVLength() * 8);
        } else if (CIPHER_AES_128_OFB.equals(this.name) || CIPHER_AES_192_OFB.equals(this.name) || CIPHER_AES_256_OFB.equals(this.name)) {
            cipher = new OFBBlockCipher(engine, getIVLength() * 8);
        } else {
            throw new RuntimeException("invalid crypt name " + this.name);
        }
        return cipher;
    }

    public void setIV(byte[] iv, boolean isEncrypt) {
        if (isEncrypt) {
            encryptIV = new byte[getIVLength()];
            System.arraycopy(iv, 0, encryptIV, 0, getIVLength());
            encCipher = getCipher(isEncrypt);
            ParametersWithIV parametersWithIV = new ParametersWithIV(new KeyParameter(key.getEncoded()), encryptIV);
            encCipher.init(isEncrypt, parametersWithIV);
        } else {
            decryptIV = new byte[getIVLength()];
            System.arraycopy(iv, 0, decryptIV, 0, getIVLength());
            decCipher = getCipher(isEncrypt);
            ParametersWithIV parametersWithIV = new ParametersWithIV(new KeyParameter(key.getEncoded()), decryptIV);
            decCipher.init(isEncrypt, parametersWithIV);
        }
    }

    
    public void encrypt(byte[] data, int length, ByteArrayOutputStream stream) {
//        byte[] d = new byte[length];
//        System.arraycopy(data, 0, d, 0, length);
//        encrypt(d, stream);
        encrypt(data.clone(), stream);
    }

    
    public void encrypt(byte[] data, ByteArrayOutputStream stream) {
        synchronized (encLock) {
            stream.reset();
            if (!encryptIVHasSet) {
                encryptIVHasSet = true;
                byte[] iv = Util.randomBytes(getIVLength());
                setIV(iv, true);
                try {
                    stream.write(iv);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }

            _encrypt(data, stream);
        }
    }

    private void _encrypt(byte[] data, ByteArrayOutputStream stream) {
        byte[] buffer = new byte[data.length];

        int processedBytes = encCipher.processBytes(data, 0, data.length, buffer, 0);
        stream.write(buffer, 0, processedBytes);
    }

    
    public void decrypt(byte[] data, int length, ByteArrayOutputStream stream) {
//        byte[] d = new byte[length];
//        System.arraycopy(data, 0, d, 0, length);
//        decrypt(d, stream);
        decrypt(data.clone(), stream);
    }


    public void decrypt(byte[] data, ByteArrayOutputStream stream) {
        byte[] newData = null;
        synchronized (decLock) {
            stream.reset();
            if (!decryptIVHasSet) {
                decryptIVHasSet = true;
                byte[] iv = new byte[getIVLength()];
                System.arraycopy(data, 0, iv, 0, getIVLength());
                setIV(iv, false);

                newData = new byte[data.length - getIVLength()];
                System.arraycopy(data, getIVLength(), newData, 0, newData.length);
            } else {
                newData = data;
            }

            _decrypt(newData, stream);
        }
    }

    private void _decrypt(byte[] data, ByteArrayOutputStream stream) {
        byte[] buffer = new byte[data.length];
        int processedBytes = decCipher.processBytes(data, 0, data.length, buffer, 0);
        stream.write(buffer, 0, processedBytes);
    }

    
    public int getIVLength() {
        return 16;
    }

    
    public int getKeyLength() {
        if (CIPHER_AES_128_CFB.equals(name) || CIPHER_AES_128_OFB.equals(name)) {
            return 16;
        }
        if (CIPHER_AES_192_CFB.equals(name) || CIPHER_AES_192_OFB.equals(name)) {
            return 24;
        }
        if (CIPHER_AES_256_CFB.equals(name) || CIPHER_AES_256_OFB.equals(name)) {
            return 32;
        }

        return 0;
    }

    public SecretKey getKey() {
        return new SecretKeySpec(ssKey.getEncoded(), "AES");
    }

    public byte[] encrypt(byte[] data) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        encrypt(data, stream);
        return stream.toByteArray();
    }

    public byte[] decrypt(byte[] data) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        decrypt(data, stream);
        return stream.toByteArray();
    }
}
