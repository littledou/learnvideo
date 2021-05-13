package cn.idu.glrenderer.jni

open class JniBase {

    companion object {
        init {
            System.loadLibrary("egluse")
        }
    }

}