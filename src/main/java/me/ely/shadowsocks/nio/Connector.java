package me.ely.shadowsocks.nio;

import me.ely.shadowsocks.crypt.AESCrypt;
import me.ely.shadowsocks.model.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Ely on 07/12/2016.
 */
public class Connector {

    private static final Logger logger = LoggerFactory.getLogger(Connector.class);

    public static int FROM_SRC = 1;
    public static int FROM_SERVER = 2;
    public static int TO_SRC = 3;
    public static int TO_SERVER = 4;

    public static int FROM_LOCAL = 1;
    public static int FROM_DEST = 2;
    public static int TO_LOCAL = 3;
    public static int TO_DEST = 4;

    // client
    public boolean isHandshaking = true;
    public boolean isHeader = true;


    // server
    public boolean isHost = true;
    public String host;
    public int port;

    private SocketChannel localChannel;

    private SocketChannel remoteChannel;

    private Queue<byte[]> localQueue;
    private Queue<byte[]> remoteQueue;

    public AESCrypt crypt;
    private Config config;


    public Connector() {
        localQueue = new LinkedList<>();
        remoteQueue = new LinkedList<>();

        config = Config.getConfig();
        crypt = new AESCrypt(config.getMethod(), config.getPassword());
    }

    public void bind(SocketChannel localChannel, SocketChannel remoteChannel) {
        this.localChannel = localChannel;
        this.remoteChannel = remoteChannel;
    }

    public SocketChannel getAnotherChannel(SocketChannel socketChannel) {
        if (socketChannel != localChannel) {
            return localChannel;
        }
        if (socketChannel != remoteChannel) {
            return remoteChannel;
        }
        throw new RuntimeException("error");
    }

    public void setLocalChannel(SocketChannel localChannel) {
        this.localChannel = localChannel;
    }

    public void setRemoteChannel(SocketChannel remoteChannel) {
        this.remoteChannel = remoteChannel;
    }

    public boolean isLocal(SocketChannel socketChannel) {
        return socketChannel == localChannel;
    }

    public boolean isRemote(SocketChannel socketChannel) {
        return socketChannel == remoteChannel;
    }


    public Queue<byte[]> getData(SocketChannel socketChannel) {
        if (isLocal(socketChannel)) {
            return remoteQueue;
        } else {
            return localQueue;
        }
    }

    public void addData(SocketChannel socketChannel, byte[] data) {
        if (isLocal(socketChannel)) {
            localQueue.add(crypt.encrypt(data));
        } else {
            remoteQueue.add(crypt.decrypt(data));
        }
    }

    public void addServerData(SocketChannel socketChannel, byte[] data) {
        if (isLocal(socketChannel)) {
            localQueue.add(crypt.decrypt(data));
        } else {
            remoteQueue.add(crypt.encrypt(data));
        }
    }

    public void addRawData(byte[] data) {
        remoteQueue.add(data);
    }
}