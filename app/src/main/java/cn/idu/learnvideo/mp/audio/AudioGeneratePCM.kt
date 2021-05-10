package cn.idu.learnvideo.mp.audio

import android.media.AudioRecord
import android.media.MediaRecorder
import cn.idu.learnvideo.mp.AUDIO_FORMAT
import cn.idu.learnvideo.mp.CHANNEL
import cn.idu.learnvideo.mp.SAMPLE_RATE_IN_HZ
import java.io.File
import java.io.FileOutputStream

/**
 * 从安卓设备使用AudioRecodrd录制PCM音频
 */
class AudioGeneratePCM() : Thread() {

    var threadRunning = false

    override fun run() {
        super.run()
        threadRunning = true;

        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE_IN_HZ, CHANNEL, AUDIO_FORMAT
        )
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ, CHANNEL, AUDIO_FORMAT, bufferSize
        )

        audioRecord.startRecording()
        val data = ByteArray(bufferSize)
        while (threadRunning) {
            val len = audioRecord.read(data, 0, data.size)

        }
        audioRecord.stop()
    }

    fun stopRecord() {
        threadRunning = false;
    }
}