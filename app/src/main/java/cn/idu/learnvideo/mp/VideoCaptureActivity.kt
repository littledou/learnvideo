package cn.idu.learnvideo.mp

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.view.View
import cn.idu.learnvideo.databinding.ActivityVideoCaptureBinding
import cn.idu.learnvideo.mp.audio.AudioGeneratePCM
import cn.idu.learnvideo.mp.codec.encoder.AudioEncoder
import cn.idu.learnvideo.mp.codec.encoder.CodecListener
import cn.idu.learnvideo.mp.codec.encoder.VideoEncoder
import cn.readsense.module.base.BaseCoreActivity
import cn.readsense.module.camera1.CameraView
import cn.readsense.module.util.DLog
import java.io.File
import java.nio.ByteBuffer

class VideoCaptureActivity : BaseCoreActivity() {
    lateinit var binding: ActivityVideoCaptureBinding

    override fun getLayoutView(): View {
        binding = ActivityVideoCaptureBinding.inflate(layoutInflater)
        return binding.root
    }

    var capture = false
    var isMuxerStart = false

    lateinit var videoEncoder: VideoEncoder
    lateinit var audioEncoder: AudioEncoder
    lateinit var pcmCreate: AudioGeneratePCM
    override fun initView() {


        binding.cameraview0.apply {
            cameraParams.facing = 1
            cameraParams.isScaleWidth = false
            cameraParams.oritationDisplay = 90
            cameraParams.previewSize.previewWidth = 640
            cameraParams.previewSize.previewHeight = 480
            cameraParams.isFilp = false

            addPreviewFrameCallback(object : CameraView.PreviewFrameCallback {
                override fun analyseData(data: ByteArray?): Any {
                    if (capture && videoEncoder != null) {
                        videoEncoder.putBuf(data!!, 0, data.size)
                    }
                    return 0
                }

                override fun analyseDataEnd(p0: Any?) {}
            })
        }
        addLifecycleObserver(binding.cameraview0)


        val saveMp4Path = "${filesDir}/test.mp4"

        binding.cameraview0.setOnClickListener {

            if (!capture) {
                DLog.d("开始录制视频")
                File(saveMp4Path).deleteOnExit()

                //定义视频编码器并启动
                videoEncoder = VideoEncoder()
                videoEncoder.setUpVideoCodec(640, 480)
                videoEncoder.start()

                //创建音频编码器并启动
                audioEncoder = AudioEncoder()
                audioEncoder.start()
                capture = true


                //定义混合器：输出并保存h.264码流为mp4
                val mediaMuxer =
                    MediaMuxer(
                        saveMp4Path,
                        MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
                    )
                var videoTrackIndex = -1
                var audioTrackIndex = -1

                fun startMuxer() {
                    if (videoTrackIndex > -1 && audioTrackIndex > -1) {
                        mediaMuxer.start()
                        isMuxerStart = true
                    }
                }

                fun stopMuxer() {
                    if (videoTrackIndex < 0 && audioTrackIndex < 0) {
                        mediaMuxer.release()
                        isMuxerStart = false
                    }
                }

                videoEncoder.setCodecListener(object : CodecListener {
                    override fun formatUpdate(format: MediaFormat) {
                        videoTrackIndex = mediaMuxer.addTrack(format)
                        startMuxer()
                    }

                    override fun bufferUpdate(
                        buffer: ByteBuffer,
                        bufferInfo: MediaCodec.BufferInfo
                    ) {
                        if (isMuxerStart)
                            mediaMuxer.writeSampleData(videoTrackIndex, buffer, bufferInfo)
                    }

                    override fun bufferOutputEnd() {
                        videoEncoder.stopWorld()
                        videoTrackIndex = -1
                        stopMuxer()
                    }
                })

                audioEncoder.setCodecListener(object : CodecListener {
                    override fun formatUpdate(format: MediaFormat) {
                        super.formatUpdate(format)
                        audioTrackIndex = mediaMuxer.addTrack(format)
                        startMuxer()
                    }

                    override fun bufferUpdate(
                        buffer: ByteBuffer,
                        bufferInfo: MediaCodec.BufferInfo
                    ) {
                        //直接将编码好的数据存到本地
                        if (isMuxerStart)
                            mediaMuxer.writeSampleData(audioTrackIndex, buffer, bufferInfo)
                    }

                    override fun bufferOutputEnd() {
                        super.bufferOutputEnd()
                        audioEncoder.stopWorld()
                        stopMuxer()
                    }
                })

                //创建音频采集器并启动
                pcmCreate = AudioGeneratePCM()
                pcmCreate.listener = { data, offset, len ->
                    audioEncoder.putBuf(data, offset, len)//采集到的音频裸流pcm交给音频编码器编码
                }
                pcmCreate.start()


            } else {
                DLog.d("停止录制视频，文件存储与：$saveMp4Path")
                capture = false
                videoEncoder.putBufEnd()
                audioEncoder.putBufEnd()
                pcmCreate.stopRecord()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

}