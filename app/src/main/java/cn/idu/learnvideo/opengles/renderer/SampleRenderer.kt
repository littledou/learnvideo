package cn.idu.learnvideo.opengles.renderer

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import cn.idu.learnvideo.opengles.texture.ITexture
import cn.idu.learnvideo.opengles.texture.TriangleTexture
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SampleRenderer(private val texture: ITexture) : GLSurfaceView.Renderer {

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        texture.createTexture()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        texture.setWorldSize(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        texture.drawFrame();
    }

    fun release() {
        texture.release()
    }

}