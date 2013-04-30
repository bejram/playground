package com.example.sampleapp;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.example.sampleapp.programs.TextureShaderProgram;
import com.example.sampleapp.util.MatrixHelper;
import com.example.sampleapp.util.TextureHelper;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

public class MyGLRenderer implements GLSurfaceView.Renderer {

	private static final int NUM_SPRITES = 8;
	
	private final Context context;
	private int texture;
	private int starTexture;
	
	private int textureTile1;
	private int textureTile2;
	private int textureTile3;
	private int textureTile4;
	
    private static final String TAG = "MyGLRenderer";
    private Square   mSquare;
    private Sprite mStar;
    
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    // Declare as volatile because we are updating it from another thread
    public volatile float mAngle;
    public volatile float mTouchX = 500;
    public volatile float mTouchDX;
    public volatile float mTouchY = 500;
    public volatile float mTouchDY;
    private int mScreenW;
    private int mScreenH;
    private float mXRatio;
    private float mYRatio;
    
    private Sprite[] mSprites = new Sprite[NUM_SPRITES];
    
    public MyGLRenderer(Context context) {
    	this.context = context;
    }
    
	@Override
	public void onDrawFrame(GL10 unused) {
		  
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        //Matrix.setLookAtM(rm, rmOffset, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)
        
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
  
        // Create a rotation for the triangle
        //long time = SystemClock.uptimeMillis() % 4000L;
        //float angle = 0.090f * ((int) time);
        
        //Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);
        //Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 0, -1.0f);

        // Combine the rotation matrix with the projection and camera view
        //Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);

        int touchX = (int) (mScreenW - mTouchX);
        int touchY = (int) (mScreenH - mTouchY);
        
        mStar.x = (float) ((touchX) * (mXRatio * 2) / mScreenW - mXRatio);
        mStar.y = (float) ((touchY) * (mYRatio * 2) / mScreenH - mYRatio);
       
		// Draw square
        mSquare.draw(mMVPMatrix);
        
        mStar.draw(mMVPMatrix);
        
        for (int n=0;n<NUM_SPRITES;n++)
        {
        	mSprites[n].draw(mMVPMatrix);
        }
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        mScreenW = width;
        mScreenH = height;
        
        float ratio;
        
        if (width > height) {
        	ratio = (float) width / height;
        	mXRatio = ratio;
        	mYRatio = 1.0f;
        } else {
        	ratio = (float) height / width;
        	mYRatio = ratio;
        	mXRatio = 1.0f;
        }
        
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjMatrix, 0, -mXRatio, mXRatio, -mYRatio, mYRatio, 3, 7);  
        //Matrix.frustumM(mProjMatrix, 0, -1, 1, -1, 1, 3, 7);
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		
		// Set the background frame color
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
     
        mSquare   = new Square();        
        mStar = new Sprite();
        
        //textureProgram = new TextureShaderProgram(context);
        texture = TextureHelper.loadTexture(context, R.drawable.unit_square);   
        starTexture = TextureHelper.loadTexture(context, R.drawable.star);
        
        mSquare.setTextureId(texture);
        mStar.setTextureId(starTexture);
        
        
        
        mSprites = new Sprite[NUM_SPRITES];
        
        textureTile1 = TextureHelper.loadTexture(context, R.drawable.tile_1);
        textureTile2 = TextureHelper.loadTexture(context, R.drawable.tile_2);
        textureTile3 = TextureHelper.loadTexture(context, R.drawable.tile_3);
        textureTile4 = TextureHelper.loadTexture(context, R.drawable.tile_4);
        
        for (int n=0;n<NUM_SPRITES;n++)
        {
        	mSprites[n] = new Sprite();
        	mSprites[n].setTextureId(textureTile1);
        	mSprites[n].x = -0.9f + (n * 0.2f); 
        	mSprites[n].y =	0f;
        }
        
	}
	
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }	
}
