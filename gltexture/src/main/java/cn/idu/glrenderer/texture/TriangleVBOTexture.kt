package cn.idu.glrenderer.texture

import android.opengl.GLES30
import cn.idu.glrenderer.util.GLBufferUtil
import cn.readsense.module.gleshelper.ShaderUtil
import java.nio.FloatBuffer

/**
 * VBO: 顶点缓冲区对象
 * VAO: 顶点数组对象，用于管理VBO
 */
class TriangleVBOTexture : ITexture {

    private var program: Int = -1

    private val vertexShader = "#version 300 es \n" +
            "layout(location = 0) in vec4 aPosition;" +
            "void main(){" +
            "   gl_Position=aPosition;" +
            "}"

    private val fragShader = "#version 300 es \n" +
            "precision mediump float;" +
            "out vec4 o_fragColor;" +
            "void main(){" +
            "   o_fragColor=vec4(1.0, 0.0, 0.0, 1.0);" +
            "}"

    private var vertexVboId = 0
    private var a_gl_Position = 0

    private val vertexCoords = floatArrayOf(
        -1f, -1f,//左下
        1f, -1f,//右下
        0f, 0f//左上
    )
    private val vertexBuffer: FloatBuffer = GLBufferUtil.fullFloatBuffer(vertexCoords)

    override fun surfaceCreated() {
        program = ShaderUtil.createProgram(vertexShader, fragShader)

        //创建1个VBO
        val vboIDs = IntArray(1)
        GLES30.glGenBuffers(1, vboIDs, 0)
        vertexVboId = vboIDs[0]
        //绑定第一个VBO，拷贝顶点数组到显存，格式为GL_ARRAY_BUFFER
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboIDs[0])
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER, vertexCoords.size * 4, vertexBuffer, GLES30.GL_STATIC_DRAW
        )

    }

    override fun updateTexImage() {
        GLES30.glUseProgram(program)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexVboId);
        GLES30.glEnableVertexAttribArray(a_gl_Position)
        GLES30.glVertexAttribPointer(a_gl_Position, 2, GLES30.GL_FLOAT, false, 0, 0)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 3)
    }

    override fun surfaceDestroyed() {
        GLES30.glDeleteProgram(program)
        GLES30.glDeleteBuffers(1, intArrayOf(vertexVboId), 0)
    }

}