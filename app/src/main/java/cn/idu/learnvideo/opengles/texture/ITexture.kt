package cn.idu.learnvideo.opengles.texture

interface ITexture {
    fun createTexture()
    fun drawFrame()
    fun release()
    fun setWorldSize(w: Int, h: Int){}
    fun setTextureSize(w: Int, h: Int){}
}