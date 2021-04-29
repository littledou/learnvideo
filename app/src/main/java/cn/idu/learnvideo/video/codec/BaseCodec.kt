package cn.idu.learnvideo.video.codec

import java.lang.Exception
import java.util.concurrent.ArrayBlockingQueue

abstract class BaseCodec : Thread(), ICodec {

    val inBlockingQueue = ArrayBlockingQueue<ByteArray>(30)


    override fun putBuf(data: ByteArray, offset: Int, size: Int) {
        val byteArray = ByteArray(size)
        System.arraycopy(data, offset, byteArray, 0, size)
        inBlockingQueue.put(byteArray)
    }

    var threadRunning = true;

    override fun run() {

        try {
            BaseCodecLoop1@ while (threadRunning) {
                val item = inBlockingQueue.take()
                dealWith(item)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun dealWith(data: ByteArray) {

    }

    override fun stopWorld() {
        threadRunning = false;
        interrupt()
        join(1000)
    }


}