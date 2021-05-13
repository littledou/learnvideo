//
// Created by loki on 2021/5/13.
//

#ifndef LEARNVIDEO_TEXTUREBASE_H
#define LEARNVIDEO_TEXTUREBASE_H

#include "libjni.h"
#include "ShaderUtil.h"

class TextureBase {
public:
    TextureBase() {
        program = 0;
    }

protected:
    GLuint program;

};

#endif //LEARNVIDEO_TEXTUREBASE_H
