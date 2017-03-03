package me.ely.shadowsocks.crypt;

import me.ely.shadowsocks.model.Config;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Ely on 30/11/2016.
 */
public class ShadowsocksKeyTest {

    private static final Logger logger = LoggerFactory.getLogger(ShadowsocksKeyTest.class);

    public static void main(String[] args) {
        String password = Config.getConfig().getPassword();
        logger.info(password);
        ShadowsocksKey key = new ShadowsocksKey(password, 32);
        System.out.println(Hex.toHexString(key.getEncoded()).equals("b2e054de48f66bd2874ead5216085368cc02349560ab6e188d05828d726910ac"));
    }

}
