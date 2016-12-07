package me.ely.shadowsocks.nio.v4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SocketChannel;
import java.util.UUID;

/**
 * Created by Ely on 07/12/2016.
 */
public class Connector {

    private static final Logger logger = LoggerFactory.getLogger(Connector.class);

    private String id = UUID.randomUUID().toString();

    private SocketChannel localChannel;

    private SocketChannel remoteChannel;

    public Connector(SocketChannel localChannel, SocketChannel remoteChannel) {
        this.localChannel = localChannel;
        this.remoteChannel = remoteChannel;
    }



    public SocketChannel getLocalChannel() {
        return localChannel;
    }

    public SocketChannel getRemoteChannel() {
        return remoteChannel;
    }
}
