package cn.idu.learnvideo.mp.codec.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import cn.idu.learnvideo.mp.CHANNEL_NUMBERS
import cn.idu.learnvideo.mp.DEST_BIT_RATE
import cn.idu.learnvideo.mp.SAMPLE_RATE_IN_HZ
import cn.idu.learnvideo.mp.codec.*
import java.nio.ByteBuffer

class AudioEncoder : BaseMediaCodec() {
    init {
        setTag("AudioEncoder")
        createCodec("audio/mp4a-latm")//MediaFormat.MIMETYPE_AUDIO_AAC
        val format = MediaFormat.createAudioFormat(
            MediaFormat.MIMETYPE_AUDIO_AAC,
            SAMPLE_RATE_IN_HZ,
            CHANNEL_NUMBERS
        )
        format.setInteger(MediaFormat.KEY_BIT_RATE, DEST_BIT_RATE)
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 100 * 1024)
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        configEncoderBitrateMode(format)
        codec.start()
    }

    override fun bufferUpdate(buffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        buffer.position(bufferInfo.offset)
        buffer.limit(bufferInfo.offset + bufferInfo.size)
        val data = ByteArray(bufferInfo.size + 7)
        addADTStoPacket(data, data.size)
        buffer.get(data, 7, bufferInfo.size)
        buffer.position(bufferInfo.offset)
        listener?.bufferUpdate(data)
        listener?.bufferUpdate(buffer, bufferInfo)
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
        packet[2] = (((profile - 1) shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
        packet[3] = (((chanCfg and 3) shl 6) + (packetLen shr 11)).toByte()
        packet[4] = ((packetLen and 0x7FF) shr 3).toByte()
        packet[5] = (((packetLen and 7) shl 5) + 0x1F).toByte()
        packet[6] = 0xFC.toByte()
    }

}