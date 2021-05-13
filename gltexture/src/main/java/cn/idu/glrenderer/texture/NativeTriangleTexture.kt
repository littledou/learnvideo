package cn.idu.glrenderer.texture

import cn.idu.glrenderer.jni.JniBase


class NativeTriangleTexture : ITexture, JniBase() {

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

    override fun updateTexImage() {
        updateTexImageJni(handle)
    }

    private external fun surfaceCreatedJni(): Long
    private external fun surfaceChangedJni(handler: Long, w: Int, h: Int)
    private external fun updateTexImageJni(handler: Long)
    private external fun surfaceDestroyedJni(handler: Long)
}