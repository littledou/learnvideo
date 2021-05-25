//
// Created by loki on 2021/5/13.
//

#ifndef LEARNVIDEO_TEXTURE_NV21_H
#define LEARNVIDEO_TEXTURE_NV21_H

#include "TextureBase.h"

class texture_nv21 : TextureBase {

public:
    texture_nv21() {
        m_yTextureId = GL_NONE;
        m_uvTextureId = GL_NONE;
        m_ySamplerLoc = GL_NONE;
        m_uvSamplerLoc = GL_NONE;
        ppPlane[0] = nullptr;
        ppPlane[1] = nullptr;
        ppPlane[2] = nullptr;

    };

public:
    uint8_t *ppPlane[3];
    int width, height;

    virtual void surfaceCreated();//EGL环境已经准备好，初始化数据
    virtual void surfaceChanged(int w, int h);

    virtual void surfaceDestroyed();

    virtual void updateTexImage();

private:
    GLuint m_yTextureId;
    GLuint m_uvTextureId;

    GLint m_ySamplerLoc;
    GLint m_uvSamplerLoc;
};


#endif //LEARNVIDEO_TEXTURE_NV21_H
