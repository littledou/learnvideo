package cn.idu.learnvideo.main

import android.Manifest
import android.view.View
import androidx.recyclerview.widget.ConcatAdapter
import cn.idu.learnvideo.mp.VideoCaptureActivity
import cn.idu.learnvideo.databinding.ActivityMainBinding
import cn.idu.learnvideo.mp.AudioCaptureActivity
import cn.idu.learnvideo.opengles.*
import cn.idu.learnvideo.renderimg.RenderImageActivity
import cn.readsense.module.base.BaseCoreActivity

class MainActivity : BaseCoreActivity() {

    var funcArray = arrayOf(
        "视频Mp4录制",
        "音频Mp3录制",
        "EGL图片渲染",
        "绘制三角形",
        "Java图片渲染",
        "视频渲染",
        "自定义EGL绘制三角形",
        "自定义EGL渲染相机"
    )

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
                openPage(VideoCaptureActivity::class.java)
            }
            1 -> {
                openPage(AudioCaptureActivity::class.java)
            }
            2 -> {
                openPage(RenderImageActivity::class.java)
            }
            3 -> {
                openPage(GlSample01TriangleActivity::class.java)
            }
            4 -> {
                openPage(GlSample02ImageActivity::class.java)
            }
            5 -> {
                openPage(GlSample03CameraActivity::class.java)
            }
            6 -> {
                openPage(EGLSample01TriangleActivity::class.java)
            }
            7 -> {
                openPage(EGLSample02CameraActivity::class.java)
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






