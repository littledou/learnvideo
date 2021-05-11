package cn.idu.learnvideo.mp.codec.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import cn.idu.learnvideo.mp.codec.BaseMediaCodec
import java.nio.ByteBuffer

class VideoEncoder : BaseMediaCodec() {

    init {
        createCodec("video/avc")
        setTag("VideoEncoder")
    }

    fun setUpVideoCodec(width: Int, height: Int) {
        val format = MediaFormat.createVideoFormat(mime, width, height)
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
        )
        //视频编码码率：width*height*frameRate*[0.1-0.2]码率控制清晰度
        format.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 3)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        //每秒出一个关键帧，设置0为每帧都是关键帧
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
//        format.setInteger(
//            MediaFormat.KEY_BITRATE_MODE,
//            MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR//遵守用户设置的码率
//        )
        configEncoderBitrateMode(format)//设置码率模式
        codec.start()
    }

    override fun bufferUpdate(buffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        listener?.bufferUpdate(buffer, bufferInfo)
    }

}