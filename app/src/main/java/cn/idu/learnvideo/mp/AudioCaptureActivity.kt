package cn.idu.learnvideo.mp

import android.media.AudioRecord
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaRecorder
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
import kotlin.concurrent.thread

class AudioCaptureActivity : BaseCoreActivity() {
    lateinit var binding: ActivityAudioCaptureBinding

    override fun getLayoutView(): View {
        binding = ActivityAudioCaptureBinding.inflate(layoutInflater)
        return binding.root
    }

    var isCapture = false

    override fun initView() {
        val file_pcm = File("$filesDir/test.pcm")
        val file_mp3 = File("$filesDir/test.mp3")


        binding.startRecord.setOnClickListener {
            file_pcm.deleteOnExit()
            file_mp3.deleteOnExit()
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
                        file_mp3.absolutePath,
                        MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
                    )
                var muxerTrackIndex = -1

                val audioEncoder = AudioEncoder()
                audioEncoder.setCodecListener(object : CodecListener {
                    override fun formatUpdate(format: MediaFormat) {
                        super.formatUpdate(format)

                    }

                    override fun bufferUpdate(data: ByteArray) {
                        super.bufferUpdate(data)
                        DLog.d("bufferUpdate ${data.size}")
                        fos_mp3.write(data, 0, data.size)
                    }

                    override fun bufferOutputEnd() {
                        super.bufferOutputEnd()
                        audioEncoder.stopWorld()
                        fos_mp3.flush()
                        fos_mp3.close()
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