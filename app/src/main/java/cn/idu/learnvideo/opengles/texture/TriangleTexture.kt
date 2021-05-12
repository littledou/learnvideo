package cn.idu.learnvideo.opengles.texture

import android.opengl.GLES20
import cn.readsense.module.gleshelper.ShaderUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TriangleTexture : ITexture {

    private var program: Int = -1

    private val vertexShader = "attribute vec4 aPosition;" +
            "void main(){" +
            "   gl_Position=aPosition;" +
            "}"

    private val fragShader = "precision mediump float;" +
            "void main(){" +
            "   gl_FragColor=vec4(1.0, 0.0, 0.0, 1.0);" +
            "}"

    private val aPositionIndex = 0
    private lateinit var vertexBuffer: FloatBuffer
    private val vertexCoords = floatArrayOf(
        -1f, -1f,//左下
        1f, -1f,//右下
        0f, 0f//左上
    )

    init {
        vertexBuffer = ByteBuffer.allocateDirect(vertexCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer()
        }.apply {
            put(vertexCoords)
            position(0)
        }
    }

    override fun createTexture() {
        program = ShaderUtil.createProgram(vertexShader, fragShader)
        GLES20.glBindAttribLocation(program, aPositionIndex, "aPosition")
    }

    override fun drawFrame() {
        GLES20.glUseProgram(program)
        GLES20.glVertexAttribPointer(aPositionIndex, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(aPositionIndex)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3)
        GLES20.glDisableVertexAttribArray(aPositionIndex)
    }

    override fun release() {
        //TODO 暂未找到放入GLThread释放的方法, 可以自定义EGL环境来解决该问题
        //？？？：是否需要释放shader
//        GLES20.glDisableVertexAttribArray(aPositionIndex)
//        GLES20.glDeleteProgram(program)
    }

}