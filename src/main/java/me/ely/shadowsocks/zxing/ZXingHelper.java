package me.ely.shadowsocks.zxing;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ely on 13/12/2016.
 */
public class ZXingHelper {

    private static final Logger logger = LoggerFactory.getLogger(ZXingHelper.class);

    public static void main(String[] args) throws WriterException, IOException, NotFoundException {
//        encode(Config.getConfig().getCurrentServer().toBase64URI());
        decode(Paths.get(String.format("%s/1.png", System.getProperty("user.home"))).toString());
    }

    public static Result decode(String path) throws NotFoundException, IOException {
        return decode(ImageIO.read(new File(path)));
    }

    public static Result decode(BufferedImage image) throws NotFoundException, IOException {
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        Binarizer binarizer = new HybridBinarizer(source);
        BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
        Map<DecodeHintType, String> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

        Result result = new MultiFormatReader().decode(binaryBitmap, hints);
        return result;
    }

    public static BufferedImage encode(String content) throws IOException, WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, "H");
        hints.put(EncodeHintType.MARGIN, "1");

        int width = 350;
        int height = 350;
        String format = "png";
        Path outPath = Paths.get(String.format("%s/QR.jpg", System.getProperty("user.home")));
        BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        MatrixToImageWriter.writeToPath(matrix, format, outPath);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

}
