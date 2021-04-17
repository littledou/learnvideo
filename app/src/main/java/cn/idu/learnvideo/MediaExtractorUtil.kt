package cn.idu.learnvideo

import android.media.*
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class MediaExtractorUtil {

    companion object {
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
