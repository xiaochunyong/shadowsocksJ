package me.ely.shadowsocks.io;

import me.ely.shadowsocks.crypt.AESCrypt;
import me.ely.shadowsocks.protocol.IProtocol;
import me.ely.shadowsocks.protocol.Socks5Protocol;
import me.ely.shadowsocks.utils.Config;
import me.ely.shadowsocks.utils.Util;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by Ely on 29/11/2016.
 */
public class LocalWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(LocalWorker.class);

    public static final int BUFFER_SIZE = 1024 * 16;
    public static final int SO_TIMEOUT = 10 * 1000;

    private Executor executor;
    private ByteArrayOutputStream localBaos;
    private ByteArrayOutputStream remoteBaos;
    private Config config;
    private Socket localSocket;
    private Socket remoteSocket;
    private AESCrypt crypt;
    private IProtocol protocol;
    private boolean isClosed;

    public LocalWorker(Executor executor, Socket localSocket, Config config) throws SocketException {
        this.executor = executor;
        this.localSocket = localSocket;
        this.localSocket.setSoTimeout(SO_TIMEOUT);
        this.config = config;

        this.crypt = new AESCrypt(config.getMethod(), config.getPassword());
        this.protocol = new Socks5Protocol();
        this.localBaos = new ByteArrayOutputStream(BUFFER_SIZE);
        this.remoteBaos = new ByteArrayOutputStream(BUFFER_SIZE);
    }

    @Override
    public void run() {
        try {


            remoteSocket = new Socket(config.getRemoteHost(), config.getRemotePort());
            remoteSocket.setSoTimeout(SO_TIMEOUT);


            executor.execute(getLocalWorker());
            executor.execute(getRemoteWorker());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public Runnable getLocalWorker() {
        return new Runnable() {
            @Override
            public void run() {
                BufferedInputStream stream;
                byte[] dataBuffer = new byte[BUFFER_SIZE];
                byte[] buffer;
                int readCount;
                List<byte[]> sendData = null;

                // prepare local stream
                try {
                    stream = new BufferedInputStream(localSocket.getInputStream());
                } catch (IOException e) {
                    logger.info(e.toString());
                    return;
                }

                // start to process data from local socket
                while (true) {
                    try {
                        // read data
                        readCount = stream.read(dataBuffer);
                        logger.info("read {} bytes from local", readCount);
                        if (readCount == -1) {
                            throw new IOException("Local socket closed (Read)!");
                        }

                        // initialize proxy
                        if (!protocol.isReady()) {
                            byte[] temp;
                            buffer = new byte[readCount];

                            // dup dataBuffer to use in later
                            System.arraycopy(dataBuffer, 0, buffer, 0, readCount);

                            temp = protocol.getResponse(buffer);
                            if ((temp != null) && (!_sendLocal(temp, temp.length))) {
                                throw new IOException("Local socket closed (proxy-Write)!");
                            }
                            // packet for remote socket
                            sendData = protocol.getRemoteResponse(buffer);
                            if (sendData == null) {
                                continue;
                            }
                            logger.info("Connected to: " + Util.getRequestedHostInfo(sendData.get(0)));
                        }
                        else {
                            sendData.clear();
                            sendData.add(dataBuffer);
                        }

                        for (byte[] bytes : sendData) {
                            // send data to remote socket
                            if (!sendRemote(bytes, bytes.length)) {
                                throw new IOException("Remote socket closed (Write)!");
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        continue;
                    } catch (IOException e) {
                        logger.trace(e.getMessage(), e);
                        break;
                    }
                }
                close();
                logger.trace("localWorker exit, Local={}, Remote={}", localSocket, remoteSocket);
            }
        };
    }

    public Runnable getRemoteWorker() {
        return new Runnable() {
            @Override
            public void run() {
                BufferedInputStream stream;
                int readCount;
                byte[] dataBuffer = new byte[4096];

                // prepare remote stream
                try {
                    //stream = _remote.getInputStream();
                    stream = new BufferedInputStream (remoteSocket.getInputStream());
                } catch (IOException e) {
                    logger.info(e.toString());
                    return;
                }

                // start to process data from remote socket
                while (true) {
                    try {
                        readCount = stream.read(dataBuffer);
                        logger.info("read {} bytes from remote", readCount);
                        if (readCount == -1) {
                            throw new IOException("Remote socket closed (Read)!");
                        }

                        // send data to local socket
                        if (!sendLocal(dataBuffer, readCount)) {
                            throw new IOException("Local socket closed (Write)!");
                        }
                    } catch (SocketTimeoutException e) {
                        continue;
                    } catch (IOException e) {
                        logger.trace(e.getMessage(), e);
                        break;
                    }

                }
                close();
                logger.trace("remoteWorker exit, Local={}, Remote={}", localSocket, remoteSocket);
            }
        };
    }

    public void close() {
        if (isClosed) {
            return;
        }
        isClosed = true;

        try {
            localSocket.shutdownInput();
            localSocket.shutdownOutput();
            localSocket.close();
        } catch (IOException e) {
            logger.error("PipeSocket failed to close local socket (I/O exception)!");
        }
        try {
            if (remoteSocket != null) {
                remoteSocket.shutdownInput();
                remoteSocket.shutdownOutput();
                remoteSocket.close();
            }
        } catch (IOException e) {
            logger.error("PipeSocket failed to close remote socket (I/O exception)!");
        }
    }

    private boolean sendRemote(byte[] data, int length) {
        crypt.encrypt(data, length, remoteBaos);
        byte[] sendData = remoteBaos.toByteArray();

        return _sendRemote(sendData, sendData.length);
    }

    private boolean _sendRemote(byte[] data, int length) {
        try {
            logger.info("send to remote: {}", Hex.toHexString(data));
            if (length > 0) {
                OutputStream outStream = remoteSocket.getOutputStream();
                outStream.write(data, 0, length);
            }
            else {
                logger.info("Nothing to sendRemote!\n");
            }
        } catch (IOException e) {
            logger.info(e.getMessage(), e);
            return false;
        }

        return true;
    }

    private boolean sendLocal(byte[] data, int length) {
        crypt.decrypt(data, length, localBaos);
        byte[] sendData = localBaos.toByteArray();

        return _sendLocal(sendData, sendData.length);
    }

    private boolean _sendLocal(byte[] data, int length) {
        try {
            logger.info("send to local: {}", Hex.toHexString(data));
            OutputStream outStream = localSocket.getOutputStream();
            outStream.write(data, 0, length);
            outStream.flush();
        } catch (IOException e) {
            logger.info(e.getMessage(), e);
            return false;
        }
        return true;
    }

}
