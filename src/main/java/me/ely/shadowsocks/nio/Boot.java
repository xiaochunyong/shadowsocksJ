package me.ely.shadowsocks.nio;

import me.ely.shadowsocks.utils.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Ely on 02/12/2016.
 */
public class Boot {

    private static final Logger logger = LoggerFactory.getLogger(Boot.class);

    public static void main(String[] args) throws InterruptedException, IOException {
        Config config = Config.getConfig();
        logger.info("{}", config.toString());

        Thread t1 = new Thread(new LocalSocketHandler(config));
        t1.start();
    }

}
