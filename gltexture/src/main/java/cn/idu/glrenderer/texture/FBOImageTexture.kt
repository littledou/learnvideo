package cn.idu.glrenderer.texture

import android.opengl.GLES20

/**
 * 使用FBO的流程
 * 1. 新建FBO纹理
 * 2. 绑定并将纹理附着在FBO的颜色附着点上
 * 3. 渲染
 * 4. 解绑、释放FBO
 */
class FBOImageTexture : ITexture {

    private var mFBOTextureID: Int = -1
    private var mFBOFramebufferID: Int = -1

    override fun surfaceCreated() {
    }

    override fun updateTexImage() {

    }

    override fun surfaceDestroyed() {
    }

    fun createFrameBufferObj(width: Int, height: Int) {
        //1. 创建并初始化FBO纹理
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        mFBOTextureID = textures[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFBOTextureID)
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE)

        //2. 创建并初始化FBO
        val fbs = IntArray(1)
        GLES20.glGenFramebuffers(1, fbs, 0)
        mFBOFramebufferID = fbs[0]
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBOFramebufferID)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFBOTextureID)

        //根据颜色参数、宽高等信息，为上面的纹理生成一个2D纹理
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
            width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
        )
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE)
    }

    /**
     * 1. 创建并初始化FBO纹理
     */
    fun createFBOTextures(width: Int, height: Int): IntArray {
        //1.1 新建纹理ID
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)

        //1.2 绑定纹理ID
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])

        //1.3 根据颜色参数、宽高等信息，为上面的纹理生成一个2D纹理
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
            width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
        )
        //1.4 设置纹理边缘参数
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE)
        return textures
    }

    //2. 新建并绑定FrameBuffer，返回FBO索引
    fun createFramebuffer(): Int {
        val fbs = IntArray(1)
        GLES20.glGenFramebuffers(1, fbs, 0)
        return fbs[0]
    }

    //3. 绑定FBO
    fun bindFBO(fb: Int, textureId: Int) {
        //先绑定上面创建的FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fb)
        //接着将FBO和上面创建的纹理通过颜色附着点 GLES20.GL_COLOR_ATTACHMENT0 绑定起来
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId, 0
        )
    }

    //4. 解绑FBO
    fun unbindFBO() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE)
    }

    //5. 删除FBO
    fun deleteFBO(fbId: Int, textureId: Int) {
        //删除Framebuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        GLES20.glDeleteFramebuffers(1, intArrayOf(fbId), 0)
        //删除纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE)
        GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
    }
}