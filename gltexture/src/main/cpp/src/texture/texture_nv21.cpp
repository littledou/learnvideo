//
// Created by loki on 2021/5/13.
//

#include "texture_nv21.h"


void texture_nv21::surfaceCreated() {
    char vertexSource[] = "#version 300 es                         \n"
                          "layout(location=0) in vec4 vPosition;   \n"
                          "void main()                             \n"
                          "{                                       \n"
                          "    gl_Position=vPosition;              \n"
                          "}                                       \n";
    char fragmentSource[] = "#version 300 es                       \n"
                            "precision mediump float;              \n"
                            "out vec4 fragColor;                   \n"
                            "void main()                           \n"
                            "{                                     \n"
                            "  fragColor=vec4(1.0, 0.0, 0.0, 1.0); \n"
                            "}                                     \n";
    program = ShaderUtil::createProgram(vertexSource, fragmentSource);
}

void texture_nv21::surfaceChanged(int w, int h) {

}

void texture_nv21::surfaceDestroyed() {
    if (program) {
        glDeleteProgram(program);
        program = GL_NONE;
    }
}

void texture_nv21::updateTexImage() {
    glUseProgram(program);

    glDrawArrays(GL_TRIANGLES, 0, 3);
    glUseProgram(GL_NONE);
}


extern "C"
JNIEXPORT jlong JNICALL
Java_cn_idu_glrenderer_texture_NativeNV21Texture_surfaceCreatedJni(JNIEnv *env, jobject thiz) {
    auto *texture = new texture_nv21();
    texture->surfaceCreated();
    return reinterpret_cast<jlong>(texture);
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_idu_glrenderer_texture_NativeNV21Texture_surfaceChangedJni(JNIEnv *env, jobject thiz,
                                                                   jlong handler, jint w,
                                                                   jint h) {
    auto *texture = reinterpret_cast<texture_nv21 *>(handler);
    texture->surfaceChanged(w, h);
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_idu_glrenderer_texture_NativeNV21Texture_updateTexImageJni(JNIEnv *env, jobject thiz,
                                                                   jlong handler) {
    auto *texture = reinterpret_cast<texture_nv21 *>(handler);
    texture->updateTexImage();
}extern "C"
JNIEXPORT void JNICALL
Java_cn_idu_glrenderer_texture_NativeNV21Texture_surfaceDestroyedJni(JNIEnv *env, jobject thiz,
                                                                     jlong handler) {
    auto *texture = reinterpret_cast<texture_nv21 *>(handler);
    texture->surfaceDestroyed();
    delete texture;
}