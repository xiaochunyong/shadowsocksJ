package me.ely.shadowsocks.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.ely.shadowsocks.view.MyCanvas;

/**
 *
 * @author Alan
 */
public class TransparentStage extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button();
        btn.setText("显示提示");

        btn.setOnAction((ActionEvent event) -> {
//            Text text = new Text("显示提示信息成功!");
//            text.setFont(new Font(20));
//            text.setFill(Color.GREEN);
//            VBox box = new VBox();
//            box.getChildren().add(text);
//            box.setStyle("-fx-background:transparent;");

            Platform.runLater(() -> {
                int width = 800;
                int height = 800;

                MyCanvas canvas = new MyCanvas(width , height);
                StackPane group = new StackPane(canvas);
                group.setStyle("-fx-border-radius:8px;-fx-opacity: 0.4;-fx-background-color: black ;");

                Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                final Scene scene = new Scene(group, width, height);
                scene.setFill(null);

                final Stage stage = new Stage(StageStyle.TRANSPARENT);
                stage.setScene(scene);

                stage.setX(0);
                stage.setY(0);
                stage.show();

//                while (width > 400) {
//                    width -= 10;
//                    height -= 10;
//                    System.out.println(width +"，" + height);
//                            canvas.setWidth(width--);
//                            canvas.setHeight(height--);
//                    canvas.drawRec(width, height);
//                    stage.show();
//                    try {
//                        System.in.read();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                stage.close();

            });

//            int width = 800;
//            int height = 800;
//
//                MyCanvas canvas = new MyCanvas(width , height);
//                StackPane group = new StackPane(canvas);
//                group.setStyle("-fx-border-radius:8px;-fx-opacity: 0.4;-fx-background-color: black ;");
//
//                Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
//                final Scene scene = new Scene(group, width, height);
//                scene.setFill(null);
//
//                final Stage stage = new Stage(StageStyle.TRANSPARENT);
//                stage.setScene(scene);
//
//                stage.setX(0);
//                stage.setY(0);
//                stage.show();

//                        while (width > 400) {
//                            width -= 10;
//                            height -= 10;
//                            System.out.println(width +"，" + height);
////                            canvas.setWidth(width--);
////                            canvas.setHeight(height--);
//                            canvas.drawRec(width, height);
//                            try {
//                                Thread.sleep(50);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        stage.close();

//                Task t = new Task() {
//                    @Override
//                    protected Object call() throws Exception {
//                        Thread.sleep(1000);
//                        Platform.runLater(stage::close);
//                        return "";
//                    }
//                };
//                try {
//
//                    Thread thread = new Thread(t);
//                    thread.start();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                width -= 100;
//            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}