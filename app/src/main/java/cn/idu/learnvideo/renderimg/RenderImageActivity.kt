package cn.idu.learnvideo.renderimg

import android.os.FileUtils
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import cn.idu.learnvideo.databinding.ActivityRenderImageBinding
import cn.readsense.module.base.BaseCoreActivity
import cn.readsense.module.util.DLog
import java.io.File
import java.io.FileOutputStream

class RenderImageActivity : BaseCoreActivity(), SurfaceHolder.Callback {
    lateinit var binding: ActivityRenderImageBinding
    lateinit var targetPngPath: String

    override fun getLayoutView(): View {
        binding = ActivityRenderImageBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        val parentPanel = binding.parentPanel
        val surfaceView = SurfaceView(baseContext)
        surfaceView.holder.addCallback(this)
        parentPanel.addView(surfaceView, 0)
        val layoutParams = surfaceView.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.width = 400
        layoutParams.height = 400

        targetPngPath = "${getExternalFilesDir("img")}/1.png"
        File(targetPngPath).apply {
            if (exists()) deleteOnExit()
            FileUtils.copy(
                assets.open("1.png"),
                FileOutputStream(absolutePath)
            )
        }
        surfaceView.setOnClickListener {
            DLog.d("surfaceView click: ${layoutParams.width}")
            if (layoutParams.width >= 500) {
                layoutParams.width = 400;
            }
            layoutParams.width = layoutParams.width + 10;
            surfaceView.requestLayout()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
//        ImagePreviewJni.init(targetPngPath, holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//        ImagePreviewJni.resize(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
//        ImagePreviewJni.stop()
    }

}