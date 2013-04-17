package com.example.sampleapp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

public class Sprite {

	// Uniform constants.
	protected static final String U_MATRIX = "u_Matrix";
	protected static final String U_TEXTURE_UNIT = "u_TextureUnit";
	
	// Attribute constants.
	protected static final String A_POSITION = "a_Position";
	protected static final String A_COLOR = "a_Color";
	protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";
	
    private final String vertexShaderCode =
    	"uniform mat4 uMVPMatrix;" +
    	"attribute vec4 vPosition;" +
    	"attribute vec2 a_TextureCoordinates;" +
    	"varying vec2 v_TextureCoordinates;" +
    	"void main() {" +
    	"  v_TextureCoordinates = a_TextureCoordinates;" +
    	"  gl_Position = uMVPMatrix * vPosition;" +
    	"}";
    
    private final String fragmentShaderCode =
    		"precision mediump float;" +
    		"uniform sampler2D u_TextureUnit;" +
    		"varying vec2 v_TextureCoordinates;" +
    		"void main() {" +
    		"  gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates);" +
    		"}";            		
        		
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer textureBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mMVPMatrixHandle;
    private int muTextureUnitLocationHandle;
    private int maTextureCoordinatesLocationHandle;
	
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = { 0.0f,  0.0f, 0.0f,   // top left
                                     0.0f, -0.4f, 0.0f,   // bottom left
                                     0.4f, -0.4f, 0.0f,   // bottom right
                                     0.4f,  0.0f, 0.0f }; // top right

    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    
    static final int COORDS_PER_TEXTURE = 2;
    static float textureCoords[] = { 1.0f, 0.0f,
    						         1.0f, 1.0f,
    						         0.0f, 1.0f,
    						         0.0f, 0.0f };
    
    private final int textureStride = COORDS_PER_TEXTURE * 4; // 4 bytes per vertex
    
    private int textureId;
    
    public void setTextureId(int textureId) {
    	this.textureId = textureId;
    }
    
    public Sprite() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize texture buffer
        ByteBuffer tbb = ByteBuffer.allocateDirect(textureCoords.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        textureBuffer = tbb.asFloatBuffer();
        textureBuffer.put(textureCoords);
        textureBuffer.position(0);
        
        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                                                   vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                                                     fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    public void draw(float[] mvpMatrix) {
    	
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                                     GLES20.GL_FLOAT, false,
                                     vertexStride, vertexBuffer);
        
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");
      
		// Retrieve uniform locations for the shader program.
        muTextureUnitLocationHandle = GLES20.glGetUniformLocation(mProgram, U_TEXTURE_UNIT);
		
		// Retrieve attribute locations for the shader program.
		maTextureCoordinatesLocationHandle = GLES20.glGetAttribLocation(mProgram, A_TEXTURE_COORDINATES);        	
		
		// Set the active texture unit to texture unit 0.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		
		// Bind the texture to this unit.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

		// Tell the texture uniform sampler to use this texture in the shader by
		// telling it to read from texture unit 0.
		GLES20.glUniform1i(muTextureUnitLocationHandle, 0);
   
        // Enable texture array
        GLES20.glEnableVertexAttribArray(maTextureCoordinatesLocationHandle);

        // Prepare the coordinate data
        GLES20.glVertexAttribPointer(maTextureCoordinatesLocationHandle, COORDS_PER_TEXTURE,
                                     GLES20.GL_FLOAT, false,
                                     textureStride, textureBuffer);
		
        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                              GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        
        // Disable texture array
        GLES20.glDisableVertexAttribArray(maTextureCoordinatesLocationHandle);
    }	
}
