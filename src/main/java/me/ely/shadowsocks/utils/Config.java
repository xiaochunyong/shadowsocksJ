package me.ely.shadowsocks.utils;

/**
 * Created by Ely on 29/11/2016.
 */
public class Config {

    private String remoteHost;

    private int remotePort;

    private String password;

    private String method;

    private String localHost;

    private int localPort;

    private String protocol;

    public Config() { }

    public Config(String remoteHost, int remotePort, String password, String method, String localHost, int localPort, String protocol) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.password = password;
        this.method = method;
        this.localHost = localHost;
        this.localPort = localPort;
        this.protocol = protocol;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
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

    public String getLocalHost() {
        return localHost;
    }

    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public static Config getConfig() {
//        return new Config("106.186.20.211", 443, "7YdKNioZcKSPpK73", "aes-256-cfb", "127.0.0.1", 1081, "SOCKS5");
        return new Config("127.0.0.1", 8388, "7YdKNioZcKSPpK73", "aes-256-cfb", "127.0.0.1", 1081, "SOCKS5");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Config{");
        sb.append("remoteHost='").append(remoteHost).append('\'');
        sb.append(", remotePort=").append(remotePort);
        sb.append(", password='").append(password).append('\'');
        sb.append(", method='").append(method).append('\'');
        sb.append(", localHost='").append(localHost).append('\'');
        sb.append(", localPort=").append(localPort);
        sb.append(", protocol='").append(protocol).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
