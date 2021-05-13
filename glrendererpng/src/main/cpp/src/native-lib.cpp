//
// Created by loki on 2021/4/22.
//

#include "native-lib.h"

extern "C"
JNIEXPORT void JNICALL
Java_cn_idu_glrenderer_jni_EGLJni_00024Companion_test(JNIEnv *env, jobject thiz, jobject surface) {
    LOGD("***************egl test init******************");
    //获取display
    EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (display == EGL_NO_DISPLAY) {
        LOGE("eglGetDisplay return error %d", eglGetError());
        return;
    }
    LOGD("eglGetDisplay");

    //初始化display
    int major, minor;
    if (!eglInitialize(display, &major, &minor)) {
        LOGE("eglInitialize return error %d", eglGetError());
        return;
    }
    LOGD("eglInitialize,major[%d], minor[%d]", major, minor);

    //将OpenGL ES的输出和设备的屏幕桥接起来，需要指定一些配置项
    const EGLint attribs[] = {
            EGL_BUFFER_SIZE, 32,
            EGL_ALPHA_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_RED_SIZE, 8,
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
            EGL_NONE
    };
    EGLConfig config;
    int numConfigs;
    //获取配置选项信息
    if (!eglChooseConfig(display, attribs, &config, 1, &numConfigs)) {
        LOGE("eglChooseConfig failed return: %d", eglGetError());
        return;
    }
    LOGD("eglChooseConfig , numConfigs[%d]", numConfigs);

    //创建OpenGL上下文环境，EGLContext
    EGLint attributes[] = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE};
    EGLContext eglContext;
    if (!(eglContext = eglCreateContext(display, config, NULL, attributes))) {
        LOGE("eglCreateContext failed return: %d", eglGetError());
        return;
    }
    LOGD("eglCreateContext");

    //将EGL与设备的屏幕连接起来，通过EGLSurface，实际上是一个FrameBuffer。
    //通过eglCreateWindowSurface可以创建一个实际显示的Surface，通过eglCreatePbufferSurface可以创建一个OffScreen的Surface
    //Surface包含基础属性，比如EGL_WIDTH、EGL_HEIGHT
    EGLSurface eglSurface = NULL;
    EGLint format;
    if (!eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &format)) {
        LOGE("eglGetConfigAttrib failed return: %d", eglGetError());
        return;
    }
    LOGD("eglGetConfigAttrib");

    {
        //接收从Java端传递过来的Surface对象, EGL和Java端的view（屏幕）连接起来
        ANativeWindow *_window = ANativeWindow_fromSurface(env, surface);
        if (!(eglSurface = eglCreateWindowSurface(display, config, _window, 0))) {
            LOGE("eglCreateWindowSurface failed return: %d", eglGetError());
            return;
        }
        ANativeWindow_setBuffersGeometry(_window, 0, 0, format);
    }
    {
        //离线渲染，在后台使用OpenGL进行一些图像的处理，需要使用离线处理的Surface
        int width = 640;
        int height = 480;
        EGLint pbufferAttributes[] = {EGL_WIDTH, width, EGL_HEIGHT, height, EGL_NONE, EGL_NONE};
        if (!(eglSurface = eglCreatePbufferSurface(display, config, pbufferAttributes))) {
            LOGE("eglCreatePbufferSurface failed return: %d", eglGetError());
            return;
        }
    }

    {
        //需要开辟一个新的线程来执行OpenGL ES的渲染操作，并且必须为该线程绑定显示设备（Surface）和上下文环境（EGLContext）
        //因为每个线程都要绑定一个上下文，这样才能执行OpenGL指令
        eglMakeCurrent(display, eglSurface, eglSurface, eglContext);
        //此时就可以执行RenderLoop循环了
    }

    //销毁资源
    eglDestroySurface(display, eglSurface);
    eglDestroyContext(display, eglContext);


}
