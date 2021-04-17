package cn.idu.kotlinnativetest

import android.media.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * 安卓设备上播mp3
 */
class Mp3Player(private val path: String) : Thread() {


    override fun run() {
        super.run()

        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(path)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
        }

        mediaPlayer.setOnCompletionListener {
            mediaPlayer.stop()
            mediaPlayer.release()
        }

    }
}