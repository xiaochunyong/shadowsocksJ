package me.ely.shadowsocks.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

/**
 * http://blog.csdn.net/wingfourever/article/details/7884272
 */
public class MyCanvas extends Canvas {
    private GraphicsContext gc;

    public MyCanvas(double width, double height) {
        super(width, height);
        gc = getGraphicsContext2D();
//        draw(gc);
        drawRec(width, height);
    }

    public void drawRec(double width, double height) {
        //绘制矩形
        gc.save();
        gc.setStroke(Color.RED);
        gc.strokeRect(0, 0, width, height);
        gc.restore();
    }

    public void draw(GraphicsContext gc) {

        gc.save();
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);                //设置线的宽度  
        gc.strokeLine(0, 0, 50, 50);       //绘制直线  
        gc.restore();

        //绘制椭圆  
        gc.save();
        gc.setFill(Color.YELLOWGREEN);
        gc.strokeOval(0, 80, 50, 50);
        gc.fillOval(100, 80, 50, 50);
        gc.restore();

        //绘制矩形  
        gc.save();
        gc.setStroke(Color.CHOCOLATE);
        gc.fillRect(0, 150, 50, 50);
        gc.strokeRect(100, 150, 50, 50);
        gc.restore();

        //绘制圆角矩形  
        gc.save();
        gc.setFill(Color.CHOCOLATE);
        gc.fillRoundRect(0, 220, 50, 50, 15, 15);
        gc.strokeRoundRect(100, 220, 50, 50, 15, 15);
        gc.restore();

        //绘制扇形  
        gc.save();
        gc.setStroke(Color.CHOCOLATE);
        gc.fillArc(10, 300, 30, 30, 40, 280, ArcType.OPEN);
        gc.fillArc(60, 300, 30, 30, 40, 280, ArcType.CHORD);
        gc.fillArc(110, 300, 30, 30, 40, 280, ArcType.ROUND);
        gc.strokeArc(10, 340, 30, 30, 40, 280, ArcType.OPEN);
        gc.strokeArc(60, 340, 30, 30, 40, 280, ArcType.CHORD);
        gc.strokeArc(110, 340, 30, 30, 40, 280, ArcType.ROUND);
        gc.restore();

        //绘制多边形  
        gc.save();
        gc.setFill(Color.RED);
        gc.setStroke(Color.CHOCOLATE);
        gc.fillPolygon(new double[]{0, 40, 50, 60, 100, 70, 85, 50, 15, 30}, new double[]{440, 440, 400, 440, 440, 460, 500, 470, 500, 460}, 10);
        gc.strokePolygon(new double[]{0, 40, 50, 60, 100, 70, 85, 50, 15, 30}, new double[]{440, 440, 400, 440, 440, 460, 500, 470, 500, 460}, 10);
        gc.restore();
    }
}