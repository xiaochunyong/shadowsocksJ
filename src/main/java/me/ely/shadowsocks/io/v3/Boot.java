package me.ely.shadowsocks.io.v3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Ely on 04/12/2016.
 */
public class Boot {

    private static final Logger logger = LoggerFactory.getLogger(Boot.class);

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1081);
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new SocksRunnable(socket)).start();
            logger.info("new request coming...");
        }
    }

}