package cn.idu.learnvideo.video.codec.encoder

import android.media.MediaCodec
import android.media.MediaFormat
import java.nio.ByteBuffer

interface CodecListener {
    fun formatUpdate(format: MediaFormat) {}
    fun bufferUpdate(buffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {}
    fun bufferUpdate(data: ByteArray) {}
    fun bufferOutputEnd(){}
}