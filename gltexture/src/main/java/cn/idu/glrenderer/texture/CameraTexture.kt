package cn.idu.glrenderer.texture

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import cn.idu.glrenderer.texture.ITexture
import cn.readsense.module.gleshelper.ShaderUtil
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
            "uniform mat4 uMatrix;" +
            "attribute vec2 aTexCoord;" +
            "varying vec2 vTexCoord;" +
            "void main(){" +
            "   gl_Position = aPosition*uMatrix;" +
            "   vTexCoord = aTexCoord;" +
            "}"

    //拓展纹理
//    private val fragShaderSource = "#extension GL_OES_EGL_image_external : require\n" +
//            "precision mediump float;" +
//            "varying vec2 vTexCoord;" +
//            "uniform samplerExternalOES yuvTexSampler;" +
//            "void main(){" +
//            "  gl_FragColor = texture2D(yuvTexSampler, vTexCoord);" +
//            "}"
    //拓展纹理灰度效果
//    private val fragShaderSource = "#extension GL_OES_EGL_image_external : require\n" +
//            "precision mediump float;" +
//            "varying vec2 vTexCoord;" +
//            "uniform samplerExternalOES yuvTexSampler;" +
//            "void main(){" +
//            "  vec4 color =  texture2D(yuvTexSampler, vTexCoord);" +
//            "  float gray = (color.r + color.g + color.b)/3.0;" +
//            "  gl_FragColor = vec4(gray, gray, gray, 1.0);" +
//            "}"

    //拓展纹理灰度效果
    private val fragShaderSource = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "varying vec2 vTexCoord;" +
            "uniform samplerExternalOES yuvTexSampler;" +
            "void main(){" +
            "  vec4 color =  texture2D(yuvTexSampler, vTexCoord);" +
            "  gl_FragColor = color+vec4(0.3, 0.3, 0.0, 0.0);" +//暖色效果
//            "  gl_FragColor = color+vec4(0.0, 0.0, 0.2, 0.0);" +//冷色效果
            "}"

    private var orthoMatrix = FloatArray(16)
    private var worldWidth: Float = -1f
    private var worldHeight: Float = -1f
    private var textureWidth: Float = -1f
    private var textureHeight: Float = -1f
    private var isFullScreen = false;


    private var aPositionIndex = 0
    private var aTexCoordIndex = 1
    private var uMartixIndex = 2
    private var sampler2DHandler = -1

    /**
     * OpenGL物体坐标系
     * A(-1, 1)      B(1, 1)
     * -----------
     * |        |
     * |        |
     * |        |
     * |        |
     * -----------
     * C(-1, -1)   D(1,- 1)
     * CADB
     * 绘制方向：CAD ADB
     */
    private val vertexPosition = floatArrayOf(
        -1.0f, -1.0f,//左下
        -1.0f, 1.0f,//左上
        1.0f, -1.0f,//
        1.0f, 1.0f
    )
    /**
     * OpenGL二维纹理坐标
     * 0, 1      1, 1
     * -----------
     * |        |
     * |        |
     * |        |
     * |        |
     * -----------
     * 0, 0   1, 0
     */

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
//    private val aTexCoord = floatArrayOf(
//        0.0f, 1.0f,
//        0.0f, 0.0f,
//        1.0f, 1.0f,
//        1.0f, 0.0f
//    )
    /*
    * 竖屏手机前置摄像头渲染，需顺时针转个90度，变换为
    * 1,0      1,1
    * -----------
    * |        |
    * |        |
    * |        |
    * |        |
    * -----------
    * 0,0      0,1
    */
