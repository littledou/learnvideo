package cn.idu.learnvideo.mp.codec

interface ICodec {

    //入队
    fun putBuf(data: ByteArray, offset: Int, size: Int)
    //处理数据
    fun dealWith(data: ByteArray)
    //停止线程
    fun stopWorld()
}


