package cn.idu.learnaudio

import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.File
import java.io.FileOutputStream

/**
 * 从安卓设备使用AudioRecodrd录制PCM音频
 */
class PcmCreator(private val path: String) : Thread() {

    var threadRunning = false

    override fun run() {
        super.run()
        threadRunning = true;
        File(path).deleteOnExit()

        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE_IN_HZ, CHANNEL, AUDIO_FORMAT
        )
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ, CHANNEL, AUDIO_FORMAT, bufferSize
        )

        val fos = FileOutputStream(path)
        audioRecord.startRecording()
        val data = ByteArray(bufferSize)
        while (threadRunning) {
            val len = audioRecord.read(data, 0, data.size)
            fos.write(data, 0, len);
        }
        fos.flush()
        audioRecord.stop()
    }

    fun stopRecord() {
        threadRunning = false;
    }
}