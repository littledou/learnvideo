package cn.idu.glrenderer.jni

import android.view.Surface

final class ImagePreviewJni : JniBase() {

    companion object {
        external fun init(imgPath: String, surface: Surface)
        external fun resize(width: Int, height: Int)
        external fun stop();
    }

}