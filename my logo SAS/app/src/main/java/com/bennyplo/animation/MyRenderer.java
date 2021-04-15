package com.bennyplo.animation;

import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class MyRenderer implements GLSurfaceView.Renderer {
    private final float[] mMVPMatrix = new float[16];//model view projection matrix
    private final float[] mProjectionMatrix = new float[16];//projection mastrix
    private final float[] mViewMatrix = new float[16];//view matrix
    private final float[] mMVMatrix = new float[16];//model view matrix
    private final float[] mModelMatrix = new float[16];//model  matrix
    private CharacterA mcharA;
    private CharacterS mcharS;
    private Sphere msphere;
    private ArbitaryShape marbitary;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color to black
        GLES32.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);
        mcharA = new CharacterA();
        mcharS = new CharacterS();
        //msphere=new Sphere();
        //marbitary=new ArbitaryShape();
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

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the view based on view window changes, such as screen rotation
        GLES32.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        float left = -ratio, right = ratio;
        Matrix.frustumM(mProjectionMatrix, 0, left, right, -1.0f, 1.0f, 1.0f, 8.0f);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        float[] mRotationMatrix = new float[16];
        float[] mRotationMatrix2 = new float[16];
        float[] mRotationMatrix3 = new float[16];
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
                0.0f, 0f, 1.0f,//camera is at (0,0,1)
                0f, 0f, 0f,//looks at the origin(target location)
                0f, 1f, 0.0f);//up vector in world axis to calculate the camera's local right axis by cross product with direction vector(position-target)
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5f);//move backward for 5 units
        Matrix.scaleM(mModelMatrix, 0, MyView.scale, MyView.scale, MyView.scale);
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrix2, 0);
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrix3, 0);
        // Calculate the projection and view transformation
        //calculate the model view matrix
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);

        mcharA.draw(mMVPMatrix);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -MyView.scale*2, 0.0f, -5f);
        Matrix.scaleM(mModelMatrix, 0, MyView.scale, MyView.scale, MyView.scale);
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrix2, 0);
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrix3, 0);
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);
        mcharS.draw(mMVPMatrix);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, MyView.scale*2, 0.0f, -5f);
        Matrix.scaleM(mModelMatrix, 0, MyView.scale, MyView.scale, MyView.scale);
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrix, 0);
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrix2, 0);
        Matrix.multiplyMM(mModelMatrix, 0, mModelMatrix, 0, mRotationMatrix3, 0);
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);
        mcharS.draw(mMVPMatrix);

        //msphere.draw(mMVPMatrix);
        //marbitary.draw(mMVPMatrix);
    }
}
