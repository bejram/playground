package com.example.sampleapp;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.MotionEvent;

public class MainActivity extends Activity {
	
	private MyGLSurfaceView mGLView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mGLView = new MyGLSurfaceView(this);
        setContentView(mGLView);		
		//setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;

	public MyGLSurfaceView(Context context) {
		super(context);

		// Create an OpenGL ES 2.0 context
		setEGLContextClientVersion(2);
					
		// Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer(context);
        setRenderer(mRenderer);

		// Render the view only when there is a change in the drawing data
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);			
	}
	
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
        	case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                  dx = dx * -1 ;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                  dy = dy * -1 ;
                }

                mRenderer.mAngle += (dx + dy) * TOUCH_SCALE_FACTOR;  // = 180.0f / 320
                
                mRenderer.mTouchX = x;
                mRenderer.mTouchDX = dx;
                mRenderer.mTouchY = y;
                mRenderer.mTouchDY = dy;
                
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }		
}