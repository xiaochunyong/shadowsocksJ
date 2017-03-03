package me.ely.shadowsocks.ui;

import javafx.animation.*;
import javafx.event.ActionEvent;  
import javafx.event.EventHandler;  
import javafx.scene.control.ToggleButton;  
import javafx.scene.control.ToggleGroup;  
import javafx.scene.effect.Bloom;  
import javafx.scene.layout.HBox;  
import javafx.scene.layout.Pane;  
import javafx.scene.paint.Color;  
import javafx.scene.shape.*;  
import javafx.util.Duration;  
  
/** 
 * 
 * @author wing 
 */  
public class TestPane extends Pane{  
    private ParallelTransition mAnimList;  
      
    private Timeline timeline;  
      
    private HBox hBox;  
    private ToggleButton start, pause, stop;  
    private ToggleGroup btnGroup;  
      
    private double duration = 200;  
    public TestPane(){  
        btnGroup = new ToggleGroup();  
        start = new ToggleButton("Start");  
        start.setToggleGroup(btnGroup);  
        start.setOnAction(new EventHandler<ActionEvent>() {  
  
            @Override  
            public void handle(ActionEvent arg0) {
                timeline.play();  
                checkUIState();  
            }  
        });  
          
        pause = new ToggleButton("Pause");  
        pause.setToggleGroup(btnGroup);  
        pause.setOnAction(new EventHandler<ActionEvent>() {  
  
            @Override  
            public void handle(ActionEvent arg0) {  
                timeline.pause();  
                checkUIState();  
            }  
        });         
   
        stop = new ToggleButton("Stop");  
        stop.setToggleGroup(btnGroup);  
        stop.setOnAction(new EventHandler<ActionEvent>() {  
  
            @Override  
            public void handle(ActionEvent arg0) {  
                timeline.stop();  
                checkUIState();  
            }  
        });     
          
        hBox = new HBox(10);  
        hBox.getChildren().addAll(start, pause, stop);  
          
        hBox.setTranslateX((Anitest.WIDTH - 200) / 2);  
        hBox.setTranslateY(20);  
          
        getChildren().add(hBox);  
          
          
        timeline = new Timeline();  
          
        timeline.setCycleCount(Timeline.INDEFINITE);  
        KeyFrame keyFrame = new KeyFrame(Duration.millis(duration), new EventHandler<ActionEvent>() {  
  
            @Override  
            public void handle(ActionEvent event) {  
                createObject();  
            }  
        });  
        timeline.getKeyFrames().add(keyFrame);  
    }  
      
    /** 
     * 检测start pause stop三个按钮的状态 
     */  
    public void checkUIState(){  
        start.setDisable(false);  
        pause.setDisable(false);  
        stop.setDisable(false);  
        switch(timeline.getStatus()){  
            case RUNNING:  
                start.setDisable(true);  
                break;  
            case PAUSED:  
                pause.setDisable(true);  
                break;  
            case STOPPED:  
                stop.setDisable(true);  
                break;  
        }  
    }  
      
      
    /** 
     * 创建一个Object 并执行动画,在这里创建了一个ParallelTransition，并在其中添加了随机左右上下平移的TranslateTransition，透明度逐渐变为0的FadeTransition和逐渐放大0.2倍的ScaleTransition。将刚创建的Object与组合这个三个动画的ParallelTransition绑定，然后执行ParallelTransition。 
     */  
    public void createObject() {  
        double width = Math.max(50, Math.random() * 200);  
        double height = Math.max(50, Math.random() * 200);          
        double x = Math.min(Math.random() * Anitest.WIDTH, Anitest.WIDTH - width);  
        double y = Math.max(Math.random() * (Anitest.HEIGHT - 100), 100);  
          
        double dx = Math.random() * 50;  
        double dy = Math.random() * 50;

        final Shape shape = new Rectangle(0, 0, 1366, 768);
        shape.setEffect(new Bloom(50));  
        shape.setFill(Color.rgb((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));  
        getChildren().add(shape);
        mAnimList = new ParallelTransition(
                shape,  
//                TranslateTransitionBuilder.create().byX(Math.random() > 0.5 ? dx : -dx).byY(Math.random() > 0.5 ? dy : -dy).duration(Duration.millis(1000)).build(),
//                FadeTransitionBuilder.create().toValue(0).duration(Duration.millis(2000)).build(),
                ScaleTransitionBuilder.create().byX(-0.8).byY(-0.8).duration(Duration.millis(800)).build());
        mAnimList.play();  
        mAnimList.setOnFinished(new EventHandler<ActionEvent>() {  
  
            @Override  
            public void handle(ActionEvent arg0) {  
                getChildren().remove(shape);  
            }  
        });  
    }  
}  