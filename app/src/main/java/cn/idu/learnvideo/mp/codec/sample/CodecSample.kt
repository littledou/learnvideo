package cn.idu.learnvideo.mp.codec.sample

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import cn.idu.learnvideo.mp.codec.CodecListener
import cn.idu.learnvideo.mp.codec.encoder.VideoEncoder
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import kotlin.system.measureTimeMillis

class CodecSample {

    companion object {
        /**
         *YUV视频流编码为H.264/H.265码流并通过MediaMuxer保存为mp4文件
         */
        fun convertYuv2Mp4(context: Context) {
            val yuvPath = "${context.filesDir}/test.yuv"
            val saveMp4Path = "${context.filesDir}/test.mp4"
            File(saveMp4Path).deleteOnExit()

            val mime = "video/avc" //若设备支持H.265也可以使用'video/hevc'编码器
            val format = MediaFormat.createVideoFormat(mime, 1920, 1080)
            format.setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
            )
            //width*height*frameRate*[0.1-0.2]码率控制清晰度
            format.setInteger(MediaFormat.KEY_BIT_RATE, 1920 * 1080 * 3)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            //每秒出一个关键帧，设置0为每帧都是关键帧
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            format.setInteger(
                MediaFormat.KEY_BITRATE_MODE,
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR//遵守用户设置的码率
            )

            //定义并启动编码器
            val videoEncoder = MediaCodec.createEncoderByType(mime)
            videoEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            videoEncoder.start()

            // 当前编码帧信息
            val bufferInfo = MediaCodec.BufferInfo()

            //定义混合器：输出并保存h.264码流为mp4
            val mediaMuxer =
                MediaMuxer(
                    saveMp4Path,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
                );
            var muxerTrackIndex = -1
            val byteArray = ByteArray(1920 * 1080 * 3 / 2)
            var read = 0
            var inputEnd = false//数据读取完毕，并且全部都加载至编码器
            var pushEnd = false //数据读取完毕，并且成功发出eof信号
            val presentTimeUs = System.nanoTime() / 1000


            //从文件中读取yuv码流，模拟输入流
            FileInputStream(yuvPath).use { fis ->
                loop1@ while (true) {
                    //step1 将需要编码的数据逐帧送往编码器
                    if (!inputEnd) {
                        //step1.1 查询编码器队列是否空闲
                        val inputQueueIndex = videoEncoder.dequeueInputBuffer(30);
                        if (inputQueueIndex > 0) {
                            read = fis.read(byteArray)
                            if (read == byteArray.size) {
                                //默认从Camera中保存的YUV NV21，编码后颜色成反，手动转为NV12后，颜色正常
                                val convertCost = measureTimeMillis {
                                    val start = 1920 * 1080
                                    val end = 1920 * 1080 / 4 - 1
                                    for (i in 0..end) {
                                        val temp = byteArray[2 * i + start]
                                        byteArray[2 * i + start] = byteArray[2 * i + start + 1]
                                        byteArray[2 * i + start + 1] = temp
                                    }
                                }
                                //step1.2 将数据送往编码器，presentationTimeUs为送往编码器的跟起始值的时间差，单位为微妙
                                val inputBuffer =
                                    videoEncoder.getInputBuffer(inputQueueIndex)
                                inputBuffer?.clear()
                                inputBuffer?.put(byteArray)
                                videoEncoder.queueInputBuffer(
                                    inputQueueIndex,
                                    0,
                                    byteArray.size,
                                    System.nanoTime() / 1000,
                                    0
                                )
                            } else {
                                inputEnd = true//文件读取结束标记
                            }
                        }
                    }

                    //step2 将结束标记传给编码器
                    if (inputEnd && !pushEnd) {
                        val inputQueueIndex = videoEncoder.dequeueInputBuffer(30);
                        if (inputQueueIndex > 0) {
                            val pts: Long = System.nanoTime() / 1000
                            videoEncoder.queueInputBuffer(
                                inputQueueIndex,
                                0,
                                0,
                                pts,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            pushEnd = true
                            println("数据输入完成,成功发出eof信号")
                        }
                    }

                    //step3 从编码器中取数据，不及时取出，缓冲队列被占用，编码器将阻塞不进行编码工作
                    val outputQueueIndex = videoEncoder.dequeueOutputBuffer(bufferInfo, 30)
                    when (outputQueueIndex) {
                        MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            //step3.1 标记新的解码数据到来，在此添加视频轨道到混合器
                            muxerTrackIndex = mediaMuxer.addTrack(videoEncoder.outputFormat)
                            mediaMuxer.start()
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
                                    videoEncoder.releaseOutputBuffer(outputQueueIndex, false)
                                    println("数据解码并获取完成,成功发出eof信号")
                                    break@loop1
                                }
                                else -> {
                                    mediaMuxer.writeSampleData(
                                        muxerTrackIndex,
                                        videoEncoder.getOutputBuffer(outputQueueIndex)!!,
                                        bufferInfo
                                    )
                                }
                            }
                            videoEncoder.releaseOutputBuffer(outputQueueIndex, false)
                        }
                    }
                }

                //释放应该释放的具柄
                mediaMuxer.release()
                videoEncoder.stop()
                videoEncoder.release()
            }
        }

        fun convertYuv2Mp4_2(context: Context) {
            val yuvPath = "${context.filesDir}/test.yuv"
            val saveMp4Path = "${context.filesDir}/test.mp4"
            File(saveMp4Path).deleteOnExit()

            //定义混合器：输出并保存h.264码流为mp4
            val mediaMuxer =
                MediaMuxer(
                    saveMp4Path,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
                );
            var muxerTrackIndex = -1

            val videoEncoder = VideoEncoder()
            videoEncoder.setUpVideoCodec(1920, 1080)
            videoEncoder.start()
            videoEncoder.setCodecListener(object : CodecListener {
                override fun formatUpdate(format: MediaFormat) {
                    //step3.1 标记新的解码数据到来，在此添加视频轨道到混合器
                    muxerTrackIndex = mediaMuxer.addTrack(format)
                    mediaMuxer.start()
                }

                override fun bufferUpdate(buffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
                    mediaMuxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                }

                override fun bufferOutputEnd() {
                    mediaMuxer.release()
                    videoEncoder.stopWorld()
                }
            })


            val byteArray = ByteArray(1920 * 1080 * 3 / 2)
            var read = 0
            FileInputStream(yuvPath).use { fis ->
                while (true) {
                    read = fis.read(byteArray)
                    if (read == byteArray.size) {
                        Thread.sleep(30)
                        videoEncoder.putBuf(byteArray, 0, byteArray.size)
                    } else {
                        videoEncoder.putBufEnd()
                        break
                    }
                }
            }

        }
    }
}