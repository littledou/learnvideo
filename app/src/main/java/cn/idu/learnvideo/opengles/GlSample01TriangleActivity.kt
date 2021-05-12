package cn.idu.learnvideo.opengles

import android.opengl.GLSurfaceView
import android.view.View
import cn.idu.learnvideo.databinding.ActivityGlSample01TriangleBinding
import cn.idu.learnvideo.opengles.renderer.SampleRenderer
import cn.idu.learnvideo.opengles.texture.TriangleTexture
import cn.readsense.module.base.BaseCoreActivity

class GlSample01TriangleActivity : BaseCoreActivity() {
    lateinit var binding: ActivityGlSample01TriangleBinding
    lateinit var renderer: SampleRenderer
    override fun getLayoutView(): View {
        binding = ActivityGlSample01TriangleBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        binding.glSurfaceview.setEGLContextClientVersion(2)
        renderer = SampleRenderer(TriangleTexture())
        binding.glSurfaceview.setRenderer(renderer)
        binding.glSurfaceview.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    override fun onDestroy() {
        renderer.release()
        super.onDestroy()
    }
}