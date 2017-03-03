package me.ely.shadowsocks.ui;

import javafx.application.Application;
import javafx.scene.Scene;  
import javafx.scene.layout.StackPane;  
import javafx.scene.paint.Color;  
import javafx.stage.Stage;  
  
/** 
 * @author wing 
 * 2012/8/30 
 */  
public class Anitest extends Application {  
    public static final int WIDTH = 800;  
    public static final int HEIGHT = 600;  
    public static void main(String[] args) {  
        launch(args);  
    }  
      
    @Override  
    public void start(Stage primaryStage) {  
        TestPane mPane = new TestPane();  
        StackPane root = new StackPane();  
        root.getChildren().add(mPane);  
        Scene scene = new Scene(root, WIDTH, HEIGHT);  
        scene.setFill(Color.BLACK);  
        primaryStage.setScene(scene);  
        primaryStage.setTitle("JavaFX示例--TimeLine和Animation的使用");  
        primaryStage.show();  
    }  
}  