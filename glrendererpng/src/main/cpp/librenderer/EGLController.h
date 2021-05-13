//
// Created by loki on 2021/4/25.
//

#ifndef LEARNVIDEO_EGLCONTROLLER_H
#define LEARNVIDEO_EGLCONTROLLER_H

#include<EGL/egl.h>
#include <EGL/eglext.h>
#include "liblog.h"

class EGLController {

public:
    virtual ~EGLController();

    EGLController();

private:
    EGLDisplay eglDisplay;
    EGLContext eglContext;
    EGLConfig eglConfig;
    EGLSurface eglSurface;

public:
    bool init();

    void eglCreateSurface(ANativeWindow *_window);

    void drawFrame();

    void makeCurrent();
};


#endif //LEARNVIDEO_EGLCONTROLLER_H
