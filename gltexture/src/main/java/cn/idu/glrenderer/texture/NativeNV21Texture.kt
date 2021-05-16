package cn.idu.glrenderer.texture

import cn.idu.glrenderer.jni.JniBase


class NativeNV21Texture : ITexture, JniBase() {

    var handle: Long = 0L
    override fun surfaceCreated() {
        handle = surfaceCreatedJni()
    }

    override fun surfaceChanged(w: Int, h: Int) {
        surfaceChangedJni(handle, w, h)
    }

    override fun surfaceDestroyed() {
        surfaceDestroyedJni(handle)
    }

    fun updateTexImage(
        format: Int,
        data: ByteArray,
        width: Int,
        height: Int
    ) {
        updateTexImageJni(handle, format, data, width, height)
    }

    private external fun surfaceCreatedJni(): Long
    private external fun surfaceChangedJni(handler: Long, w: Int, h: Int)
    private external fun updateTexImageJni(
        handler: Long,
        format: Int,
        data: ByteArray,
        width: Int,
        height: Int
    )

    private external fun surfaceDestroyedJni(handler: Long)
}