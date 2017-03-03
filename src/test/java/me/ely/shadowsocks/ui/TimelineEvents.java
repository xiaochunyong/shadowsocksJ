package me.ely.shadowsocks.ui;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;
 
public class TimelineEvents extends Application {
    
    //主时间轴
    private Timeline timeline;
    private AnimationTimer timer;
 
    //用于指定实际帧的变量
    private Integer i=0;
 
    @Override public void start(Stage stage) {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = primaryScreenBounds.getWidth();
        double screenHeight = primaryScreenBounds.getHeight();

        Group p = new Group();
        Scene scene = new Scene(p);
        stage.setScene(scene);
        stage.setWidth(screenWidth);
        stage.setHeight(screenHeight);
        p.setTranslateX(0);
        p.setTranslateY(0);



        double toX = 350 / screenWidth;
        double toY = 350 / screenHeight;
 
        //创建一个带有特效的圆
        Rectangle r = new Rectangle(0, 0, screenWidth, screenHeight);
        r.setStroke(Color.RED);
        r.setOpacity(0.3);

        //为带有文本的圆创建一个布局
        final StackPane stack = new StackPane();
        stack.getChildren().addAll(r);
        stack.setLayoutX(0);
        stack.setLayoutY(0);
 
        p.getChildren().add(stack);
        stage.show();
 
        //为了移动圆创建一个时间轴
        timeline = new Timeline();
        timeline.setCycleCount(1);
        timeline.setAutoReverse(false);

        //创建一个带有缩放因子的keyValue:将圆缩放2倍
        KeyValue keyValueX = new KeyValue(stack.scaleXProperty(), toX);
        KeyValue keyValueY = new KeyValue(stack.scaleYProperty(), toY);
 
        //创建一个KeyFrame, keyValue会在2秒钟时抵达
        Duration duration = Duration.millis(750);
//        //当抵达关键帧时可以指定一个特定的动作
        EventHandler onFinished = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                 stack.setTranslateX(600);
                 stack.setTranslateY(400);
//                 复位计数器
//                 i = 0;
            }
        };
 
        KeyFrame keyFrame = new KeyFrame(duration, onFinished , keyValueX, keyValueY);
 
        //将关键帧添加到时间轴中
        timeline.getKeyFrames().add(keyFrame);
 
        timeline.play();
//        timer.start();
    }
        
        
    public static void main(String[] args) {
        Application.launch(args);
    }
  }