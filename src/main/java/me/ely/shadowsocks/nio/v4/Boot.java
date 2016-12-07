package me.ely.shadowsocks.nio.v4;

import me.ely.shadowsocks.utils.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * Created by Ely on 07/12/2016.
 */
public class Boot {

    private static final Logger logger = LoggerFactory.getLogger(Boot.class);

    public static Config config;
    public static ByteBuffer buffer = ByteBuffer.allocate(1024 * 32);

    public static void main(String[] args) throws IOException {
        config = Config.getConfig();

        Selector selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(config.getLocalHost(), config.getLocalPort()));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();

                if (key.isAcceptable()) {
                    onAcceptable(key);
                } else if (key.isConnectable()) {
                    onConnect(key);
                } else if (key.isReadable()) {
                    onRead(key);
                } else if (key.isWritable()) {
                    onWrite(key);
                }

                it.remove();
            }
        }
    }

    public static void onAcceptable(SelectionKey key) throws IOException {
        Connector connector = new Connector();

        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(key.selector(), SelectionKey.OP_READ, connector);


        SocketChannel remoteChannel = SocketChannel.open();
        remoteChannel.configureBlocking(false);
        remoteChannel.connect(new InetSocketAddress(config.getRemoteHost(), config.getRemotePort()));
        remoteChannel.register(key.selector(), SelectionKey.OP_CONNECT, connector);

        connector.bind(socketChannel, remoteChannel);
    }

    public void transfer() {
//        1. 根据一个SocketChannel 找到另一个
//        2. 加解密，我要加密还是解密
//        socketChannel.read(buffer);
//        changeEvent socketChannel2 to write
        socketChannel2.write(buffer);
    }

    public static void onConnect(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        if (socketChannel.finishConnect()) {
            logger.info("connect established!");
        } else {
            logger.info("connect established fail!");
        }
    }

    public static void onRead(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.
    }

    public static void onWrite(SelectionKey key) {

    }


    public static void registe2Selector(Selector selector, SocketChannel socketChannel, int ops, Object att) throws ClosedChannelException {
        socketChannel.register(selector, ops, att);
    }

}
