package cn.idu.learnvideo.video.codec

import android.media.AudioFormat

interface ICodec {

    //入队
    fun putBuf(data: ByteArray, offset: Int, size: Int)
    //处理数据
    fun dealWith(data: ByteArray)
    //停止线程
    fun stopWorld()
}

const val SAMPLE_RATE_IN_HZ = 44100 //采样率44.1KHz
const val CHANNEL = AudioFormat.CHANNEL_IN_MONO //单声道，立体声：CHANNEL_IN_STEREO
const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT //每个采样点16bit
const val DEST_BIT_RATE = 128000 //编码码率
