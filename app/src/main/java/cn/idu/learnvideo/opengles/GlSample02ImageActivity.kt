package cn.idu.learnvideo.opengles

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.FileUtils
import android.view.Surface
import android.view.View
import cn.idu.learnvideo.databinding.ActivityGlSample02ImageBinding
import cn.idu.learnvideo.opengles.renderer.SampleRenderer
import cn.idu.learnvideo.opengles.texture.ImageTexture
import cn.idu.learnvideo.opengles.texture.TriangleTexture
import cn.readsense.module.base.BaseCoreActivity
import cn.readsense.module.util.BitmapUtil
import java.io.File
import java.io.FileOutputStream

class GlSample02ImageActivity : BaseCoreActivity() {
    lateinit var binding: ActivityGlSample02ImageBinding
    lateinit var renderer: SampleRenderer
    override fun getLayoutView(): View {
        binding = ActivityGlSample02ImageBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        binding.glSurfaceview.setEGLContextClientVersion(2)

        val bitmap = BitmapFactory.decodeStream(assets.open("1.png"))
        renderer = SampleRenderer(ImageTexture(bitmap))
        binding.glSurfaceview.setRenderer(renderer)
        binding.glSurfaceview.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    override fun onDestroy() {
        renderer.release()
        super.onDestroy()
    }
}