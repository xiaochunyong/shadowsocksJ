package me.ely.shadowsocks;

import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import me.ely.shadowsocks.model.Config;
import me.ely.shadowsocks.model.Server;
import me.ely.shadowsocks.nio.LocalServer;
import me.ely.shadowsocks.utils.Constant;
import me.ely.shadowsocks.utils.NetworkSetup;
import me.ely.shadowsocks.view.QRCodeController;
import me.ely.shadowsocks.view.ServerPreferencesController;
import me.ely.shadowsocks.zxing.ZXingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by Ely on 07/12/2016.
 */
public class BootGUI extends Application {

    private static final Logger logger = LoggerFactory.getLogger(BootGUI.class);

    private Stage primaryStage;

    private TrayIcon trayIcon;

    public Config config;

    private LocalServer localServer;

    @Override
    public void start(final Stage primaryStage) throws Exception {
        Platform.setImplicitExit(false);
        this.primaryStage = primaryStage;
        this.primaryStage.initStyle(StageStyle.UTILITY);
        this.primaryStage.getIcons().add(new javafx.scene.image.Image(this.getClass().getResourceAsStream("/images/ios_128.png")));

        this.config = Config.loadConfig();
        this.localServer = new LocalServer(this.config);

//        设置Dock图标
//        boolean isMacOS = true;
//        if (isMacOS) {
//            com.apple.eawt.Application application = com.apple.eawt.Application.getApplication();
//            Image image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/images/ssw128.png"));
//            application.setDockIconImage(image);
//            application.setDockIconBadge("Shadowsocks");
//        }


        initTrayIcon();
        this.setTooltip("Shadowsocks 1.0.0\n系统代理已经启用: PAC模式\nvpn.ely.me (106.186.20.211:8388)");
    }

