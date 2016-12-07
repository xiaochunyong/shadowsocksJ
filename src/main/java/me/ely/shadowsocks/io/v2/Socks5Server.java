package me.ely.shadowsocks.io.v2;

import me.ely.shadowsocks.crypt.AESCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Ely on 04/12/2016.
 */
public class Socks5Server {

    private static final Logger logger = LoggerFactory.getLogger(Socks5Server.class);

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1081);
        while (true) {
            AESCrypt crypt = new AESCrypt("aes-256-cfb", "7YdKNioZcKSPpK73");
            Socket socket = serverSocket.accept();
            socket.setSoTimeout(10 * 1000);
            Socket remoteSocket = new Socket("106.186.20.211", 443);
            remoteSocket.setSoTimeout(10 * 1000);
            logger.info("new request coming...");
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            InputStream remoteIn = remoteSocket.getInputStream();
            OutputStream remoteOut = remoteSocket.getOutputStream();

            String READ_FROM_LOCAL = "read from local";
            String WRITE_TO_LOCAL = "write to local";
            String READ_FROM_REMOTE = "read from remote";
            String WRITE_TO_REMOTE = "write to remote";

            byte[] data = read(READ_FROM_LOCAL, in);                                // 协商请求
            write(WRITE_TO_LOCAL,  out, new byte[]{5, 0});                          // 协商响应
            data = read(READ_FROM_LOCAL, in);                                       // REQUEST
            write(WRITE_TO_LOCAL, out, new byte[]{5, 0, 0, 1, 0, 0, 0, 0, 0, 0});   // RESPONSE


            //remove socks5 header
            byte[] newData = new byte[data.length - 3];
            System.arraycopy(data, 3, newData, 0, newData.length);
            write(WRITE_TO_REMOTE, remoteOut, crypt.encrypt(newData));

            // 请求方，代理方，执行方 传输
            data = read(READ_FROM_LOCAL, in);
            write(WRITE_TO_REMOTE, remoteOut, crypt.encrypt(data));
            data = read(READ_FROM_REMOTE, remoteIn);
            if (data != null) {
                write(WRITE_TO_LOCAL, out, crypt.decrypt(data));
            }

            remoteSocket.close();
            socket.close();
        }
    }

    public static byte[] read(String PREFIX, InputStream in) throws IOException {
        byte[] buffer = new byte[1024 * 16];
        int readCount = in.read(buffer);
        if (readCount != -1) {
            byte[] data = new byte[readCount];
            System.arraycopy(buffer, 0, data, 0, readCount);
            logger.info("{}[{}]: {}", PREFIX, data.length, unsignedInteger(data));
//            logger.info("{}: {}", PREFIX, Hex.toHexString(data));
            return data;
        } else {
            logger.info("readCount is {}", readCount);
            return null;
        }

    }

    public static void write(String PREFIX, OutputStream out, byte[] data) throws IOException {
        logger.info("{}[{}]: {}", PREFIX, data.length, unsignedInteger(data));
//        logger.info("{}: {}", me, Hex.toHexString(data));
        out.write(data);
        out.flush();
    }

    public static String unsignedInteger(byte[] data) {
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0; data != null && i < data.length; i++) {
            if (data[i] >= 0) {
                sb.append(data[i]);
            } else {
                sb.append(256 + data[i]);
            }
            if (i != data.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

}