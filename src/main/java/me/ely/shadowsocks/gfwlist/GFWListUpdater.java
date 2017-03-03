package me.ely.shadowsocks.gfwlist;

import com.alibaba.fastjson.JSON;
import me.ely.shadowsocks.Boot;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * Created by ely on 15/12/2016.
 */
public class GFWListUpdater {

    private static final Logger logger = LoggerFactory.getLogger(GFWListUpdater.class);

    public static String GFWLIST_URL = "https://raw.githubusercontent.com/gfwlist/gfwlist/master/gfwlist.txt";

    public static void update() throws IOException {
        Map<String, String> config = getDefaultConfig();

        String content = decodeGFWList(loadGFWList());
        if (content != null) {
//            logger.info(content);

            Rules rules = parseRuleList(content);
            config.put("directRegexpList", JSON.toJSONString(rules.getDirectRegexpList(), true));
            config.put("directWildcardList", JSON.toJSONString(rules.getDirectWildcardList(), true));
            config.put("proxyRegexpList", JSON.toJSONString(rules.getProxyRegexpList(), true));
            config.put("proxyWildcardList", JSON.toJSONString(rules.getProxyWildcardList(), true));

            String pacContent = createPacFile(config);
            FileWriter fw = new FileWriter(new File("my-gfwlist.js"));
            fw.write(pacContent);
            fw.flush();
            fw.close();
//            System.out.println(pacContent);

        }
    }

    public static String decodeGFWList(String gfwlist) {
        if (gfwlist.contains(".")) {
            return gfwlist;
        } else {
            return new String(Base64.decode(gfwlist));
        }
    }

    public static String getHostname(String uri) {
        if (!uri.startsWith("http:")) {
            uri = "http://" + uri;
        }
        try {
            return new URL(uri).getHost();
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static void addDomainToSet(Set<String> s, String uri) {
        String hostname = getHostname(uri);
        if (hostname != null) {
            if (hostname.startsWith(".")) {
                hostname = hostname.substring(1);
            }
            if (hostname.endsWith("/")) {
                hostname = hostname.substring(0, hostname.length() - 1);
            }
            if (hostname.length() > 0) {
                s.add(hostname);
            }
        }
    }


    public static Set<String> parseGFWList(String content, String userRule) {
        List<String> gfwlist = new ArrayList<>();
        gfwlist.addAll(Arrays.asList(content.split("\n")));
        gfwlist.addAll(Arrays.asList(userRule.split("\n")));

        Set<String> domains = new HashSet<>();
        domains.addAll(Arrays.asList(readFile("buildin.txt").split("\n")));

        for (String line : gfwlist) {
            if (line.contains(".*")) {
                continue;
            } else if (line.contains("*")) {
                line = line.replace("*", "/");
            }

            if (line.startsWith("!")) {
                continue;
            } else if (line.startsWith("[")) {
                continue;
            } else if (line.startsWith("@")) {
                // ignore white list
                continue;
            } else if (line.startsWith("||")) {
                addDomainToSet(domains, line.substring(2));
            } else if (line.startsWith("|")) {
                addDomainToSet(domains, line.substring(1));
            } else if (line.startsWith(".")) {
                addDomainToSet(domains, line.substring(1));
            } else {
                addDomainToSet(domains, line);
            }
        }
        return domains;
    }

    public static Set<String> reduceDomains(Set<String> domains) {
        // reduce 'www.google.com' to 'google.com'
        // remove invalid domains
        Set<String> tlds = new HashSet<>();
        tlds.addAll(Arrays.asList(readFile("tld.txt").split("\n")));

        Set<String> newDomains = new HashSet<>();
        for (String domain : domains) {
            String[] domainParts = domain.split("\\.");
            String lastRootDomain = null;
            for (int i = 0; i < domainParts.length; i++) {
                String rootDomain = String.join(".", sub(domainParts, domainParts.length - i - 1, domainParts.length));
                if (i == 0) {
                    if (!tlds.contains(rootDomain)) {
                        // root_domain is not a valid tld
                        break;
                    }
                }
                lastRootDomain = rootDomain;
                if (tlds.contains(rootDomain)) {
                    continue;
                } else {
                    break;
                }
            }

            if (lastRootDomain != null) {
                newDomains.add(lastRootDomain);
            }
        }
        return newDomains;
    }

    public static String[] sub(String[] arr, int start, int end) {
        String[] newArr = new String[end - start];
        for (int i = start,j = 0; i < end; i++, j++) {
            newArr[j] = arr[i];
        }
        return newArr;
    }

    public static String generatePAC(Set<String> domains, String proxy) {
        String pacContent = readFile("proxy.pac");
        Map<String, Integer> domainMap = new HashMap<>();
        for (String domain : domains) {
            domainMap.put(domain, 1);
        }
        pacContent = pacContent.replace("__PROXY__", proxy);
        pacContent = pacContent.replace("__DOMAINS__", JSON.toJSONString(domainMap, true));

        return pacContent;
    }

    public static Map<String, String> getDefaultConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("ver", "1.0.0");
        config.put("generated", "Mon, 19 Dec 2016 07:14:17 GMT");
        config.put("gfwmodified", "None");
        config.put("proxy", "SOCKS5 127.0.0.1:1080; SOCKS 127.0.0.1:1080; DIRECT;");
        config.put("directUserRegexpList", "[]");
        config.put("directUserWildcardList", "[]");
        config.put("proxyUserRegexpList", "[]");
        config.put("proxyUserWildcardList", "[]");
        config.put("directRegexpList", "[]");
        config.put("directWildcardList", "[]");
        config.put("proxyRegexpList", "[]");
        config.put("proxyWildcardList", "[]");
        return config;
    }

    public static Rules parseRuleList(String ruleList) throws IOException {
        FileChannel channel = new FileOutputStream("my-debug_rule.txt").getChannel();

        Rules rules = new Rules();
        List<String> directWildcardList = new ArrayList<>();
        List<String> directRegexpList = new ArrayList<>();
        List<String> proxyWildcardList = new ArrayList<>();
        List<String> proxyRegexpList = new ArrayList<>();
        String[] lines = ruleList.split("\n");
        for (String line : lines) {
            if (line.length() == 0 || line.startsWith("!") || line.startsWith("[")) {
                continue;
            }

            boolean isDirect = false;
            boolean isRegexp = true;
            String originLine = line;

            if (line.startsWith("@@")) {
                line = line.substring(2);
                isDirect = true;
            }

            if (line.startsWith("/") && line.endsWith("/")) {
                line = line.substring(1, line.length() - 1);
            } else if (line.contains("^")) {
                line = wildcardToRegexp(line.substring(2));
                line = line.replaceAll("\\\\\\^", "(?:[^\\w\\-.%\u0080-\uFFFF]|$)");
            } else if (line.startsWith("||")) {
                line = wildcardToRegexp(line.substring(2));
                // When using the constructor function, the normal string escape rules (preceding
                // special characters with \ when included in a string) are necessary.
                // For example, the following are equivalent:
                // re = new RegExp("\\w+")
                // re = /\w+/
                // via: http://aptana.com/reference/api/RegExp.html
                line = "^[\\\\w\\\\-]+:\\\\/+(?!\\\\/)(?:[^\\\\/]+\\\\.)?" + line;
            } else if (line.startsWith("|") || line.endsWith("|")) {
                line = wildcardToRegexp(line);
                line = line.replaceFirst("^\\\\\\|", "^");
                line = line.replaceAll("\\\\\\|$", "$");
            } else {
                isRegexp = false;
            }


            if (!isRegexp) {
                if (!line.startsWith("*")) {
                    line = "*" + line;
                }
                if (!line.endsWith("*")) {
                    line += "*";
                }
            }

            if (isDirect) {
                if (isRegexp) {
                    directRegexpList.add(line);
                } else {
                    directWildcardList.add(line);
                }
            } else {
                if (isRegexp) {
                    proxyRegexpList.add(line);
                } else {
                    proxyWildcardList.add(line);
                }
            }

            boolean isDebug = true;
            if (isDebug) {
                channel.write(ByteBuffer.wrap(String.format("%s\n\t%s\n\n", originLine, line).getBytes()));
            }


        }
        rules.setDirectRegexpList(directRegexpList);
        rules.setDirectWildcardList(directWildcardList);
        rules.setProxyRegexpList(proxyRegexpList);
        rules.setProxyWildcardList(proxyWildcardList);
        channel.close();
        return rules;
    }

    public static String wildcardToRegexp(String pattern) {
        pattern = pattern.replaceAll("([\\\\\\+\\|\\{\\}\\[\\]\\(\\)\\^\\$\\.\\#])", "\\\\$1");
        pattern = pattern.replaceAll("\\*", ".*");
        pattern = pattern.replaceAll("\\？", ".");
        return pattern;
    }

    public static String createPacFile(Map<String, String> config) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(GFWListUpdater.class.getResourceAsStream("pac.tpl.txt")));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        reader.close();

