package me.ely.shadowsocks.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * http://devework.com/automatic-proxy-configuration-pac-applescript.html
 * Created by Ely on 12/12/2016.
 */
public class NetworkSetup {

    private static final Logger logger = LoggerFactory.getLogger(NetworkSetup.class);

    public static final String CMD_GET_AUTO_PROXY_URL = "networksetup -getautoproxyurl <networkservice>";

    public static final String CMD_SET_AUTO_PROXY_URL = "networksetup -setautoproxyurl <networkservice> <url>";

    public static final String CMD_SET_AUTO_PROXY_STATE = "networksetup -setautoproxystate <networkservice> <on off>";

    public static final String CMD_GET_SOCKS_FIREWALL_PROXY = "networksetup -getsocksfirewallproxy <networkservice>";

    public static final String CMD_SET_SOCKS_FIREWALL_PROXY = "networksetup -setsocksfirewallproxy <networkservice> <domain> <port number> <authenticated> <username> <password>";

    public static final String CMD_SET_SOCKS_FIREWALL_PROXY_STATE = "networksetup -setsocksfirewallproxystate <networkservice> <on off>";


    public static String getAutoProxyURL(String networkservice) throws IOException {
        return exec(CMD_GET_AUTO_PROXY_URL, networkservice);
    }

    public static String setAutoProxyURL(String networkservice, String url) throws IOException {
        return exec(CMD_SET_AUTO_PROXY_URL, networkservice, url);
    }

    public static String enableAutoProxyURL(String networkservice) throws IOException {
        return exec(CMD_SET_AUTO_PROXY_STATE, networkservice, "on");
    }

    public static String disableAutoProxyURL(String networkservice) throws IOException {
        return exec(CMD_SET_AUTO_PROXY_STATE, networkservice, "off");
    }

    public static String getSocksFirewallProxyURL(String networkservice) throws IOException {
        return exec(CMD_GET_SOCKS_FIREWALL_PROXY, networkservice);
    }

    public static String setSocksFirewallProxyURL(String networkservice, String domain, String port) throws IOException {
        return exec(CMD_SET_SOCKS_FIREWALL_PROXY, networkservice, domain, port);
    }

    public static String enableSocksFirewallProxyURL(String networkservice) throws IOException {
        return exec(CMD_SET_SOCKS_FIREWALL_PROXY_STATE, networkservice, "on");
    }

    public static String disableSocksFirewallProxyURL(String networkservice) throws IOException {
        return exec(CMD_SET_SOCKS_FIREWALL_PROXY_STATE, networkservice, "off");
    }

    public static void main(String[] args) throws IOException {
//        setAutoProxyURL("Wi-Fi", "http://127.0.0.1:8090/proxy.pac");
//        disableAutoProxyURL("Wi-Fi");
//        enableAutoProxyURL("Wi-Fi");
//        System.out.println(getAutoProxyURL("Wi-Fi"));

//        setSocksFirewallProxyURL("Wi-Fi", "127.0.0.1", "1080");
//        disableSocksFirewallProxyURL("Wi-Fi");
//        enableSocksFirewallProxyURL("Wi-Fi");
//        System.out.println(getSocksFirewallProxyURL("Wi-Fi"));

//        enableSocksFirewallProxy();
        enableAutoProxyConfiguration();
    }

    public static void enableAutoProxyConfiguration() {
        try {
            disableSocksFirewallProxyURL("Wi-Fi");
            setSocksFirewallProxyURL("Wi-Fi", "", "");

            setAutoProxyURL("Wi-Fi", "http://127.0.0.1:8090/proxy.pac");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void enableSocksFirewallProxy() {
        try {
            disableAutoProxyURL("Wi-Fi");
            setAutoProxyURL("Wi-Fi", "");

            setSocksFirewallProxyURL("Wi-Fi", "127.0.0.1", "1080");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static String convert(String command) {
        return command.replaceAll("<.*?>", "%s");
    }

    private static int indexOfCount(String command) {
        int count = 0;
        int fromIndex = 0;
        int i = -1;
        while ((i = command.indexOf("%s", fromIndex)) != -1) {
            count++;
            fromIndex = i + 2;
        }
        return count;
    }

    public static String exec(String cmd, String... args) throws IOException {
        String command1 = convert(cmd);
        int count = indexOfCount(command1);
        List<String> argList = new ArrayList<>();
        argList.addAll(Arrays.asList(args));

        while (argList.size() < count) {
            argList.add("");
        }
        String command = String.format(command1, argList.toArray());
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

}
