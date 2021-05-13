//
// Created by loki on 2021/5/13.
//

#include <cstdlib>
#include "ShaderUtil.h"

int ShaderUtil::createProgram(const char *vertexSource, const char *fragmentSource) {
    GLuint program = 0;
    int vertexShaderHandler = loadShader(GL_VERTEX_SHADER, vertexSource);
    if (!vertexShaderHandler)return program;
    int fragmentShaderHandler = loadShader(GL_FRAGMENT_SHADER, fragmentSource);
    if (!fragmentShaderHandler)return program;

    program = glCreateProgram();
    if (program) {
        glAttachShader(program, vertexShaderHandler);
        checkGLError("glAttachShader V");
        glAttachShader(program, fragmentShaderHandler);
        checkGLError("glAttachShader F");

        glLinkProgram(program);
        GLint linkStatus = GL_FALSE;
        glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);

        unloadShader(program, vertexShaderHandler);
        unloadShader(program, fragmentShaderHandler);

        if (linkStatus != GL_TRUE) {
            LOGE("Link program Failed!!");
            GLint bufLength = 0;
            glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);
            if (bufLength) {
                char *buf = (char *) malloc((size_t) bufLength);
                if (buf) {
                    glGetProgramInfoLog(program, bufLength, NULL, buf);
                    LOGE("CreateProgram Could not link program:\n%s\n", buf);
                    free(buf);
                }
            }
            glDeleteProgram(program);
            program = 0;
        }
    }

    return program;
}

int ShaderUtil::loadShader(GLenum shaderType, const char *source) {
    GLuint shaderHandle = glCreateShader(shaderType);//创建一个shader，并记录id
    if (shaderHandle != 0) {
        glShaderSource(shaderHandle, 1, &source, NULL);//加载着色器源码
        glCompileShader(shaderHandle);//编译着色器源码
        GLint compiled = 0;
        glGetShaderiv(shaderHandle, GL_COMPILE_STATUS, &compiled);
        if (!compiled) {
            LOGE("编译着色器代码失败！shaderType[%d]", shaderType);
            GLint infoLen = 0;
            glGetShaderiv(shaderHandle, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen) {
                char *buf = (char *) malloc((size_t) infoLen);
                if (buf) {
                    glGetShaderInfoLog(shaderHandle, infoLen, NULL, buf);
                    LOGE("GLUtils::LoadShader Could not compile shaderHandle %d:\n%s\n", shaderType,
                         buf);
                    free(buf);
                }
            }
            glDeleteShader(shaderHandle);
            shaderHandle = 0;
        }
    }
    return shaderHandle;
}

void ShaderUtil::checkGLError(const char *op) {
    for (GLint error = glGetError(); error; error = glGetError()) {
        LOGE("CheckGLError: %s , glError (0x%x)\n", op, error);
    }
}

void ShaderUtil::unloadShader(GLuint program, GLuint shaderHandle) {
    glDetachShader(program, shaderHandle);
    glDeleteShader(shaderHandle);
}
