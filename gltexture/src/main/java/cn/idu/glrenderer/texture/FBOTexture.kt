package cn.idu.glrenderer.texture

import android.opengl.GLES20

class FBOTexture : ITexture {


    override fun surfaceCreated() {
    }

    override fun updateTexImage() {

    }

    override fun surfaceDestroyed() {
    }

    /**
     * 1. 新建纹理
     */
    fun createFBOTextures(): IntArray {
        //1.1 新建纹理ID
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures,0)

        //1.2 绑定纹理ID
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])

        //1.3 根据颜色参数、宽高等信息，为上面的纹理生成一个2D纹理

        return textures
    }

}