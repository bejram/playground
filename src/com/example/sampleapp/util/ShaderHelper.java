package com.example.sampleapp.util;

import android.opengl.GLES20;

public class ShaderHelper {

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
    
	public static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {
		
		int program;

		// Compile the shaders.
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource);
	
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);		
		
		return program;
	}
}
