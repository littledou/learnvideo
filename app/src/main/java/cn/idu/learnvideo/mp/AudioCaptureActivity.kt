package cn.idu.learnvideo.mp

import android.media.*
import android.view.View
import cn.idu.learnvideo.databinding.ActivityAudioCaptureBinding
import cn.idu.learnvideo.mp.audio.AudioGeneratePCM
import cn.idu.learnvideo.mp.codec.encoder.AudioEncoder
import cn.idu.learnvideo.mp.codec.CodecListener
import cn.readsense.module.base.BaseCoreActivity
import cn.readsense.module.util.DLog
import java.io.File
import java.nio.ByteBuffer

class AudioCaptureActivity : BaseCoreActivity() {
    lateinit var binding: ActivityAudioCaptureBinding

    override fun getLayoutView(): View {
        binding = ActivityAudioCaptureBinding.inflate(layoutInflater)
        return binding.root
    }

    lateinit var pcmCreate: AudioGeneratePCM
    lateinit var audioEncoder: AudioEncoder
    override fun initView() {
        //使用混合器mediaMuxer存储编码好的aac数据
        val file_mp32 = File("$filesDir/test.aac")

        binding.startRecord.setOnClickListener {
            file_mp32.deleteOnExit()
            //定义混合器：输出并保存编码后的aac为mp3
            val mediaMuxer =
                MediaMuxer(
                    file_mp32.absolutePath,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
                )
            var muxerTrackIndex = -1

            //创建编码器并启动
            audioEncoder = AudioEncoder()
            audioEncoder.start()
            audioEncoder.setCodecListener(object : CodecListener {
                override fun formatUpdate(format: MediaFormat) {
                    super.formatUpdate(format)
                    muxerTrackIndex = mediaMuxer.addTrack(format)
                    mediaMuxer.start()
                }

                override fun bufferUpdate(data: ByteArray) {
                    super.bufferUpdate(data)//编码好的数据加上ADTS头后的数据
                    DLog.d("bufferUpdate ${data.size}")
                }

                override fun bufferUpdate(
                    buffer: ByteBuffer,
                    bufferInfo: MediaCodec.BufferInfo
                ) {
                    //直接将编码好的数据存到本地
                    mediaMuxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                }

                override fun bufferOutputEnd() {
                    super.bufferOutputEnd()
                    audioEncoder.stopWorld()
                    mediaMuxer.release()
                }
            })


            //创建音频采集器并启动
            pcmCreate = AudioGeneratePCM()
            pcmCreate.listener = { data, offset, len ->
                audioEncoder.putBuf(data, offset, len)//采集到的音频裸流pcm交给编码器编码
            }
            pcmCreate.start()

        }

        binding.stopRecord.setOnClickListener {
            DLog.d("停止录制")
            pcmCreate.stopRecord()
            audioEncoder.putBufEnd()
        }
    }
}