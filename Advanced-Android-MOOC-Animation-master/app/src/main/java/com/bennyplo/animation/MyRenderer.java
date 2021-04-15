package com.bennyplo.animation;

import android.opengl.GLES20;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;


public class MyRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "MyREnderer";
    private final float[] mMVPMatrix = new float[16];//model view projection matrix
    private final float[] mProjectionMatrix = new float[16];//projection mastrix
    private final float[] mViewMatrix = new float[16];//view matrix
    private final float[] mMVMatrix = new float[16];//model view matrix
    private final float[] mModelMatrix = new float[16];//model  matrix
    private CharacterA mcharA;
    private CharacterS mcharS;
    private Sphere msphere;
    private ArbitaryShape marbitary;
    private flatSurface mFlat;
    private flatSurface mFlat2;
    private MyFrameBuffer1 myFrameBuffer1 = null;
    private MyFrameBuffer1 myFrameBuffer2 = null;

    int portWidth, portHeight;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color to black
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        mcharA = new CharacterA();
        mcharS = new CharacterS();

        GLES32.glCullFace(GLES32.GL_BACK);
        GLES32.glEnable(GLES32.GL_CULL_FACE);
        msphere = new Sphere();
        marbitary = new ArbitaryShape();
        mFlat=new flatSurface(R.drawable.grl);
        mFlat2=new flatSurface(R.drawable.spoonchar);
    }

    public static void checkGlError(String glOperation) {
        int error;
        if ((error = GLES32.glGetError()) != GLES32.GL_NO_ERROR) {
            Log.e("MyRenderer", glOperation + ": glError " + error);
        }
    }

    public static int loadShader(int type, String shaderCode) {
        // create a vertex shader  (GLES32.GL_VERTEX_SHADER) or a fragment shader (GLES32.GL_FRAGMENT_SHADER)
        int shader = GLES32.glCreateShader(type);
        GLES32.glShaderSource(shader, shaderCode);// add the source code to the shader and compile it
        GLES32.glCompileShader(shader);
        return shader;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the view based on view window changes, such as screen rotation
        GLES32.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        float left = -ratio, right = ratio;

        portWidth = width;
        portHeight = height;

        Log.i(TAG, "onSurfaceChanged: width "+width);
        Log.i(TAG, "onSurfaceChanged: height "+height);

        //Matrix.frustumM(mProjectionMatrix, 0, left, right, -1.0f, 1.0f, 1.0f, 20.0f);

        //TODO: this if else may be ommitted and just the code of if acan also be used if zooming feature is available
        if (width > height) {
            Log.i(TAG, "onSurfaceChanged: if");
            ratio = (float) width / height;
            left = -ratio; right = ratio;
            Matrix.orthoM(mProjectionMatrix, 0, left, right, -1.0f, 1.0f, -10.0f, 200.0f);
        }else {
            Log.i(TAG, "onSurfaceChanged: else");
            ratio = (float) height / width;
            left = -ratio; right = ratio;
            Matrix.orthoM(mProjectionMatrix, 0, -1, 1, left, right, -10.0f, 200.0f);
            //Matrix.orthoM(mProjectionMatrix, 0, left, right, -1.0f, 1.0f, -10.0f, 200.0f);
        }

        myFrameBuffer1=new MyFrameBuffer1(width,height,0);
        myFrameBuffer2=new MyFrameBuffer1(width,height,1);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        float[] mRotationMatrix = new float[16];
        float[] mRotationMatrix2 = new float[16];
        float[] mRotationMatrix3 = new float[16];

        float degToRad= (float) (Math.PI/180);
        // Draw background color
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);//to clear the previous set buffers
        GLES32.glClearDepthf(1.0f);//set up the depth buffer
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);//enable depth test TODO:(so, it will not look through the surfaces). Will not perform depth comparisons with pixels if not enabled
        GLES32.glDepthFunc(GLES32.GL_LEQUAL);//indicate what type of depth test(pixel passes the depth test for a depth lesser than or equal to the one set by the depth buffer)
        Matrix.setIdentityM(mMVPMatrix, 0);//set the model view projection matrix to an identity matrix
        Matrix.setIdentityM(mMVMatrix, 0);//set the model view  matrix to an identity matrix
        Matrix.setIdentityM(mModelMatrix, 0);//set the model matrix to an identity matrix
        Matrix.setRotateM(mRotationMatrix2, 0, MyView.angleY, 0f, 1f, 0);//rotate around the y-axis
        Matrix.setRotateM(mRotationMatrix, 0, MyView.angleX, 1f, 0f, 0);//rotate around the x-axis
        Matrix.setRotateM(mRotationMatrix3, 0, MyView.angleZ, 0f, 0f, 1f);//z

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0,
                0.0f, 0f, 1.0f * MyView.scale / 2,//camera is at (0,0,1)
                0f, 0f, 0f,//looks at the origin(target location)
                0f, 1f, 0.0f);//up vector in worldmap axis to calculate the camera's local right axis by cross product with direction vector(position-target)
        Matrix.translateM(mModelMatrix, 0, MyView.angleY*degToRad+3, MyView.angleX*degToRad, -5f);
        Matrix.scaleM(mModelMatrix, 0, MyView.scale, MyView.scale, MyView.scale);
        //Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrix, 0);
        //Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrix2, 0);
        //Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrix3, 0);
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);

        /*
        GLES32.glDisable(GLES20.GL_BLEND);
        mcharA.draw(mMVPMatrix);

        GLES32.glBlendFunc(GLES20.GL_CONSTANT_COLOR, GLES20.GL_ONE);
        GLES32.glBlendColor(1,1,1,1);
        GLES32.glDisable(GLES20.GL_CULL_FACE);
        GLES32.glDisable(GLES20.GL_DEPTH_TEST);
        GLES32.glBlendEquation(GLES20.GL_FUNC_ADD);
        GLES32.glEnable(GLES20.GL_BLEND);

        marbitary.draw(mMVPMatrix);
        */

        //msphere.setLightLocation(-10,-10,-10);
        //msphere.draw(mMVPMatrix);

        if (myFrameBuffer1!=null){
            GLES32.glViewport(0,0,myFrameBuffer1.width,myFrameBuffer1.height);
            GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER,myFrameBuffer1.frameBufferID[0]);
            GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT|GLES32.GL_DEPTH_BUFFER_BIT);
            mFlat.draw(mMVPMatrix);
            mFlat2.draw(mMVPMatrix);
            Matrix.scaleM(mModelMatrix,0,0.4f,0.2f,1f);
            Matrix.translateM(mModelMatrix, 0, 0,0,3);
            Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);
            msphere.draw(mMVPMatrix,0);
            float[] model=myFrameBuffer1.getModel(MyView.angleX,MyView.angleY,MyView.angleZ);
            Matrix.multiplyMM(mMVMatrix,0,myFrameBuffer1.mViewMatrix,0,model,0);
            Matrix.multiplyMM(mMVPMatrix,0,myFrameBuffer1.mProjectionMatrix,0,mMVMatrix,0);
            msphere.draw(mMVPMatrix,1);
            GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER,0);
        }
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, MyView.angleY*degToRad+3, MyView.angleX*degToRad, -5f);
        Matrix.scaleM(mModelMatrix, 0, MyView.scale, MyView.scale, MyView.scale);
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);
        if (myFrameBuffer2!=null){
            GLES32.glViewport(0,0,myFrameBuffer2.width,myFrameBuffer2.height);
            GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER,myFrameBuffer2.frameBufferID[0]);
            GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT|GLES32.GL_DEPTH_BUFFER_BIT);
            mFlat.draw(mMVPMatrix);
            mFlat2.draw(mMVPMatrix);
            Matrix.scaleM(mModelMatrix,0,0.4f,0.2f,1f);
            Matrix.translateM(mModelMatrix, 0, 0,0,3);
            Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);
            msphere.draw(mMVPMatrix,0);
            float[] model=myFrameBuffer2.getModel(MyView.angleX,MyView.angleY,MyView.angleZ);
            Matrix.multiplyMM(mMVMatrix,0,myFrameBuffer2.mViewMatrix,0,model,0);
            Matrix.multiplyMM(mMVPMatrix,0,myFrameBuffer2.mProjectionMatrix,0,mMVMatrix,0);
            msphere.draw(mMVPMatrix,1);
            GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER,0);
        }

        GLES32.glViewport(0,0,portWidth,portHeight);
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT|GLES32.GL_DEPTH_BUFFER_BIT);
        myFrameBuffer1.draw();
        myFrameBuffer2.draw();
    }
}
