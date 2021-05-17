package cn.idu.glrenderer.texture

interface ITexture {
    //加载纹理
    fun surfaceCreated()
    //绘制纹理
    fun updateTexImage(){}
    //释放纹理
    fun surfaceDestroyed()
    //辅助方法
    fun surfaceChanged(w: Int, h: Int){}
    fun setTextureSize(w: Int, h: Int){}
}