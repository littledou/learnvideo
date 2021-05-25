package cn.idu.learnvideo.opengles.renderer

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import cn.idu.glrenderer.texture.ITexture
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SampleYUVRenderer(private val texture: ITexture) : GLSurfaceView.Renderer {

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        texture.surfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        texture.surfaceChanged(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        data?.run {
            texture.updateTexImage(data!!, w, h);
        }
    }

    var data: ByteArray? = null
    var w: Int = 0
    var h: Int = 0
    fun updateTexImage(data: ByteArray, w: Int, h: Int) {
        if (this.data == null) {
            this.data = ByteArray(data.size)
        }
        System.arraycopy(data, 0, this.data, 0, data.size)
        this.w = w
        this.h = h
    }


    fun release() {
        texture.surfaceDestroyed()
    }

}