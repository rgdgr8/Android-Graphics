package com.bennyplo.android_mooc_graphics_3d;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class MyView extends View {
    private Paint redPaint; //paint object for drawing the lines
    private Coordinate[][] draw_cube_vertices = new Coordinate[16][8];
    double pi = 3.14;
    Coordinate[] cube_vertices;

    Path[][] path;
    int angle = 10;
    int angleleg = 10;

    int colors[] = {Color.RED, Color.GREEN, Color.BLUE, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.MAGENTA, Color.MAGENTA,Color.MAGENTA,Color.MAGENTA,Color.MAGENTA,getResources().getColor(R.color.colorPrimaryDark),getResources().getColor(R.color.colorPrimaryDark),getResources().getColor(R.color.colorPrimaryDark),getResources().getColor(R.color.colorPrimaryDark),getResources().getColor(R.color.colorPrimaryDark),getResources().getColor(R.color.colorPrimaryDark)};

    int ztrans = -2;

    double height;
    double width;

    public MyView(Context context) {
        super(context, null);
        final MyView thisview = this;

        /*path= new Path[10][6];

        for (int i=0;i<10;i++){
            for (int j=0;j<6;j++) {
                path[i][j]=new Path();
            }
        }

         */

        redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        redPaint.setStyle(Paint.Style.STROKE);

        height = getResources().getDisplayMetrics().heightPixels;
        width = getResources().getDisplayMetrics().widthPixels;

        cube_vertices = new Coordinate[8];
        cube_vertices[0] = new Coordinate(-1, -1, -1, 1);
        cube_vertices[1] = new Coordinate(-1, -1, 1, 1);
        cube_vertices[2] = new Coordinate(-1, 1, -1, 1);
        cube_vertices[3] = new Coordinate(-1, 1, 1, 1);
        cube_vertices[4] = new Coordinate(1, -1, -1, 1);
        cube_vertices[5] = new Coordinate(1, -1, 1, 1);
        cube_vertices[6] = new Coordinate(1, 1, -1, 1);
        cube_vertices[7] = new Coordinate(1, 1, 1, 1);

        cube(4.5, 2, ztrans, 120, 120, 120, 0);//head
        cube(9, 7, ztrans, 60, 60, 120, 1);//neck
        cube(2.25, 2.5, ztrans, 240, 320, 120, 2);//body
        cube(2.25, 23.4, ztrans, 240, 50, 120, 3);//waist
        cube(5.3, 13.2, ztrans, 70, 100, 120, 4);//my left leg upper
        cube(5.3, 11.95, ztrans, 70, 130, 120, 5);//my left leg lower
        cube(10.15, 13.2, ztrans, 70, 100, 120, 6);//my right leg upper
        cube(10.15, 11.95, ztrans, 70, 130, 120, 7); // my right leg lower
        cube(5.3, 57, ztrans, 70, 30, 140, 8);//feet mmy left
        cube(10.15, 57, ztrans, 70, 30, 140, 9);//feet my right
        cube(3.3, 4.7, ztrans, 70, 130, 120, 10);//hand uppermy my left
        cube(3.3, 6.7, ztrans, 70, 130, 120, 11);//hand lower my left
        cube(3.3, 26, ztrans, 70, 40, 140, 12);//hand my left
        cube(12.15, 4.7, ztrans, 70, 130, 120, 13);//hand uppermy my right
        cube(12.15, 6.7, ztrans, 70, 130, 120, 14);//hand lower my right
        cube(12.15, 26, ztrans, 70, 40, 140, 15);//hand my right

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {

                cube(4.5, 2, ztrans, 120, 120, 120, 0);//head
                cube(9, 7, ztrans, 60, 60, 120, 1);//neck
                cube(2.25, 2.5, ztrans, 240, 320, 120, 2);//body
                cube(2.25, 23.4, ztrans, 240, 50, 120, 3);//waist
                cube(5.3, 13.2, ztrans, 70, 100, 120, 4);//my left leg upper
                cube(5.3, 11.95, ztrans, 70, 130, 120, 5);//my left leg lower
                cube(10.15, 13.2, ztrans, 70, 100, 120, 6);//my right leg upper
                cube(10.15, 11.95, ztrans, 70, 130, 120, 7); // my right leg lower
                cube(5.3, 57, ztrans, 70, 30, 140, 8);//feet mmy left
                cube(10.15, 57, ztrans, 70, 30, 140, 9);//feet my right
                cube(3.3, 4.7, ztrans, 70, 130, 120, 10);//hand uppermy my left
                cube(3.3, 6.7, ztrans, 70, 130, 120, 11);//hand lower my left
                cube(3.3, 26, ztrans, 70, 40, 140, 12);//hand my left
                cube(12.15, 4.7, ztrans, 70, 130, 120, 13);//hand uppermy my right
                cube(12.15, 6.7, ztrans, 70, 130, 120, 14);//hand lower my right
                cube(12.15, 26, ztrans, 70, 40, 140, 15);//hand my right

                angle += 10;
                //angleleg += 10;

                if (angle >= 360) {
                    angle = 0;
                }

                /*if (angleleg>=50){
                    angleleg=0;
                }

                 */


                invalidate();
            }
        };

        timer.scheduleAtFixedRate(timerTask, 100, 100);


    }

    private void cube(final double xTrans, final double yTrans, final double zTrans, final double scaleX, final double scaleY, final double scaleZ, final int cubeNum) {
        draw_cube_vertices[cubeNum] = translate(cube_vertices, xTrans, yTrans, zTrans);
        draw_cube_vertices[cubeNum] = scale(draw_cube_vertices[cubeNum], scaleX, scaleY, scaleZ);
        draw_cube_vertices[cubeNum] = rotateY(draw_cube_vertices[cubeNum], angle);
        draw_cube_vertices[cubeNum] = translate(draw_cube_vertices[cubeNum], 500, 0, 0);
        //if (cubeNum==4 || cubeNum==6) {
        // draw_cube_vertices[cubeNum] = rotateX(draw_cube_vertices[cubeNum], angleleg);
        //}
    }

    private void DrawLinePairs(Canvas canvas, Coordinate[] vertices, int start, int end, Paint paint) {//draw a line connecting 2 points
        canvas.drawLine((int) vertices[start].x, (int) vertices[start].y, (int) vertices[end].x, (int) vertices[end].y, paint);
    }

    private void DrawCube(Canvas canvas, int cubeNum) {//draw a cube on the screen
        DrawLinePairs(canvas, draw_cube_vertices[cubeNum], 0, 1, redPaint);
        //path[cubeNum][0].moveTo((float) draw_cube_vertices[cubeNum][0].x, (float) draw_cube_vertices[cubeNum][0].y);
        //path[cubeNum][0].lineTo((float) draw_cube_vertices[cubeNum][1].x, (float) draw_cube_vertices[cubeNum][1].y);
        DrawLinePairs(canvas, draw_cube_vertices[cubeNum], 1, 3, redPaint);
        //path[cubeNum][0].lineTo((float) draw_cube_vertices[cubeNum][3].x, (float) draw_cube_vertices[cubeNum][3].y);
        DrawLinePairs(canvas, draw_cube_vertices[cubeNum], 3, 2, redPaint);
        //path[cubeNum][0].lineTo((float) draw_cube_vertices[cubeNum][2].x, (float) draw_cube_vertices[cubeNum][2].y);
        DrawLinePairs(canvas, draw_cube_vertices[cubeNum], 2, 0, redPaint);
        //path[cubeNum][0].lineTo((float) draw_cube_vertices[cubeNum][0].x, (float) draw_cube_vertices[cubeNum][0].y);//back

        DrawLinePairs(canvas, draw_cube_vertices[cubeNum], 4, 5, redPaint);
        // path[cubeNum][1].moveTo((float) draw_cube_vertices[cubeNum][4].x, (float) draw_cube_vertices[cubeNum][4].y);
        //path[cubeNum][1].lineTo((float) draw_cube_vertices[cubeNum][5].x, (float) draw_cube_vertices[cubeNum][5].y);
        DrawLinePairs(canvas, draw_cube_vertices[cubeNum], 5, 7, redPaint);
        //path[cubeNum][1].lineTo((float) draw_cube_vertices[cubeNum][7].x, (float) draw_cube_vertices[cubeNum][7].y);
        DrawLinePairs(canvas, draw_cube_vertices[cubeNum], 7, 6, redPaint);
        //path[cubeNum][1].lineTo((float) draw_cube_vertices[cubeNum][6].x, (float) draw_cube_vertices[cubeNum][6].y);
        DrawLinePairs(canvas, draw_cube_vertices[cubeNum], 6, 4, redPaint);
        //path[cubeNum][1].lineTo((float) draw_cube_vertices[cubeNum][4].x, (float) draw_cube_vertices[cubeNum][4].y);//front

        DrawLinePairs(canvas, draw_cube_vertices[cubeNum], 0, 4, redPaint);
        /*path[cubeNum][2].moveTo((float) draw_cube_vertices[cubeNum][0].x, (float) draw_cube_vertices[cubeNum][0].y);
        path[cubeNum][2].lineTo((float) draw_cube_vertices[cubeNum][4].x, (float) draw_cube_vertices[cubeNum][4].y);
        path[cubeNum][2].lineTo((float) draw_cube_vertices[cubeNum][6].x, (float) draw_cube_vertices[cubeNum][6].y);
        path[cubeNum][2].lineTo((float) draw_cube_vertices[cubeNum][2].x, (float) draw_cube_vertices[cubeNum][2].y);
        path[cubeNum][2].close();//my left

         */


        DrawLinePairs(canvas, draw_cube_vertices[cubeNum], 1, 5, redPaint);
       /* path[cubeNum][3].moveTo((float) draw_cube_vertices[cubeNum][1].x, (float) draw_cube_vertices[cubeNum][1].y);
        path[cubeNum][3].lineTo((float) draw_cube_vertices[cubeNum][5].x, (float) draw_cube_vertices[cubeNum][5].y);
        path[cubeNum][3].lineTo((float) draw_cube_vertices[cubeNum][7].x, (float) draw_cube_vertices[cubeNum][7].y);
        path[cubeNum][3].lineTo((float) draw_cube_vertices[cubeNum][3].x, (float) draw_cube_vertices[cubeNum][3].y);
        path[cubeNum][3].close();//my right

        */


        DrawLinePairs(canvas, draw_cube_vertices[cubeNum], 2, 6, redPaint);
        /*path[cubeNum][4].moveTo((float) draw_cube_vertices[cubeNum][2].x, (float) draw_cube_vertices[cubeNum][2].y);
        path[cubeNum][4].lineTo((float) draw_cube_vertices[cubeNum][6].x, (float) draw_cube_vertices[cubeNum][6].y);
        path[cubeNum][4].lineTo((float) draw_cube_vertices[cubeNum][7].x, (float) draw_cube_vertices[cubeNum][7].y);
        path[cubeNum][4].lineTo((float) draw_cube_vertices[cubeNum][3].x, (float) draw_cube_vertices[cubeNum][3].y);
        path[cubeNum][4].close();//bottom

         */


        DrawLinePairs(canvas, draw_cube_vertices[cubeNum], 3, 7, redPaint);
       /* path[cubeNum][5].moveTo((float) draw_cube_vertices[cubeNum][0].x, (float) draw_cube_vertices[cubeNum][0].y);
        path[cubeNum][5].lineTo((float) draw_cube_vertices[cubeNum][4].x, (float) draw_cube_vertices[cubeNum][4].y);
        path[cubeNum][5].lineTo((float) draw_cube_vertices[cubeNum][5].x, (float) draw_cube_vertices[cubeNum][5].y);
        path[cubeNum][5].lineTo((float) draw_cube_vertices[cubeNum][1].x, (float) draw_cube_vertices[cubeNum][1].y);
        path[cubeNum][5].close();//top

        */
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //draw objects on the screen
        super.onDraw(canvas);
        for (int i = 0; i < 16; i++) {
            redPaint.setColor(colors[i]);
            DrawCube(canvas, i);
        }

        /*for (int i=0;i<10;i++){

            redPaint.setColor(colors[i]);

            for (int j=0;j<6;j++){
                canvas.drawPath(path[i][j],redPaint);
            }
        }

         */
    }

    //*********************************
    //matrix and transformation functions
    public double[] GetIdentityMatrix() {//return an 4x4 identity matrix
        double[] matrix = new double[16];
        matrix[0] = 1;
        matrix[1] = 0;
        matrix[2] = 0;
        matrix[3] = 0;
        matrix[4] = 0;
        matrix[5] = 1;
        matrix[6] = 0;
        matrix[7] = 0;
        matrix[8] = 0;
        matrix[9] = 0;
        matrix[10] = 1;
        matrix[11] = 0;
        matrix[12] = 0;
        matrix[13] = 0;
        matrix[14] = 0;
        matrix[15] = 1;
        return matrix;
    }

    public Coordinate Transformation(Coordinate vertex, double[] matrix) {//affine transformation with homogeneous coordinates
        Coordinate result = new Coordinate();
        result.x = matrix[0] * vertex.x + matrix[1] * vertex.y + matrix[2] * vertex.z + matrix[3] * vertex.w;
        result.y = matrix[4] * vertex.x + matrix[5] * vertex.y + matrix[6] * vertex.z + matrix[7] * vertex.w;
        result.z = matrix[8] * vertex.x + matrix[9] * vertex.y + matrix[10] * vertex.z + matrix[11] * vertex.w;
        result.w = matrix[12] * vertex.x + matrix[13] * vertex.y + matrix[14] * vertex.z + matrix[15] * vertex.w;
        return result;
    }

    public Coordinate[] Transformation(Coordinate[] vertices, double[] matrix) {   //Affine transform a 3D object with vertices
        Coordinate[] result = new Coordinate[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            result[i] = Transformation(vertices[i], matrix);
            result[i].Normalise();
        }
        return result;
    }

    public Coordinate QuatTransformation(Coordinate vertex, double[] matrix) {//affine transformation with homogeneous coordinates
        Coordinate result = new Coordinate();
        result.x = matrix[0] * vertex.x + matrix[1] * vertex.y + matrix[2] * vertex.z;
        result.y = matrix[4] * vertex.x + matrix[5] * vertex.y + matrix[6] * vertex.z;
        result.z = matrix[8] * vertex.x + matrix[9] * vertex.y + matrix[10] * vertex.z;
        result.w = vertex.w;
        return result;
    }

    public Coordinate[] QuatTransformation(Coordinate[] vertices, double[] matrix) {   //Affine transform a 3D object with vertices
        Coordinate[] result = new Coordinate[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            result[i] = QuatTransformation(vertices[i], matrix);
        }
        return result;
    }

    //***********************************************************
    //Affine transformation
    public Coordinate[] translate(Coordinate[] vertices, double tx, double ty, double tz) {
        double[] matrix = GetIdentityMatrix();
        matrix[3] = tx;
        matrix[7] = ty;
        matrix[11] = tz;
        return Transformation(vertices, matrix);
    }

    private Coordinate[] scale(Coordinate[] vertices, double sx, double sy, double sz) {
        double[] matrix = GetIdentityMatrix();
        matrix[0] = sx;
        matrix[5] = sy;
        matrix[10] = sz;
        return Transformation(vertices, matrix);
    }

    private Coordinate[] quaternion(Coordinate[] vertices, int degree, double[] axis) {
        double theta = ((pi / 180) * degree) / 2;

        double w = Math.cos(theta);
        double x = axis[0] * Math.sin(theta);
        double y = axis[1] * Math.sin(theta);
        double z = axis[2] * Math.sin(theta);

        return rotation(w, x, y, z, vertices);

    }

    private Coordinate[] rotation(double w, double x, double y, double z, Coordinate[] vertices) {
        double[] matrix = new double[16];
        matrix[0] = w * w + x * x - y * y - z * z;
        matrix[1] = 2 * x * y - 2 * w * z;
        matrix[2] = 2 * x * z + 2 * w * z;
        matrix[3] = matrix[7] = matrix[11] = matrix[12] = matrix[13] = matrix[14] = 0;
        matrix[4] = 2 * x * y + 2 * w * z;
        matrix[5] = w * w + y * y - x * x - z * z;
        matrix[6] = 2 * y * z - 2 * w * x;
        matrix[8] = 2 * x * z - 2 * w * y;
        matrix[9] = 2 * y * z + 2 * w * x;
        matrix[10] = w * w + z * z - x * x - y * y;
        matrix[15] = 1;

        return QuatTransformation(vertices, matrix);
    }

    private Coordinate[] rotateY(Coordinate[] vertices, int deg) {

        double rad = (pi / 180) * deg;

        double[] matrix = GetIdentityMatrix();
        matrix[0] = Math.cos(rad);
        matrix[2] = Math.sin(rad);
        matrix[8] = -Math.sin(rad);
        matrix[10] = Math.cos(rad);
        return Transformation(vertices, matrix);
    }

    private Coordinate[] rotateX(Coordinate[] vertices, int deg) {

        double rad = (pi / 180) * deg;

        double[] matrix = GetIdentityMatrix();
        matrix[5] = Math.cos(rad);
        matrix[6] = -Math.sin(rad);
        matrix[9] = Math.sin(rad);
        matrix[10] = Math.cos(rad);
        return Transformation(vertices, matrix);
    }

    private Coordinate[] rotateZ(Coordinate[] vertices, int deg) {

        double rad = (pi / 180) * deg;

        double[] matrix = GetIdentityMatrix();
        matrix[0] = Math.cos(rad);
        matrix[1] = -Math.sin(rad);
        matrix[4] = Math.sin(rad);
        matrix[5] = Math.cos(rad);
        return Transformation(vertices, matrix);
    }


}