package cn.idu.learnvideo.opengles.texture

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
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
class CameraTexture() : ITexture {

    private var program = -1
    private val vertexShaderSource = "attribute vec4 aPosition;" +
            "attribute vec2 aTexCoord;" +
            "varying vec2 vTexCoord;" +
            "void main(){" +
            "   gl_Position = aPosition;" +
            "   vTexCoord = aTexCoord;" +
            "}"

    //拓展纹理
    private val fragShaderSource = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "varying vec2 vTexCoord;" +
            "uniform samplerExternalOES yuvTexSampler;" +
            "void main(){" +
            "  vec4 color =  texture2D(yuvTexSampler, vTexCoord);" +
            "  float gray = (color.r + color.g + color.b)/3.0;" +
            "  gl_FragColor = vec4(gray, gray, gray, 1.0);" +
            "}"

    private var aPositionIndex = 0
    private var aTexCoordIndex = 1
    private var sampler2DHandler = -1

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
    private var surfaceTexture: SurfaceTexture? = null

    //高阶函数
    private var sftCB: ((SurfaceTexture) -> Unit)? = null

    fun getSurfaceTexture(cb: (st: SurfaceTexture) -> Unit) {
        sftCB = cb
    }

    override fun createTexture() {
        program = ShaderUtil.createProgram(vertexShaderSource, fragShaderSource)
        GLES20.glBindAttribLocation(program, aPositionIndex, "aPosition")
        GLES20.glBindAttribLocation(program, aTexCoordIndex, "aTexCoord")
        sampler2DHandler = GLES20.glGetUniformLocation(program, "yuvTexSampler")

        //2D纹理一、创建2D纹理
        var texture = IntArray(1)
        GLES20.glGenTextures(1, texture, 0)
        textureID = texture[0]

        //视频的渲染需要SurfaceTexture来更新画面
        surfaceTexture = SurfaceTexture(textureID)

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureID)
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST
        )//放大双线性过滤
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST
        )//缩小双线性过滤
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )//S轴归一化，纹理坐标过1归1，过0归0
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )//T轴归一化

        sftCB?.invoke(surfaceTexture!!)
    }

    override fun drawFrame() {
        GLES20.glUseProgram(program)

        GLES20.glVertexAttribPointer(aPositionIndex, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(aPositionIndex);
        GLES20.glVertexAttribPointer(aTexCoordIndex, 2, GLES20.GL_FLOAT, false, 0, fragBuffer)
        GLES20.glEnableVertexAttribArray(aTexCoordIndex)

        //2D纹理三、绘制2D纹理
        //2D纹理二、激活纹理并向2D纹理上绑定数据
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)//激活指定纹理单元
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureID)//绑定纹理ID到纹理单元
        GLES20.glUniform1i(sampler2DHandler, 0);//将激活到纹理单元传递到着色器里
        //绑定位图到被激活的纹理单元
        surfaceTexture?.updateTexImage()

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }


    override fun release() {
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