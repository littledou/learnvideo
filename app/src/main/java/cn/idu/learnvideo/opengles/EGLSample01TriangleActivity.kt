package cn.idu.learnvideo.opengles

import android.graphics.BitmapFactory
import android.os.FileUtils
import android.view.View
import cn.idu.learnvideo.databinding.ActivityEGLSample01TriangleBinding
import cn.idu.learnvideo.opengles.egl.EGLRenderer
import cn.idu.learnvideo.opengles.texture.ImageTexture
import cn.idu.learnvideo.opengles.texture.TriangleTexture
import cn.readsense.module.base.BaseCoreActivity
import cn.readsense.module.util.BitmapUtil
import java.io.File
import java.io.FileOutputStream

class EGLSample01TriangleActivity : BaseCoreActivity() {

    lateinit var binding: ActivityEGLSample01TriangleBinding
    lateinit var eglRenderer: EGLRenderer
    override fun getLayoutView(): View? {
        binding = ActivityEGLSample01TriangleBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        eglRenderer = EGLRenderer()
        eglRenderer.addTexture(TriangleTexture())

        val bitmap = BitmapFactory.decodeStream(assets.open("1.png"))
        eglRenderer.addTexture(ImageTexture(bitmap))
        eglRenderer.setSurfaceView(binding.surfaceview)
    }

}