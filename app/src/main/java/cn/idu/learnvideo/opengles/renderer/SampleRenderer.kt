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
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        run {//这些代码放置在onSurfaceCreated起始位置，会崩溃GLES30: glAttach_vertexShader:glError 1280
//            GLES20.glClearColor(0f, 1f, 0f, 0f)
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
//            GLES20.glEnable(GLES20.GL_BLEND)//开启混合，即半透明
//            GLES20.glBlendFunc(GLES20.GL_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        }
        texture.drawFrame();
    }

    fun release() {
        texture.release()
    }

}