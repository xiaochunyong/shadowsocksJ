package me.ely.shadowsocks.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ely on 29/11/2016.
 */
public class Socks5Protocol implements IProtocol {

    private static final Logger logger = LoggerFactory.getLogger(Socks5Protocol.class);

    public final static int VER = 0x5;

    public final static int CMD_CONNECT = 0x1;
    public final static int CMD_BIND = 0x2;
    public final static int CMD_UDP_ASSOCIATE = 0x3;

    public final static int RSV = 0x0;

    public final static int ATYP_IP_V4 = 0x1;
    public final static int ATYP_DOMAIN_NAME = 0x3;
    public final static int ATYP_IP_V6 = 0x4;

    public final static int REP_SUCCEEDED = 0x0;
    public final static int REP_GENERAL_SOCKS_SERVER_FAILURE = 0x1;
    public final static int REP_CONNECTION_NOT_ALLOWED_BY_RULESET = 0x2;
    public final static int REP_NETWORK_UNREACHABLE = 0x3;
    public final static int REP_HOST_UNREACHABLE = 0x4;
    public final static int REP_CONNECTION_REFUSED = 0x5;
    public final static int REP_TTL_EXPIRED = 0x6;
    public final static int REP_COMMAND_NOT_SUPPORTED = 0x7;
    public final static int REP_ADDRESS_TYPE_NOT_SUPPORTED = 0x8;
    public final static int REP_XFF_UNASSIGNED = 0x9;


    private enum STAGE {SOCK5_HELLO, SOCKS_ACK, SOCKS_READY}

    private STAGE stage;

    public final static int STAGE_HELLO = 0x1;
    public final static int STAGE_ACK = 0x2;
    public final static int STAGE_READY = 0x3;

    public Socks5Protocol() {
        this.stage = STAGE.SOCK5_HELLO;
    }

    @Override
    public boolean isReady() {
        return stage == STAGE.SOCKS_READY;
    }

    @Override
    public TYPE getType() {
        return TYPE.SOCKS5;
    }

    @Override
    public byte[] getResponse(byte[] data) {
        byte[] respData = null;
        switch (stage) {
            case SOCK5_HELLO:
                if (isMine(data)) {
                    respData = new byte[]{5, 0};
                } else {
                    respData = new byte[]{0, 91};
                }
                stage = STAGE.SOCKS_ACK;
                break;
            case SOCKS_ACK:
                respData = new byte[] {5, 0, 0, 1, 0, 0, 0, 0, 0, 0};
                stage = STAGE.SOCKS_READY;
            break;
            default:
                // TODO exception
                break;
        }
        return respData;
    }

    @Override
    public List<byte[]> getRemoteResponse(byte[] data) {
        List<byte[]> respData = null;

        /*
        There are two stage of establish Sock5:
            1. HELLO (3 bytes)
            2. ACK (3 bytes + dst info)
        as Client sending ACK, it might contain dst info.
        In this case, server needs to send back ACK response to client and start the remote socket right away,
        otherwise, client will wait until timeout.
         */
        if (stage == STAGE.SOCKS_READY) {
            respData = new ArrayList<>(1);
            // remove socks5 header (partial)
            if (data.length > 3) {
                byte[] temp = new byte[data.length - 3];
                System.arraycopy(data, 3, temp, 0, temp.length);
                respData.add(temp);
            }
        }
        return respData;
    }

    @Override
    public boolean isMine(byte[] data) {
        if (data[0] == 0x5) {
            return true;
        }
        return false;
    }
}
