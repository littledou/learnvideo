//
// Created by loki on 2021/5/13.
//

#include "texture_triangle.h"


texture_triangle::texture_triangle() = default;

texture_triangle::~texture_triangle() = default;

GLfloat vertices[] = {
        -1.0f, -1.0f,
        1.0f, -1.0f,
        0.0f, 0.0f
};

void texture_triangle::surfaceCreated() {
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

void texture_triangle::surfaceChanged(int w, int h) {

}

void texture_triangle::surfaceDestroyed() {
    if (program) {
        glDeleteProgram(program);
        program = GL_NONE;
    }
}

void texture_triangle::updateTexImage() {
    glUseProgram(program);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 0, vertices);
    glEnableVertexAttribArray(0);
    glDrawArrays(GL_TRIANGLES, 0, 3);
    glUseProgram(GL_NONE);
}


extern "C"
JNIEXPORT jlong JNICALL
Java_cn_idu_glrenderer_texture_NativeTriangleTexture_surfaceCreatedJni(JNIEnv *env, jobject thiz) {
    auto *texture = new texture_triangle();
    texture->surfaceCreated();
    return reinterpret_cast<jlong>(texture);
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_idu_glrenderer_texture_NativeTriangleTexture_surfaceChangedJni(JNIEnv *env, jobject thiz,
                                                                       jlong handler, jint w,
                                                                       jint h) {
    auto *texture = reinterpret_cast<texture_triangle *>(handler);
    texture->surfaceChanged(w, h);
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_idu_glrenderer_texture_NativeTriangleTexture_updateTexImageJni(JNIEnv *env, jobject thiz,
                                                                       jlong handler) {
    auto *texture = reinterpret_cast<texture_triangle *>(handler);
    texture->updateTexImage();
}extern "C"
JNIEXPORT void JNICALL
Java_cn_idu_glrenderer_texture_NativeTriangleTexture_surfaceDestroyedJni(JNIEnv *env, jobject thiz,
                                                                         jlong handler) {
    auto *texture = reinterpret_cast<texture_triangle *>(handler);
    texture->surfaceDestroyed();
    delete texture;
}