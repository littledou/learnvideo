package cn.idu.glrenderer.texture

interface ITexture {
    fun surfaceCreated()
    fun updateTexImage(){}
    fun surfaceDestroyed()
    fun surfaceChanged(w: Int, h: Int){}
    fun setTextureSize(w: Int, h: Int){}
}