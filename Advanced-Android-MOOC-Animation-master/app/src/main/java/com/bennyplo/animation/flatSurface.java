package com.bennyplo.animation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLES32;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class flatSurface {

    private final String vertexShaderCode =
            "attribute vec3 aVertexPosition;" + "uniform mat4 uMVPMatrix;varying vec4 vColor;" +
                    "attribute vec3 aVertexNormal;" +//attribute variable for normal vectors
                    "attribute vec4 aVertexColor;" +//attribute variable for vertex colors
                    "uniform vec3 uLightSourceLocation;" +//location of the light source (for diffuse and specular light)
                    "uniform vec3 uAmbientColor;" +//uniform variable for Ambient color
                    "varying vec3 vAmbientColor;" +
                    "uniform vec4 uDiffuseColor;" +//color of the diffuse light
                    "varying vec4 vDiffuseColor;" +
                    "varying float vDiffuseLightWeighting;" +//diffuse light intensity
                    "uniform vec3 uAttenuation;" +//light attenuation
                    "uniform vec4 uSpecularColor;" +
                    "varying vec4 vSpecularColor;" +
                    "varying float vSpecularLightWeighting; " +
                    "uniform float uMaterialShininess;" +
                    "attribute vec2 aTexCoord;" +
                    "varying vec2 vTexCoord;" +
                    //----------
                    "void main() {" +
                    "gl_Position = uMVPMatrix *vec4(aVertexPosition,1.0);" +
                    "vec4 mvPosition=uMVPMatrix*vec4(aVertexPosition,1.0);" +
                    "vec3 lightDirection=normalize(uLightSourceLocation-mvPosition.xyz);" +
                    "vec3 transformedNormal = normalize((uMVPMatrix * vec4(aVertexNormal, 0.0)).xyz);" +
                    "vAmbientColor=uAmbientColor;" +
                    "vDiffuseColor=uDiffuseColor;" +
                    "vSpecularColor=uSpecularColor; " +
                    "vTexCoord=aTexCoord;" +
                    "vec3 eyeDirection=normalize(-mvPosition.xyz);" +
                    "vec3 reflectionDirection=reflect(-lightDirection,transformedNormal);" +
                    "vec3 vertexToLightSource = mvPosition.xyz-uLightSourceLocation;" +
                    "float diff_light_dist = length(vertexToLightSource);" +
                    "float attenuation = 1.0 / (uAttenuation.x" +
                    "                           + uAttenuation.y * diff_light_dist" +
                    "                           + uAttenuation.z * diff_light_dist * diff_light_dist);" +
                    "vDiffuseLightWeighting =attenuation*max(dot(transformedNormal,lightDirection),0.0);" +
                    "vSpecularLightWeighting=attenuation*pow(max(dot(reflectionDirection,eyeDirection), 0.0), uMaterialShininess);" +
                    "vColor=aVertexColor;" +
                    "}";
    private final String fragmentShaderCode =
            "precision lowp float;" +
                    "varying vec4 vColor; " +
                    "varying vec3 vAmbientColor;" +
                    "varying vec4 vDiffuseColor;" +
                    "varying float vDiffuseLightWeighting;" +
                    "varying vec4 vSpecularColor;" +
                    "varying float vSpecularLightWeighting; " +
                    "varying vec2 vTexCoord;" +
                    "uniform sampler2D tex1;" +
                    "uniform bool useTex;" +

                    "void main() {" +
                    "vec4 diffuseColor=vDiffuseLightWeighting*vDiffuseColor;" +
                    "vec4 specularColor=vSpecularLightWeighting*vSpecularColor;" +
                    "if(useTex) {" +
                    "vec4 fragColor= texture2D(tex1,vec2(vTexCoord.s,vTexCoord.t));" +
                    "if(fragColor.a<0.1) discard;"+
                    "gl_FragColor= fragColor*vec4(vAmbientColor,1) + diffuseColor + specularColor;" +
                    "} else {" +
                    "gl_FragColor = vec4(vColor.xyz*vAmbientColor,1) + diffuseColor + specularColor; " +
                    "}" +
                    "}";

    private final FloatBuffer vertexBuffer,colorBuffer,Tex1Buffer,normalBuffer;
    private final IntBuffer indexBuffer;
    private final int mProgram;
    private int mPositionHandle, mNormalHandle, mColorHandle;

    private int diffuseColorHandle;
    private int mMVPMatrixHandle;
    private int lightLocationHandle, uAmbientColorHandle;
    private int specularColorHandle;
    private int materialShininessHandle;
    private int attenuateHandle;

    private int Image1Handle, CoordHandle, UseHandle, SamplerHandle;
    private static int COORDS_PER_TEX = 2;
    private static int texStride = COORDS_PER_TEX * 4;
    static final int COORDS_PER_VERTEX = 3, COLOR_PER_VERTEX = 4;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int colorStride = COLOR_PER_VERTEX * 4;

    static final float[] lightlocation ={2,1,2};
    static final float[] attenuation ={1,0.14f,0.07f};//light attenuation
    static final float[] diffusecolor ={1,1,1,1};//diffuse light colour
    static final float[] specularcolor = {1,1,1,1};//specular highlight colour
    static final float MaterialShininess = 10f;//material shiness

    static float[] SphereVertex=new float[16];

    static final float[] SphereVertex1={
            -1,-1,0.4f,
            1,-1,0.4f,
            1,1,0.4f,
            -1,1,0.4f
    };

    static final float[] SphereVertex2={
            1,-1,0.4f,
            4,-1,0.4f,
            4,1,0.4f,
            1,1,0.4f
    };

    static final float[] SphereColor={
            0,1,0,0,
            0,1,0,0,
            0,1,0,0,
            0,1,0,0
    };
    static final int[] SphereIndex={
            0,1,2,
            2,3,0
    };
    static final float[] SphereNormal={
            0,0,0.4f,
            0,0,0.4f,
            0,0,0.4f,
            0,0,0.4f
    };

    static final float[] tex1 ={
            0,1,
            1,1,
            1,0,
            0,0,
    };

    private int texHandler(final Context context, int ResID){
        int[] handle=new int[1];
        GLES32.glGenTextures(1,handle,0);

        if (handle[0]!=0){
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inScaled=false;

            Bitmap bitmap=BitmapFactory.decodeResource(context.getResources(),ResID,options);
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D,handle[0]);
            GLES32.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES32.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLUtils.texImage2D(GLES32.GL_TEXTURE_2D,0,bitmap,0);
            bitmap.recycle();
        }
        else {
            throw new RuntimeException("no handle generated");
        }

        return handle[0];
    }

    public flatSurface(int imageID) {

        if (imageID==R.drawable.spoonchar){
            SphereVertex= Arrays.copyOf(SphereVertex2,SphereVertex2.length);
        }else {
            SphereVertex= Arrays.copyOf(SphereVertex1,SphereVertex1.length);
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(SphereVertex.length * 4);// (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(SphereVertex);
        vertexBuffer.position(0);
        IntBuffer ib = IntBuffer.allocate(SphereIndex.length);
        indexBuffer = ib;
        indexBuffer.put(SphereIndex);
        indexBuffer.position(0);
        ByteBuffer cb = ByteBuffer.allocateDirect(SphereColor.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(SphereColor);
        colorBuffer.position(0);
        ByteBuffer nb = ByteBuffer.allocateDirect(SphereNormal.length * 4);// (# of coordinate values * 4 bytes per float)
        nb.order(ByteOrder.nativeOrder());
        normalBuffer = nb.asFloatBuffer();
        normalBuffer.put(SphereNormal);
        normalBuffer.position(0);
        ByteBuffer t = ByteBuffer.allocateDirect(tex1.length * 4);// (# of coordinate values * 4 bytes per float)
        t.order(ByteOrder.nativeOrder());
        Tex1Buffer = t.asFloatBuffer();
        Tex1Buffer.put(tex1);
        Tex1Buffer.position(0);

        int vertexShader = MyRenderer.loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyRenderer.loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES32.glCreateProgram();             // create empty OpenGL Program
        GLES32.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES32.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES32.glLinkProgram(mProgram);                  // link the  OpenGL program to create an executable
        GLES32.glUseProgram(mProgram);

        Image1Handle = texHandler(MainActivity.getContext(),imageID);
        mPositionHandle = GLES32.glGetAttribLocation(mProgram, "aVertexPosition");
        GLES32.glEnableVertexAttribArray(mPositionHandle);
        mColorHandle = GLES32.glGetAttribLocation(mProgram, "aVertexColor");
        GLES32.glEnableVertexAttribArray(mColorHandle);
        mNormalHandle = GLES32.glGetAttribLocation(mProgram, "aVertexNormal");
        GLES32.glEnableVertexAttribArray(mNormalHandle);

        lightLocationHandle = GLES32.glGetUniformLocation(mProgram, "uLightSourceLocation");
        diffuseColorHandle = GLES32.glGetUniformLocation(mProgram, "uDiffuseColor");
        attenuateHandle = GLES32.glGetUniformLocation(mProgram, "uAttenuation");
        uAmbientColorHandle = GLES32.glGetUniformLocation(mProgram, "uAmbientColor");
        specularColorHandle = GLES32.glGetUniformLocation(mProgram, "uSpecularColor");
        materialShininessHandle = GLES32.glGetUniformLocation(mProgram, "uMaterialShininess");
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix");

        CoordHandle = GLES32.glGetAttribLocation(mProgram, "aTexCoord");
        GLES32.glEnableVertexAttribArray(CoordHandle);
        SamplerHandle = GLES32.glGetUniformLocation(mProgram, "tex1");
        UseHandle = GLES32.glGetUniformLocation(mProgram, "useTex");

        MyRenderer.checkGlError("ERRor");

    }

    private final float ambCoeff = 1f;

    public void draw(float[] mvpMatrix) {

        Log.i("TA", "draw: flat");
        GLES32.glUseProgram(mProgram);
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        //MyRenderer.checkGlError("glUniformMatrix4fv");
        GLES32.glUniform3fv(lightLocationHandle, 1, lightlocation, 0);
        GLES32.glUniform4fv(diffuseColorHandle, 1, diffusecolor, 0);
        GLES32.glUniform3fv(attenuateHandle, 1, attenuation, 0);
        GLES32.glUniform3f(uAmbientColorHandle, ambCoeff, ambCoeff, ambCoeff);
        GLES32.glUniform4fv(specularColorHandle, 1, specularcolor, 0);
        GLES32.glUniform1f(materialShininessHandle, MaterialShininess);
        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES32.glVertexAttribPointer(mColorHandle, COLOR_PER_VERTEX,
                GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        GLES32.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, normalBuffer);
        GLES32.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES32.glBindTexture(GLES20.GL_TEXTURE_2D, Image1Handle);
        GLES32.glVertexAttribPointer(CoordHandle, COORDS_PER_TEX, GLES32.GL_FLOAT, false, texStride, Tex1Buffer);
        GLES32.glUniform1i(SamplerHandle, 0);
        GLES32.glUniform1i(UseHandle, 1);
        // Draw the sphere
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, SphereIndex.length, GLES32.GL_UNSIGNED_INT, indexBuffer);
    }
}
