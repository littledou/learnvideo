package cn.idu.kotlinnativetest

import android.media.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * 安卓设备上播mp3pcm
 */
class PcmPlayer(private val path: String) : Thread() {

    override fun run() {
        super.run()

        val pcmFile = File(path);

        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            SAMPLE_RATE_IN_HZ,
            CHANNEL,
            AUDIO_FORMAT,
            pcmFile.length().toInt(),
            AudioTrack.MODE_STATIC
        )
        val fis = FileInputStream(pcmFile)
        val buffer = ByteArray(1024 * 1024)

        while (true) {
            val len = fis.read(buffer)
            if (len == -1) break
            audioTrack.write(buffer, 0, len)
        }

        audioTrack.play()

    }

}