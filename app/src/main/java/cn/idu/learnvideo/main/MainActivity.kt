package cn.idu.learnvideo.main

import android.Manifest
import android.view.View
import androidx.recyclerview.widget.ConcatAdapter
import cn.idu.learnvideo.camera.CameraCaptureActivity
import cn.idu.learnvideo.databinding.ActivityMainBinding
import cn.idu.learnvideo.opengles.GlSample01TriangleActivity
import cn.idu.learnvideo.opengles.GlSample02ImageActivity
import cn.idu.learnvideo.renderimg.RenderImageActivity
import cn.readsense.module.base.BaseCoreActivity

class MainActivity : BaseCoreActivity() {

    var funcArray = arrayOf("视频YUV录制", "EGL图片渲染", "绘制三角形", "Java图片渲染")

    lateinit var binding: ActivityMainBinding
    override fun getLayoutView(): View {
        requestPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        binding = ActivityMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {

        val headAdapter = HeadAdapter()
        val functionAdapter = FunctionAdapter(funcArray) { index -> funcClick(index) }
        val concatAdapter = ConcatAdapter(headAdapter, functionAdapter)
        binding.recyclerView.adapter = concatAdapter
        headAdapter.flushCount(0)
    }

    fun funcClick(index: Int) {
        when (index) {
            0 -> {
                openPage(CameraCaptureActivity::class.java)
            }
            1 -> {
                openPage(RenderImageActivity::class.java)
            }
            2 -> {
                openPage(GlSample01TriangleActivity::class.java)
            }
            3->{
                openPage(GlSample02ImageActivity::class.java)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        finishAfterTransition()//返回键退出桌面内存泄漏
    }
}






