package com.bennyplo.animation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLES32;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL;

public class ArbitaryShape {
    private final String vertexShaderCode =
            "attribute vec3 aVertexPosition;" +//vertex of an object
                    "attribute vec4 aVertexColor;" +//the colour  of the object
                    "uniform mat4 uMVPMatrix;" +//model view  projection matrix
                    "varying vec4 vColor;" +//variable to be accessed by the fragment shader

                    "attribute vec3 aVertexNormal;" +//normal to a vertex
                    "uniform vec3 lightLocation;" +//light source location

                    "uniform vec4 uDiffuseColor;" +//for vShader
                    "varying vec4 vDiffuseColor;" +//for fShader
                    "varying float lightWeight;" +//resulting diffuse intensity
                    "uniform vec3 attenuation;" +//attenuation coefficients

                    "uniform vec3 uAmbient;" +//ambient coloring values for vShader
                    "varying vec3 vAmbient;" +//ambient coloring values for fShader

                    "uniform vec4 uSpecular;" +
                    "varying vec4 vSpecular;" +
                    "varying float specLightWeight;" +//resulting specular intensity
                    "uniform float shininess;" +
                    "attribute vec2 aTexCoord;"+
                    "varying vec2 vTexCoord;"+

                    "void main() {" +
                    "gl_Position = uMVPMatrix * vec4(aVertexPosition, 1.0);" +//calculate the position of the vertex

                    "vec4 mvPos=uMVPMatrix*vec4(aVertexPosition,1.0);" +//store each vertex position
                    "vec3 unitDiffuseLightDirection=normalize(lightLocation-mvPos.xyz);" +//unit light direction vector
                    "vec3 unitNormal=normalize((uMVPMatrix*vec4(aVertexNormal,0.0)).xyz);" +//unit normal vector
                    "vec3 unitReflection=reflect(-unitDiffuseLightDirection,unitNormal);" +
                    "vec3 unitView=normalize(-mvPos.xyz);" +
                    "vDiffuseColor=uDiffuseColor;" +
                    "vSpecular=uSpecular;" +
                    "vAmbient=uAmbient;" +
                    "vec3 lightToSource=lightLocation-mvPos.xyz;" +
                    "float distance=length(lightToSource);" +
                    "float attenuation2=1.0/(attenuation.x+(attenuation.y)*distance+(attenuation.z)*distance*distance);" +
                    "lightWeight=attenuation2*max(dot(unitDiffuseLightDirection,unitNormal),0.0);" +
                    "specLightWeight=attenuation2*pow(max(dot(unitView,unitReflection),0.0),shininess);" +
                    "vTexCoord=aTexCoord;"+

                    "vColor=aVertexColor; " +//get the colour from the application program
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" + //define the precision of float
                    "varying vec4 vColor;" + //variable from the vertex shader
                    "varying vec4 vDiffuseColor;" +
                    "varying float lightWeight;" +
                    "varying vec3 vAmbient;" +
                    "varying vec4 vSpecular;" +
                    "varying float specLightWeight;"+
                    "varying vec2 vTexCoord;"+
                    "uniform sampler2D tex1;"+
                    "uniform bool useTex;"+

                    "void main() {" +
                    "vec4 DiffuseColorChange=vDiffuseColor*lightWeight;" +
                    "vec4 SpecColorChange=vSpecular*specLightWeight;" +

                    "if(useTex) {"+
                    "vec4 fragColor= texture2D(tex1,vec2(vTexCoord.s,vTexCoord.t));"+
                    "gl_FragColor= fragColor*vec4(vAmbient,1) + DiffuseColorChange + SpecColorChange;"+
                    "} else {"+
                    "gl_FragColor = vec4(vColor.xyz*vAmbient,1) + DiffuseColorChange + SpecColorChange; " +
                    "}"+
                    "}";//change the colour based on the variable from the vertex shader and illumination

    private final FloatBuffer vertexBuffer, colorBuffer, Tex1Buffer, Tex2Buffer,TexMidBuffer;
    private final IntBuffer indexBuffer;
    private final FloatBuffer vertex2Buffer, color2Buffer;
    private final IntBuffer index2Buffer;
    private final FloatBuffer ringVertexBuffer, ringColorBuffer;
    private final IntBuffer ringIndexBuffer;
    private final FloatBuffer normal1Buffer, normal2Buffer, normal3Buffer;
    private final int mProgram;
    private int mPositionHandle, mColorHandle, normalHandle, lightLocationHandle, diffuseColorHandle, attenuationHandle, ambientHandle, specHandle, shinyHandle;
    private int mMVPMatrixHandle;