        String pacTemplateContent = sb.toString();
        for (String key : config.keySet()) {
            pacTemplateContent = pacTemplateContent.replaceFirst("%\\(" + key + "\\)s", config.get(key));
        }
        return pacTemplateContent;
    }

    public static String readFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(GFWListUpdater.class.getResourceAsStream(filename)));) {
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            return sb.toString();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    public static String updateGFWList() throws IOException {
        try {
            URL url = new URL(GFWLIST_URL);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            InputStream in = con.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            con.disconnect();
            return sb.toString();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static String loadGFWList() throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(GFWListUpdater.class.getResourceAsStream("gfwlist.txt")));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        update();
//        String content = createPacFile(getDefaultConfig());
//        System.out.println(content);
    }

}

class Rules {

    private List<String> directWildcardList = new ArrayList<>();

    private List<String> directRegexpList = new ArrayList<>();

    private List<String> proxyWildcardList = new ArrayList<>();

    private List<String> proxyRegexpList = new ArrayList<>();

    public List<String> getDirectWildcardList() {
        return directWildcardList;
    }

    public void setDirectWildcardList(List<String> directWildcardList) {
        this.directWildcardList = directWildcardList;
    }

    public List<String> getDirectRegexpList() {
        return directRegexpList;
    }

    public void setDirectRegexpList(List<String> directRegexpList) {
        this.directRegexpList = directRegexpList;
    }

    public List<String> getProxyWildcardList() {
        return proxyWildcardList;
    }

    public void setProxyWildcardList(List<String> proxyWildcardList) {
        this.proxyWildcardList = proxyWildcardList;
    }

    public List<String> getProxyRegexpList() {
        return proxyRegexpList;
    }

    public void setProxyRegexpList(List<String> proxyRegexpList) {
        this.proxyRegexpList = proxyRegexpList;
    }

}