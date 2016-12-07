package me.ely.shadowsocks.nio;

import me.ely.shadowsocks.utils.Config;
import me.ely.shadowsocks.utils.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Ely on 01/12/2016.
 */
public abstract class SocketHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SocketHandler.class);

    protected Config config;
    protected Selector selector;
    protected final List<RequestContext> requestContexts = new LinkedList<>();
    protected final ConcurrentHashMap<SocketChannel, List<ByteBuffer>> pendingData = new ConcurrentHashMap<>();
    protected ConcurrentMap<SocketChannel, Connector> connectors = new ConcurrentHashMap<>();
    protected ByteBuffer readBuffer = ByteBuffer.allocate(Constant.BUFFER_SIZE);

    public SocketHandler(Config config) throws IOException {
        this.config = config;
        this.selector = Selector.open();
    }

    public void accept(SelectionKey key) throws IOException {
        logger.error("un implement accpet method");
    }
    public void finishConnect(SelectionKey key) throws IOException {
        logger.error("un implement finishConnect method");
    }
    public abstract void read(SelectionKey key) throws IOException;
    public abstract void write(SelectionKey key) throws IOException;
    public abstract boolean processRequestContext(RequestContext changeRequest);

    @Override
    public void run() {
        while (true) {
            try {
                synchronized (requestContexts) {
                    Iterator<RequestContext> it = requestContexts.iterator();
                    while (it.hasNext()) {
                        RequestContext requestContext = it.next();
                        it.remove();
                        if (!processRequestContext(requestContext)) {
                            break;
                        }
                    }
                }

                this.selector.select(100);

                Iterator<SelectionKey> it = this.selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    try {
                        if (key.isValid()) {
                            if (key.isAcceptable()) {
                                accept(key);
                            } else if (key.isConnectable()) {
                                finishConnect(key);
                            } else if (key.isReadable()) {
                                read(key);
                            } else if (key.isWritable()) {
                                write(key);
                            }
                        }
                    } catch (IOException | CancelledKeyException e) {
                        logger.error(e.getMessage(), e);
                        cleanUp((SocketChannel) key.channel());
                    }
                }
            } catch (ClosedChannelException e) {
                logger.error(e.getMessage(), e);
                break;
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.info("{} closed", this.getClass().getName());
    }

    protected void initSocket(SocketChannel socketChannel, Connector connector) {
        if (pendingData.putIfAbsent(socketChannel, new ArrayList<ByteBuffer>()) != null) {
            logger.error("Dup write buffer creation: " + socketChannel);
        }

        if (connectors.putIfAbsent(socketChannel, connector) != null) {
            logger.error("Dup connection creation: " + socketChannel);
        }
    }

    public void send(RequestContext context, byte[] data) {
        switch (context.type) {
            case RequestContext.CHANGE_SOCKET_OP:
                List<ByteBuffer> queue = pendingData.get(context.socketChannel);
                if (queue != null) {
                    synchronized (queue) {
                        queue.add(ByteBuffer.wrap(data));
                    }
                } else {
                    logger.warn("Socket is closed! dropping this request");
                }
                break;
        }

        synchronized (requestContexts) {
            requestContexts.add(context);
        }

        // TODO ???
        selector.wakeup();
    }

    public void send(RequestContext context) {
        send(context, null);
    }

    protected void cleanUp(SocketChannel socketChannel) {
        logger.info("cleanUp");
        try {
            socketChannel.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        SelectionKey selectionKey = socketChannel.keyFor(selector);
        if (selectionKey != null) {
            selectionKey.cancel();
        }

        if (pendingData.containsKey(socketChannel)) {
            pendingData.remove(socketChannel);
        }
    }

    public void close() {

        System.out.println("close");
        for (Connector connector : connectors.values()) {
            connector.forceClose();
        }

        connectors.clear();

        try {
            selector.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }


}
