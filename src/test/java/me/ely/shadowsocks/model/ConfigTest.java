package me.ely.shadowsocks.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

/**
 * Created by Ely on 13/12/2016.
 */
public class ConfigTest {

    private static final Logger logger = LoggerFactory.getLogger(ConfigTest.class);

    public static void main(String[] args) {
        System.out.println(Config.getConfig().getCurrentServer().toPlainURI());
        System.out.println(Config.getConfig().getCurrentServer().toBase64URI());
        System.out.println(new String(Base64.getDecoder().decode("YmYtY2ZiLWF1dGg6dGVzdEAxOTIuMTY4LjEwMC4xOjg4ODg")));
    }

}
