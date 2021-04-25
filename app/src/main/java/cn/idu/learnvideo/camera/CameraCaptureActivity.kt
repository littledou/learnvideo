package cn.idu.learnvideo.camera

import android.view.View
import cn.idu.learnvideo.databinding.ActivityCameraCaptureBinding
import cn.readsense.module.base.BaseCoreActivity
import cn.readsense.module.camera1.CameraView

class CameraCaptureActivity : BaseCoreActivity() {
    lateinit var binding: ActivityCameraCaptureBinding

    override fun getLayoutView(): View {
        binding = ActivityCameraCaptureBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        binding.cameraview0.apply {
            cameraParams.facing = 1
            cameraParams.isScaleWidth = false
            cameraParams.oritationDisplay = 90
            cameraParams.previewSize.previewWidth = 640
            cameraParams.previewSize.previewHeight = 480
            cameraParams.isFilp = false

            addPreviewFrameCallback(object : CameraView.PreviewFrameCallback {
                override fun analyseData(p0: ByteArray?): Any {

                    return 0
                }

                override fun analyseDataEnd(p0: Any?) {}
            })
        }
        addLifecycleObserver(binding.cameraview0)

    }

}