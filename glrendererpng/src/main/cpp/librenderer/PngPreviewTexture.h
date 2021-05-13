//
// Created by loki on 2021/4/25.
//

#ifndef LEARNVIDEO_PNGPREVIEWTEXTURE_H
#define LEARNVIDEO_PNGPREVIEWTEXTURE_H

#include "../src/native-lib.h"

class PngPreviewTexture {
public:
    PngPreviewTexture();

    virtual ~PngPreviewTexture();
    int createTexture();
    void updateTexImage(const void *pixels, const int width, const int height);
    void bindTexture(GLint sample2DPosition);

private:
    GLuint texture;
    bool checkGlError(const char* op);
};


#endif //LEARNVIDEO_PNGPREVIEWTEXTURE_H
