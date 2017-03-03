package me.ely.shadowsocks.nio;

import me.ely.shadowsocks.crypt.AESCrypt;
import me.ely.shadowsocks.model.Config;
import me.ely.shadowsocks.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Queue;

/**
 * 重构Shadowsocks
 * 纠结点:
 * 1. 根据一个SocketChannel 找到另一个
 * 2. 加解密，我要加密还是解密
 * Created by Ely on 07/12/2016.
 */
public class LocalServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(LocalServer.class);

    public Selector selector;

    public Config config;

    public ByteBuffer buffer = ByteBuffer.allocate(1024 * 32);

    public AESCrypt crypt;

    private ServerSocketChannel serverSocketChannel;

    private boolean running;

    public LocalServer(Config config) {
        this.config = config;
    }

    public void run() {
        try {
            this.crypt = new AESCrypt(config.getMethod(), config.getPassword());


            this.running = true;
            this.selector = Selector.open();

            this.serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(config.getLocalHost(), config.getLocalPort()));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            logger.info("Shadowsocks started on {}:{} (socks5)", config.getLocalHost(), config.getLocalPort());


            while (this.running) {
                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    try {
                        if (key.isValid()) {
                            if (key.isAcceptable()) {
                                onAcceptable(key);
                            } else if (key.isConnectable()) {
                                onConnect(key);
                            } else if (key.isReadable()) {
                                onRead(key);
                            } else if (key.isWritable()) {
                                onWrite(key);
                            } else {
                                System.out.println("unknown key");
                            }
                        }
                    } catch (IOException e) {
                        logger.error("I/O has occurred error!", e);
                        closeSocketChannel(key);
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close();
        }
    }

    public void onAcceptable(SelectionKey key) throws IOException {
        logger.debug("onAcceptable");
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        Connector connector = new Connector();

        SocketChannel localChannel = serverSocketChannel.accept();
        localChannel.configureBlocking(false);
//        localChannel.register(key.selector(), SelectionKey.OP_READ, connector);


        SocketChannel remoteChannel = SocketChannel.open();
        remoteChannel.configureBlocking(false);
        remoteChannel.connect(new InetSocketAddress(config.getRemoteHost(), config.getRemotePort()));

        // 方案1，连接一直处于ConnectPending
        remoteChannel.register(key.selector(), SelectionKey.OP_CONNECT, connector);

        // 方案2
//        while (!remoteChannel.finishConnect()) {
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        logger.info("Socket connected to ss server: {}:{}", config.getRemoteHost(), config.getRemotePort());
//        remoteChannel.register(key.selector(), SelectionKey.OP_READ, connector);

        connector.bind(localChannel, remoteChannel);
    }

    public void onConnect(SelectionKey key) throws IOException {
        logger.debug("onConnect");
        SocketChannel remoteChannel = (SocketChannel) key.channel();
        Connector connector = (Connector) key.attachment();
        SocketChannel localChannel = connector.getAnotherChannel(remoteChannel);

        if (remoteChannel.finishConnect()) {
            key.interestOps(SelectionKey.OP_READ);
            localChannel.register(key.selector(), SelectionKey.OP_READ, connector);

            logger.info("Socket connected to ss server: {}:{}", config.getRemoteHost(), config.getRemotePort());
        } else {
            logger.info("Socket not connected to ss server: {}:{}");
        }
    }

    public void onRead(SelectionKey key) throws IOException {
        logger.debug("onRead");
        SocketChannel channel1 = (SocketChannel) key.channel();
        Connector connector = (Connector) key.attachment();
        SocketChannel channel2 = connector.getAnotherChannel(channel1);

        buffer.clear();

        int readCount = channel1.read(buffer);
        if (readCount == -1) {
            closeSocketChannel(channel1, channel2);
            return;
        }

        buffer.flip();
        byte[] data = new byte[readCount];
        buffer.get(data, 0, readCount);

        if (connector.isRemote(channel1)) {
            logger.debug("read from remote: {}", readCount);
        } else {
            logger.debug("read from local: {}", Arrays.toString(data));
        }

        if (connector.isHandshaking) {
            connector.isHandshaking = false;
            connector.addRawData(new byte[]{5, 0});
            key.interestOps(SelectionKey.OP_WRITE);
        } else if (connector.isHeader) {
            connector.isHeader = false;
            connector.addRawData(new byte[]{5, 0, 0, 1, 0, 0, 0, 0, 0, 0});
            key.interestOps(SelectionKey.OP_WRITE);


            byte[] hostData = new byte[data.length - 3];
            System.arraycopy(data, 3, hostData, 0, hostData.length);
            logger.info("Request Host is {}", Util.getRequestedHostInfo(hostData.clone()));
            connector.addData(channel1, hostData);
            channel2.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
        } else {
            connector.addData(channel1, data);


            channel2.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
        }

    }

    public void onWrite(SelectionKey key) throws IOException {
        logger.debug("onWrite");
        SocketChannel channel = (SocketChannel) key.channel();
        Connector connector = (Connector) key.attachment();
        Queue<byte[]> datas = connector.getData(channel);

        while (true) {
            byte[] data = datas.poll();
            if (data == null) break;

            if (connector.isRemote(channel)) {
                logger.debug("write to remote: {}", Arrays.toString(data));
            } else {
                logger.debug("write to local: {}", data.length);
            }

            ByteBuffer bf = ByteBuffer.wrap(data);
            while (bf.hasRemaining()) {
                channel.write(bf);
            }
        }

        key.interestOps(SelectionKey.OP_READ);

    }

    public void closeSocketChannel(SelectionKey key) {
        SelectableChannel channel = key.channel();
        if (channel instanceof ServerSocketChannel) {
            close();
        } else {
            SocketChannel channel1 = (SocketChannel) key.channel();
            Connector connector = (Connector) key.attachment();
            SocketChannel channel2 = connector.getAnotherChannel(channel1);
            closeSocketChannel(channel1, channel2);
        }
    }

    public void closeSocketChannel(SocketChannel channel1, SocketChannel channel2) {
        try {
            logger.trace("close channel");
            channel1.close();
            channel2.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void close() {
        try {
            if (this.serverSocketChannel.isOpen()) {
                this.serverSocketChannel.close();
            }
            if (this.selector.isOpen()) {
                this.selector.close();
            }
            logger.info("Shadowsocks stoped");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        this.running = false;
        this.selector.wakeup();
    }

    public void start() {
        new Thread(this).start();
    }
}