    private void initTrayIcon() {
        // ensure awt is initialized
        Toolkit.getDefaultToolkit();

        // make sure system tray is supported
        if (!SystemTray.isSupported()) {
            logger.warn("No system tray support!");
        }

        final SystemTray tray = SystemTray.getSystemTray();
        try {
            Image image = ImageIO.read(this.getClass().getResource("/images/2595d57708ce4d7aadd6394420716dd7-1.png"));
            trayIcon = new TrayIcon(image);
            trayIcon.setImageAutoSize(true);

            MenuItem statusLabel = new MenuItem(Constant.PROG_NAME + ": Off");
            statusLabel.setEnabled(false);
            MenuItem statusSwitcher = new MenuItem("Turn Shadowsocks On");
            statusSwitcher.addActionListener(e -> {
                if (localServer.isRunning()) {
                    localServer.stop();
                    statusLabel.setLabel(Constant.PROG_NAME + ": Off");
                    statusSwitcher.setLabel("Turn Shadowsocks On");
                } else {
                    localServer.start();
                    statusLabel.setLabel(Constant.PROG_NAME + ": On");
                    statusSwitcher.setLabel("Turn Shadowsocks Off");
                }
            });

            CheckboxMenuItem autoProxyMode = new CheckboxMenuItem("Auto Proxy Mode");
            CheckboxMenuItem globalMode = new CheckboxMenuItem("Global Mode", true);


            Menu serversItem = new Menu("Servers");
            MenuItem openServerPreferences = new MenuItem("Open Server Preferences");
            for (int i = 0; i < this.config.getConfigs().size(); i++) {
                Server serverConfig = this.config.getConfigs().get(i);
                boolean selected = this.config.getIndex() == i;
                serversItem.add(new CheckboxMenuItem(serverConfig.getHost() + ":" + serverConfig.getPort(), selected));
            }
            for (int i = 0; i < serversItem.getItemCount() - 2; i++) {

            }


            serversItem.addSeparator();
            serversItem.add(openServerPreferences);

            MenuItem editPacForAutoAutoProxyMode = new MenuItem("Edit PAC for Auto Proxy Mode...");
            MenuItem updatePACFromGWFList = new MenuItem("Update PAC from GWFList");
            MenuItem editUserRuleForGWFList = new MenuItem("Edit User Rule for GWFList...");

            MenuItem generateQrCodeItem = new MenuItem("Generate QR Code...");
            MenuItem scanQRFromScreenItem = new MenuItem("Scan QR Code from Screen...");

            MenuItem showLogMenu = new MenuItem("Show Logs...");
            MenuItem helpMenu = new MenuItem("Help");

            MenuItem quitItem = new MenuItem("Quit");



            autoProxyMode.addItemListener(e -> {
                globalMode.setState(false);
                NetworkSetup.enableAutoProxyConfiguration();
            });
            globalMode.addItemListener(e -> {
                autoProxyMode.setState(false);
                NetworkSetup.enableSocksFirewallProxy();
            });

            openServerPreferences.addActionListener(e -> Platform.runLater(this::showServerPreferences));


            generateQrCodeItem.addActionListener(e -> Platform.runLater(this::showQRCode));

            scanQRFromScreenItem.addActionListener(e -> {
                try {
                    int width = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();  //要截取的宽度
                    int height = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();  //要截取的高度
                    Robot robot = new Robot();
                    BufferedImage bufferedImage = robot.createScreenCapture(new Rectangle(width,height));

                    Result result = ZXingHelper.decode(bufferedImage);
                    String text = result.getText();

                    for (ResultPoint point : result.getResultPoints()) {
                        System.out.println(point.getX() + "," + point.getY());
                    }

                    double minX = result.getResultPoints()[0].getX();
                    double minY = result.getResultPoints()[0].getY();
                    double maxX = result.getResultPoints()[0].getX();
                    double maxY = result.getResultPoints()[0].getY();
                    for (int i = 1; i < result.getResultPoints().length; i++) {
                        ResultPoint temp = result.getResultPoints()[i];
                        if (temp.getX() > maxX) {
                            maxX = temp.getX();
                        }

                        if (temp.getY() > maxY) {
                            maxY = temp.getY();
                        }

                        if (temp.getX() < minX) {
                            minX = temp.getX();
                        }

                        if (temp.getY() < minY) {
                            minY = temp.getY();
                        }
                    }
                    double margin = (maxX - minX) * 0.2;
                    minX -= margin;
                    minY -= margin;
                    maxX += margin;
                    maxY += margin;
                    System.out.printf("LeftTopPoint: %f, %f", minX, minY);
                    System.out.printf("LeftBottomPoint: %f, %f", minX, maxY);
                    System.out.printf("RightTopPoint: %f, %f", maxX, minY);
                    System.out.printf("RightBottomPoint: %f, %f", maxX, maxY);


                    double x = minX;
                    double y = minY;
                    double w = maxX - minX;
                    double h = maxY - minY;

                    Platform.runLater(() -> {
                        scanQRCode(text, x, y, w, h);
                    });
                } catch (AWTException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (NotFoundException e1) {
                    e1.printStackTrace();
                    Platform.runLater(() -> {
                        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "No QRCode found. Try to zoom in or move it to the center of the screen.", ok);
                        alert.show();
                    });
                }
            });

            quitItem.addActionListener(e -> {
//                    controller.closeServer();
                SystemTray.getSystemTray().remove(trayIcon);
                Platform.exit();
            });

            PopupMenu popup = new PopupMenu();
            popup.add(statusLabel);
            popup.add(statusSwitcher);
            popup.addSeparator();
            popup.add(autoProxyMode);
            popup.add(globalMode);
            popup.addSeparator();
            popup.add(serversItem);
            popup.addSeparator();
            popup.add(editPacForAutoAutoProxyMode);
            popup.add(updatePACFromGWFList);
            popup.add(editUserRuleForGWFList);
            popup.addSeparator();
            popup.add(generateQrCodeItem);
            popup.add(scanQRFromScreenItem);
            popup.addSeparator();
            popup.add(showLogMenu);
            popup.add(helpMenu);
            popup.addSeparator();
            popup.add(quitItem);
            trayIcon.setPopupMenu(popup);
            trayIcon.setToolTip("Not Connected");
            tray.add(trayIcon);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private void scanQRCode(String text, double x, double y, double w, double h) {
        Stage stage = new Stage(StageStyle.TRANSPARENT);
        Group root = new Group();
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = primaryScreenBounds.getWidth();
        double screenHeight = primaryScreenBounds.getHeight();

        double toX = w / screenWidth;
        double toY = h / screenHeight;

        Scene scene = new Scene(root, screenWidth, screenHeight, javafx.scene.paint.Color.BLACK);
        scene.setFill(null);

        javafx.scene.shape.Rectangle r = new javafx.scene.shape.Rectangle(0, 0, screenWidth, screenHeight);
        r.setStroke(javafx.scene.paint.Color.RED);
        r.setStrokeWidth(5);
        r.setOpacity(0.3);
        root.getChildren().add(r);

        TranslateTransition translate = new TranslateTransition(Duration.millis(750));
        translate.setToX(x - (screenWidth - w) / 2);
        translate.setToY(y - (screenHeight - h) / 2);

//        FillTransition fill = new FillTransition(Duration.millis(750));
//        fill.setToValue(Color.RED);
//
//        RotateTransition rotate = new RotateTransition(Duration.millis(750));
//        rotate.setToAngle(360);

        ScaleTransition scale = new ScaleTransition(Duration.millis(750));
        scale.setToX(toX);
        scale.setToY(toY);

//        SequentialTransition transition = new SequentialTransition(r, translate, scale);
        ParallelTransition transition = new ParallelTransition(r, translate, scale);
        transition.setCycleCount(1);
        transition.setAutoReverse(true);
        transition.play();
        transition.setOnFinished(value -> {

            Platform.runLater(() -> {
                stage.close();
                Platform.runLater(() -> {
                    ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                    ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, text, cancel, ok);
                    alert.setTitle("Use this Server?");
                    alert.setHeaderText("Use this Server?");
                    alert.initOwner(primaryStage);
                    alert.show();
                });
            });



        });

        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.show();

//        Platform.runLater();
//        new Thread(() -> {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        }).start();

    }

    private void showQRCode() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("view/QRCode.fxml"));
            Pane pane = loader.load();

            Scene scene = new Scene(pane);
            primaryStage.setScene(scene);


            QRCodeController controller = loader.getController();

            primaryStage.setTitle("QR Code");
            primaryStage.setAlwaysOnTop(false);
            primaryStage.show();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void showServerPreferences() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("view/ServerPreferences.fxml"));
            AnchorPane anchorPane = loader.load();

            Scene scene = new Scene(anchorPane);
            primaryStage.setScene(scene);


            ServerPreferencesController controller = loader.getController();
            controller.setApp(this);

            primaryStage.setTitle("Server Preferences");
            primaryStage.show();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void hide() {
        primaryStage.close();
    }

    public void setTooltip(String message) {
        if (trayIcon != null) {
            trayIcon.setToolTip(message);
        }
    }

    public void showNotification(String message) {
        Platform.runLater(() -> trayIcon.displayMessage(
                "shadowsocks-java",
                message,
                TrayIcon.MessageType.INFO
        ));
    }

    public static void main(String[] args) {
        launch(args);
    }

}
