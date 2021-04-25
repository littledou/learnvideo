package cn.idu.glrenderer

import android.view.Surface

final class EGLJni : JniBase() {

    companion object {
        external fun test(surface: Surface)
    }
}