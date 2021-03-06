package me.ely.shadowsocks.utils;

import java.util.Locale;

/**
 * Created by Ely on 02/12/2016.
 */
public class Constant {

    public static final String PROG_NAME = "Shadowsocks";

    public static final String VERSION = "1.0";

    public static final String CONF_DIR = String.format("%s/.ShadowsocksJ", System.getProperty("user.home"));

    public static final String CONF_FILE = String.format("%s/%s", CONF_DIR, "config.json");

//    public static final int BUFFER_SIZE = 64;
    public static final int BUFFER_SIZE = 1024 * 16;

    public static final Locale LOCALE = Locale.getDefault();

}
