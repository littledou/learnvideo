//
// Created by loki on 2021/4/25.
//

#include "PngPreviewTexture.h"
#include "png_decoder.h"


PngPreviewTexture::PngPreviewTexture() {

}

PngPreviewTexture::~PngPreviewTexture() {
    LOGD("~PngPreviewTexture");
    if (texture) {
        glDeleteTextures(1, &texture);
    }
}


int PngPreviewTexture::createTexture() {
    glGenTextures(1, &texture);
    glBindTexture(GL_TEXTURE_2D, texture);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);//放大双线性过滤
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);//缩小双线性过滤
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);//S轴归一化，纹理坐标过1归1，过0归0
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);//T轴归一化
    return 1;
}


void PngPreviewTexture::updateTexImage(const void *pixels, const int width, const int height) {
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, texture);
    if (checkGlError("glBindTexture")) {
        return;
    }
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                 (byte *) pixels);
}


bool PngPreviewTexture::checkGlError(const char *op) {
    GLint error;
    while ((error = glGetError())) {
        LOGD("error: after %s() glerror (0x%x)\n", op, error);
        return true;
    }
    return false;
}

void PngPreviewTexture::bindTexture(GLint sample2DPosition) {
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, texture);
    if (checkGlError("glBindTexture")) {
        return;
    }
    glUniform1i(sample2DPosition, 0);
}
