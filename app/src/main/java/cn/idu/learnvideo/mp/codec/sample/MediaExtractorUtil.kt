package cn.idu.learnvideo.mp.codec.sample

import android.content.Context
import android.media.*
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class MediaExtractorUtil {

    companion object {

        fun test(context: Context) {

            val videoPath = "${context.filesDir}/a1.mp4"

            //解复用器 demuxer
            val mediaExtractor = MediaExtractor();
            mediaExtractor.setDataSource(videoPath);
            val numTracks = mediaExtractor.trackCount;
            println("extrarot streams $numTracks;")

            var audioFrameIndex = -1
            var videoFrameIndex = -1
            for (i in 0 until numTracks) {
                val mediaFormat = mediaExtractor.getTrackFormat(i)
                val mine = mediaFormat.getString(MediaFormat.KEY_MIME)//获取MIME格式内容
                println("stream $i [mime: $mine]")
                if (mine?.startsWith("video/")!!) {
                    videoFrameIndex = i
                }
                if (mine.startsWith("audio/")) {
                    audioFrameIndex = i
                }

                val language = mediaFormat.getString(MediaFormat.KEY_LANGUAGE)//获取语言格式内容
                println("stream $i [language: $language]")

                val durationTime = mediaFormat.getLong(MediaFormat.KEY_DURATION) //总时间
                println("stream $i [总时长:$durationTime]")

                val maxByteCount =
                    mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) //获取缓存输出的最大大小
                println("stream $i [缓存输出的最大大小:$maxByteCount 字节]")

                if (mine.contains("video")) {
                    val width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH) //获取高度
                    val height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT) //获取高度
                    println("stream $i 宽高：[width: $width] [height: $height]")
                    val frameRate = mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE) //帧率
                    println("stream $i [frameRate:$frameRate]")
//                val colorFormat = mediaFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT) //颜色格式
//                println("stream $i [颜色格式:$colorFormat]")
//                val isAdts = mediaFormat.getInteger(MediaFormat.KEY_IS_ADTS)
//                println("stream $i [isAdts:$isAdts]")

                } else {
                    val sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE) //获取音频采样率
                    println("stream $i [音频采样率:$sampleRate]")

                    val bitRate = mediaFormat.getInteger(MediaFormat.KEY_BIT_RATE) //获取比特
                    println("stream $i [音频比特率:$sampleRate]")

                    val channelCount =
                        mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) //获取声道数量
                    println("stream $i [声道数:$channelCount]")

//                val pcmEncoding =
//                    mediaFormat.getInteger(MediaFormat.KEY_PCM_ENCODING) //PCM-编码 模拟信号编码
//                println("stream $i [pcmEncoding:$pcmEncoding]")

