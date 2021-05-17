package cn.idu.glrenderer.texture

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import cn.idu.glrenderer.texture.ITexture
import cn.readsense.module.gleshelper.ShaderUtil
import cn.readsense.module.util.BitmapUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * 1. 编译shader
 * 2. 获取shader传递变量handler
 * 创建2D纹理
 * 3. 绘制，绑定2D纹理
 * 4. 释放
 */
class ImageTexture(val mBitmap: Bitmap) : ITexture {

    private var program = -1
    private val vertexShaderSource = "attribute vec4 aPosition;" +
            "attribute vec2 aTexCoord;" +
            "varying vec2 vTexCoord;" +
            "void main(){" +
            "   gl_Position = aPosition;" +
            "   vTexCoord = aTexCoord;" +
            "}"

    private val fragShaderSource = "precision mediump float;" +
            "varying vec2 vTexCoord;" +
            "uniform sampler2D yuvTexSampler;" +
            "void main(){" +
            "   gl_FragColor = texture2D(yuvTexSampler, vTexCoord);" +
            "}"

    private var aPositionIndex = 0
    private var aTexCoordIndex = 1
    //TODO 注释掉2D纹理在片段着色器上定义掉handler也可以正常显示图片？？？
//    private var sampler2DHandler = -1

    /**
     * OpenGL物体坐标系
     * -1, 1      1, 1
     * -----------
     * |        |
     * |        |
     * |        |
     * |        |
     * -----------
     * -1, -1   1,- 1
     */
    private val vertexPosition = floatArrayOf(
        -1.0f, -1.0f,//左下
        -1.0f, 1.0f,//左上
        1.0f, -1.0f,//
        1.0f, 1.0f
    )

    /**
     * 计算机二维纹理坐标
     * 0, 0      1, 0
     * -----------
     * |        |
     * |        |
     * |        |
     * |        |
     * -----------
     * 0, 1   1, 1
     */
    private val aTexCoord = floatArrayOf(
        0.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    )
    private val vertexBuffer = fullFloatBuffer(vertexPosition)
    private var fragBuffer = fullFloatBuffer(aTexCoord)
    private var textureID = -1

    override fun surfaceCreated() {
        program = ShaderUtil.createProgram(vertexShaderSource, fragShaderSource)
        GLES20.glBindAttribLocation(program, aPositionIndex, "aPosition")
        GLES20.glBindAttribLocation(program, aTexCoordIndex, "aTexCoord")
        //TODO 注释掉2D纹理在片段着色器上定义掉handler也可以正常显示图片？？？
        //解释：当只有一个纹理时，yuvTexSampler默认就绑定在GL_TEXTURE0这个纹理上，也可以不用定义参数再来绑定
//        sampler2DHandler = GLES20.glGetUniformLocation(program, "yuvTexSampler")

        //2D纹理一、创建2D纹理
        val texture = IntArray(1)
        GLES20.glGenTextures(1, texture, 0)
        textureID = texture[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID)

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST
        )//放大双线性过滤
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST
        )//缩小双线性过滤
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )//S轴归一化，纹理坐标过1归1，过0归0
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )//T轴归一化

        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, GLES20.GL_NONE)
    }

    override fun updateTexImage() {
        GLES20.glUseProgram(program)

        GLES20.glVertexAttribPointer(aPositionIndex, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(aPositionIndex);
        GLES20.glVertexAttribPointer(aTexCoordIndex, 2, GLES20.GL_FLOAT, false, 0, fragBuffer)
        GLES20.glEnableVertexAttribArray(aTexCoordIndex)

        //2D纹理二、激活纹理并向2D纹理上绑定数据
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)//激活指定纹理单元
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID)//绑定纹理ID到纹理单元
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0)

        //TODO 注释掉2D纹理在片段着色器上定义的handler也可以正常显示图片？？？
        //解释：当只有一个纹理时，yuvTexSampler默认就绑定在GL_TEXTURE0这个纹理上，也可以不用定义参数再来绑定
        //GLES20.glUniform1i(sampler2DHandler, 0);//将激活到纹理单元传递到着色器里，此处x：0为前面激活的纹理id

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)//解绑纹理
    }


    override fun surfaceDestroyed() {
        GLES20.glDeleteTextures(1, intArrayOf(textureID), 0)
        GLES20.glDeleteProgram(program);
    }

    fun fullFloatBuffer(arr: FloatArray): FloatBuffer {
        return ByteBuffer.allocateDirect(arr.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer()
        }.apply {
            put(arr)
            position(0)
        }
    }
}