package com.rekoj134.earthlivewallpaper.util

import android.opengl.GLES32.GL_COMPILE_STATUS
import android.opengl.GLES32.GL_FRAGMENT_SHADER
import android.opengl.GLES32.GL_LINK_STATUS
import android.opengl.GLES32.GL_VALIDATE_STATUS
import android.opengl.GLES32.GL_VERTEX_SHADER
import android.opengl.GLES32.glAttachShader
import android.opengl.GLES32.glCompileShader
import android.opengl.GLES32.glCreateProgram
import android.opengl.GLES32.glCreateShader
import android.opengl.GLES32.glDeleteProgram
import android.opengl.GLES32.glDeleteShader
import android.opengl.GLES32.glGetProgramInfoLog
import android.opengl.GLES32.glGetProgramiv
import android.opengl.GLES32.glGetShaderInfoLog
import android.opengl.GLES32.glGetShaderiv
import android.opengl.GLES32.glLinkProgram
import android.opengl.GLES32.glShaderSource
import android.opengl.GLES32.glValidateProgram
import android.util.Log

object ShaderHelper {
    private const val TAG = "ShaderHelper"

    // Biên dịch Vertex Shader
    fun compileVertexShader(shaderCode: String) : Int {
        return compileShader(GL_VERTEX_SHADER, shaderCode)
    }

    // Biên dịch Fragment Shader
    fun compileFragmentShader(shaderCode: String) : Int {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode)
    }

    private fun compileShader(type: Int, shaderCode: String) : Int {
        // Tạo và lưu trữ id của Shader
        val shaderObjectId = glCreateShader(type)

        // Khi id của Shader = 0 có nghĩa là việc tạo Shader đã thất bại
        if (shaderObjectId == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Could not create new shader")
            }

            return 0
        }

        // Truyền source code vào shader vừa tạo thông qua id
        glShaderSource(shaderObjectId, shaderCode)
        // Biên dịch shader
        glCompileShader(shaderObjectId)

        // Lấy trạng thái của shader vừa biên dịch
        val compileStatus = IntArray(1)
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0)

        if (LoggerConfig.ON) {
            Log.v(TAG, "Results of compiling source:" + "\n" + shaderCode + "\n:" + glGetShaderInfoLog(shaderObjectId))
        }

        // Tương tự như việc kiểm tra Shader id, compileStatus = 0 có nghĩa là quá trình biên dịch thất bại
        if (compileStatus[0] == 0) {
            // Khi biên dịch thất bại thì xóa đi shader vừa tạo để tiết kiệm tài nguyên
            glDeleteShader(shaderObjectId)

            if (LoggerConfig.ON) {
                Log.w(TAG, "Compilation of shader failed.")
            }

            return 0
        }

        return shaderObjectId
    }

    fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int) : Int {
        // Tạo và lưu trữ id của OpenGL program
        val programObjectId = glCreateProgram()

        // Khi id của program = 0 có nghĩa là việc tạo program đã thất bại
        if (programObjectId == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Could not create new program")
            }

            return 0
        }

        // Liên kết vertex shader và fragment shader vào program thông qua id của chúng
        glAttachShader(programObjectId, vertexShaderId)
        glAttachShader(programObjectId, fragmentShaderId)

        // Liên kết program vào OpenGLES
        glLinkProgram(programObjectId)

        // Lấy trạng thái liên liên kết program
        val linkStatus = IntArray(1)
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0)
        if (LoggerConfig.ON) {
            Log.v(TAG, "Results of linking program:\n" + glGetProgramInfoLog(programObjectId))
        }

        // Tương tự như việc kiểm tra program id, linkStatus = 0 có nghĩa là quá trình liên kết thất bại
        if (linkStatus[0] == 0) {
            // Khi liên kết thất bại, xóa program vừa tạo
            glDeleteProgram(programObjectId)
            if (LoggerConfig.ON) {
                Log.w(TAG, "Linking of program failed.")
            }
            return 0
        }

        return programObjectId
    }

    // Lấy và in ra trạng thái của OpenGLES program
    fun validateProgram(programObjectId: Int) : Boolean {
        glValidateProgram(programObjectId)

        val validateStatus = IntArray(1)
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0)
        Log.v(TAG, "Result of validating program: " + validateStatus[0] + "\nLog:" + glGetProgramInfoLog(programObjectId))

        return validateStatus[0] != 0
    }

    fun buildProgram(vertexShaderSource: String, fragmentShaderSource: String) : Int {
        var program = 0

        // Biên dịch Vertex Shader và Fragment Shader
        val vertexShader = compileVertexShader(vertexShaderSource)
        val fragmentShader = compileFragmentShader(fragmentShaderSource)

        // Tạo OpenGLES program và liên kết với các Shader vừa biên dịch
        program = linkProgram(vertexShader, fragmentShader)

        // In ra trạng thái của OpenGL program
        if (LoggerConfig.ON) {
            validateProgram(program)
        }

        return program
    }
}