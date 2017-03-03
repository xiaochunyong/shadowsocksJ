package me.ely.shadowsocks.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 直接连接远程服务器
 * Created by Ely on 11/12/2016.
 */
public class Demo1 {

    private static String remoteHost = "www.ithome.com";

    private static int remotePort = 80;

    public static void main(String[] args) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 32);
        byte[] arr = new byte[]{-12, -113, -81, -5, -67, -76, -52, 34, 98, 91, 89, 65, 86, -11, 55, 111, -9, 70, 24, -77, -100, -19, -121, -123, -120, -75, -124, 51, -57, -67, -66, -33, -24, -125, -65, -113, 36, 90, -119, 127, -48, -19, -11, 90, -117, 16, 75, -16, -9, 108, -39, 40, 25, 19, -119, -44, -6, 93, 96, 97, -74, 62, -30, 27, -3, -7, 24, -37, 74, 41, -84, -1, 93, -61, -62, 45, -82};
        buffer.put(arr);

        Selector selector = Selector.open();

        SocketChannel remoteChannel = SocketChannel.open();
        remoteChannel.configureBlocking(false);
        remoteChannel.register(selector, SelectionKey.OP_CONNECT);
        remoteChannel.connect(new InetSocketAddress(remoteHost, remotePort));

        while (true) {
            selector.select();
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                if (key.isValid()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    if (key.isConnectable()) {
                        System.out.println("connectable:" + socketChannel.finishConnect());
                        key.interestOps(SelectionKey.OP_WRITE);
                    } else if (key.isReadable()) {
                        System.out.println("readable");

                        buffer.clear();

                        int readCount = socketChannel.read(buffer);
                        if (readCount == -1) {
                            System.out.println("readCount is -1");
                            socketChannel.close();
                            return;
                        }

                        buffer.flip();
                        byte[] data = new byte[readCount];
                        buffer.get(data, 0, readCount);

                        System.out.println(new String(data));


                    } else if (key.isWritable()) {
                        System.out.println("writeable");
                        socketChannel.write(buffer);
                        key.interestOps(SelectionKey.OP_READ);
                    } else {
                        System.out.println("none");
                    }
                } else {
                    System.out.println("invalid");
                }
            }
        }
    }

}
