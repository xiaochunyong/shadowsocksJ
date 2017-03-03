package me.ely.shadowsocks.nio.v2;

import me.ely.shadowsocks.model.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Ely on 05/12/2016.
 */
public class Boot {

    private static final Logger logger = LoggerFactory.getLogger(Boot.class);

    private Selector selector;

    private Config config;
    private ByteBuffer buffer;
    private Map<SocketChannel, SocketContext> socketContextMap = new HashMap<>();

    public Boot(Config config) {
        this.config = config;
        buffer = ByteBuffer.allocate(1024 * 16);
    }

    public static void main(String[] args) throws IOException {
        new Boot(Config.getConfig()).start();
    }

    public void start() throws IOException {
        selector = Selector.open();


        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(config.getLocalPort()));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();

                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isConnectable()) {
                    connect(key);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }
            }
        }
    }

    public void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel localChannel = serverSocketChannel.accept();
        localChannel.configureBlocking(false);
        localChannel.register(selector, SelectionKey.OP_READ, SocketContext.CHANNEL_FROM_LOCAL);

        SocketChannel remoteChannel = SocketChannel.open();
        remoteChannel.configureBlocking(false);
        remoteChannel.connect(new InetSocketAddress(config.getRemoteHost(), config.getRemotePort()));
        remoteChannel.register(selector, SelectionKey.OP_CONNECT, SocketContext.CHANNEL_FROM_REMOTE);

        SocketContext context = new SocketContext(selector, localChannel, remoteChannel);
        socketContextMap.put(localChannel, context);
        socketContextMap.put(remoteChannel, context);
    }

    public void connect(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        System.out.println(key.attachment());
        socketChannel.finishConnect();
    }

    public void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        Integer channelType = (Integer) key.attachment();
        SocketContext context = socketContextMap.get(socketChannel);

        buffer.clear();
        int readCount = socketChannel.read(buffer);
        if (readCount != -1) {
            byte[] data = new byte[readCount];
            buffer.flip();
            buffer.get(data, 0, data.length);
            logger.info("read from {}[{}] {}", channelType == SocketContext.CHANNEL_FROM_LOCAL ? "local" : "remote", readCount, Arrays.toString(data));


            if (channelType == SocketContext.CHANNEL_FROM_LOCAL) {
//                socketChannel.register(selector, SelectionKey.OP_WRITE);
                // 本地读取，加密，让Remote写出去，等待remote读取，解密，让local响应回去
                context.handle(data, SocketContext.DATA_TYPE_FROM_LOCAL);
            } else {
                context.handle(data, SocketContext.DATA_TYPE_FROM_REMOTE);
            }
        } else {
            context.close();
            logger.warn("{} read count is -1", channelType);
        }
    }

    public void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        Integer channelType = (Integer) key.attachment();
        SocketContext context = socketContextMap.get(socketChannel);


        if (channelType == SocketContext.CHANNEL_FROM_LOCAL) {
            logger.info("write to local[{}] {}", context.getData1().length, Arrays.toString(context.getData1()));
            socketChannel.write(ByteBuffer.wrap(context.getData1()));
            context.handle(context.getData1(), SocketContext.DATA_TYPE_TO_LOCAL);
        } else {
            logger.info("write to remote[{}] {}", context.getData2().length, Arrays.toString(context.getData2()));
            socketChannel.write(ByteBuffer.wrap(context.getData2()));
            context.handle(context.getData2(), SocketContext.DATA_TYPE_TO_REMOTE);
        }


//        socketChannel.register(selector, SelectionKey.OP_READ);
    }

}