//                val maxWidth = mediaFormat.getInteger(MediaFormat.KEY_MAX_WIDTH) //最大宽度
//                val maxHeight = mediaFormat.getInteger(MediaFormat.KEY_MAX_HEIGHT) //最大高度
//                println("stream $i 最大宽高：[width: $maxWidth] [height: $maxHeight]")
                }
            }


            //实例1：从mp4里分离出音频文件1
            if (audioFrameIndex != -1) {
                //实例1.1
//            val path2Save = "$filesDir/test.aac"
//            MediaExtractorUtil.extractorAudio1(mediaExtractor, audioFrameIndex, path2Save)
                //实例1.2：从mp4里分离出音频文件2
                val path2Save = "${context.filesDir}/test.mp3"
                mediaExtractor.extractorMedia(audioFrameIndex, path2Save)
            }

            //实例2：从mp4里分离出视频文件
            if (videoFrameIndex != -1) {
                //实例2.1
//            val path2Save = "$filesDir/test.h264"
//            MediaExtractorUtil.extractorVideo1(mediaExtractor, videoFrameIndex, path2Save)
                //实例2.2
                val path2Save = "${context.filesDir}/test.mp4"
                mediaExtractor.extractorMedia(videoFrameIndex, path2Save)
            }
            mediaExtractor.release()
            //实例3：合并解析好的音频和视频为mp4
            muxerAudioAndVideo(
                "${context.filesDir}/test.mp3",
                "${context.filesDir}/test.mp4",
                "${context.filesDir}/a1out.mp4"
            )
        }


        fun muxerAudioAndVideo(audioPath: String, videoPath: String, outPath: String) {
            //合并解析好的音频和视频为mp4

            val audioExtractor = MediaExtractor();
            audioExtractor.setDataSource(audioPath)
            val audioFrameIndex = audioExtractor.findTargetStreamIndex("audio/")

            val videoExtractor = MediaExtractor()
            videoExtractor.setDataSource(videoPath)
            val videoFrameIndex = videoExtractor.findTargetStreamIndex("video/")

            if (audioFrameIndex != -1 && videoFrameIndex != -1) {
                val mediaMuxer =
                    MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                mediaMuxer.muxerAudioAndVideo(
                    audioExtractor,
                    videoExtractor,
                    audioFrameIndex,
                    videoFrameIndex
                )
                mediaMuxer.release()
            } else {
                println("输入音频[$audioPath: $audioFrameIndex]或视频[$videoPath: $videoFrameIndex]有异常，请检查")
            }

            audioExtractor.release()
            videoExtractor.release()
        }


        fun extractorAudio1(
            mediaExtractor: MediaExtractor,
            audioFrameIndex: Int,
            path2Save: String
        ) {

            mediaExtractor.selectTrack(audioFrameIndex)
            val format = mediaExtractor.getTrackFormat(audioFrameIndex)

            val outAudioFile = File(path2Save)
            outAudioFile.deleteOnExit()
            val fos = FileOutputStream(outAudioFile)

            val maxInputSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            val byteBuffer = ByteBuffer.allocate(maxInputSize)
            val byteArray = ByteArray(byteBuffer.capacity());
            while (true) {
                val readSampleDataSize = mediaExtractor.readSampleData(byteBuffer, 0)
                if (readSampleDataSize < 0) {
                    println("读取无音频数据，停止！")
                    break
                }
                println("写入音频数据 [$readSampleDataSize] 字节")
                byteBuffer.get(byteArray, 0, readSampleDataSize)
                fos.write(byteArray, 0, readSampleDataSize)
                byteBuffer.clear()
                if (!mediaExtractor.advance()) {
                    println("切换至下一读取点后，无数据，停止！")
                    break
                }
            }
            fos.flush()
            fos.close()
            mediaExtractor.unselectTrack(audioFrameIndex)
        }

        fun extractorVideo1(
            mediaExtractor: MediaExtractor,
            videoFrameIndex: Int,
            path2Save: String
        ) {
            mediaExtractor.selectTrack(videoFrameIndex)
            val format = mediaExtractor.getTrackFormat(videoFrameIndex)
            val outVideoFile = File(path2Save)
            outVideoFile.deleteOnExit()
            val fos = FileOutputStream(outVideoFile)

            //读取视频时缓冲区大小设置为100*1024时，可能会出现崩溃，改到500未出发崩溃
            //Caused by: java.lang.IllegalArgumentException
            //        at android.media.MediaExtractor.readSampleData(Native Method)
            val maxInputSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            val byteBuffer = ByteBuffer.allocate(maxInputSize)

            val byteArray = ByteArray(byteBuffer.capacity());
            while (true) {
                val readSampleDataSize = mediaExtractor.readSampleData(byteBuffer, 0)
                if (readSampleDataSize < 0) {
                    println("读取无视频数据，停止！")
                    break
                }
                println("写入视频数据 [$readSampleDataSize] 字节")
                byteBuffer.get(byteArray, 0, readSampleDataSize)
                fos.write(byteArray, 0, readSampleDataSize)
                byteBuffer.clear()
                if (!mediaExtractor.advance()) {
                    println("切换至下一读取点后，无视频数据，停止！")
                    break
                }
            }
            fos.flush()
            fos.close()
            mediaExtractor.unselectTrack(videoFrameIndex)
        }

    }
}
