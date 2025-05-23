package com.rekoj134.earthlivewallpaper

import android.content.Context
import android.opengl.GLES32.glClearColor
import android.opengl.GLES32
import android.opengl.GLES32.GL_COLOR_BUFFER_BIT
import android.opengl.GLES32.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES32.GL_DEPTH_TEST
import android.opengl.GLES32.glClear
import android.opengl.GLES32.glEnable
import android.opengl.GLES32.glViewport
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix
import com.rekoj134.earthlivewallpaper.model.createSphere
import com.rekoj134.earthlivewallpaper.util.LoggerConfig
import com.rekoj134.earthlivewallpaper.util.ShaderHelper
import com.rekoj134.earthlivewallpaper.util.ShaderReader
import com.rekoj134.earthlivewallpaper.util.TextureLoader
import com.rekoj134.earthlivewallpaper.model.skyboxVertices
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
    private var animationSpeed: Float = 0.2f

    private var program = 0
    private var VBO = 0
    private var VAO = 0
    private var EBO = 0

    private var skyboxVAO = 0
    private var skyboxVBO = 0
    private var skyboxProgram = 0
    private var skyboxTexture = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val sphere = createSphere(radius = 0.8f, stacks = 62, slices = 62)
        sphereVertices = sphere.vertices
        sphereIndices = sphere.indices

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glEnable(GL_DEPTH_TEST)

        val skyboxVertexShaderSource = ShaderReader.readTextFileFromResource(context, R.raw.skybox_vertex_shader)
        val skyboxFragmentShaderSource = ShaderReader.readTextFileFromResource(context, R.raw.skybox_fragment_shader)
        skyboxProgram = ShaderHelper.buildProgram(skyboxVertexShaderSource, skyboxFragmentShaderSource)

        // Tạo VAO, VBO cho skybox
        val vaoBufferSkybox = IntBuffer.allocate(1)
        val vboBufferSkybox = IntBuffer.allocate(1)
        GLES32.glGenVertexArrays(1, vaoBufferSkybox)
        GLES32.glGenBuffers(1, vboBufferSkybox)
        skyboxVAO = vaoBufferSkybox.get(0)
        skyboxVBO = vboBufferSkybox.get(0)

        GLES32.glBindVertexArray(skyboxVAO)
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, skyboxVBO)
        val vertexBufferSkybox = ByteBuffer.allocateDirect(skyboxVertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(skyboxVertices)
        vertexBufferSkybox.position(0)
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, skyboxVertices.size * 4, vertexBufferSkybox, GLES32.GL_STATIC_DRAW)

        GLES32.glEnableVertexAttribArray(0)
        GLES32.glVertexAttribPointer(0, 3, GLES32.GL_FLOAT, false, 3 * 4, 0)
        GLES32.glBindVertexArray(0)

        val faces = listOf<Int>(
            R.drawable.skybox_right,
            R.drawable.skybox_left,
            R.drawable.skybox_up,
            R.drawable.skybox_down,
            R.drawable.skybox_front,
            R.drawable.skybox_back
        )
        val cubeMapTexture = TextureLoader.loadCubeMap(faces, context)
        skyboxTexture = cubeMapTexture
        GLES32.glBindTexture(GLES32.GL_TEXTURE_CUBE_MAP, cubeMapTexture)

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

        val earthTexture = TextureLoader.loadTexture2D(context, R.drawable.earth_texture)
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, earthTexture)
        GLES32.glUniform1i(GLES32.glGetUniformLocation(program, "earthTexture"), 0)

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0)
        GLES32.glBindVertexArray(0)
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        val aspectRatio = if (width > height) width.toFloat() / height else height.toFloat() / width

        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, -8f,
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

        GLES32.glUseProgram(program)
        Matrix.setIdentityM(modelMatrix, 0)

        Matrix.setIdentityM(rotationMatrix, 0)
        Matrix.setRotateM(rotationMatrix, 0, timeElapsed * 360.0f, 0.0f, 1.0f, 0.0f)
        Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotationMatrix, 0)

        GLES32.glUniformMatrix4fv(0, 1, false, modelMatrix, 0)
        GLES32.glBindVertexArray(VAO)
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, sphereIndices.size, GLES32.GL_UNSIGNED_INT, 0)
        GLES32.glBindVertexArray(0)

        // Vẽ skybox
        GLES32.glDepthFunc(GLES32.GL_LEQUAL)  // đổi depth function để skybox vẽ phía sau mọi thứ
        GLES32.glDepthMask(false)
        GLES32.glUseProgram(skyboxProgram)

        // Lấy uniform location cho view và projection
        val viewLoc = GLES32.glGetUniformLocation(skyboxProgram, "view")
        val projLoc = GLES32.glGetUniformLocation(skyboxProgram, "projection")

        // Loại bỏ thành phần dịch chuyển trong view matrix (chỉ lấy rotation để quay quanh camera)
        val viewNoTranslation = FloatArray(16)
        System.arraycopy(viewMatrix, 0, viewNoTranslation, 0, 16)
        viewNoTranslation[12] = 0f
        viewNoTranslation[13] = 0f
        viewNoTranslation[14] = 0f

        GLES32.glUniformMatrix4fv(viewLoc, 1, false, viewNoTranslation, 0)
        GLES32.glUniformMatrix4fv(projLoc, 1, false, projectionMatrix, 0)

        GLES32.glBindVertexArray(skyboxVAO)

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_CUBE_MAP, skyboxTexture)
        GLES32.glUniform1i(GLES32.glGetUniformLocation(skyboxProgram, "skybox"), 0)

        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, 36)

        GLES32.glBindVertexArray(0)
        GLES32.glDepthMask(true)
        GLES32.glDepthFunc(GLES32.GL_LESS) // reset lại depth func mặc định
    }
}