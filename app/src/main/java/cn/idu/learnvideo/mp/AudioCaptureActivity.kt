package cn.idu.learnvideo.mp

import android.media.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import cn.idu.learnvideo.databinding.ActivityAudioCaptureBinding
import cn.idu.learnvideo.mp.codec.encoder.AudioEncoder
import cn.idu.learnvideo.mp.codec.encoder.CodecListener
import cn.readsense.module.base.BaseCoreActivity
import cn.readsense.module.util.DLog
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.concurrent.thread

class AudioCaptureActivity : BaseCoreActivity() {
    lateinit var binding: ActivityAudioCaptureBinding

    override fun getLayoutView(): View {
        binding = ActivityAudioCaptureBinding.inflate(layoutInflater)
        return binding.root
    }

    var isCapture = false

    override fun initView() {
        //存储pcm裸数据
        val file_pcm = File("$filesDir/test.pcm")
        //直接FileOutputStream写出，存储编码完成的aac数据
        val file_mp3 = File("$filesDir/test.aac")
        //使用混合器mediaMuxer存储编码好的aac数据
        val file_mp32 = File("$filesDir/test2.aac")

        binding.startRecord.setOnClickListener {
            file_pcm.deleteOnExit()
            file_mp3.deleteOnExit()
            file_mp32.deleteOnExit()
            thread {
                DLog.d("开始录制")
                isCapture = true
                val bufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE_IN_HZ, CHANNEL, AUDIO_FORMAT
                )
                val audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_IN_HZ,
                    CHANNEL,
                    AUDIO_FORMAT,
                    bufferSize
                )

                audioRecord.startRecording()
                val data = ByteArray(bufferSize)
                val fos = FileOutputStream(file_pcm)
                val fos_mp3 = FileOutputStream(file_mp3)

                //定义混合器：输出并保存编码后的aac为mp3
                val mediaMuxer =
                    MediaMuxer(
                        file_mp32.absolutePath,
                        MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
                    )
                var muxerTrackIndex = -1

                val audioEncoder = AudioEncoder()
                audioEncoder.setCodecListener(object : CodecListener {
                    override fun formatUpdate(format: MediaFormat) {
                        super.formatUpdate(format)
                        muxerTrackIndex = mediaMuxer.addTrack(format)
                        mediaMuxer.start()
                    }

                    override fun bufferUpdate(data: ByteArray) {
                        super.bufferUpdate(data)
                        DLog.d("bufferUpdate ${data.size}")
                        fos_mp3.write(data, 0, data.size)
                    }

                    override fun bufferUpdate(
                        buffer: ByteBuffer,
                        bufferInfo: MediaCodec.BufferInfo
                    ) {
                        mediaMuxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                    }

                    override fun bufferOutputEnd() {
                        super.bufferOutputEnd()
                        audioEncoder.stopWorld()
                        fos_mp3.flush()
                        fos_mp3.close()
                        mediaMuxer.release()
                    }
                })
                audioEncoder.start()

                while (isCapture) {
                    val len = audioRecord.read(data, 0, data.size)
                    audioEncoder.putBuf(data, 0, len)
                    fos.write(data, 0, len);
                }

                fos.flush()
                fos.close()

                audioEncoder.putBufEnd()
                audioRecord.stop()
                DLog.d("录制结束")
            }
        }

        binding.stopRecord.setOnClickListener {
            isCapture = false
            DLog.d("停止录制")
        }
    }
}