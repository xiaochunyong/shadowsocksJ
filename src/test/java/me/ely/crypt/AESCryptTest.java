package me.ely.crypt;

import me.ely.shadowsocks.crypt.AESCrypt;
import me.ely.shadowsocks.utils.Config;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by Ely on 30/11/2016.
 */
public class AESCryptTest {

    private static final Logger logger = LoggerFactory.getLogger(AESCryptTest.class);

    public static void main(String[] args) throws UnsupportedEncodingException {
        Config config = Config.getConfig();
        AESCrypt aesCrypt = new AESCrypt(config.getMethod(), config.getPassword());
        String content = "hello,everybody";
        System.out.println(content);
        byte[] raw = content.getBytes("UTF-8");
        System.out.println(Hex.toHexString(raw));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        aesCrypt.encrypt(raw, raw.length, stream);


        byte[] encryptData = stream.toByteArray();
        System.out.println(Hex.toHexString(encryptData));


        aesCrypt.decrypt(encryptData, encryptData.length, stream);
        byte[] decryptData = stream.toByteArray();
        System.out.println(Hex.toHexString(decryptData));
        System.out.println(new String(decryptData));

    }

}
