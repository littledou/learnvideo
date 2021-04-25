package cn.idu.learnaudio

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val parentPath = getExternalFilesDir(null)
        val pcmPath = "$parentPath/test.pcm";
        File(pcmPath).deleteOnExit()
        File(pcmPath).deleteOnExit()
        val mp3Path = "$parentPath/test.mp3";


        val pcmCreator = PcmCreator(pcmPath)
        val mp3Encoder = Mp3Encoder();

        findViewById<Button>(R.id.startRecord).setOnClickListener {
            showToast("开始录制pcm，已删除旧文件$pcmPath")
            pcmCreator.start()
        }
        findViewById<Button>(R.id.stopRecord).setOnClickListener {
            pcmCreator.stopRecord()
            showToast("结束录制pcm,pcm文件存储于$pcmPath")
        }

        findViewById<Button>(R.id.playpcm).setOnClickListener {
            showToast("播放pcm：$pcmPath")
            PcmPlayer(pcmPath).start()
        }

        findViewById<Button>(R.id.initEncoder).setOnClickListener {
            File("$filesDir/test.mp3").delete()
            mp3Encoder.init(
                pcmPath,
                CHANNEL, AUDIO_FORMAT, SAMPLE_RATE_IN_HZ,
                mp3Path
            )
            Thread {
                println("encoder start")
                mp3Encoder.encode()
                println("encoder end")
                runOnUiThread {
                    showToast("pcm转mp3成功，存储于$mp3Path,可以点击播放mp3按钮了")
                }
                mp3Encoder.destory()
            }.start()
        }


        findViewById<Button>(R.id.playmp3).setOnClickListener {
            showToast("播放mp3：$mp3Path")
            Mp3Player(mp3Path).start()
        }


    }

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    private fun showToast(msg: String) {
        runOnUiThread {
            Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show();
        }
    }
}