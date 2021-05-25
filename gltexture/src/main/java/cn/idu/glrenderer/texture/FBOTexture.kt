package cn.idu.glrenderer.texture

import android.opengl.GLES30

/**
 * 使用FBO的流程
 * 1. 新建FBO纹理
 * 2. 绑定并将纹理附着在FBO的颜色附着点上
 * 3. 渲染
 * 4. 解绑、释放FBO
 */
abstract class FBOTexture : ITexture {

    //创建新的FBO
    fun createFBO() {
        val fbos = IntArray(1)
        GLES30.glGenFramebuffers(1, fbos, 0)
        val fobID = fbos[0]
        //绑定一个新的FBO来修改和使用
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fobID)
        //将渲染区RenderBuffer绑定到GL_COLOR_ATTACHMENT0上
        GLES30.glFramebufferRenderbuffer(
            GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_RENDERBUFFER, fobID
        )

    }


    /**
     * 1. 新建FBO纹理
     */
    fun createFBOTextures(width: Int, height: Int): IntArray {
        //1.1 新建纹理ID
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)

        //1.2 绑定纹理ID
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0])

        //1.3 根据颜色参数、宽高等信息，为上面的纹理生成一个2D纹理
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA,
            width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_SHORT, null
        )
        //1.4 设置纹理边缘参数
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST
        )
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR
        )
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE
        )
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE
        )
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, GLES30.GL_NONE)
        return textures
    }

    //2. 新建一个FrameBuffer，返回FBO索引
    fun createFramebuffer(): Int {
        val fbs = IntArray(1)
        GLES30.glGenFramebuffers(1, fbs, 0)
        return fbs[0]
    }

    //3. 绑定FBO
    fun bindFBO(fb: Int, textureId: Int) {
        //先绑定上面创建的FBO
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fb)
        //接着将FBO和上面创建的纹理通过颜色附着点 GLES30.GL_COLOR_ATTACHMENT0 绑定起来
        GLES30.glFramebufferTexture2D(
            GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, textureId, 0
        )
    }

    //4. 解绑FBO
    fun unbindFBO() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_NONE)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, GLES30.GL_NONE)
    }

    //5. 删除FBO
    fun deleteFBO(fbId: Int, textureId: Int) {
        //删除Framebuffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_NONE)
        GLES30.glDeleteFramebuffers(1, intArrayOf(fbId), 0)
        //删除纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, GLES30.GL_NONE)
        GLES30.glDeleteTextures(1, intArrayOf(textureId), 0)
    }
}