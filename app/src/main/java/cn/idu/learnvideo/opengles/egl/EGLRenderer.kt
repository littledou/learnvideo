package cn.idu.learnvideo.opengles.egl

import android.opengl.GLES20
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.SurfaceHolder
import android.view.SurfaceView
import cn.idu.glrenderer.texture.ITexture
import java.lang.ref.WeakReference

class EGLRenderer {

    private val rendererThread = RendererThread("EGLRenderer-Thread-${System.currentTimeMillis()}")

    //所有的绘制器
    private val mDrawers = mutableListOf<ITexture>()

    //页面上的SurfaceView弱引用
    private var mSurfaceView: WeakReference<SurfaceView>? = null

    init {
        rendererThread.start()
    }

    fun setSurfaceView(surfaceView: SurfaceView) {
        mSurfaceView = WeakReference(surfaceView)
        surfaceView.holder.addCallback(rendererThread)
//        surfaceView.addOnAttachStateChangeListener(object: View.OnAttachStateChangeListener{
//            override fun onViewAttachedToWindow(v: View?) {
//            }
//
//            override fun onViewDetachedFromWindow(v: View?) {
//            }
//        })
    }

    fun addTexture(iTexture: ITexture) {
        mDrawers.add(iTexture)
    }

    fun updateTexImage() {
        rendererThread.updateTexImage()
    }

    /**
     * EGL环境下的所有操作（OpenGL ES API的调用）需要绑定到该线程执行
     */
    enum class RendererState {
        SURFACE_NONE,
        SURFACE_CREATE,
        SURFACE_CHANGE,
        SURFACE_RENDERER,
        SURFACE_DESTROY
    }

    private inner class RendererThread(nameT: String) : Thread(nameT),
        SurfaceHolder.Callback {

        private val eglCore = EGLCore()
        private var mWidth = 0
        private var mHeight = 0
        private var handler: Handler? = null

        override fun run() {
            Looper.prepare()
            eglCore.init()
            handler = object : Handler(Looper.myLooper()!!) {
                override fun handleMessage(msg: Message) {
                    val state = msg.obj
                    when (state) {
                        RendererState.SURFACE_CREATE -> {
                            eglCore.createSurface(mSurfaceView?.get()?.holder?.surface)
                            eglCore.makeCurrent()

                            GLES20.glClearColor(0f, 0f, 0f, 0f)
                            //开启混合，即半透明
                            GLES20.glEnable(GLES20.GL_BLEND)
                            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

                            mDrawers.map { it.surfaceCreated() }
                        }
                        RendererState.SURFACE_CHANGE -> {
                            GLES20.glViewport(0, 0, mWidth, mHeight)
                            mDrawers.map {
                                it.surfaceChanged(mWidth, mHeight)
                            }
                            updateTexImage()
                        }
                        RendererState.SURFACE_RENDERER -> {
                            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                            mDrawers.map { it.updateTexImage() }
                            eglCore.swapBuffers()
                        }
                        RendererState.SURFACE_DESTROY -> {
                            mDrawers.map { it.surfaceDestroyed() }
                            eglCore.release()
                            handler = null
                            Looper.myLooper()?.quitSafely()
                        }
                        else -> {
                        }
                    }
                }
            }
            Looper.loop()
        }


        override fun surfaceCreated(holder: SurfaceHolder) {
            handler?.sendMessage(Message.obtain().apply { obj = RendererState.SURFACE_CREATE })
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            mWidth = width
            mHeight = height
            handler?.sendMessage(Message.obtain().apply { obj = RendererState.SURFACE_CHANGE })
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            handler?.sendMessage(Message.obtain().apply { obj = RendererState.SURFACE_DESTROY })
        }

        fun updateTexImage() {
            handler?.sendMessage(Message.obtain().apply { obj = RendererState.SURFACE_RENDERER })
        }
    }


}