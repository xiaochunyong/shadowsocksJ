package me.ely.shadowsocks.model;

import java.util.Base64;

/**
 * Created by Ely on 09/12/2016.
 */
public class Server {

    private String host;

    private int port = 8388;

    private String password;

    private String method;

    private String remark;

    public Server() {
    }

    public Server(String host, int port, String password, String method, String remark) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.method = method;
        this.remark = remark;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return String.format("%s:%s@%s:%s", this.getMethod(), this.getPassword(), this.getHost(), this.getPort());
    }

    public String toPlainURI() {
        return "ss://" + this.toString();
    }

    public String toBase64URI() {
        return String.format("ss://%s#%s", Base64.getEncoder().encodeToString(this.toString().getBytes()), this.getRemark());
    }


}
