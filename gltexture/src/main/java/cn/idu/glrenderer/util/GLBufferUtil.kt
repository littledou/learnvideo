package cn.idu.glrenderer.util

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

class GLBufferUtil {

    companion object {

        fun fullFloatBuffer(arr: FloatArray): FloatBuffer {
            return ByteBuffer.allocateDirect(arr.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer()
            }.apply {
                put(arr)
                position(0)
            }
        }

        fun fullIntBuffer(arr: IntArray): IntBuffer {
            return ByteBuffer.allocateDirect(arr.size * 4).run {
                order(ByteOrder.nativeOrder())
                asIntBuffer()
            }.apply {
                put(arr)
                position(0)
            }
        }
    }
}