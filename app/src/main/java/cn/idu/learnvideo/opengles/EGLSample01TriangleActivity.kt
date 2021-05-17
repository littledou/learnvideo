package cn.idu.learnvideo.opengles

import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.Camera
import android.view.View
import cn.idu.glrenderer.texture.CameraTexture
import cn.idu.glrenderer.texture.ImageTexture
import cn.idu.glrenderer.texture.NativeTriangleTexture
import cn.idu.glrenderer.texture.TriangleTexture
import cn.idu.learnvideo.databinding.ActivityEGLSample01TriangleBinding
import cn.idu.learnvideo.opengles.egl.EGLRenderer
import cn.readsense.module.base.BaseCoreActivity

class EGLSample01TriangleActivity : BaseCoreActivity() {

    lateinit var binding: ActivityEGLSample01TriangleBinding
    lateinit var eglRenderer: EGLRenderer

    override fun getLayoutView(): View? {
        binding = ActivityEGLSample01TriangleBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        eglRenderer = EGLRenderer()
        eglRenderer.addTexture(NativeTriangleTexture())
        eglRenderer.setSurfaceView(binding.surfaceview)
    }

}