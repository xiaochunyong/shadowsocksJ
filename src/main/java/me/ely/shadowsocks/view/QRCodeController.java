package me.ely.shadowsocks.view;

import com.google.zxing.WriterException;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import me.ely.shadowsocks.model.Config;
import me.ely.shadowsocks.zxing.ZXingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by ely on 14/12/2016.
 */
public class QRCodeController {

    private static final Logger logger = LoggerFactory.getLogger(QRCodeController.class);

    @FXML
    Pane pane;

    @FXML
    private void initialize() {
        BufferedImage bf = null;
        try {
            bf = ZXingHelper.encode(Config.getConfig().getCurrentServer().toBase64URI());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (WriterException e) {
            logger.error(e.getMessage(), e);
        }


        WritableImage wr = null;
        if (bf != null) {
            wr = new WritableImage(bf.getWidth(), bf.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < bf.getWidth(); x++) {
                for (int y = 0; y < bf.getHeight(); y++) {
                    pw.setArgb(x, y, bf.getRGB(x, y));
                }
            }
        }

        pane.getChildren().add(new ImageView(wr));
    }

}
