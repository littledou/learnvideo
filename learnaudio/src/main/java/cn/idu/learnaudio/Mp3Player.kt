package cn.idu.learnaudio

import android.media.*

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