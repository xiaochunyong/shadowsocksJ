package me.ely.shadowsocks.io;

import me.ely.shadowsocks.utils.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Ely on 29/11/2016.
 */
public class LocalServer {

    private static final Logger logger = LoggerFactory.getLogger(LocalServer.class);

    public static void main(String[] args) throws IOException {
        LocalServer localServer = new LocalServer();
        localServer.start(Config.getConfig());
    }

    private Executor executor = Executors.newCachedThreadPool();

    public void start(Config config) throws IOException {
        ServerSocket serverSocket = new ServerSocket(config.getLocalPort());
        while (true) {
            Socket socket = serverSocket.accept();
            logger.info("new request");
            LocalWorker worker = new LocalWorker(executor, socket, config);
            executor.execute(worker);
        }
    }

}
