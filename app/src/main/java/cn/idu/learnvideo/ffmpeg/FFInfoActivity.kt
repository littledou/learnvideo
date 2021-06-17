package cn.idu.learnvideo.ffmpeg

import android.opengl.GLSurfaceView
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import cn.idu.glrenderer.texture.NativeNV21Texture
import cn.idu.learnvideo.databinding.ActivityFfinfoBinding
import cn.idu.learnvideo.opengles.renderer.SampleYUVRenderer
import cn.idu.newffmpeg.FFNative
import cn.readsense.module.base.BaseCoreActivity
import cn.readsense.module.util.DLog
import kotlin.concurrent.thread

class FFInfoActivity : BaseCoreActivity() {
    lateinit var binding: ActivityFfinfoBinding
    lateinit var yuvrenderer: SampleYUVRenderer
    val viewModel: FaceInfoViewModel by viewModels()
    override fun getLayoutView(): View {
        binding = ActivityFfinfoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        viewModel.init(baseContext)
        yuvrenderer = SampleYUVRenderer(NativeNV21Texture())
        binding.glSurfaceview.setEGLContextClientVersion(3)
        binding.glSurfaceview.setRenderer(yuvrenderer)
        binding.glSurfaceview.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        thread {
            FFNative.testLoadingRTSP("rtsp://admin:wwj6184074@192.168.2.108:554/cam/realmonitor?channel=1&subtype=1",
                object : FFNative.Companion.FrameCallback {
                    override fun onFrame(data: ByteArray, iw: Int, ih: Int) {
                        viewModel.putColorBuffer(data)

                        runOnUiThread {
                            yuvrenderer.updateTexImage(data, iw, ih)
                        }
                    }
                })
        }
    }

}