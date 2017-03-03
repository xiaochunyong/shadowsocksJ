package me.ely.shadowsocks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

/**
 * Created by Ely on 12/12/2016.
 */
public class GFWList {

    private static final Logger logger = LoggerFactory.getLogger(GFWList.class);

    public static void main(String[] args) throws URISyntaxException, IOException {
        List<String> lines = Files.readAllLines(Paths.get(GFWList.class.getResource("/gfwlist.txt").toURI()));
        System.out.println(new String(Base64.getDecoder().decode(String.join("", lines))));
    }

}
