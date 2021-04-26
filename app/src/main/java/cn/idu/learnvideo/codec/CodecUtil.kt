package cn.idu.learnvideo.codec

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.view.Surface

class CodecUtil {

    companion object {

        //h264:video/avc，h265:video/hevc
//        fun findEncoder(mimeType: String): MediaCodecInfo? {
//            return findCoder(mimeType, true)
//        }
//
//        fun findDecoder(mimeType: String): MediaCodecInfo? {
//            return findCoder(mimeType, false)
//        }

        fun findCoder(
            mimeType: String,
            isEncoder: Boolean,
            isHard: Boolean = true
        ): MediaCodecInfo? {
            val mediaCodecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            val codecInfos = mediaCodecList.codecInfos

            return codecInfos.find {
                it.isEncoder == isEncoder && !it.isSoftwareOnly == isHard &&
                        hasThisCodec(it, mimeType)

            }
        }

        private fun hasThisCodec(codecInfo: MediaCodecInfo, mimeType: String): Boolean {
            return codecInfo.supportedTypes.find { it.equals(mimeType) } != null
        }

        fun printCodecInfo() {
            val mediaCodecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)//创建查看设备可以使用的编解码器
            val codecInfos = mediaCodecList.codecInfos
            codecInfos.forEach { codecInfo ->
                if (codecInfo.isEncoder)
                    println(
                        "encoder name: ${codecInfo.name} \n" +
                                "       canonicalName: ${codecInfo.canonicalName} \n" +
                                "       isAlias: ${codecInfo.isAlias} \n" +
                                "       isSoftwareOnly: ${codecInfo.isSoftwareOnly} \n" +
                                "       supportedTypes: ${
                                    codecInfo.supportedTypes.map {
                                        println("encoder: $it")
                                    }
                                } \n" +
                                "       isVendor: ${codecInfo.isVendor} \n" +
                                "       isHardwareAccelerated: ${codecInfo.isHardwareAccelerated}" +
                                ""
                    )
            }

            codecInfos.forEach { codecInfo ->
                if (!codecInfo.isEncoder)
                    println(
                        "decoder name: ${codecInfo.name} \n" +
                                "       canonicalName: ${codecInfo.canonicalName} \n" +
                                "       isAlias: ${codecInfo.isAlias} \n" +
                                "       isSoftwareOnly: ${codecInfo.isSoftwareOnly} \n" +
                                "       supportedTypes: ${
                                    codecInfo.supportedTypes.map {
                                        println("decoder: $it")
                                    }
                                } \n" +
                                "       isVendor: ${codecInfo.isVendor} \n" +
                                "       isHardwareAccelerated: ${codecInfo.isHardwareAccelerated}" +
                                ""
                    )
            }
        }

        fun newCodecRunnable(path: String, surface: Surface): Runnable {
            return Runnable {

            }
        }
    }
}