package com.bennyplo.animation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLES32;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class Sphere {
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
                    "gl_FragColor= fragColor*vec4(vAmbientColor,1) + diffuseColor + specularColor;" +
                    "} else {" +
                    "gl_FragColor = vec4(vColor.xyz*vAmbientColor,1) + diffuseColor + specularColor; " +
                    "}" +
                    "}";
    private final FloatBuffer vertexBuffer, colorBuffer, normalBuffer, Tex1Buffer;
    private final IntBuffer indexBuffer;
    private final int mProgram;
    private int mPositionHandle, mNormalHandle, mColorHandle;
    //--------
    private int diffuseColorHandle;
    private int mMVPMatrixHandle;
    private int lightLocationHandle, uAmbientColorHandle;
    private int specularColorHandle;
    private int materialShininessHandle;
    private int attenuateHandle;

    private int Image1Handle, CoordHandle, UseHandle, SamplerHandle;
    private float[] tex1 = new float[65535];
    private static int COORDS_PER_TEX = 2;
    private static int texStride = COORDS_PER_TEX * 4;
    //--------
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3, COLOR_PER_VERTEX = 4;
    //---------
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int colorStride = COLOR_PER_VERTEX * 4;
    static float SphereVertex[];
    static float SphereColor[];
    static int SphereIndex[];
    static float SphereNormal[];
    static float lightlocation[] = new float[3];
    static float attenuation[] = new float[3];//light attenuation
    static float diffusecolor[] = new float[4];//diffuse light colour
    static float specularcolor[] = new float[4];//specular highlight colour
    static float MaterialShininess = 10f;//material shiness
    //--------

    private void createShpere(float radius, int nolatitude, int nolongitude) {
        float vertices[] = new float[65535];
        float normal[] = new float[65535];
        int pindex[] = new int[65535];
        float pcolor[] = new float[65535];
        int vertexindex = 0;
        int normindex = 0;
        int colorindex = 0;
        int indx = 0;
        float dist = 0f;
        int texInd = 0;

        for (int row = 0; row <= nolatitude; row++) {
            double theta = row * Math.PI / nolatitude;
            double sinTheta = Math.sin(theta);
            double cosTheta = Math.cos(theta);
            for (int col = 0; col <= nolongitude; col++) {
                double phi = col * 2 * Math.PI / nolongitude;
                double sinPhi = Math.sin(phi);
                double cosPhi = Math.cos(phi);
                double x = cosPhi * sinTheta;
                double y = cosTheta;
                double z = sinPhi * sinTheta;
                normal[normindex++] = (float) x;
                normal[normindex++] = (float) y;
                normal[normindex++] = (float) z;
                vertices[vertexindex++] = (float) (radius * x);
                vertices[vertexindex++] = (float) (radius * y) + dist;
                vertices[vertexindex++] = (float) (radius * z);
                pcolor[colorindex++] = 1f;
                pcolor[colorindex++] = 0;//Math.abs(tcolor);
                pcolor[colorindex++] = 0f;
                pcolor[colorindex++] = 1f;
                //--------
                tex1[texInd++] = (col / (float) nolongitude);
                tex1[texInd++] = (row / (float) nolatitude);
            }
        }
        for (int row = 0; row < nolatitude; row++) {
            for (int col = 0; col < nolongitude; col++) {
                int first = (row * (nolongitude + 1)) + col;
                int second = first + nolongitude + 1;
                pindex[indx++] = first;
                pindex[indx++] = second;
                pindex[indx++] = first + 1;
                pindex[indx++] = second;
                pindex[indx++] = second + 1;
                pindex[indx++] = first + 1;
            }
        }

        SphereVertex = Arrays.copyOf(vertices, vertexindex);
        SphereIndex = Arrays.copyOf(pindex, indx);
        SphereNormal = Arrays.copyOf(normal, normindex);
        SphereColor = Arrays.copyOf(pcolor, colorindex);
    }

    private int texHandler() {
        int[] handle = new int[1];
        GLES32.glGenTextures(1, handle, 0);

        if (handle[0] != 0) {

            Bitmap bitmap=MainActivity.getBitmap();
            GLES32.glBindTexture(GLES20.GL_TEXTURE_2D, handle[0]);
            GLES32.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES32.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0);
        } else {
            throw new RuntimeException("no handle generated");
        }

        return handle[0];
    }

    private int texHandler(final Context context, int ResID){
        int[] handle=new int[1];
        GLES32.glGenTextures(1,handle,0);

        if (handle[0]!=0){
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inScaled=false;

            Bitmap bitmap=BitmapFactory.decodeResource(context.getResources(),ResID,options);
            GLES32.glBindTexture(GLES20.GL_TEXTURE_2D,handle[0]);
            GLUtils.texImage2D(GLES32.GL_TEXTURE_2D,0,bitmap,0);
            bitmap.recycle();
        }
        else {
            throw new RuntimeException("no handle generated");
        }

        return handle[0];
    }

    public Sphere() {

        createShpere(2, 30, 30);
        // initialize vertex byte buffer for shape coordinates
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

        Image1Handle = texHandler(MainActivity.getContext(),R.drawable.iclc);
        GLES32.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES32.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        lightlocation[0] = -1;
        lightlocation[1] = 1;
        lightlocation[2] = -5;
        specularcolor[0] = 1;
        specularcolor[1] = 1;
        specularcolor[2] = 1;
        specularcolor[3] = 1;

        int vertexShader = MyRenderer.loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyRenderer.loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES32.glCreateProgram();             // create empty OpenGL Program
        GLES32.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES32.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES32.glLinkProgram(mProgram);                  // link the  OpenGL program to create an executable
        GLES32.glUseProgram(mProgram);// Add program to OpenGL environment

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES32.glGetAttribLocation(mProgram, "aVertexPosition");
        GLES32.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the triangle coordinate data
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, vertexStride, vertexBuffer);
        MyRenderer.checkGlError("glVertexAttribPointer");
        mColorHandle = GLES32.glGetAttribLocation(mProgram, "aVertexColor");
        GLES32.glEnableVertexAttribArray(mColorHandle);
        GLES32.glVertexAttribPointer(mColorHandle, COLOR_PER_VERTEX, GLES32.GL_FLOAT, false, colorStride, colorBuffer);

        mNormalHandle = GLES32.glGetAttribLocation(mProgram, "aVertexNormal");
        GLES32.glEnableVertexAttribArray(mNormalHandle);
        GLES32.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, vertexStride, normalBuffer);
        MyRenderer.checkGlError("glVertexAttribPointer");
        // get handle to shape's transformation matrix
        //nMatrixHandle=GLES32.glGetUniformLocation(mProgram, "uNMatrix");
        lightLocationHandle = GLES32.glGetUniformLocation(mProgram, "uLightSourceLocation");
        diffuseColorHandle = GLES32.glGetUniformLocation(mProgram, "uDiffuseColor");
        diffusecolor[0] = 1;
        diffusecolor[1] = 1;
        diffusecolor[2] = 1;
        diffusecolor[3] = 1;
        attenuateHandle = GLES32.glGetUniformLocation(mProgram, "uAttenuation");
        attenuation[0] = 1;
        attenuation[1] = 0.14f;
        attenuation[2] = 0.07f;
        uAmbientColorHandle = GLES32.glGetUniformLocation(mProgram, "uAmbientColor");
        MyRenderer.checkGlError("uAmbientColor");
        specularColorHandle = GLES32.glGetUniformLocation(mProgram, "uSpecularColor");
        materialShininessHandle = GLES32.glGetUniformLocation(mProgram, "uMaterialShininess");
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyRenderer.checkGlError("glGetUniformLocation-mMVPMatrixHandle");

        CoordHandle = GLES32.glGetAttribLocation(mProgram, "aTexCoord");
        GLES32.glEnableVertexAttribArray(CoordHandle);
        SamplerHandle = GLES32.glGetUniformLocation(mProgram, "tex1");
        UseHandle = GLES32.glGetUniformLocation(mProgram, "useTex");

        MyRenderer.checkGlError("glGetUniformLocation");
    }

    private float ambCoeff = 1f;

    public void draw(float[] mvpMatrix, int use) {

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
        GLES32.glUniform1i(UseHandle, use);
        // Draw the sphere
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, SphereIndex.length, GLES32.GL_UNSIGNED_INT, indexBuffer);
    }

    public void setLightLocation(float px, float py, float pz) {
        lightlocation[0] = px;
        lightlocation[1] = py;
        lightlocation[2] = pz;
    }
}
