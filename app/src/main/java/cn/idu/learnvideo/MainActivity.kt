package cn.idu.learnvideo

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.idu.learnvideo.codec.CodecSample
import java.io.File
import java.io.FileInputStream
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    val mime = "video/avc"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        thread {
            Thread.sleep(2000)
            //TODO yuv视频编码为h264
            CodecSample.convertYuv2Mp4(baseContext)
        }


    }


}






