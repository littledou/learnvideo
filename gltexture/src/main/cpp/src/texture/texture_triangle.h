//
// Created by loki on 2021/5/13.
//

#ifndef LEARNVIDEO_TEXTURE_TRIANGLE_H
#define LEARNVIDEO_TEXTURE_TRIANGLE_H


#include "TextureBase.h"

class texture_triangle : public TextureBase {

public:
    texture_triangle();

    virtual ~texture_triangle();
    virtual void surfaceCreated();//EGL环境已经准备好，初始化数据
    virtual void surfaceChanged(int w, int h);

    virtual void surfaceDestroyed();

    virtual void updateTexImage();


};


#endif //LEARNVIDEO_TEXTURE_TRIANGLE_H
