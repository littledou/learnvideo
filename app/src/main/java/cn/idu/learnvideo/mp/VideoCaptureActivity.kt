package cn.idu.learnvideo.mp

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.view.View
import cn.idu.learnvideo.databinding.ActivityVideoCaptureBinding
import cn.idu.learnvideo.mp.codec.encoder.CodecListener
import cn.idu.learnvideo.mp.codec.encoder.VideoEncoder
import cn.readsense.module.base.BaseCoreActivity
import cn.readsense.module.camera1.CameraView
import cn.readsense.module.util.DLog
import java.io.File
import java.nio.ByteBuffer
import kotlin.experimental.and

class VideoCaptureActivity : BaseCoreActivity() {
    lateinit var binding: ActivityVideoCaptureBinding

    override fun getLayoutView(): View {
        binding = ActivityVideoCaptureBinding.inflate(layoutInflater)
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
                val data = ByteArray(buffer.limit())
                buffer.get(data)
                val nalu_type = (data[4] and 0x1F)
                DLog.d("nalu_type: $nalu_type ： arr:${data[0]} ${data[1]} ${data[2]} ${data[3]} ${data[4]}")
                mediaMuxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
            }

            override fun bufferOutputEnd() {
                mediaMuxer.release()
                videoEncoder.stopWorld()
            }
        })

        binding.cameraview0.setOnClickListener {

            if (!capture) {
                DLog.d("开始录制视频")
                File(saveMp4Path).deleteOnExit()
                capture = true
            } else {
                DLog.d("停止录制视频，文件存储与：$saveMp4Path")
                capture = false
                videoEncoder.putBufEnd()
            }
        }


    }

    override fun onDestroy() {

        super.onDestroy()
    }

}