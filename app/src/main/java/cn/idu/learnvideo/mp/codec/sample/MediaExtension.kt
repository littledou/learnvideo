package cn.idu.learnvideo.mp.codec.sample

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import java.nio.ByteBuffer

/**
 * 分离对应轨道中的媒体，并保存到指定路径
 */
fun MediaExtractor.extractorMedia(frameIndex: Int, path2Save: String) {
    val mediaMuxer = MediaMuxer(path2Save, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    mediaMuxer.demuxerMedia(this, frameIndex)
    mediaMuxer.release()
}

/**
 * 是否解复用出指定header，video/或audio/
 */
fun MediaExtractor.findTargetStreamIndex(header: String="video/"): Int {
    val trackCount = trackCount
    for (i in 0 until trackCount) {
        val format = getTrackFormat(i)
        val mine = format.getString(MediaFormat.KEY_MIME)
        if (mine?.startsWith(header)!!) {
            println("found target stream [$header] index[$i]")
            return i
        }
    }
    println("not found target stream [$header]")
    return -1
}


/**
 * 将分离器中指定index数据，写入指定混合器
 */
fun MediaMuxer.demuxerMedia(extractor: MediaExtractor, frameIndex: Int) {
    extractor.selectTrack(frameIndex)
    val format = extractor.getTrackFormat(frameIndex)

    val trackIndex = addTrack(format)
    start()
    writeMediaBuffer(extractor, format, trackIndex)

    extractor.unselectTrack(frameIndex)
    release()
}


/**
 * 合并两路流
 */
fun MediaMuxer.muxerAudioAndVideo(
    audioExtractor: MediaExtractor,
    videoExtractor: MediaExtractor,
    audioFrameIndex: Int,
    videoFrameIndex: Int
) {

    var audioTrackIndex = -1
    audioExtractor.selectTrack(audioFrameIndex)
    var audioFormat = audioExtractor.getTrackFormat(audioFrameIndex)

    var videoTrackIndex = -1
    videoExtractor.selectTrack(videoFrameIndex)
    val videoFormat = videoExtractor.getTrackFormat(videoFrameIndex)

    audioTrackIndex = addTrack(audioFormat)//追踪音频信道
    videoTrackIndex = addTrack(videoFormat)//追踪视频信道
    start()//开始准备混合
    //写入音频流
    writeMediaBuffer(audioExtractor, audioFormat, audioTrackIndex)
    //写入视频流
    writeMediaBuffer(videoExtractor, videoFormat, videoTrackIndex)

    audioExtractor.unselectTrack(audioFrameIndex)
    videoExtractor.unselectTrack(videoFrameIndex)
    release()
}

/**
 * 写入流
 */
private fun MediaMuxer.writeMediaBuffer(
    extractor: MediaExtractor,
    format: MediaFormat,
    trackIndex: Int
) {
    val maxMediaBufferCount: Int = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
    val byteBuffer = ByteBuffer.allocate(maxMediaBufferCount)
    val bufferInfo = MediaCodec.BufferInfo()
    while (true) {
        val readSampleDataSize = extractor.readSampleData(byteBuffer, 0)
        if (readSampleDataSize < 0) {
//            println("index [$trackIndex] 读取无媒体数据，停止！")
            break
        }
        bufferInfo.size = readSampleDataSize
        bufferInfo.offset = 0
        bufferInfo.presentationTimeUs = extractor.sampleTime
        bufferInfo.flags = extractor.sampleFlags
//        println("index [$trackIndex] write data:[$readSampleDataSize]")
        writeSampleData(trackIndex, byteBuffer, bufferInfo)
        if (!extractor.advance()) {
//            println("index [$trackIndex] 切换至下一读取点后，无媒体数据，停止！")
            break
        }
    }
}
