package me.ely.shadowsocks.crypt;

import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Ely on 30/11/2016.
 */
public class ByteArrayCloneTest {

    private static final Logger logger = LoggerFactory.getLogger(ByteArrayCloneTest.class);

    public static void main(String[] args) {
        byte[] rawBytes = "helloworld".getBytes();
        byte[] newBytes1 = new byte[rawBytes.length];
        System.arraycopy(rawBytes, 0, newBytes1, 0, rawBytes.length);


        byte[] newBytes2 = rawBytes.clone();

        rawBytes[0] = 12;
        rawBytes[1] = 15;

        System.out.println(Hex.toHexString(newBytes1).equals(Hex.toHexString(newBytes2)));
    }

}
