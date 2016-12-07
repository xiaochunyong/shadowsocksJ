package me.ely.shadowsocks.nio;

import me.ely.shadowsocks.utils.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * Created by Ely on 01/12/2016.
 */
public class RemoteSocketHandler extends SocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(RemoteSocketHandler.class);

    public RemoteSocketHandler(Config config) throws IOException {
        super(config);
    }

    private int i = 0;
    private int count = 0;
    public void read(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        Connector connector = connectors.get(socketChannel);
        if (connector == null) {
            cleanUp(socketChannel);
            return;
        }

        readBuffer.clear();

        try {
            int readCount = socketChannel.read(readBuffer);
            if (readCount != -1) {
                count+=readCount;
                logger.info("{} - {}", ++i, count);
                connector.addDataPacket(readBuffer.array(), readCount, false);
            } else {
                cleanUp(socketChannel);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            cleanUp(socketChannel);
        }


    }

    public void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        List<ByteBuffer> queue = pendingData.get(socketChannel);

        if (queue != null) {
            synchronized (queue) {
                while (!queue.isEmpty()) {
                    ByteBuffer buffer = queue.get(0);
                    socketChannel.write(buffer);

                    if (buffer.remaining() > 0) {
                        break;
                    }

                    queue.remove(0);
                }

                if (queue.isEmpty()) {
                    key.interestOps(SelectionKey.OP_READ);
                }
            }
        } else {
            logger.warn("write queue is null" + socketChannel);
        }

    }

    public void finishConnect(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            socketChannel.finishConnect();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            cleanUp(socketChannel);
        }
    }

    @Override
    public boolean processRequestContext(RequestContext requestContext) {
        if (requestContext.type != RequestContext.REGISTER_CHANNEL && requestContext.socketChannel.isConnectionPending()) {
            return false;
        }

        switch (requestContext.type) {
            case RequestContext.REGISTER_CHANNEL:
                try {
                    requestContext.socketChannel.register(selector, requestContext.op);
                } catch (ClosedChannelException e) {
                    logger.error(e.getMessage(), e);
                    cleanUp(requestContext.socketChannel);
                }
                break;
            case RequestContext.CHANGE_SOCKET_OP:
                SelectionKey key = requestContext.socketChannel.keyFor(selector);
                if (key != null && key.isValid()) {
                    key.interestOps(requestContext.op);
                } else {
                    logger.warn("drop request context " + key + requestContext.socketChannel);
                }
                break;
            case RequestContext.CLOSE_CHANNEL:
                cleanUp(requestContext.socketChannel);
                break;
            default:
                throw new RuntimeException("invalid type");

        }
        return true;
    }

    public SocketChannel connect(String host, int port) throws IOException {
        SocketChannel remoteChannel = SocketChannel.open();
        remoteChannel.configureBlocking(false);
        remoteChannel.connect(new InetSocketAddress(host, port));
        remoteChannel.register(selector, SelectionKey.OP_CONNECT);

        System.out.println("connect");

        return remoteChannel;
    }

//    @Override
//    protected void initSocket(SocketChannel socketChannel, Connector connector) {
//        super.initSocket(socketChannel, connector);
//        synchronized (requestContexts) {
//            requestContexts.add(new RequestContext(socketChannel, RequestContext.REGISTER_CHANNEL, SelectionKey.OP_CONNECT));
//        }
//    }

}