    private int Image1Handle,Image2Handle,Image3Handle,CoordHandle,UseHandle,SamplerHandle;
    private float[] tex1=new float[65535];
    private float[] tex2=new float[65535];
    private float[] texMid=new float[65535];
    private static int COORDS_PER_TEX=2;
    private static int texStride=COORDS_PER_TEX*4;

    //---------
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static final int COLOR_PER_VERTEX = 4;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int colorStride = COLOR_PER_VERTEX * 4;//4 bytes per vertex
    static float SphereVertex[];
    static int SphereIndex[];
    static float SphereColor[];
    //2nd sphere
    static float Sphere2Vertex[];
    static int Sphere2Index[];
    static float Sphere2Color[];
    //ring
    static float ringVertex[];
    static int ringIndex[];
    static float ringColor[];

    static float[] SphereVertexNormal;
    static float[] Sphere2VertexNormal;
    static float[] ringVertexNormal;
    static float[] diffuseLightLocation = new float[3];
    static float[] diffuseColor = new float[4];
    static float[] attenuation = new float[3];

    static int shininess = 32;
    static float[] specColors = new float[4];

    private void createSphere(float radius, int nolatitude, int nolongitude) {
        float vertices[] = new float[65535];
        int index[] = new int[65535];
        float color[] = new float[65535];
        int pnormlen = (nolongitude + 1) * 3 * 3;
        int vertexindex = 0;
        int colorindex = 0;
        int indx = 0;
        float[] vertices2 = new float[65535];
        int[] index2 = new int[65535];
        float[] color2 = new float[65525];
        int vertex2index = 0;
        int color2index = 0;
        int indx2 = 0;
        float[] ring_vertices = new float[65535];
        int[] ring_index = new int[65535];
        float[] ring_color = new float[65525];
        int rvindx = 0;
        int rcindex = 0;
        int rindx = 0;
        float dist = 3;
        int plen = (nolongitude + 1) * 3 * 3;// ghis variable is used since the last if(row==20) will fill up along with the first condition so we need to skip indexes
        int pcolorlen = (nolongitude + 1) * 4 * 3;

        float[] normal1 = new float[65535];
        float[] normal2 = new float[65535];
        float[] normal3 = new float[65535];
        int indxnormal1 = 0;
        int indxnormal2 = 0;
        int indxnormal3 = 0;

        int tex1ind=0;
        int tex2ind=0;
        int tex3ind=0;

        for (int row = 0; row < nolatitude + 1; row++) {
            double theta = row * Math.PI / nolatitude;
            double sinTheta = Math.sin(theta);
            double cosTheta = Math.cos(theta);
            float tcolor = -0.5f;
            float tcolorinc = 1 / (float) (nolongitude + 1);
            for (int col = 0; col < nolongitude + 1; col++) {
                double phi = col * 2 * Math.PI / nolongitude;
                double sinPhi = Math.sin(phi);
                double cosPhi = Math.cos(phi);
                double x = cosPhi * sinTheta;
                double y = cosTheta;
                double z = sinPhi * sinTheta;
                vertices[vertexindex++] = (float) (radius * x);
                vertices[vertexindex++] = (float) (radius * y) + dist;
                vertices[vertexindex++] = (float) (radius * z);

                vertices2[vertex2index++] = (float) (radius * x);
                vertices2[vertex2index++] = (float) (radius * y) - dist;
                vertices2[vertex2index++] = (float) (radius * z);

                color[colorindex++] = 1;
                color[colorindex++] = Math.abs(tcolor);
                color[colorindex++] = 0;
                color[colorindex++] = 1;

                color2[color2index++] = 0;
                color2[color2index++] = 1;
                color2[color2index++] = Math.abs(tcolor);
                color2[color2index++] = 1;

                normal1[indxnormal1++] = (float) (radius * x);
                normal1[indxnormal1++] = (float) (radius * y) + dist;
                normal1[indxnormal1++] = (float) (radius * z);

                normal2[indxnormal2++] = (float) (radius * x);
                normal2[indxnormal2++] = (float) (radius * y) - dist;
                normal2[indxnormal2++] = (float) (radius * z);

                tex1[tex1ind++]=(float)col/nolongitude;
                tex1[tex1ind++]=(float)row/nolatitude;
                tex2[tex2ind++]=(float)col/nolongitude;
                tex2[tex2ind++]=(float)row/nolatitude;

                if (row == 20) {
                    ring_vertices[rvindx++] = (float) (radius * x);
                    ring_vertices[rvindx++] = (float) (radius * y) + dist;
                    ring_vertices[rvindx++] = (float) (radius * z);
                    ring_color[rcindex++] = 1;
                    ring_color[rcindex++] = Math.abs(tcolor);
                    ring_color[rcindex++] = 0;
                    ring_color[rcindex++] = 1;

                    normal3[indxnormal3++] = (float) (radius * x);
                    normal3[indxnormal3++] = (float) (radius * y) + dist;
                    normal3[indxnormal3++] = (float) (radius * z);

                    texMid[tex3ind++]=(float)col/nolongitude;
                    texMid[tex3ind++]=0;
                }
                if (row == 15) {
                    ring_vertices[rvindx++] = (float) (radius * x) / 2;
                    ring_vertices[rvindx++] = (float) (radius * y) / 2 + 0.2f * dist;
                    ring_vertices[rvindx++] = (float) (radius * z) / 2;
                    ring_color[rcindex++] = 1;
                    ring_color[rcindex++] = Math.abs(tcolor);
                    ring_color[rcindex++] = 0;
                    ring_color[rcindex++] = 1;

                    normal3[indxnormal3++] = (float) (radius * x) / 2;
                    normal3[indxnormal3++] = (float) (radius * y) / 2 + 0.2f * dist;
                    normal3[indxnormal3++] = (float) (radius * z) / 2;

                    texMid[tex3ind++]=(float)col/nolongitude;
                    texMid[tex3ind++]=0.33f;
                }
                if (row == 10) {
                    ring_vertices[rvindx++] = (float) (radius * x) / 2;
                    ring_vertices[rvindx++] = (float) (radius * y) / 2 - 0.1f * dist;
                    ring_vertices[rvindx++] = (float) (radius * z) / 2;
                    ring_color[rcindex++] = 0;
                    ring_color[rcindex++] = 1;
                    ring_color[rcindex++] = Math.abs(tcolor);
                    ring_color[rcindex++] = 1;

                    normal3[indxnormal3++] = (float) (radius * x) / 2;
                    normal3[indxnormal3++] = (float) (radius * y) / 2 - 0.1f * dist;
                    normal3[indxnormal3++] = (float) (radius * z) / 2;

                    texMid[tex3ind++]=(float)col/nolongitude;
                    texMid[tex3ind++]=0.66f;
                }
                if (row == 20) {
                    ring_vertices[plen++] = (float) (radius * x);
                    ring_vertices[plen++] = (float) (-radius * y) - dist;
                    ring_vertices[plen++] = (float) (radius * z);
                    ring_color[pcolorlen++] = 0;
                    ring_color[pcolorlen++] = 1;
                    ring_color[pcolorlen++] = Math.abs(tcolor);
                    ring_color[pcolorlen++] = 1;

                    normal3[indxnormal3++] = (float) (radius * x);
                    normal3[indxnormal3++] = (float) (-radius * y) - dist;
                    normal3[indxnormal3++] = (float) (radius * z);

                    texMid[tex3ind++]=(float)col/nolongitude;
                    texMid[tex3ind++]=1;
                }
                tcolor += tcolorinc;
            }
        }
        //index buffer
        for (int row = 0; row < nolatitude; row++) {
            for (int col = 0; col < nolongitude; col++) {
                int P0 = (row * (nolongitude + 1)) + col;
                int P1 = P0 + nolongitude + 1;
                index[indx++] = P1;
                index[indx++] = P0;
                index[indx++] = P0 + 1;
                index[indx++] = P1 + 1;
                index[indx++] = P1;
                index[indx++] = P0 + 1;

                index2[indx2++] = P1;
                index2[indx2++] = P0;
                index2[indx2++] = P0 + 1;
                index2[indx2++] = P1 + 1;
                index2[indx2++] = P1;
                index2[indx2++] = P0 + 1;

            }
        }
        rvindx = (nolongitude + 1) * 3 * 4;
        rcindex = (nolongitude + 1) * 4 * 4;
        plen = nolongitude + 1;

        for (int j = 0; j < plen - 1; j++) {// here order seems reversed since row 20 comes first which is above row 15
            ring_index[rindx++] = j;
            ring_index[rindx++] = j + plen;
            ring_index[rindx++] = j + 1;
            ring_index[rindx++] = j + plen + 1;
            ring_index[rindx++] = j + 1;
            ring_index[rindx++] = j + plen;

            ring_index[rindx++] = j + plen;
            ring_index[rindx++] = j + plen * 2;
            ring_index[rindx++] = j + plen + 1;
            ring_index[rindx++] = j + plen * 2 + 1;
            ring_index[rindx++] = j + plen + 1;
            ring_index[rindx++] = j + plen * 2;

            ring_index[rindx++] = j + plen * 3;
            ring_index[rindx++] = j;
            ring_index[rindx++] = j + 1;
            ring_index[rindx++] = j + 1;
            ring_index[rindx++] = j + plen * 3 + 1;
            ring_index[rindx++] = j + plen * 3;
        }


        //set the buffers
        SphereVertex = Arrays.copyOf(vertices, vertexindex);
        SphereIndex = Arrays.copyOf(index, indx);
        SphereColor = Arrays.copyOf(color, colorindex);
        Sphere2Vertex = Arrays.copyOf(vertices2, vertex2index);
        Sphere2Index = Arrays.copyOf(index2, indx2);
        Sphere2Color = Arrays.copyOf(color2, color2index);
        ringVertex = Arrays.copyOf(ring_vertices, rvindx);
        ringColor = Arrays.copyOf(ring_color, rcindex);
        ringIndex = Arrays.copyOf(ring_index, rindx);

        indxnormal3 = (nolongitude + 1) * 3 * 4;

        SphereVertexNormal = Arrays.copyOf(normal1, indxnormal1);
        Sphere2VertexNormal = Arrays.copyOf(normal2, indxnormal2);
        ringVertexNormal = Arrays.copyOf(normal3, indxnormal3);
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

    public ArbitaryShape() {
        createSphere(2, 30, 30);
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(SphereVertex.length * 4);// (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(SphereVertex);
        vertexBuffer.position(0);
        ByteBuffer cb = ByteBuffer.allocateDirect(SphereColor.length * 4);// (# of coordinate values * 4 bytes per float)
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(SphereColor);
        colorBuffer.position(0);
        IntBuffer ib = IntBuffer.allocate(SphereIndex.length);
        indexBuffer = ib;
        indexBuffer.put(SphereIndex);
        indexBuffer.position(0);
        //2nd sphere
        ByteBuffer bb2 = ByteBuffer.allocateDirect(Sphere2Vertex.length * 4);// (# of coordinate values * 4 bytes per float)
        bb2.order(ByteOrder.nativeOrder());
        vertex2Buffer = bb2.asFloatBuffer();
        vertex2Buffer.put(Sphere2Vertex);
        vertex2Buffer.position(0);
        ByteBuffer cb2 = ByteBuffer.allocateDirect(Sphere2Color.length * 4);// (# of coordinate values * 4 bytes per float)
        cb2.order(ByteOrder.nativeOrder());
        color2Buffer = cb2.asFloatBuffer();
        color2Buffer.put(Sphere2Color);
        color2Buffer.position(0);
        IntBuffer ib2 = IntBuffer.allocate(Sphere2Index.length);
        index2Buffer = ib2;
        index2Buffer.put(SphereIndex);
        index2Buffer.position(0);
        ByteBuffer rbb = ByteBuffer.allocateDirect(ringVertex.length * 4);// (# of coordinate values * 4 bytes per float)
        rbb.order(ByteOrder.nativeOrder());
        ringVertexBuffer = rbb.asFloatBuffer();
        ringVertexBuffer.put(ringVertex);
        ringVertexBuffer.position(0);
        ByteBuffer rcb = ByteBuffer.allocateDirect(ringColor.length * 4);// (# of coordinate values * 4 bytes per float)
        rcb.order(ByteOrder.nativeOrder());
        ringColorBuffer = rcb.asFloatBuffer();
        ringColorBuffer.put(ringColor);
        ringColorBuffer.position(0);
        IntBuffer rib = IntBuffer.allocate(ringIndex.length);
        ringIndexBuffer = rib;
        ringIndexBuffer.put(ringIndex);
        ringIndexBuffer.position(0);
        //--------------------
        ByteBuffer n = ByteBuffer.allocateDirect(SphereVertexNormal.length * 4);// (# of coordinate values * 4 bytes per float)
        n.order(ByteOrder.nativeOrder());
        normal1Buffer = n.asFloatBuffer();
        normal1Buffer.put(SphereVertexNormal);
        normal1Buffer.position(0);
        ByteBuffer n2 = ByteBuffer.allocateDirect(Sphere2VertexNormal.length * 4);// (# of coordinate values * 4 bytes per float)
        n2.order(ByteOrder.nativeOrder());
        normal2Buffer = n2.asFloatBuffer();
        normal2Buffer.put(Sphere2VertexNormal);
        normal2Buffer.position(0);
        ByteBuffer n3 = ByteBuffer.allocateDirect(ringVertexNormal.length * 4);// (# of coordinate values * 4 bytes per float)
        n3.order(ByteOrder.nativeOrder());
        normal3Buffer = n3.asFloatBuffer();
        normal3Buffer.put(ringVertexNormal);
        normal3Buffer.position(0);

        ByteBuffer t1 = ByteBuffer.allocateDirect(tex1.length * 4);// (# of coordinate values * 4 bytes per float)
        t1.order(ByteOrder.nativeOrder());
        Tex1Buffer = t1.asFloatBuffer();
        Tex1Buffer.put(tex1);
        Tex1Buffer.position(0);
        ByteBuffer t2 = ByteBuffer.allocateDirect(tex2.length * 4);// (# of coordinate values * 4 bytes per float)
        t2.order(ByteOrder.nativeOrder());
        Tex2Buffer = t2.asFloatBuffer();
        Tex2Buffer.put(tex2);
        Tex2Buffer.position(0);
        ByteBuffer t3 = ByteBuffer.allocateDirect(texMid.length * 4);// (# of coordinate values * 4 bytes per float)
        t3.order(ByteOrder.nativeOrder());
        TexMidBuffer = t3.asFloatBuffer();
        TexMidBuffer.put(texMid);
        TexMidBuffer.position(0);

        Image1Handle=texHandler(MainActivity.getContext(),R.drawable.iclc);
        GLES32.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
        GLES32.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_NEAREST);
        Image2Handle=texHandler(MainActivity.getContext(),R.drawable.iclc);
        GLES32.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
        GLES32.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_NEAREST);
        Image3Handle=texHandler(MainActivity.getContext(),R.drawable.iclc);
        //GLES32.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        //GLES32.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
        GLES32.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
        GLES32.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_NEAREST);
        //----------
        // prepare shaders and OpenGL program
        int vertexShader = MyRenderer.loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyRenderer.loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES32.glCreateProgram();             // create empty OpenGL Program
        GLES32.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES32.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES32.glLinkProgram(mProgram);                  // link the  OpenGL program to create an executable
        GLES32.glUseProgram(mProgram);// Add program to OpenGL environment
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES32.glGetAttribLocation(mProgram, "aVertexPosition");
        // Enable a handle to the triangle vertices
        GLES32.glEnableVertexAttribArray(mPositionHandle);

        mColorHandle = GLES32.glGetAttribLocation(mProgram, "aVertexColor");
        // Enable a handle to the  colour
        GLES32.glEnableVertexAttribArray(mColorHandle);

        // Prepare the colour coordinate data
        GLES32.glVertexAttribPointer(mColorHandle, COLOR_PER_VERTEX, GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        //---------
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix");
        normalHandle = GLES32.glGetAttribLocation(mProgram, "aVertexNormal");
        GLES32.glEnableVertexAttribArray(normalHandle);
        lightLocationHandle = GLES32.glGetUniformLocation(mProgram, "lightLocation");
        diffuseColorHandle = GLES32.glGetUniformLocation(mProgram, "uDiffuseColor");
        attenuationHandle = GLES32.glGetUniformLocation(mProgram, "attenuation");
        ambientHandle = GLES32.glGetUniformLocation(mProgram, "uAmbient");
        shinyHandle = GLES32.glGetUniformLocation(mProgram, "shininess");
        specHandle = GLES32.glGetUniformLocation(mProgram, "uSpecular");

        diffuseLightLocation[0] = 3;
        diffuseLightLocation[1] = 2;
        diffuseLightLocation[2] = 2;
        for (int i = 0; i < 4; i++) {
            diffuseColor[i] = 1;
            specColors[i]=1;
        }
        attenuation[0] = 1;
        attenuation[1] = 0.35f;
        attenuation[2] = 0.44f;

        CoordHandle=GLES32.glGetAttribLocation(mProgram,"aTexCoord");
        GLES32.glEnableVertexAttribArray(CoordHandle);
        SamplerHandle=GLES32.glGetUniformLocation(mProgram,"tex1");
        UseHandle=GLES32.glGetUniformLocation(mProgram,"useTex");

        MyRenderer.checkGlError("glGetUniformLocation");
    }

    private float ambCoeff=1.5f;

    public void draw(float[] mvpMatrix) {

        GLES32.glUseProgram(mProgram);
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLES32.glUniform3fv(lightLocationHandle, 1, diffuseLightLocation, 0);
        GLES32.glUniform4fv(diffuseColorHandle, 1, diffuseColor, 0);
        GLES32.glUniform3fv(attenuationHandle, 1, attenuation, 0);
        GLES32.glUniform4fv(specHandle, 1, specColors, 0);
        GLES32.glUniform3f(ambientHandle, ambCoeff,ambCoeff,ambCoeff);
        GLES32.glUniform1f(shinyHandle,shininess);
        MyRenderer.checkGlError("glUniform3fv");
        MyRenderer.checkGlError("glUniform1f");
        MyRenderer.checkGlError("glUniform3f");
        MyRenderer.checkGlError("glUniform4fv");
        MyRenderer.checkGlError("glUniformMatrix4fv");

        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES32.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        GLES32.glVertexAttribPointer(normalHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, vertexStride, normal1Buffer);
        GLES32.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES32.glBindTexture(GLES20.GL_TEXTURE_2D,Image1Handle);
        GLES32.glVertexAttribPointer(CoordHandle, COORDS_PER_TEX, GLES32.GL_FLOAT, false, texStride, Tex1Buffer);
        GLES32.glUniform1i(SamplerHandle,0);
        GLES32.glUniform1i(UseHandle,0);
        // Draw the Sphere
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, SphereIndex.length, GLES32.GL_UNSIGNED_INT, indexBuffer);
        //---------
        //2nd sphere
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, vertexStride, vertex2Buffer);
        GLES32.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, colorStride, color2Buffer);
        GLES32.glVertexAttribPointer(normalHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, vertexStride, normal2Buffer);
        GLES32.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES32.glBindTexture(GLES20.GL_TEXTURE_2D,Image2Handle);
        GLES32.glVertexAttribPointer(CoordHandle, COORDS_PER_TEX, GLES32.GL_FLOAT, false, texStride, Tex2Buffer);
        GLES32.glUniform1i(SamplerHandle,1);
        GLES32.glUniform1i(UseHandle,0);
        // Draw the Sphere
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, Sphere2Index.length, GLES32.GL_UNSIGNED_INT, index2Buffer);
        ///////////////////

        //Rings
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, vertexStride, ringVertexBuffer);
        GLES32.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, colorStride, ringColorBuffer);
        GLES32.glVertexAttribPointer(normalHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, vertexStride, normal3Buffer);
        GLES32.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES32.glBindTexture(GLES20.GL_TEXTURE_2D,Image3Handle);
        GLES32.glVertexAttribPointer(CoordHandle, COORDS_PER_TEX, GLES32.GL_FLOAT, false, texStride, TexMidBuffer);
        GLES32.glUniform1i(SamplerHandle,2);
        GLES32.glUniform1i(UseHandle,0);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, ringIndex.length, GLES32.GL_UNSIGNED_INT, ringIndexBuffer);
    }
}
