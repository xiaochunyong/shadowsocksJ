package me.ely.shadowsocks;

import me.ely.shadowsocks.model.Config;
import me.ely.shadowsocks.nio.LocalServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Ely on 11/12/2016.
 */
public class Boot {

    private static final Logger logger = LoggerFactory.getLogger(Boot.class);

    public static void main(String[] args) throws IOException {
        Config config = Config.loadConfig();
        LocalServer server = new LocalServer(config);
        new Thread(server).start();
    }

}
