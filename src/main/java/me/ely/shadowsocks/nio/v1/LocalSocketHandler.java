package me.ely.shadowsocks.nio.v1;

import me.ely.shadowsocks.model.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Ely on 29/11/2016.
 */
public class LocalSocketHandler extends SocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(LocalSocketHandler.class);

    private ExecutorService executor;

    private RemoteSocketHandler remoteSocketHandler;

    private ServerSocketChannel serverSocketChannel;

    public LocalSocketHandler(Config config) throws IOException {
        super(config);

        bind(config.getLocalHost(), config.getLocalPort());

        this.executor = Executors.newCachedThreadPool();
        this.remoteSocketHandler = new RemoteSocketHandler(config);
        this.executor.execute(this.remoteSocketHandler);
    }

    public void bind(String host, int port) throws IOException {
        this.serverSocketChannel =  ServerSocketChannel.open();
        this.serverSocketChannel.configureBlocking(false);
        this.serverSocketChannel.socket().bind(new InetSocketAddress(host, port));

        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        logger.info("Shadowsocks Listened at {}:{}", host, port);
    }

    @Override
    public boolean processRequestContext(RequestContext requestContext) {
        switch (requestContext.type) {
            case RequestContext.REGISTER_CHANNEL:
                // nothing
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

    private int i = 0;
    public void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(key.selector(), SelectionKey.OP_READ);



        // 建立与远程服务器的连接
        SocketChannel remoteChannel = remoteSocketHandler.connect(config.getRemoteHost(), config.getRemotePort());

        // 将本地连接  与  远程连接关联起来
        Connector connector = new Connector(this, socketChannel, remoteSocketHandler, remoteChannel, config);

        initSocket(socketChannel, connector);
        remoteSocketHandler.initSocket(remoteChannel, connector);


        executor.execute(connector);
        logger.info("new request coming {}", ++i);
    }

    public void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();


        Connector connector = connectors.get(socketChannel);
        if (connector == null) {
            cleanUp(socketChannel);
            return;
        }


        try {
            readBuffer.clear();
            int readCount = socketChannel.read(readBuffer);
            if (readCount != -1) {
                byte[] data = readBuffer.array();
                connector.addDataPacket(data, readCount, true);
            } else {
                key.cancel();
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

                    // TODO ???
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
            logger.warn("LocalSocket write queue is null: " + socketChannel);
        }
    }

    protected void cleanUp(SocketChannel socketChannel) {
        super.cleanUp(socketChannel);

        Connector connector = connectors.get(socketChannel);
        if (connector != null) {
            connector.close();
            connectors.remove(socketChannel);
            logger.trace(connector.LOG_PREFIX + "LocaleSocket closed");
        } else {
            logger.trace("Null Connector LocaleSocket closed " + socketChannel);
        }
    }

    @Override
    public void close() {
        super.close();
        executor.shutdownNow();

        try {
            serverSocketChannel.close();
            remoteSocketHandler.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("closed");
    }


}
