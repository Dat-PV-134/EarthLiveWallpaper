package com.rekoj134.earthlivewallpaper

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rekoj134.earthlivewallpaper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var rendererSet = false

    private lateinit var scaleGestureDetector: ScaleGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initOpenGLES()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOpenGLES() {
        // Kiểm tra phiên bản của OpenGLES
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        // 0x30002 là phiên bản 3.2 của OpenGLES
        val supportsEs32 = configurationInfo.reqGlEsVersion >= 0x30002

        if (supportsEs32) {
            // Set phiên bản và Renderer cho GL SurfaceView
            // Khi set EGLContextClientVersion = 3, ta đang cho GLSurfaceView biết rằng mình sử dụng phiên bản 3.0 trở lên
            val myRenderer = MyRenderer(this@MainActivity)
            binding.myGLSurfaceView.setEGLContextClientVersion(3)
            binding.myGLSurfaceView.setRenderer(myRenderer)
            rendererSet = true

            scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    myRenderer.scaleFactor *= detector.scaleFactor
                    myRenderer.scaleFactor = myRenderer.scaleFactor.coerceIn(0.2f, 3.0f)
                    Log.e("DatPV", detector.scaleFactor.toString() + " - " + myRenderer.scaleFactor.toString())
                    return true
                }
            })

            var previousX = 0f
            var previousY = 0f
            binding.myGLSurfaceView.setOnTouchListener {  _, event ->
                scaleGestureDetector.onTouchEvent(event)

                if (event.pointerCount == 1) {
                    when (event.action) {
                        MotionEvent.ACTION_MOVE -> {
                            val dx = event.x - previousX
                            val dy = event.y - previousY

                            myRenderer.yRotation += dx * 0.5f
                            myRenderer.xRotation += dy * 0.5f
                        }
                    }
                    previousX = event.x
                    previousY = event.y
                }

                true
            }
        } else {
            Toast.makeText(this@MainActivity, "This device doesn't support OpenGL ES 3.2", Toast.LENGTH_SHORT).show()
            return
        }
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet) {
            binding.myGLSurfaceView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet) {
            binding.myGLSurfaceView.onPause()
        }
    }
}