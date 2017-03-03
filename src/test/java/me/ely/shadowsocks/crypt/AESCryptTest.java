package me.ely.shadowsocks.crypt;

import me.ely.shadowsocks.model.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by Ely on 30/11/2016.
 */
public class AESCryptTest {

    private static final Logger logger = LoggerFactory.getLogger(AESCryptTest.class);

    public static void main(String[] args) throws UnsupportedEncodingException {
        Config config = Config.getConfig();
        AESCrypt aesCrypt = new AESCrypt(config.getMethod(), config.getPassword());
//        AESCrypt aesDecript = new AESCrypt(config.getMethod(), config.getPassword());
//        String content = "hello你们好,everybody";
//        System.out.println("原始内容: " + content);
//        byte[] raw = content.getBytes("UTF-8");
//        System.out.println("原始内容16进制: " + Hex.toHexString(raw));
//
//        for (int i = 0; i < 10; i++) {
//            ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
//            aesCrypt.encrypt(raw, raw.length, stream1);
//
//            byte[] encryptData = stream1.toByteArray();
//            System.out.println("加密后16进制:" + Hex.toHexString(encryptData));
//            encryptData = Hex.decode("d648d83b4a2da13c46241b68f0caf180f669a355b64056c4a251666eaec08ea15deffca7ffb047be");
//
//            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
//            aesDecript.decrypt(encryptData, encryptData.length, stream2);
//            byte[] decryptData = stream2.toByteArray();
//            System.out.println("解密后16进制:" + Hex.toHexString(decryptData));
//            System.out.println("解密后原文:" + new String(decryptData));
//        }


        byte[] data1 = new byte[]{-12, -35, -47, -89, -85, -94, -39, -123, 16, -83, 11, -26, -96, -55, 114, -72, -53, 114, 45, 30, 2, -119, -84};
//        byte[] data2 = new byte[]{71, 69, 84, 32, 47, 32, 72, 84, 84, 80, 47, 49, 46, 49, 13, 10, 72, 111, 115, 116, 58, 32, 119, 119, 119, 46, 105, 116, 104, 111, 109, 101, 46, 99, 111, 109, 13, 10, 85, 115, 101, 114, 45, 65, 103, 101, 110, 116, 58, 32, 99, 117, 114, 108, 47, 55, 46, 53, 49, 46, 48, 13, 10, 65, 99, 99, 101, 112, 116, 58, 32, 42, 47, 42, 13, 10, 13, 10};
        byte[] data2 = new byte[]{-10, -1, -103, -47, 90, 53, -75, 88, 8, -127, -78, 124, -2, -57, -90, -84, -127, 59, -26, -85, 10, 113, -54, -39, -88, 58, 40, -58, 14, 62, 114, 16, -69, -126, -77, -75, -23, 82, 4, -31, 126, -83, 125, -89, 95, -13, 10, 121, 74, 15, 106, -72, 93, 90, 28, 62, 35, -3, 102, -37, -25, 1, 72, -77, -44, -37, 84, -15, -101, 100, -23, -1, 47, -71, -2, 52, 22, 110};

        System.out.println(Arrays.toString(aesCrypt.decrypt(data1)));
        System.out.println(Arrays.toString(aesCrypt.decrypt(data2)));

    }

}
