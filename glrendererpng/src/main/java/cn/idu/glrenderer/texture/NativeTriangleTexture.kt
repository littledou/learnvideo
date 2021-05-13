package cn.idu.glrenderer.texture

import cn.idu.glrenderer.jni.JniBase


class NativeTriangleTexture : ITexture, JniBase() {

    override fun surfaceCreated() {
        surfaceCreated_()
    }

    override fun surfaceChanged(w: Int, h: Int) {
        surfaceChanged_(w, h)
    }

    override fun surfaceDestroyed() {
        surfaceDestroyed_()
    }

    override fun updateTexImage() {
        updateTexImage_()
    }

    external fun surfaceCreated_()
    external fun surfaceChanged_(w: Int, h: Int)
    external fun surfaceDestroyed_()
    external fun updateTexImage_()
}