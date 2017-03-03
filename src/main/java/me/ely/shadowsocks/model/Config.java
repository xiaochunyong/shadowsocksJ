package me.ely.shadowsocks.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import me.ely.shadowsocks.utils.Constant;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ely on 29/11/2016.
 */
public class Config {

    private String localHost = "127.0.0.1";

    private int localPort = 1080;

    private int index;

    private List<Server> configs = new ArrayList<>();

    public Config() { }

    public Config(String localHost, int localPort, Server config) {
        this.localHost = localHost;
        this.localPort = localPort;
        this.configs.add(config);
    }

    @JSONField(serialize = false)
    public Server getCurrentServer() {
        if (index >= 0 && i < configs.size()) {
            return configs.get(i);
        } else {
            return new Server();
        }
    }

    @JSONField(serialize = false)
    public String getRemark() {
        return getCurrentServer().getRemark();
    }

    @JSONField(serialize = false)
    public String getRemoteHost() {
        return getCurrentServer().getHost();
    }

    @JSONField(serialize = false)
    public int getRemotePort() {
        return getCurrentServer().getPort();
    }

    @JSONField(serialize = false)
    public String getPassword() {
        return getCurrentServer().getPassword();
    }

    @JSONField(serialize = false)
    public String getMethod() {
        return getCurrentServer().getMethod();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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

    public List<Server> getConfigs() {
        return configs;
    }

    public void setConfigs(List<Server> configs) {
        this.configs = configs;
    }

    public static int i = 0;
    public static Config getConfig() {
        return new Config("127.0.0.1", 1081, new Server("106.186.20.211", 443, "7YdKNioZcKSPpK73", "aes-256-cfb", "vpn.ely.me"));
//        return new Config("127.0.0.1", 8388, "7YdKNioZcKSPpK73", "aes-256-cfb", "127.0.0.1", 1081, "vpn.ely.me" + (++i));
    }

    public static Config loadConfig() {
        try {
            StringBuffer content = new StringBuffer();
//            Files.readAllLines(Paths.get(Constant.CONF_FILE), StandardCharsets.UTF_8).forEach(line -> content.append(line + "\n"));
            Files.lines(Paths.get(Constant.CONF_FILE), StandardCharsets.UTF_8).forEach(line -> content.append(line + "\n"));
            Config config =  JSON.parseObject(content.toString(), Config.class);
            if (config != null) {

            }
            return config;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Config();
    }

    public static void saveConfig(Config config) {
        try {
            String data = JSON.toJSONString(config, SerializerFeature.PrettyFormat);
            Files.write(Paths.get(Constant.CONF_FILE), data.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public String toPlainURI() {
        return String.format("ss://{}:{}@{}:{}", this.getMethod(), this.getPassword(), this.getRemoteHost(), this.getRemotePort());
    }

}
