package com.bennyplo.graphics2d;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Shader;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

/**
 * Created by benlo on 09/05/2018.
 */

public class MyView extends View {
    private static final String TAG = "Myview";
    private Paint redPaint;
    private Paint black;
    private Path myPath;

    private Paint paint;

    int[] colors={Color.RED,Color.BLUE,Color.GRAY,Color.GREEN,Color.YELLOW};
    int[] data={10,20,30,40,50};
    float start=0;

    float height;
    float width;

    Point[] points=new Point[5];

    public MyView(Context context) {
        super(context, null);

        height=getResources().getDisplayMetrics().heightPixels-100;
        width=getResources().getDisplayMetrics().widthPixels;

        points[0]=new Point(50,300);
        points[1]=new Point(150,400);
        points[2]=new Point(100,340);
        points[3]=new Point(240,420);
        points[4]=new Point(300,200);

        LinearGradient linearGradient = new LinearGradient(100, 20, 250, 10, Color.RED, Color.GREEN, Shader.TileMode.MIRROR);
        redPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        black=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint=new Paint(Paint.ANTI_ALIAS_FLAG);

        /*myPath=new Path();
        myPath.moveTo(50,300);
        myPath.lineTo(150,400);
        myPath.lineTo(180,340);
        myPath.lineTo(240,420);
        myPath.lineTo(300,200);
        myPath.close();
        black.setStyle(Paint.Style.STROKE);
        black.setColor(getResources().getColor(android.R.color.black));
        redPaint.setStyle(Paint.Style.FILL);
        redPaint.setShader(linearGradient);
        black.setStrokeWidth(5);

         */


    }

   /* protected void UpdatePath(Point[] newPoints){
        myPath.reset();
        myPath.moveTo(newPoints[0].x,newPoints[0].y);

        for (int i=0;i<newPoints.length;i++){
            myPath.lineTo(newPoints[i].x,newPoints[i].y);
        }

        myPath.close();
    }

    */

    protected Point[] Transform(Point[] input, double[][] matrix){

        Point[] newPoints=new Point[input.length];

        for (int j=0;j<input.length;j++){
            int x= (int) (matrix[0][0]*input[j].x + matrix[0][1]*input[j].y+matrix[0][2]);
            int y= (int) (matrix[1][0]*input[j].x + matrix[1][1]*input[j].y+matrix[1][2]);
            newPoints[j]=new Point(x,y);
        }

        return newPoints;
    }

    protected Point[] translate(Point[] input,int x, int y){
        double[][] matrix=new double[3][3];

        matrix[0][0]=1;
        matrix[0][1]=0;
        matrix[0][2]=x;
        matrix[1][0]=0;
        matrix[1][1]=1;
        matrix[1][2]=y;
        matrix[2][0]=matrix[2][1]=0;
        matrix[2][2]=1;
        return Transform(input,matrix);
    }

    protected Point[] Scale(Point[] input,double x, double y){
        double[][] matrix=new double[3][3];

        matrix[0][0]=x;
        matrix[0][1]=0;
        matrix[0][2]=0;
        matrix[1][0]=0;
        matrix[1][1]=y;
        matrix[1][2]=0;
        matrix[2][0]=matrix[2][1]=0;
        matrix[2][2]=1;
        return Transform(input,matrix);
    }

    private Point[] bar(){

        int min=99999;
        int max=-99999;
        start=70;
        Point[] ptArr=new Point[data.length];

        for(int i=0;i<data.length;i++){
            /*canvas.drawArc(100,400,getWidth()-100,getHeight()-600,start,scaledValues[i],false,paint);
            start=start+scaledValues[i];
            */

            ptArr[i]=new Point((int) start, (int) (height-data[i]));

            min= (int) Math.min(min,height-data[i]);
            max= (int) Math.max(max,height-data[i]);

            start+=50;

        }

        double xscale=(double)width/(data.length-1);
        double yscale=(double)height/(max-min);

        ptArr=Scale(ptArr,1,yscale);

        return ptArr;

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //float[] scaledValues = scalePie();
        paint.setStyle(Paint.Style.FILL);

        Point[] recpoints=bar();

        start=100;

        for (int i=0;i<data.length;i++){

            paint.setColor(colors[i]);

            canvas.drawRect(start-30,height-data[i],start,height,paint);

            start+=50;
        }

    }

    private float[] scalePie() {
        float[] scaledValues = new float[data.length];
        float total = getTotal();
        for (int i = 0; i < data.length; i++) {
            scaledValues[i] = (data[i] / total) * 360;
        }
        return scaledValues;
    }

    private float getTotal() {
        float total = 0;
        for (float val : data)
            total += val;
        return total;
    }
}
