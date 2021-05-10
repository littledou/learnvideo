package cn.idu.learnvideo.video.codec

import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.util.Log
import cn.idu.learnvideo.video.codec.encoder.CodecListener
import cn.readsense.module.util.DLog
import java.nio.ByteBuffer

abstract class BaseMediaCodec : BaseCodec() {
//    var inputBuffers: Array<ByteBuffer>? = null
//    var outputBuffers: Array<ByteBuffer>? = null

    lateinit var codec: MediaCodec
    lateinit var mime: String
    private var gatherThread: Thread? = null
    private val bufferInfo = MediaCodec.BufferInfo()
    protected var listener: CodecListener? = null

    /**
     * video/avc: h.264
     * video/hevc: h.265
     * audio/mp4a-latm: aac
     */
    fun createCodec(mime: String = "video/avc") {
        this.mime = mime
        codec = MediaCodec.createEncoderByType(mime)
    }

    fun configEncoderBitrateMode(format: MediaFormat) {
        try {
            configEncoderWithCQ(codec, format)
        } catch (e: Exception) {
            e.printStackTrace()
            // 捕获异常，设置为系统默认配置 BITRATE_MODE_VBR
            try {
                configEncoderWithVBR(codec, format)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(mime, "配置编解码器失败")
            }
        }
    }

    private fun configEncoderWithCQ(codec: MediaCodec, outputFormat: MediaFormat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 本部分手机不支持 BITRATE_MODE_CQ 模式，有可能会异常
            outputFormat.setInteger(
                MediaFormat.KEY_BITRATE_MODE,
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ
            )
        }
        codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }

    private fun configEncoderWithVBR(codec: MediaCodec, outputFormat: MediaFormat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outputFormat.setInteger(
                MediaFormat.KEY_BITRATE_MODE,
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR
            )
        }
        codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }


    override fun run() {
        gatherThread = Thread {
            while (threadRunning) {
                val outputQueueIndex = codec.dequeueOutputBuffer(bufferInfo, -1)
                DLog.d("数据解码出队 $outputQueueIndex")
                when (outputQueueIndex) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        formatUpdate(codec.outputFormat)
                    }
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    }
                    else -> {
                        when (bufferInfo.flags) {
                            MediaCodec.BUFFER_FLAG_CODEC_CONFIG -> {
                                // SPS or PPS, which should be passed by MediaFormat.
                            }
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM -> {
                                bufferInfo.set(0, 0, 0, bufferInfo.flags)
                                codec.releaseOutputBuffer(outputQueueIndex, false)
                                println("数据解码并获取完成,成功发出eof信号")
                                bufferOutputEnd()
                                break
                            }
                            else -> {
                                bufferUpdate(
                                    codec.getOutputBuffer(outputQueueIndex)!!,
                                    bufferInfo
                                )
                            }
                        }
                        codec.releaseOutputBuffer(outputQueueIndex, false)
                    }
                }
            }
        }
        gatherThread!!.name = "${mime}-dequeue-thread-${System.currentTimeMillis()}"
        name = "${mime}-queue-thread-${System.currentTimeMillis()}"
        gatherThread?.start()
        super.run()
    }

    /**
     *处理输入的数据，将输入的数据放置入codec
     */
    override fun dealWith(data: ByteArray) {
        val inputQueueIndex = codec.dequeueInputBuffer(-1)
        DLog.d("数据入队 $inputQueueIndex")
        if (inputQueueIndex > 0) {
            val inputBuffer = codec.getInputBuffer(inputQueueIndex)
            inputBuffer?.clear()
            inputBuffer?.put(data)
            codec.queueInputBuffer(
                inputQueueIndex,
                0,
                data.size,
                System.nanoTime() / 1000,
                0
            )
        }
    }
    abstract fun bufferUpdate(buffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo)

    private fun formatUpdate(format: MediaFormat) {
        listener?.formatUpdate(format)
    }
    private fun bufferOutputEnd() {
        listener?.bufferOutputEnd()
    }

    fun setCodecListener(listener: CodecListener) {
        this.listener = listener
    }

    public fun putBufEnd() {
        DLog.d("发送结束标记")
        val inputQueueIndex = codec.dequeueInputBuffer(-1);
        if (inputQueueIndex > 0) {
            codec.queueInputBuffer(
                inputQueueIndex,
                0,
                0,
                System.nanoTime() / 1000,
                MediaCodec.BUFFER_FLAG_END_OF_STREAM
            )
        }
    }

    override fun stopWorld() {
        super.stopWorld()
        try {
            gatherThread?.interrupt()
            gatherThread?.join(1000)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}