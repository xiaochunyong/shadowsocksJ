package me.ely.shadowsocks.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SocketChannel;

/**
 * Created by Ely on 01/12/2016.
 */
public class RequestContext {

    private static final Logger logger = LoggerFactory.getLogger(RequestContext.class);

    public static final int REGISTER_CHANNEL = 1;
    public static final int CHANGE_SOCKET_OP = 2;
    public static final int CLOSE_CHANNEL = 3;

    public SocketChannel socketChannel;
    public int type;
    public int op;

    public RequestContext(SocketChannel socketChannel, int type, int op) {
        this.socketChannel = socketChannel;
        this.type = type;
        this.op = op;
    }

    public RequestContext(SocketChannel socketChannel, int type) {
        this(socketChannel, type, 0);
    }
}
