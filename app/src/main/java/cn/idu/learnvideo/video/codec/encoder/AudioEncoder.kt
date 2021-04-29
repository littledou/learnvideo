package cn.idu.learnvideo.video.codec.encoder

import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import cn.idu.learnvideo.video.codec.*
import java.nio.ByteBuffer

class AudioEncoder : BaseMediaCodec() {
    private val TAG = "AudioEncoder"

    init {
        createCodec("audio/mp4a-latm")
        val format = MediaFormat.createAudioFormat(mime, SAMPLE_RATE_IN_HZ, CHANNEL)
        format.setInteger(MediaFormat.KEY_BIT_RATE, DEST_BIT_RATE)
        //buffer 最大值
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL, AUDIO_FORMAT)
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSize)
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        configEncoderBitrateMode(format)
//        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()
    }

    override fun formatUpdate(format: MediaFormat) {
        listener?.formatUpdate(format)
    }

    override fun bufferUpdate(buffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        buffer.position(bufferInfo.offset)
        buffer.limit(bufferInfo.offset + bufferInfo.size)
        val data = ByteArray(bufferInfo.size + 7)
        addADTStoPacket(data, data.size)
        buffer.get(data, 7, bufferInfo.size)
        buffer.position(bufferInfo.offset)
        listener?.bufferUpdate(data)
    }

    /**
     * 添加ADTS头部的7个字节
     */
    private fun addADTStoPacket(packet: ByteArray, packetLen: Int) {
        val profile = 2 // AAC LC
        val freqIdx: Int = 4// 44.1kHz
        val chanCfg = 2 // CPE
        packet[0] = 0xFF.toByte()
        packet[1] = 0xF9.toByte()
        packet[2] = ((profile - 1 shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
        packet[3] = ((chanCfg and 3 shl 6) + (packetLen shr 11)).toByte()
        packet[4] = ((packetLen and 0x7FF) shr 3).toByte()
        packet[5] = ((packetLen and 7 shl 5) + 0x1F).toByte()
        packet[6] = 0xFC.toByte()
    }
}