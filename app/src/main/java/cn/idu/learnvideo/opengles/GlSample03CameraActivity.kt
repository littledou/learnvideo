package cn.idu.learnvideo.opengles

import android.graphics.ImageFormat
import android.hardware.Camera
import android.opengl.GLSurfaceView
import android.view.SurfaceHolder
import android.view.View
import cn.idu.learnvideo.databinding.ActivityGlSample03CameraBinding
import cn.idu.learnvideo.opengles.renderer.SampleRenderer
import cn.idu.learnvideo.opengles.texture.CameraTexture
import cn.readsense.module.base.BaseCoreActivity
import cn.readsense.module.util.DLog

/**
 * 由
 */
class GlSample03CameraActivity : BaseCoreActivity() {
    lateinit var binding: ActivityGlSample03CameraBinding
    lateinit var renderer: SampleRenderer
    var camera: Camera? = null
    override fun getLayoutView(): View {
        binding = ActivityGlSample03CameraBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
//
        binding.glSurfaceview.setEGLContextClientVersion(2)
        val videoTexture = CameraTexture()
        videoTexture.setTextureSize(480, 640)
        renderer = SampleRenderer(videoTexture)
        binding.glSurfaceview.setRenderer(renderer)
        binding.glSurfaceview.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        videoTexture.getSurfaceTexture { surfaceTexture ->
            surfaceTexture.setOnFrameAvailableListener {
                binding.glSurfaceview.requestRender()
            }

            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
            val parameters = camera!!.parameters
            parameters!!.setPreviewSize(640, 480)
            camera?.parameters = parameters
            camera?.setPreviewTexture(surfaceTexture)
//            camera?.addCallbackBuffer(
//                ByteArray(
//                    640 * 480 * ImageFormat.getBitsPerPixel(
//                        ImageFormat.NV21
//                    ) / 8
//                )
//            )
//            camera?.setPreviewCallbackWithBuffer { data: ByteArray, camera: Camera ->
//                camera.addCallbackBuffer(data)
//            }
            camera?.startPreview()

        }

//        kotlin.run {
//            val camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
//            val parameters = camera!!.parameters
//            parameters!!.setPreviewSize(640, 480)
//            camera?.parameters = parameters
//            binding.surfaceview.holder.addCallback(object : SurfaceHolder.Callback {
//                override fun surfaceCreated(holder: SurfaceHolder) {
//                    camera.setPreviewDisplay(holder)
//                    camera.startPreview()
//                }
//
//                override fun surfaceChanged(
//                    holder: SurfaceHolder,
//                    format: Int,
//                    width: Int,
//                    height: Int
//                ) {
//                }
//
//                override fun surfaceDestroyed(holder: SurfaceHolder) {
//                    camera?.stopPreview()
//                    camera?.release()
//                }
//            })
//        }

    }

    override fun onDestroy() {
        camera?.stopPreview()
        camera?.release()
        super.onDestroy()
    }

}