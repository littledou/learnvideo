package cn.idu.learnvideo.opengles

import android.graphics.BitmapFactory
import android.hardware.Camera
import android.os.FileUtils
import android.view.View
import cn.idu.learnvideo.databinding.ActivityEGLSample01TriangleBinding
import cn.idu.learnvideo.opengles.egl.EGLRenderer
import cn.idu.learnvideo.opengles.texture.CameraTexture
import cn.idu.learnvideo.opengles.texture.ImageTexture
import cn.idu.learnvideo.opengles.texture.TriangleTexture
import cn.readsense.module.base.BaseCoreActivity
import cn.readsense.module.util.BitmapUtil
import cn.readsense.module.util.DLog
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread

class EGLSample01TriangleActivity : BaseCoreActivity() {

    lateinit var binding: ActivityEGLSample01TriangleBinding
    lateinit var eglRenderer: EGLRenderer

    var camera: Camera? = null
    override fun getLayoutView(): View? {
        binding = ActivityEGLSample01TriangleBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        eglRenderer = EGLRenderer()

        eglRenderer.addTexture(TriangleTexture())

        val bitmap = BitmapFactory.decodeStream(assets.open("1.png"))
        eglRenderer.addTexture(ImageTexture(bitmap))

        val videoTexture = CameraTexture()
        videoTexture.setTextureSize(480, 640)
        videoTexture.getSurfaceTexture { surfaceTexture ->
            surfaceTexture.setOnFrameAvailableListener {
                eglRenderer.updateTexImages()
            }
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
            val parameters = camera!!.parameters
            parameters!!.setPreviewSize(640, 480)
            camera?.parameters = parameters
            camera?.setPreviewTexture(surfaceTexture)
            camera?.startPreview()
        }
        eglRenderer.addTexture(videoTexture)

        eglRenderer.setSurfaceView(binding.surfaceview)

    }

    override fun onDestroy() {
        camera?.stopPreview()
        camera?.release()
        super.onDestroy()
    }
}