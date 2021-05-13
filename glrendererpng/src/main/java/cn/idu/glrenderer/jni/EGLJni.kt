package cn.idu.glrenderer.jni

import android.view.Surface

final class EGLJni : JniBase() {

    companion object {
        external fun test(surface: Surface)
    }
}