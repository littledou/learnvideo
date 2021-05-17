package cn.idu.learnvideo.opengles

import android.graphics.ImageFormat
import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import cn.idu.glrenderer.texture.CameraTexture
import cn.idu.learnvideo.databinding.ActivityEGLSample02CameraBinding
import cn.idu.learnvideo.databinding.ActivityGlSample03CameraBinding
import cn.idu.learnvideo.opengles.egl.EGLRenderer
import cn.idu.learnvideo.opengles.renderer.SampleRenderer
import cn.readsense.module.base.BaseCoreActivity

class EGLSample02CameraActivity : BaseCoreActivity() {
    lateinit var binding: ActivityEGLSample02CameraBinding
    lateinit var eglRenderer: EGLRenderer

    var camera: Camera? = null
    override fun getLayoutView(): View {
        binding = ActivityEGLSample02CameraBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        eglRenderer = EGLRenderer()

        val videoTexture = CameraTexture()
        videoTexture.setTextureSize(480, 640)
        videoTexture.getSurfaceTexture { surfaceTexture ->
            surfaceTexture.setOnFrameAvailableListener {
                eglRenderer.updateTexImage()
            }
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
            val parameters = camera!!.parameters
            parameters!!.setPreviewSize(640, 480)
            camera?.parameters = parameters
            camera?.setPreviewTexture(surfaceTexture)
            camera?.addCallbackBuffer(
                ByteArray(
                    640 * 480 * ImageFormat.getBitsPerPixel(
                        ImageFormat.NV21
                    ) / 8
                )
            )
            camera?.setPreviewCallbackWithBuffer { data: ByteArray, camera: Camera ->
                camera.addCallbackBuffer(data)
            }
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