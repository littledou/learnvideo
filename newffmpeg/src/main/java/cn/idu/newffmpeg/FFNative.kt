package cn.idu.newffmpeg

class FFNative {


    companion object {
        external fun ffInfo(): String
        external fun testLoading()

        init {
            System.loadLibrary("ffuse")
        }
    }


}