package cn.idu.learnvideo.camera

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.view.View
import cn.idu.learnvideo.databinding.ActivityCameraCaptureBinding
import cn.idu.learnvideo.video.codec.encoder.CodecListener
import cn.idu.learnvideo.video.codec.encoder.VideoEncoder
import cn.readsense.module.base.BaseCoreActivity
import cn.readsense.module.camera1.CameraView
import cn.readsense.module.util.DLog
import java.io.File
import java.nio.ByteBuffer

class CameraCaptureActivity : BaseCoreActivity() {
    lateinit var binding: ActivityCameraCaptureBinding

    override fun getLayoutView(): View {
        binding = ActivityCameraCaptureBinding.inflate(layoutInflater)
        return binding.root
    }

    var capture = false;

    //视频编码为mp4
    val videoEncoder = VideoEncoder()
    override fun initView() {

        videoEncoder.setUpVideoCodec(640, 480)
        videoEncoder.start()
        binding.cameraview0.apply {
            cameraParams.facing = 1
            cameraParams.isScaleWidth = false
            cameraParams.oritationDisplay = 90
            cameraParams.previewSize.previewWidth = 640
            cameraParams.previewSize.previewHeight = 480
            cameraParams.isFilp = false

            addPreviewFrameCallback(object : CameraView.PreviewFrameCallback {
                override fun analyseData(data: ByteArray?): Any {
                    if (capture) {
                        videoEncoder.putBuf(data!!, 0, data.size)
                    }
                    return 0
                }
                override fun analyseDataEnd(p0: Any?) {}
            })
        }
        addLifecycleObserver(binding.cameraview0)


        val saveMp4Path = "${filesDir}/test.mp4"
        File(saveMp4Path).deleteOnExit()
        //定义混合器：输出并保存h.264码流为mp4
        val mediaMuxer =
            MediaMuxer(
                saveMp4Path,
                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
            )
        var muxerTrackIndex = -1

        videoEncoder.setCodecListener(object : CodecListener {
            override fun formatUpdate(format: MediaFormat) {
                muxerTrackIndex = mediaMuxer.addTrack(format)
                mediaMuxer.start()
            }

            override fun bufferUpdate(buffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
                mediaMuxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
            }

            override fun bufferOutputEnd() {
                mediaMuxer.release()
                videoEncoder.stopWorld()
            }
        })

        binding.cameraview0.setOnClickListener {
            DLog.d("录制：$capture")
            capture = !capture
        }

    }

    override fun onDestroy() {
        videoEncoder.putBufEnd()
        super.onDestroy()
    }

}