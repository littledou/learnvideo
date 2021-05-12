package cn.idu.learnvideo.opengles.egl

import android.opengl.*
import cn.readsense.module.util.DLog

class EGLCore {

    /**
     * EGL相关变量
     */
    private var mEGLDisplay = EGL14.EGL_NO_DISPLAY
    private var mEGLContext = EGL14.EGL_NO_CONTEXT
    private var mEGLSurface = EGL14.EGL_NO_SURFACE
    private var mEGLConfig: EGLConfig? = null

    /**
     * 初始化EGLDisplay
     */
    fun init() {
        DLog.d("创建EGL环境")
        if (mEGLDisplay !== EGL14.EGL_NO_DISPLAY)
            throw RuntimeException("EGL already setup")

        //1. 创建EGLDisplay
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (mEGLDisplay === EGL14.EGL_NO_DISPLAY)
            throw RuntimeException("unable to get EGL14 display")

        //2. 初始化EGLDisplay
        val version = IntArray(2)
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            mEGLDisplay = EGL14.EGL_NO_DISPLAY
            throw RuntimeException("unable to initialize EGL14")
        }
        DLog.d("eglInitialize init end, major-v:${version[0]}, minor-v:${version[1]}")

        //3. 初始化EGLConfig，EGLContext上下文
        if (mEGLContext === EGL14.EGL_NO_CONTEXT) {
            mEGLConfig = getConfig()
            val attr2List = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
            mEGLContext =
                EGL14.eglCreateContext(mEGLDisplay, mEGLConfig, EGL14.EGL_NO_CONTEXT, attr2List, 0)
        }
        DLog.d("创建EGL环境成功")
    }

    /**
     * 获取EGL配置信息
     */
    private fun getConfig(): EGLConfig? {
//        var renderableType = EGL14.EGL_OPENGL_ES2_BIT
//        if (version >= 3) {
//            renderableType = renderableType or EGLExt.EGL_OPENGL_ES3_BIT_KHR
//        }

        //配置数组，主要配置RGBA位数和深度位数
        val attrList = intArrayOf(
            EGL14.EGL_BUFFER_SIZE, 32,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            //EGL14.EGL_DEPTH_SIZE, 16,//深度位数
            //EGL14.EGL_STENCIL_SIZE, 0,//模版缓冲位数
            //EGLExt.EGL_RECORDABLE_ANDROID,1//该属性key为0x3142，声明当前EGL是可录屏的
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        //获取可用的EGL配置列表
        if (!EGL14.eglChooseConfig(
                mEGLDisplay, attrList, 0,
                configs, 0, configs.size,
                numConfigs, 0
            )
        ) {
            DLog.w("Unable to find RGB8888 EGLConfig")
            return null
        }
        return configs[0]
    }

    /**
     * 创建渲染表面
     * surface可以是Surface也可以是SurfaceTexture
     */
    fun createSurface(
        surface: Any?,
        offscreen: Boolean = false,
        width: Int = 640,
        height: Int = 480
    ) {
        if (offscreen) {
            val surfaceAttr = intArrayOf(
                EGL14.EGL_WIDTH, width,
                EGL14.EGL_HEIGHT, height,
                EGL14.EGL_NONE
            )
            mEGLSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, surfaceAttr, 0)
        } else {
            val surfaceAttr = intArrayOf(EGL14.EGL_NONE)
            mEGLSurface =
                EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface, surfaceAttr, 0)
        }
        if (mEGLSurface == null) {
            throw RuntimeException("mEGLSurface create failed !!")
        }

    }

    /**
     * 将当前线程与上下文对象进行绑定
     */
    fun makeCurrent() {
        val suc = EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)
        DLog.d("eglMakeCurrent $suc")
    }

    /**
     * 将缓存图像数据发送到设备进行显示
     */
    fun swapBuffers(): Boolean {
        return EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface)
    }

    /**
     * 释放资源
     */
    fun release() {
        DLog.d("释放EGL环境")
        EGL14.eglMakeCurrent(
            mEGLDisplay, EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT
        )
        EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface)
        EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)
        EGL14.eglReleaseThread()
        EGL14.eglTerminate(mEGLDisplay)
        mEGLDisplay = EGL14.EGL_NO_DISPLAY
        mEGLContext = EGL14.EGL_NO_CONTEXT
        mEGLConfig = null
    }

}