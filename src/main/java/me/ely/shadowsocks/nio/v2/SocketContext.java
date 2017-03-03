package me.ely.shadowsocks.nio.v2;

import me.ely.shadowsocks.crypt.AESCrypt;
import me.ely.shadowsocks.model.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by Ely on 05/12/2016.
 */
public class SocketContext {

    private static final Logger logger = LoggerFactory.getLogger(SocketContext.class);


    public static final int CHANNEL_FROM_LOCAL = 1;
    public static final int CHANNEL_FROM_REMOTE = 2;

    public static final int DATA_TYPE_FROM_LOCAL = 1;
    public static final int DATA_TYPE_TO_LOCAL = 2;
    public static final int DATA_TYPE_FROM_REMOTE = 3;
    public static final int DATA_TYPE_TO_REMOTE = 4;

    private Selector selector;
    private SocketChannel localChannel;
    private SocketChannel remoteChannel;
    private AESCrypt crypt;
    private int stage;
    private byte[] data1;
    private byte[] data2;

    public SocketContext(Selector selector, SocketChannel localChannel, SocketChannel remoteChannel) {
        this.selector = selector;
        this.localChannel = localChannel;
        this.remoteChannel = remoteChannel;
        Config config = Config.getConfig();
        this.crypt = new AESCrypt(config.getMethod(), config.getPassword());
    }

    public byte[] getData1() {
        return data1;
    }

    public byte[] getData2() {
        return data2;
    }

    public void handle(byte[] readData, int dataType) {
        switch (dataType) {
            case DATA_TYPE_FROM_LOCAL:
                try {
                    if (this.stage == 0) {
                        this.stage = 1;
                        this.data1 = new byte[]{5, 0};
                        localChannel.register(selector, SelectionKey.OP_WRITE, CHANNEL_FROM_LOCAL);
                    } else if (this.stage == 1) {
                        this.stage = 2;
                        this.data1 = new byte[]{5, 0, 0, 1, 0, 0, 0, 0, 0, 0};

                        this.data2 = new byte[readData.length - 3];
                        System.arraycopy(readData, 3, this.data2, 0, this.data2.length);

                        this.data2 = crypt.encrypt(this.data2);
                        this.remoteChannel.register(this.selector, SelectionKey.OP_WRITE, CHANNEL_FROM_REMOTE);
                        this.localChannel.register(this.selector, SelectionKey.OP_WRITE, CHANNEL_FROM_LOCAL);
                    } else {
                        this.data2 = crypt.encrypt(readData);
                        this.remoteChannel.register(this.selector, SelectionKey.OP_WRITE, CHANNEL_FROM_REMOTE);
                    }

                } catch (ClosedChannelException e) {
                    logger.error(e.getMessage(), e);
                }
                break;
            case DATA_TYPE_TO_REMOTE:
                try {
                    this.remoteChannel.register(this.selector, SelectionKey.OP_READ, CHANNEL_FROM_REMOTE);
                    this.data2 = null;
                } catch (ClosedChannelException e) {
                    logger.error(e.getMessage(), e);
                }
                break;
            case DATA_TYPE_FROM_REMOTE:
                try {
                    this.data1 = crypt.decrypt(readData);
                    this.localChannel.register(this.selector, SelectionKey.OP_WRITE, CHANNEL_FROM_LOCAL);
                } catch (ClosedChannelException e) {
                    logger.error(e.getMessage(), e);
                }
                break;
            case DATA_TYPE_TO_LOCAL:
                try {
                    localChannel.register(selector, SelectionKey.OP_READ, CHANNEL_FROM_LOCAL);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
                break;
            default:
                throw new RuntimeException("invalid dataType");
        }
    }

    public void close() {
        try {
            localChannel.close();
            remoteChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
