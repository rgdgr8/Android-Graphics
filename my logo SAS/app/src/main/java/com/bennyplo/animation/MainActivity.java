package com.bennyplo.animation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int READ_IMAGE=13;
    public static Bitmap bitmap;

    public static Bitmap getBitmap(){
        return bitmap;
    }

    private MyView GLView;
    private View mControlsView;
    private static Context context;

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

        //ImageSearch();
        context=this;

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*if (bitmap!=null) {
            GLView = new MyView(this);
            //setContentView(R.layout.activity_main);
            setContentView(GLView);
        }*/
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

        Toast.makeText(context, "Pinch to ZOOM. Move finger to ROTATE!", Toast.LENGTH_LONG).show();
    }
}
