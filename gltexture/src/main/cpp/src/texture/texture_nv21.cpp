//
// Created by loki on 2021/5/13.
//

#include <cstdlib>
#include "texture_nv21.h"

void texture_nv21::surfaceCreated() {
    char vertexSource[] = "#version 300 es                         \n"
                          "layout(location=0) in vec4 a_position;   \n"
                          "layout(location=1) in vec2 a_texCoord;\n"
                          "out vec2 v_texCoord;\n"
                          "void main()                             \n"
                          "{                                       \n"
                          "    gl_Position=a_position;              \n"
                          "    v_texCoord = a_texCoord;             \n"
                          "}                                       \n";
    char fragmentSource[] = "#version 300 es                       \n"
                            "precision mediump float;              \n"
                            "in vec2 v_texCoord;              \n"
                            "layout(location=0) out vec4 outColor;\n"
                            "uniform sampler2D y_texture;            \n"
                            "uniform sampler2D uv_texture;           \n"
                            "void main()                           \n"
                            "{                                     \n"
                            "   vec3 yuv;                           \n"
                            "   yuv.x = texture(y_texture, v_texCoord).r;\n"
                            "   yuv.y = texture(uv_texture, v_texCoord).a-0.5;\n"
                            "   yuv.z = texture(uv_texture, v_texCoord).r-0.5;\n"
                            "   highp vec3 rgb = mat3(1,1,1, 0,-0.344,1.770, 1.403,-0.714,0)*yuv;\n"
                            "   outColor=vec4(rgb, 1.0); \n"
                            "}                                     \n";
    program = ShaderUtil::createProgram(vertexSource, fragmentSource);
    // Get the sampler location
    m_ySamplerLoc = glGetUniformLocation(program, "y_texture");
    m_uvSamplerLoc = glGetUniformLocation(program, "uv_texture");

    GLuint textureIds[2] = {0};
    glGenTextures(2, textureIds);
    m_yTextureId = textureIds[0];
    m_uvTextureId = textureIds[1];
}

void texture_nv21::surfaceChanged(int w, int h) {

}

void texture_nv21::surfaceDestroyed() {
    if (program) {
        glDeleteProgram(program);
        glDeleteTextures(1, &m_yTextureId);
        glDeleteTextures(1, &m_uvTextureId);
        program = GL_NONE;
    }
}

void texture_nv21::updateTexImage() {

    //绑定Y通道数据到纹理上
    glBindTexture(GL_TEXTURE_2D, m_yTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, width, height, 0,
                 GL_LUMINANCE, GL_UNSIGNED_BYTE, ppPlane[0]);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glBindTexture(GL_TEXTURE_2D, GL_NONE);

    //绑定uv通道数据到纹理上
    glBindTexture(GL_TEXTURE_2D, m_uvTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE_ALPHA, width >> 1, height >> 1, 0,
                 GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, ppPlane[1]);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glBindTexture(GL_TEXTURE_2D, GL_NONE);

    GLfloat verticesCoords[] = {
            -1.0f, 1.0f,  // Position 0
            -1.0f, -1.0f,  // Position 1
            1.0f, -1.0f,  // Position 2
            1.0f, 1.0f   // Position 3
    };

    GLfloat textureCoords[] = {
            0.0f, 0.0f,        // TexCoord 0
            0.0f, 1.0f,        // TexCoord 1
            1.0f, 1.0f,        // TexCoord 2
            1.0f, 0.0f         // TexCoord 3
    };
    GLushort indices[] = {0, 1, 2, 0, 2, 3};

    glUseProgram(program);

    glVertexAttribPointer(0, 2, GL_FLOAT, 0, 2 * sizeof(GLfloat), verticesCoords);
    glVertexAttribPointer(1, 2, GL_FLOAT, 0, 2 * sizeof(GLfloat), textureCoords);
    glEnableVertexAttribArray(0);
    glEnableVertexAttribArray(1);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, m_yTextureId);
    glUniform1i(m_ySamplerLoc, 0);

    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, m_uvTextureId);
    glUniform1i(m_uvSamplerLoc, 1);

    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, indices);
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
                                                                   jlong handler, jint format,
                                                                   jbyteArray data, jint width,
                                                                   jint height) {
    auto *texture = reinterpret_cast<texture_nv21 *>(handler);
    if (texture->ppPlane[0] == nullptr) {
        texture->width = width;
        texture->height = height;
        texture->ppPlane[0] = static_cast<uint8_t *>(malloc(width * height * 1.5));
        texture->ppPlane[1] = texture->ppPlane[0] + width * height;
    }
    int len = env->GetArrayLength(data);
    env->GetByteArrayRegion(data, 0, len, reinterpret_cast<jbyte *>(texture->ppPlane[0]));
    texture->updateTexImage();
}
extern "C"
JNIEXPORT void JNICALL
Java_cn_idu_glrenderer_texture_NativeNV21Texture_surfaceDestroyedJni(JNIEnv *env, jobject thiz,
                                                                     jlong handler) {
    auto *texture = reinterpret_cast<texture_nv21 *>(handler);
    texture->surfaceDestroyed();
    delete texture;
}