package me.ely.shadowsocks.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 在本机1081端口监听，当有连接接入后，连接远程服务器
 * Created by Ely on 11/12/2016.
 */
public class Demo2 {

    private static final Logger logger = LoggerFactory.getLogger(Demo2.class);

    private static String localeHost = "127.0.0.1";

    private static int localPort = 1081;
//
//    private static String remoteHost = "www.baidu.com";
//
//    private static int remotePort = 80;

    private static String remoteHost = "103.205.9.65";

    private static int remotePort = 8388;


    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(localeHost, localPort));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                if (key.isValid()) {
                    if (key.isAcceptable()) {
                        onAcceptable(key);
                    } else if (key.isConnectable()) {
                        onConnectable(key);
                    } else if (key.isReadable()) {
                        onRead(key);
                    }
                }
            }
        }
    }

    public static void onAcceptable(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel localChannel = serverSocketChannel.accept();
        localChannel.configureBlocking(false);
        localChannel.register(key.selector(), SelectionKey.OP_READ);

        // 连接远程服务器
        SocketChannel remoteChannel = SocketChannel.open();
        remoteChannel.configureBlocking(false);
        remoteChannel.connect(new InetSocketAddress(remoteHost, remotePort));
        // 注册CONNECT事件，但是之后的select()得到的selectedKeys里面一直没有这个channel的事件过来
        remoteChannel.register(key.selector(), SelectionKey.OP_CONNECT);


    }

    public static void onConnectable(SelectionKey key) throws IOException {
        System.out.println("onConnect");
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.finishConnect();
    }

    public static ByteBuffer buffer = ByteBuffer.allocate(1024 * 32);

    public static void onRead(SelectionKey key) throws IOException {
        SocketChannel channel1 = (SocketChannel) key.channel();
        logger.info("onRead {}", channel1.toString());
//        Connector connector = (Connector) key.attachment();
//        SocketChannel channel2 = connector.getAnotherChannel(channel1);

        buffer.clear();
        int readCount = channel1.read(buffer);

        if (readCount == -1) {
            channel1.close();
//            channel2.close();
//            closeSocketChannel(channel1, channel2);
            return;
        }

        buffer.flip();
        byte[] data = new byte[readCount];
        buffer.get(data, 0, readCount);

//        if (connector.isHandshaking) {
//            connector.isHandshaking = false;
//            connector.addRawData(new byte[]{5, 0});
//            key.interestOps(SelectionKey.OP_WRITE);
//        } else if (connector.isHeader) {
//            connector.isHeader = false;
//            connector.addRawData(new byte[]{5, 0, 0, 1, 0, 0, 0, 0, 0, 0});
//            key.interestOps(SelectionKey.OP_WRITE);
//
//
//            byte[] header = new byte[data.length - 3];
//            System.arraycopy(data, 3, header, 0, header.length);
//            logger.info("Request Host is {}", Util.getRequestedHostInfo(header.clone()));
//            connector.addData(channel1, header);
//
//            channel2.keyFor(key.selector()).interestOps(SelectionKey.OP_WRITE);
//        } else {
//
//            connector.addData(channel1, data);
//
//
//            channel2.keyFor(key.selector()).interestOps(SelectionKey.OP_WRITE);
//        }

    }

}
