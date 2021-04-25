//
// Created by loki on 2021/4/25.
//

#include "EGLController.h"

EGLController::EGLController() {
    eglDisplay = EGL_NO_DISPLAY;
    eglContext = EGL_NO_CONTEXT;
    eglSurface = EGL_NO_SURFACE;
}

EGLController::~EGLController() {
    LOGD("~EGLController");
    eglDestroySurface(eglDisplay, eglSurface);
    eglSurface = EGL_NO_SURFACE;
    eglMakeCurrent(eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    eglDestroyContext(eglDisplay, eglContext);
    eglDisplay = EGL_NO_DISPLAY;
    eglContext = EGL_NO_CONTEXT;
}


/**
 * 创建上下文环境
 * @return
 */
bool EGLController::init() {
    EGLint numConfigs;
    EGLint width;
    EGLint height;

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

    //获取display
    if ((eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY)) == EGL_NO_DISPLAY) {
        LOGE("eglGetDisplay return error %d", eglGetError());
        return false;
    }
    LOGD("eglGetDisplay");

    //初始化display
    int major, minor;
    if (!eglInitialize(eglDisplay, &major, &minor)) {
        LOGE("eglInitialize return error %d", eglGetError());
        return false;
    }
    LOGD("eglInitialize,major[%d], minor[%d]", major, minor);

    //获取配置选项信息
    if (!eglChooseConfig(eglDisplay, attribs, &eglConfig, 1, &numConfigs)) {
        LOGE("eglChooseConfig failed return: %d", eglGetError());
        return false;
    }
    LOGD("eglChooseConfig , numConfigs[%d]", numConfigs);

    //创建OpenGL上下文环境，EGLContext
    EGLint attributes[] = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE};
    if (!(eglContext = eglCreateContext(eglDisplay, eglConfig, NULL, attributes))) {
        LOGE("eglCreateContext failed return: %d", eglGetError());
        return false;
    }
    LOGD("eglCreateContext");
    return true;
}

void EGLController::eglCreateSurface(ANativeWindow *_window) {
    //将EGL与设备的屏幕连接起来，通过EGLSurface，实际上是一个FrameBuffer。
    //通过eglCreateWindowSurface可以创建一个实际显示的Surface，通过eglCreatePbufferSurface可以创建一个OffScreen的Surface
    //Surface包含基础属性，比如EGL_WIDTH、EGL_HEIGHT
    EGLint format;
    if (!eglGetConfigAttrib(eglDisplay, eglConfig, EGL_NATIVE_VISUAL_ID, &format)) {
        LOGE("eglGetConfigAttrib failed return: %d", eglGetError());
        return;
    }
    LOGD("eglGetConfigAttrib");

    //接收从Java端传递过来的Surface对象, EGL和Java端的view（屏幕）连接起来
    if (!(eglSurface = eglCreateWindowSurface(eglDisplay, eglConfig, _window, 0))) {
        LOGE("eglCreateWindowSurface failed return: %d", eglGetError());
        return;
    }
    makeCurrent();
}

void EGLController::makeCurrent() {
    eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
}

void EGLController::drawFrame() {

    if (!eglSwapBuffers(eglDisplay, eglSurface)) {
        LOGE("eglSwapBuffers() returned error %d", eglGetError());
    }

}


