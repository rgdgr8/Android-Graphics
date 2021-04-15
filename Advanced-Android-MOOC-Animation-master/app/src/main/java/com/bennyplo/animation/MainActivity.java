package com.bennyplo.animation;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int READ_IMAGE=13;
    public static Bitmap bitmap;

    public static Bitmap getBitmap(){
        return bitmap;
    }

    private MyView GLView;
    private View mControlsView;
    private static Context context;

    boolean imageChosen=false;

    Sensor mRotationSensor;
    SensorManager sensorManager;
    WindowManager windowManager;

    public static Context getContext(){
        return context;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void ImageSearch(){
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent,READ_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==READ_IMAGE && resultCode==RESULT_OK && data!=null){
            Uri uri=data.getData();
            try {
                bitmap=imageFile(uri);
                //imageChosen=true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private Bitmap imageFile(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor=getContentResolver().openFileDescriptor(uri,"r");
        assert parcelFileDescriptor != null;
        FileDescriptor fileDescriptor=parcelFileDescriptor.getFileDescriptor();
        Bitmap bitmapf= BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return bitmapf;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        if (!imageChosen) {
           // ImageSearch();
        }
        context=this;
        super.onCreate(savedInstanceState);

        windowManager=getWindow().getWindowManager();
        sensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
        mRotationSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onResume() {
        super.onResume();

        /*if (bitmap!=null) {
            GLView = new MyView(this);
            setContentView(GLView);
        }

         */
        GLView = new MyView(this);
        //setContentView(R.layout.activity_main);
        setContentView(GLView);
        //set full screen
        mControlsView=getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        mControlsView.setSystemUiVisibility(uiOptions);

        if (mRotationSensor!=null) {
            sensorManager.registerListener(this, mRotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        //Toast.makeText(context, "Pinch to ZOOM. Move finger to ROTATE!", Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {//ensure that no matter which orientation, the app will use full screen!
        super.onConfigurationChanged(newConfig);
        mControlsView=getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        mControlsView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor==mRotationSensor /*&& bitmap!=null*/){
            updateOrientation(sensorEvent.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void updateOrientation(float[] values){
        float[] rotationMAtrix=new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMAtrix,values);
        int x=0;
        int y=0;

        switch (windowManager.getDefaultDisplay().getRotation()){

            case Surface.ROTATION_0:
                x=SensorManager.AXIS_X;
                y=SensorManager.AXIS_Z;
                break;
            case Surface.ROTATION_90:
                x=SensorManager.AXIS_Z;
                y=SensorManager.AXIS_MINUS_X;
                break;
            case Surface.ROTATION_180:
                x=SensorManager.AXIS_MINUS_X;
                y=SensorManager.AXIS_MINUS_Z;
                break;
            case Surface.ROTATION_270:
                x=SensorManager.AXIS_MINUS_Z;
                y=SensorManager.AXIS_X;
                break;
        }

        float[] adjustedRotationMAtrix=new float[9];
        SensorManager.remapCoordinateSystem(rotationMAtrix,x,y,adjustedRotationMAtrix);
        float[] rotationAngles=new float[3];
        SensorManager.getOrientation(adjustedRotationMAtrix,rotationAngles);
        float RadtoDeg= (float) (-180/Math.PI);

        float yaw=rotationAngles[0]*RadtoDeg;
        float pitch=rotationAngles[1]*RadtoDeg;
        float roll=rotationAngles[2]*RadtoDeg;

        GLView.sensorRotate(yaw,pitch,roll);

    }

    @Override
    protected void onDestroy() {

        imageChosen=false;
        super.onDestroy();
    }
}
