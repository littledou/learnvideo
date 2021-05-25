package cn.idu.glrenderer.texture

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils
import cn.idu.glrenderer.util.GLBufferUtil
import cn.readsense.module.gleshelper.ShaderUtil
import cn.readsense.module.util.DLog

/**
 * 使用FBO的流程
 * 1. 新建FBO纹理
 * 2. 绑定并将纹理附着在FBO的颜色附着点上
 * 3. 渲染
 * 4. 解绑、释放FBO
 */
class ImageSoulTexture(private val mBitmap: Bitmap) : ITexture {

    private var program = -1
    private val vertexShaderSource = "#version 300 es \n" +
            "layout(location=0) in vec4 aPosition;" +
            "layout(location=1) in vec2 aTexCoord;" +
            "out vec2 vTexCoord;" +
            "void main(){" +
            "   gl_Position = aPosition;" +
            "   vTexCoord = aTexCoord;" +
            "}"

    private val fragShaderSource = "#version 300 es \n" +
            "precision mediump float;" +
            "in vec2 vTexCoord;" +
            "uniform sampler2D bitmapTexture;" +
            "uniform float progress;" +
            "layout(location=0) out vec4 outColor;" +
            "void main(){" +
            "   float x=vTexCoord.x;" +
            "   float y=vTexCoord.y;" +

            "   vec4 writeMask = vec4(1.0,1.0,1.0,1.0);" +
            "   vec4 itexture = texture(bitmapTexture, vec2(x, y));" +
            "   outColor = itexture*(1.0-progress)+writeMask*progress;" +
            "}"
    private var bitmapTextureLoc = -1

    private var mProgressLoc = -1

    private var modifyTime: Long = -1

    private val vertexPosition = floatArrayOf(
        -1.0f, -1.0f,//左下
        -1.0f, 1.0f,//左上
        1.0f, -1.0f,//
        1.0f, 1.0f
    )
    private val aTexCoord = floatArrayOf(
        0.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    )
    private val vertexBuffer = GLBufferUtil.fullFloatBuffer(vertexPosition)
    private var fragBuffer = GLBufferUtil.fullFloatBuffer(aTexCoord)
    private var textureID = -1

    override fun surfaceCreated() {
        program = ShaderUtil.createProgram(vertexShaderSource, fragShaderSource)
        bitmapTextureLoc = GLES30.glGetUniformLocation(program, "bitmapTexture")
        mProgressLoc = GLES30.glGetUniformLocation(program, "progress")

        //2D纹理一、创建2D纹理
        val texture = IntArray(1)
        GLES30.glGenTextures(1, texture, 0)
        textureID = texture[0]
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID)//绑定纹理ID到纹理单元

        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST
        )//放大双线性过滤
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST
        )//缩小双线性过滤
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE
        )//S轴归一化，纹理坐标过1归1，过0归0
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE
        )//T轴归一化
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, mBitmap, 0)
        //解绑纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, GLES30.GL_NONE)

    }

    override fun updateTexImage() {

        if (System.currentTimeMillis() - modifyTime > 500) {
            modifyTime = System.currentTimeMillis()
        }

        GLES30.glUseProgram(program)

        GLES30.glEnableVertexAttribArray(0)
        GLES30.glEnableVertexAttribArray(1)

        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, fragBuffer)

        GLES30.glVertexAttrib1f(2, 0.5f)
        val progress = (System.currentTimeMillis() - modifyTime) / 500f
        DLog.d("progress: $progress")
        GLES30.glUniform1f(mProgressLoc, progress)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)//激活指定纹理单元
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID)//绑定纹理ID到纹理单元
        GLES30.glUniform1i(bitmapTextureLoc, 0)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, GLES30.GL_NONE)//解绑纹理
    }

    override fun surfaceDestroyed() {
        GLES30.glDeleteTextures(1, intArrayOf(textureID), 0)
        GLES30.glDeleteProgram(program);
    }


}