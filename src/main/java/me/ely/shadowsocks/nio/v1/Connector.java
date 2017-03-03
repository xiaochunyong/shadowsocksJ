package me.ely.shadowsocks.nio.v1;

import me.ely.shadowsocks.crypt.AESCrypt;
import me.ely.shadowsocks.model.Config;
import me.ely.shadowsocks.protocol.Socks5Protocol;
import me.ely.shadowsocks.utils.Constant;
import me.ely.shadowsocks.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Ely on 01/12/2016.
 */
public class Connector implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Connector.class);

    private SocketHandler localSocketHandler;
    private SocketHandler remoteSocketHandler;
    private SocketChannel localChannel;
    private SocketChannel remoteChannel;

    private AESCrypt crypt;
    private Socks5Protocol protocol;

    public String LOG_PREFIX;

    private ByteArrayOutputStream stream;
    private BlockingQueue<DataPacket> processQueue;
    private boolean requestedClose;

    public Connector(LocalSocketHandler localSocketHandler, SocketChannel localChannel, RemoteSocketHandler remoteSocketHandler, SocketChannel remoteChannel, Config config) {
        this.localSocketHandler = localSocketHandler;
        this.localChannel = localChannel;
        this.remoteSocketHandler = remoteSocketHandler;
        this.remoteChannel = remoteChannel;

        this.crypt = new AESCrypt(config.getMethod(), config.getPassword());
        this.protocol = new Socks5Protocol();

        this.LOG_PREFIX = String.format("Local: %s, Remote: %s > ", localChannel, remoteChannel);

        this.stream = new ByteArrayOutputStream(Constant.BUFFER_SIZE);

        this.processQueue = new LinkedBlockingQueue<>();
        this.requestedClose = false;
    }

    public void close() {
        requestedClose = true;
        addDataPacket(null,0,  false);
    }

    public void forceClose() {
        logger.trace(LOG_PREFIX + "force close");

        try {
            if (localChannel.isOpen()) {
                localChannel.close();
            }
            if (remoteChannel.isOpen()) {
                remoteChannel.close();
            }
        } catch (IOException e) {
            logger.trace(LOG_PREFIX + "force close occurred error", e);
        }

        close();
    }
    
    public void addDataPacket(byte[] data, int count, boolean isEncrypt) {
        if (data != null) {
            byte[] newData = new byte[count];
            System.arraycopy(data, 0, newData, 0, count);
            processQueue.add(new DataPacket(newData, isEncrypt));
        } else {
            processQueue.add(new DataPacket());
        }
    }

    @Override
    public void run() {
        DataPacket dataPacket;
        SocketHandler handler;
        SocketChannel channel;
        List<byte[]> sendData = null;

        while (true) {
            if (processQueue.isEmpty() && requestedClose) {
                logger.trace(LOG_PREFIX + " Connector has closed");

                if (localChannel.isOpen()) {
                    localSocketHandler.send(new RequestContext(localChannel, RequestContext.CLOSE_CHANNEL));
                }

                if (remoteChannel.isOpen()) {
                    remoteSocketHandler.send(new RequestContext(remoteChannel, RequestContext.CLOSE_CHANNEL));
                }

                break;
            }

            try {
                dataPacket = processQueue.take();
                if (dataPacket.data == null) {
                    continue;
                }

                if (!protocol.isReady()) {
                    byte[] temp = protocol.getResponse(dataPacket.data);
                    if (temp != null) {
                        localSocketHandler.send(new RequestContext(localChannel, RequestContext.CHANGE_SOCKET_OP, SelectionKey.OP_WRITE), temp);
                    }

                    sendData = protocol.getRemoteResponse(dataPacket.data);
                    if (sendData == null || sendData.isEmpty()) {
                        continue;
                    }

                    logger.info("Connected to :" + Util.getRequestedHostInfo(sendData.get(0)));
                } else {
                    sendData.clear();
                    sendData.add(dataPacket.data);
                }

                for (byte[] data : sendData) {
                    stream.reset();
                    if (dataPacket.isEncrypt) {
                        crypt.encrypt(data, stream);
                        channel = remoteChannel;
                        handler = remoteSocketHandler;
                    } else {
                        crypt.decrypt(data, stream);
                        channel = localChannel;
                        handler = localSocketHandler;
                    }

                    handler.send(new RequestContext(channel, RequestContext.CHANGE_SOCKET_OP, SelectionKey.OP_WRITE), stream.toByteArray());
                }
            } catch (InterruptedException e) {
                logger.trace(e.getMessage(), e);
                break;
            }
        }
    }
}
