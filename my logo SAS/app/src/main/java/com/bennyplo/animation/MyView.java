package com.bennyplo.animation;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.util.Timer;
import java.util.TimerTask;


public class MyView extends GLSurfaceView {
    private final MyRenderer mRenderer;
    static int angleX=0;
    static int angleY=0;
    static int angleZ=0;
    static float scale=0.7f;

    private float TOUCH_SCALE_FACTOR;
    private float prevX,prevY;
    private ScaleGestureDetector scaleGestureDetector;

    public MyView(Context context) {
        super(context);
        setEGLContextClientVersion(2);// Create an OpenGL ES 2.0 context.
        mRenderer = new MyRenderer();// Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        Timer timer=new Timer();
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                angleY+=10;
                requestRender();
            }
        };

        timer.scheduleAtFixedRate(timerTask,100,100);
        float height=getResources().getDisplayMetrics().heightPixels;
        float width=getResources().getDisplayMetrics().widthPixels;
        TOUCH_SCALE_FACTOR=180/width;

        scaleGestureDetector=new ScaleGestureDetector(context, new ScaleListener());
    }

    private static class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            scale*=detector.getScaleFactor();
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        scaleGestureDetector.onTouchEvent(event);

        float x=event.getX();
        float y=event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                float dx=x-prevX;
                float dy=y-prevY;
                if (y>getHeight()/2f){
                    dx=-dx;
                }
                if (x<getWidth()/2f){
                    dy=-dy;
                }
                angleX+=dy*TOUCH_SCALE_FACTOR;
                //angleY+=dx*TOUCH_SCALE_FACTOR;
                angleZ+=dx*TOUCH_SCALE_FACTOR;

                //TODO; you can rotate on touch about either z or y axis not both using touch.
                requestRender();
        }
        prevY=y;
        prevX=x;

        return true;
    }

}
