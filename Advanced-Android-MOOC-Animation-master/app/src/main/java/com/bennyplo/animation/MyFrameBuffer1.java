package com.bennyplo.animation;

import android.opengl.GLES20;
import android.opengl.GLES32;
import android.opengl.Matrix;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class MyFrameBuffer1 {

    private static final String TAG = "MyFrameBuffer1";

    private final String vertexShaderCode =
            "attribute vec3 aVertexPosition;" +
                    "uniform mat4 uMVPMatrix;" +
                    "attribute vec2 aTexCoord;" +
                    "varying vec2 vTexCoord;" +

                    "void main() {" +
                    "gl_Position = uMVPMatrix *vec4(aVertexPosition,1.0);" +
                    "vTexCoord=aTexCoord;" +
                    "}";
    private final String fragmentShaderCode =
            "precision lowp float;" +
                    "varying vec2 vTexCoord;" +
                    "uniform sampler2D tex1;" +

                    "void main() {" +
                    "vec4 fragColor= texture2D(tex1,vec2(vTexCoord.s,vTexCoord.t));" +
                    //"if(fragColor.r<0.01 || fragColor.g<0.01 || fragColor.b<0.01) discard; else " +
                    "gl_FragColor= vec4(fragColor.rgb,fragColor.a);" +
                    "}";

    private final FloatBuffer vertexBuffer,Tex1Buffer;
    private final IntBuffer indexBuffer;
    private final int mProgram;

    private int mPositionHandle, textureCoordHandle, textureHandle, mMVPMatrixHandle;

    public int[] frameBufferID=new int[1];
    public int[] textureID=new int[1];
    public int[] renderID=new int[1];
    public final int COORDS_PER_VERTEX=3;
    public final int COORDS_PER_TEX=2;
    private final int vertexStride=COORDS_PER_VERTEX*4;
    private final int texStride=COORDS_PER_TEX*4;

    public int width,height;
    private final float[] mMVPMatrix = new float[16];//model view projection matrix

    public float[] mProjectionMatrix=new float[16];
    public final float[] mViewMatrix = new float[16];//view matrix
    public final float[] mMVMatrix = new float[16];//model view matrix
    public final float[] mModelMatrix = new float[16];//model  matrix

    public final float[] mFrameProjectionMatrix = new float[16];//projection matrix for the frame buffer plane
    public final float[] mFrameModelMatrix = new float[16];//model  matrix for the frame buffer plane
    public final float[] mFrameViewMatrix = new float[16];//model  matrix the frame buffer plan

    private float aspect;
    public float depthZ=-1;
    private float nearZ=1;
    private float screenZ=-10;
    private float IOD=0.8f;
    private float frustumShift=-(IOD/2)*(nearZ/screenZ);
    private float modelShift;

    private final float[] vertices={
           -1,-1,1,
            1,-1,1,
            1,1,1,
            -1,1,1
    };

    private final int[] indexes={
            0,1,2,2,3,0
    };

    private final float[] texCooords={
            0,0,
            1,0,
            1,1,
            0,1
    };

    private void texture(int which, int textureID, int pixel_format, int widthT, int heightT, int type){
        GLES32.glActiveTexture(which);
        GLES32.glBindTexture(GLES20.GL_TEXTURE_2D,textureID);
        GLES32.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES32.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES32.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES32.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES32.glTexImage2D(GLES32.GL_TEXTURE_2D,0,pixel_format,widthT,heightT,0,pixel_format,type,null);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void createBuffer(int widthF, int heightF){
        GLES32.glGenFramebuffers(1,frameBufferID,0);
        GLES32.glGenTextures(1,textureID,0);
        texture(GLES32.GL_TEXTURE1,textureID[0],GLES32.GL_RGBA,widthF,heightF,GLES32.GL_UNSIGNED_BYTE);
        GLES32.glBindFramebuffer(GLES32.GL_DRAW_FRAMEBUFFER,frameBufferID[0]);
        GLES32.glFramebufferTexture2D(GLES32.GL_FRAMEBUFFER,GLES32.GL_COLOR_ATTACHMENT0,GLES32.GL_TEXTURE_2D,textureID[0],0);
        GLES32.glGenRenderbuffers(1,renderID,0);
        GLES32.glBindRenderbuffer(GLES32.GL_RENDERBUFFER,renderID[0]);
        GLES32.glRenderbufferStorage(GLES32.GL_RENDERBUFFER,GLES32.GL_DEPTH_COMPONENT24,widthF,heightF);
        GLES32.glFramebufferRenderbuffer(GLES32.GL_FRAMEBUFFER,GLES32.GL_DEPTH_ATTACHMENT,GLES32.GL_RENDERBUFFER,renderID[0]);

        if (GLES32.glCheckFramebufferStatus(GLES32.GL_FRAMEBUFFER)!=GLES32.GL_FRAMEBUFFER_COMPLETE){
            Log.i(TAG, "createBuffer: problem!!!!!!!!");
        }

        GLES32.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        GLES32.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public MyFrameBuffer1(int widthC, int heightC, int left_or_right){

        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);// (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
        IntBuffer ib = IntBuffer.allocate(indexes.length);
        indexBuffer = ib;
        indexBuffer.put(indexes);
        indexBuffer.position(0);
        ByteBuffer t = ByteBuffer.allocateDirect(texCooords.length * 4);// (# of coordinate values * 4 bytes per float)
        t.order(ByteOrder.nativeOrder());
        Tex1Buffer = t.asFloatBuffer();
        Tex1Buffer.put(texCooords);
        Tex1Buffer.position(0);

        int vertexShader = MyRenderer.loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyRenderer.loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES32.glCreateProgram();
        GLES32.glAttachShader(mProgram, vertexShader);
        GLES32.glAttachShader(mProgram, fragmentShader);
        GLES32.glLinkProgram(mProgram);
        GLES32.glUseProgram(mProgram);

        mPositionHandle = GLES32.glGetAttribLocation(mProgram, "aVertexPosition");
        GLES32.glEnableVertexAttribArray(mPositionHandle);
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix");
        textureCoordHandle = GLES32.glGetAttribLocation(mProgram, "aTexCoord");
        GLES32.glEnableVertexAttribArray(textureCoordHandle);
        textureHandle = GLES32.glGetUniformLocation(mProgram, "tex1");

        width=widthC/2;
        height=heightC;
        aspect=(float)width/height;
        float ratio=(float)width/height;
        float left=-ratio, right=ratio;

        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.setIdentityM(mMVMatrix, 0);
        Matrix.setIdentityM(mModelMatrix, 0);

        Matrix.setIdentityM(mFrameModelMatrix, 0);

        //TODO: this if else may be ommitted and just the code of if acan also be used if zooming feature is available
        //To display the 2D plane
        if (widthC > heightC) {
            ratio = (float) widthC / heightC;
            left = -ratio; right = ratio;
            Matrix.orthoM(mFrameProjectionMatrix, 0, left, right, -1.0f, 1.0f, -10.0f, 200.0f);
        }else {
            ratio = (float) heightC / widthC;
            left = -ratio; right = ratio;
            Matrix.orthoM(mFrameProjectionMatrix, 0, -1, 1, left, right, -10.0f, 200.0f);
        }
        Matrix.setLookAtM(mFrameViewMatrix,0,
                0,0,0.1f,
                0,0,0,
                0,1,0);
        Matrix.scaleM(mFrameModelMatrix,0,(float)width/height,1,1);

        if (left_or_right==0){
            Matrix.translateM(mFrameModelMatrix,0,-1,0,0);
        }else {
            Matrix.translateM(mFrameModelMatrix,0,1,0,0);
        }
        Matrix.multiplyMM(mMVMatrix, 0, mFrameViewMatrix, 0, mFrameModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mFrameProjectionMatrix, 0, mMVMatrix, 0);

        //for drawing on framebuffer
        if (left_or_right==0){
            Matrix.frustumM(mProjectionMatrix,0,frustumShift-aspect,frustumShift+aspect,-1,1,nearZ,20);
            Matrix.setLookAtM(mViewMatrix,0,
                    -IOD/2,0,0.1f,
                    0,0,screenZ,
                    0,1,0);
            modelShift=IOD/2;
        }else {
            Matrix.frustumM(mProjectionMatrix,0,-frustumShift-aspect,-frustumShift+aspect,-1,1,nearZ,20);
            Matrix.setLookAtM(mViewMatrix,0,
                    IOD/2,0,0.1f,
                    0,0,screenZ,
                    0,1,0);
            modelShift=-IOD/2;
        }
        Matrix.translateM(mModelMatrix,0,modelShift,0,depthZ);

        createBuffer(width,height);
    }

    public float[] getModel(float x, float y, float z){
        float[] mModel=new float[16];
        Matrix.setIdentityM(mModel, 0);
        float[] mRotationMatrix=new float[16];
        float[] mRotationMatrix2=new float[16];
        float[] mRotationMatrix3=new float[16];
        Matrix.setRotateM(mRotationMatrix2, 0, y, 0f, 1f, 0);//rotate around the y-axis
        Matrix.setRotateM(mRotationMatrix, 0, x, 1f, 0f, 0);//rotate around the x-axis
        Matrix.setRotateM(mRotationMatrix3, 0, z, 0f, 0f, 1f);//z
        Matrix.multiplyMM(mModel, 0, mModelMatrix, 0, mRotationMatrix, 0);
        Matrix.multiplyMM(mModel, 0, mModel, 0, mRotationMatrix2, 0);
        //Matrix.multiplyMM(mModel, 0, mModel, 0, mRotationMatrix3, 0);
        return mModel;
    }

    public void draw(/*float[] mvpMatrix*/) {

        GLES32.glUseProgram(mProgram);
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES32.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES32.glBindTexture(GLES20.GL_TEXTURE_2D, textureID[0]);
        GLES32.glVertexAttribPointer(textureCoordHandle, COORDS_PER_TEX, GLES32.GL_FLOAT, false, texStride, Tex1Buffer);
        GLES32.glUniform1i(textureHandle, 1);
        // Draw the sphere
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, indexes.length, GLES32.GL_UNSIGNED_INT, indexBuffer);
    }

}
