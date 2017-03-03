package me.ely.shadowsocks.nio;

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
 * Created by ely on 23/02/2017.
 */
public class RemoteServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RemoteServer.class);

    private Config config;

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public ByteBuffer buffer = ByteBuffer.allocate(1024 * 32);

    public RemoteServer(Config config) {
        this.config = config;
    }

    @Override
    public void run() {
        try {
            selector = Selector.open();

            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(config.getRemoteHost(), config.getRemotePort()));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            logger.info("Shadowsocks started on {}:{} (socks5)", config.getRemoteHost(), config.getRemotePort());

            while (true) {
                int count = selector.select();
                logger.info("selectedKeys count is {}", count);
                if (count == 0) break;

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    try {
                        if (key.isAcceptable()) {
                            onAcceptable(key);
                        } else if (key.isConnectable()) {
                            onConnectable(key);
                        } else if (key.isReadable()) {
                            onReadable(key);
                        } else if (key.isWritable()) {
                            onWritable(key);
                        }
                    } catch (IOException e) {
                        logger.error("I/O has occurred error!", e);
                        closeSocketChannel(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void onAcceptable(SelectionKey key) throws IOException {
        logger.info("onAcceptable");
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        Connector connector = new Connector();

        SocketChannel localChannel = serverSocketChannel.accept();
        localChannel.configureBlocking(false);
        localChannel.register(key.selector(), SelectionKey.OP_READ, connector);

        SocketChannel remoteChannel = SocketChannel.open();
        remoteChannel.configureBlocking(false);

        connector.bind(localChannel, remoteChannel);
    }

    private void onConnectable(SelectionKey key) throws IOException {
        logger.info("onConnectable");
        SocketChannel remoteChannel = (SocketChannel) key.channel();
        Connector connector = (Connector) key.attachment();
        if (remoteChannel.finishConnect()) {
            logger.info("Socket connected to destination: {}:{}", connector.host, connector.port);
            key.interestOps(SelectionKey.OP_WRITE);
        } else {
            logger.info("Socket not connected to destination!");
        }
    }

    private void onReadable(SelectionKey key) throws IOException {
        SocketChannel channel1 = (SocketChannel) key.channel();
        Connector connector = (Connector) key.attachment();
        SocketChannel channel2 = connector.getAnotherChannel(channel1);
        if (connector.isRemote(channel1)) {
            logger.info("read from remote...");
        } else {
            logger.info("read from local...");
        }

        buffer.clear();

        int readCount = channel1.read(buffer);
        if (readCount == -1) {
            logger.info("readCount is -1");
//             closeSocketChannel(channel1, channel2);
            channel1.close();
            return;
        }

        buffer.flip();
        byte[] data = new byte[readCount];
        buffer.get(data, 0, readCount);

        if (connector.isRemote(channel1)) {
            logger.info("read from remote: {}", readCount);
        } else {
            logger.info("read from local: {}", Arrays.toString(data));
        }

        if (connector.isLocal(channel1)) {
            if (connector.isHost) {
                connector.isHost = false;
                String host = Util.getRequestedHostInfo(connector.crypt.decrypt(data));
                logger.info("receive a request host is {}", host);
                String[] hostAndPort = host.split(":");
                if (hostAndPort.length == 2) {
                    connector.host = hostAndPort[0];
                    connector.port = Integer.parseInt(hostAndPort[1]);
                    channel2.connect(new InetSocketAddress(connector.host, connector.port));
                    channel2.register(key.selector(), SelectionKey.OP_CONNECT, connector);
                } else {
                    logger.error("invalid a request host is {}", host);
                }
            } else {
                logger.info("receive data {}", data.length);
                connector.addServerData(channel1, data);
                if (channel2.isConnected()) {
                    channel2.keyFor(key.selector()).interestOps(SelectionKey.OP_WRITE);
                }
            }
        } else {
            connector.addServerData(channel1, data);
            try {
                channel2.keyFor(key.selector()).interestOps(SelectionKey.OP_WRITE);
            } catch (NullPointerException e) {
                e.printStackTrace();
                System.out.println("channel2 is " + channel2);
                System.out.println("channel2.keyFor is " + channel2.keyFor(key.selector()));
            }
        }
    }

    private void onWritable(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Connector connector = (Connector) key.attachment();
        Queue<byte[]> datas = connector.getData(channel);
        if (connector.isRemote(channel)) {
            logger.info("write to remote...");
        } else {
            logger.info("write to local...");
        }

        while (true) {
            byte[] data = datas.poll();
            if (data == null) break;
            if (connector.isRemote(channel)) {
                logger.info("write to remote: {}", Arrays.toString(data));
            } else {
                logger.info("write to local: {}", data.length);
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


}
