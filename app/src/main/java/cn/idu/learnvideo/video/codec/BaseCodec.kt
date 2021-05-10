package cn.idu.learnvideo.video.codec

import android.media.AudioFormat
import java.lang.Exception
import java.util.concurrent.ArrayBlockingQueue

abstract class BaseCodec : Thread(), ICodec {

    val inBlockingQueue = ArrayBlockingQueue<ByteArray>(30)


    override fun putBuf(data: ByteArray, offset: Int, size: Int) {
        val byteArray = ByteArray(size)
        System.arraycopy(data, offset, byteArray, 0, size)
        inBlockingQueue.put(byteArray)
    }

    var threadRunning = true;

    override fun run() {

        try {
            BaseCodecLoop1@ while (threadRunning) {
                val item = inBlockingQueue.take()
                dealWith(item)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun dealWith(data: ByteArray) {}

    override fun stopWorld() {
        inBlockingQueue.clear()
        threadRunning = false;
        interrupt()
        join(1000)
    }
}

const val SAMPLE_RATE_IN_HZ = 44100 //采样率44.1KHz
const val CHANNEL = AudioFormat.CHANNEL_IN_MONO //单声道，立体声：CHANNEL_IN_STEREO
const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT //每个采样点16bit
const val DEST_BIT_RATE = 128000 //编码码率