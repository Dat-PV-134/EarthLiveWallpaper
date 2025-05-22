package com.rekoj134.earthlivewallpaper

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES32.glClearColor
import android.opengl.GLES32
import android.opengl.GLES32.GL_COLOR_BUFFER_BIT
import android.opengl.GLES32.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES32.GL_DEPTH_TEST
import android.opengl.GLES32.glClear
import android.opengl.GLES32.glEnable
import android.opengl.GLES32.glViewport
import android.opengl.GLSurfaceView.Renderer
import android.opengl.GLUtils
import android.opengl.Matrix
import com.rekoj134.earthlivewallpaper.util.LoggerConfig
import com.rekoj134.earthlivewallpaper.util.ShaderHelper
import com.rekoj134.earthlivewallpaper.util.ShaderReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyRenderer(private val context: Context) : Renderer {
    private lateinit var sphereVertices: FloatArray
    private lateinit var sphereIndices: IntArray

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    private var timeElapsed: Float = 0.0f
    private var animationSpeed: Float = 0.25f

    private var program = 0
    private var VBO = 0
    private var VAO = 0
    private var EBO = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val sphere = createSphere(radius = 1.0f, stacks = 62, slices = 62)
        sphereVertices = sphere.vertices
        sphereIndices = sphere.indices

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glEnable(GL_DEPTH_TEST)

        val vertexShaderSource = ShaderReader.readTextFileFromResource(context, R.raw.vertex_shader)
        val fragmentShaderSource = ShaderReader.readTextFileFromResource(context, R.raw.fragment_shader)
        program = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource)
        if (LoggerConfig.ON) {
            ShaderHelper.validateProgram(program)
        }
        GLES32.glUseProgram(program)

        val vaoBuffer = IntBuffer.allocate(1)
        val vboBuffer = IntBuffer.allocate(1)
        val eboBuffer = IntBuffer.allocate(1)
        GLES32.glGenVertexArrays(1, vaoBuffer)
        GLES32.glGenBuffers(1, vboBuffer)
        GLES32.glGenBuffers(1, eboBuffer)
        VAO = vaoBuffer.get(0)
        VBO = vboBuffer.get(0)
        EBO = eboBuffer.get(0)

        GLES32.glBindVertexArray(VAO)
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, VBO)
        val vertexBuffer: FloatBuffer = ByteBuffer
            .allocateDirect(sphereVertices.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(sphereVertices)
        vertexBuffer.position(0)
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, sphereVertices.size * Float.SIZE_BYTES, vertexBuffer, GLES32.GL_STATIC_DRAW)
        GLES32.glVertexAttribPointer(0, 3, GLES32.GL_FLOAT, false, 5 * Float.SIZE_BYTES, 0)
        GLES32.glEnableVertexAttribArray(0)
        GLES32.glVertexAttribPointer(1, 2, GLES32.GL_FLOAT, false, 5 * Float.SIZE_BYTES, 3 * Float.SIZE_BYTES)
        GLES32.glEnableVertexAttribArray(1)

        val indicesBuffer: IntBuffer = ByteBuffer
            .allocateDirect(sphereIndices.size * Int.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
        indicesBuffer.put(sphereIndices)
        indicesBuffer.position(0)
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, EBO)
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, sphereIndices.size * Int.SIZE_BYTES, indicesBuffer, GLES32.GL_STATIC_DRAW)

        val textureObjectIds = IntArray(1)
        GLES32.glGenTextures(1, textureObjectIds, 0)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureObjectIds[0])
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_REPEAT)
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_REPEAT)
        GLES32.glTexParameteri(
            GLES32.GL_TEXTURE_2D,
            GLES32.GL_TEXTURE_MIN_FILTER,
            GLES32.GL_LINEAR_MIPMAP_LINEAR
        )
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR)
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.earth_texture)
        GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES32.glGenerateMipmap(GLES32.GL_TEXTURE_2D)

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureObjectIds[0])
        GLES32.glUniform1i(GLES32.glGetUniformLocation(program, "earthTexture"), 0)

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0)
        GLES32.glBindVertexArray(0)
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        val aspectRatio = if (width > height) width.toFloat() / height else height.toFloat() / width

        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, -10f,
            0f, 0f, 0f,
            0f, 1f, 0f)
        GLES32.glUniformMatrix4fv(1, 1, false, viewMatrix, 0)

        Matrix.perspectiveM(projectionMatrix, 0, 45f, 1/aspectRatio, 0.1f, 10f)

        val uniformLocation = GLES32.glGetUniformLocation(program, "projectionMatrix")
        GLES32.glUniformMatrix4fv(uniformLocation, 1, false, projectionMatrix, 0)

        glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        timeElapsed += animationSpeed * 0.016f
        if (timeElapsed > 1.0f) {
            timeElapsed -= 1.0f
        } else if (timeElapsed < 0.0f) {
            timeElapsed += 1.0f
        }

        Matrix.setIdentityM(modelMatrix, 0)

        Matrix.setIdentityM(rotationMatrix, 0)
        Matrix.setRotateM(rotationMatrix, 0, timeElapsed * 360.0f, 0.0f, 1.0f, 0.0f)
        Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotationMatrix, 0)

        GLES32.glUniformMatrix4fv(0, 1, false, modelMatrix, 0)
        GLES32.glBindVertexArray(VAO)
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, sphereIndices.size, GLES32.GL_UNSIGNED_INT, 0)
        GLES32.glBindVertexArray(0)
    }
}