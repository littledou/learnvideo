package cn.idu.glrenderer

open class JniBase {

    companion object {
        init {
            System.loadLibrary("egluse")
        }
    }

}