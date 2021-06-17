package cn.idu.newffmpeg

import java.io.*

class FFNative {


    companion object {
        external fun ffInfo(): String
        external fun testLoading()
        external fun testLoadingRTSP(path: String, callback: FrameCallback)

        interface FrameCallback {
            fun onFrame(data: ByteArray, iw: Int, ih: Int);
        }

        init {
            System.loadLibrary("ffuse")
        }
    }

}