//
// Created by loki on 2021/5/13.
//

#ifndef LEARNVIDEO_SHADERUTIL_H
#define LEARNVIDEO_SHADERUTIL_H

#include "libjni.h"
class ShaderUtil {
public:
    static int createProgram(const char* vertexSource, const char* fragmentSource);



private:
    static void checkGLError(const char* op);
    static int loadShader(GLenum shaderType, const char* source);
    static void unloadShader(GLuint program, GLuint shaderHandle);
};


#endif //LEARNVIDEO_SHADERUTIL_H
