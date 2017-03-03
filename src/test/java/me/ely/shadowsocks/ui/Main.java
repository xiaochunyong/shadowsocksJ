package me.ely.shadowsocks.ui;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
 
public class Main extends Application { 
 
    @Override 
    public void start(Stage stage) {
//        stage.initStyle(StageStyle.TRANSPARENT);
        Group root = new Group();
        //

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = primaryScreenBounds.getWidth();
        double screenHeight = primaryScreenBounds.getHeight();

        double toX = 350 / screenWidth;
        double toY = 350 / screenHeight;

        double x = 870;
        double y = 267;

        Scene scene = new Scene(root, screenWidth, screenHeight, Color.BLACK);
        scene.setFill(null);

        Rectangle r = new Rectangle(0, 0, screenWidth, screenHeight);
        r.setStroke(Color.RED);
        r.setOpacity(0.3);
        r.setEffect(new DropShadow());
        root.getChildren().add(r);



        TranslateTransition translate = new TranslateTransition(Duration.millis(750));
        translate.setToX(x - (screenWidth - 350) / 2);
        translate.setToY(y - (screenHeight - 350) / 2);

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

        stage.setTitle("JavaFX Scene Graph Demo"); 
        stage.setScene(scene); 
        stage.show();
    }
 
    public static void main(String[] args) { 
        launch(args); 
    } 
}