//    private val aTexCoord = floatArrayOf(
//        0.0f, 0.0f,
//        1.0f, 0.0f,
//        0.0f, 1.0f,
//        1.0f, 1.0f
//    )
    /*
    * 竖屏手机前置摄像头可能会左右反转，通过反转纹理实现预览正常
    * 1,1      1,0
    * -----------
    * |        |
    * |        |
    * |        |
    * |        |
    * -----------
    * 0,1      0,0
    */
    private val aTexCoord = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
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

    override fun surfaceCreated() {
        program = ShaderUtil.createProgram(vertexShaderSource, fragShaderSource)
        GLES20.glBindAttribLocation(program, aPositionIndex, "aPosition")
        GLES20.glBindAttribLocation(program, aTexCoordIndex, "aTexCoord")
        uMartixIndex = GLES20.glGetUniformLocation(program, "uMatrix");
        sampler2DHandler = GLES20.glGetUniformLocation(program, "yuvTexSampler")

        //2D纹理一、创建2D纹理
        val texture = IntArray(1)
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

    override fun updateTexImage() {
        initOrthoMatrix()
        GLES20.glUseProgram(program)

        GLES20.glVertexAttribPointer(aPositionIndex, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(aPositionIndex);
        GLES20.glVertexAttribPointer(aTexCoordIndex, 2, GLES20.GL_FLOAT, false, 0, fragBuffer)
        GLES20.glEnableVertexAttribArray(aTexCoordIndex)

        GLES20.glUniformMatrix4fv(uMartixIndex, 1, false, orthoMatrix, 0)
        //2D纹理三、绘制2D纹理
        //2D纹理二、激活纹理并向2D纹理上绑定数据
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)//激活指定纹理单元
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureID)//绑定纹理ID到纹理单元
        GLES20.glUniform1i(sampler2DHandler, 0);//将激活到纹理单元传递到着色器里
        //绑定位图到被激活的纹理单元
        surfaceTexture?.updateTexImage()

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    private fun initOrthoMatrix() {
        var left = -1f
        var right = 1f
        var buttom = -1f
        var top = 1f
        //模式一：将纹理全部显示到视口里，不允许裁剪
        if (!isFullScreen) {
            //高度拉伸: 世界的宽高比大于纹理的宽高比，纹理高度是满屏，宽度拉伸后两边有黑边
            //最终该值为纹理从满屏压缩值指定值的倍数
            if (worldWidth / worldHeight > textureWidth / textureHeight) {
                left = -worldWidth / (textureWidth * worldHeight / textureHeight)
                right = worldWidth / (textureWidth * worldHeight / textureHeight)
            }
            //宽度拉伸：纹理宽度满屏，高度拉伸后上下有黑边
            //最终该值为纹理从满屏压缩值指定值的倍数
            if (worldWidth / worldHeight < textureWidth / textureHeight) {
                buttom = -worldHeight / (textureHeight * worldWidth / textureWidth)
                top = worldHeight / (textureHeight * worldWidth / textureWidth)
            }
        } else {
            //模式二：在视口中全屏显示，允许越界裁剪
            //宽度拉伸：视口宽高比大于纹理宽高比，纹理宽度等于视口宽度时，纹理高度越界，上下被裁剪掉
            //左右为1，上下为小于1, 上下分别为纹理放大后的度到底是视口高度的多少倍
            if (worldWidth / worldHeight > textureWidth / textureHeight) {
                buttom = -textureHeight * (worldWidth / textureWidth) / worldHeight
                top = textureHeight * (worldWidth / textureWidth) / worldHeight
                //反过来，最终该值为纹理能够显示的比例
                buttom = 1 / buttom
                top = 1 / top
            }
            //高度拉伸：视口宽高比小于纹理宽高比，高度满屏，拉伸后的宽度会越界视口范围，相当于被裁剪里左右两边
            //上下为1， 左右小于1，左右分别为纹理拉伸后，纹理宽度到底是视口宽度的多少倍
            if (worldWidth / worldHeight < textureWidth / textureHeight) {
                left = -textureWidth * (worldHeight / textureHeight) / worldWidth.toFloat()
                right = textureWidth * (worldHeight / textureHeight) / worldWidth.toFloat()
                //反过来，最终该值为纹理能够显示的比例
                left = 1 / left
                right = 1 / right
            }
        }
//        println("$left , $right, $buttom, $top")

        Matrix.orthoM(
            orthoMatrix, 0,
            left, right,
            buttom, top,
            -1f, 6f
        )
//        Matrix.setLookAtM(
//            orthoMatrix, 0,
//            0f, 0f, 5f,
//            0f, 0f, 0f,
//            0f, 1f, 0f
//        )

    }

    override fun surfaceChanged(w: Int, h: Int) {
        worldWidth = w.toFloat()
        worldHeight = h.toFloat()
    }

    override fun setTextureSize(w: Int, h: Int) {
        textureWidth = w.toFloat()
        textureHeight = h.toFloat()
    }


    override fun surfaceDestroyed() {

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