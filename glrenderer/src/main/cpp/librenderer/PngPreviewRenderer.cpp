//
// Created by loki on 2021/4/25.
//

#include "PngPreviewRenderer.h"
#include "PngPreviewTexture.h"

enum {
    ATTRIBUTE_VERTEX, ATTRIBUTE_TEXCOORD,
};

PngPreviewRenderer::PngPreviewRenderer() {}

PngPreviewRenderer::~PngPreviewRenderer() {
    LOGD("~PngPreviewRenderer");
    if (vertShader)
        glDeleteShader(vertShader);
    if (fragShader)
        glDeleteShader(fragShader);
    if (program) {
        glDeleteProgram(program);
        program = 0;
    }
}

void PngPreviewRenderer::initRender() {
    vertShader = compileShader(GL_VERTEX_SHADER, PIC_PREVIEW_VERTEX_SHADER_2);
    if (!vertShader) {
        LOGE("Failed to compile GL_VERTEX_SHADER");
        return;
    }
    fragShader = compileShader(GL_FRAGMENT_SHADER, PIC_PREVIEW_FRAG_SHADER_2);
    if (!fragShader) {
        LOGE("Failed to compile GL_FRAGMENT_SHADER");
        return;
    }

    if (!useProgram()) {
        LOGE("use program failed!");
    }

}

void PngPreviewRenderer::render(PngPreviewTexture *pTexture, int width, int height) {
    LOGD("glViewport: %d, %d", width, height);
    glViewport(0, 0, width, height);
    glClearColor(0.0f, 0.0f, 1.0f, 0.0f);
    glClear(GL_COLOR_BUFFER_BIT);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    glUseProgram(program);
    static const GLfloat _vertices[] = {-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f};
    glVertexAttribPointer(ATTRIBUTE_VERTEX, 2, GL_FLOAT, 0, 0, _vertices);
    glEnableVertexAttribArray(ATTRIBUTE_VERTEX);
    static const GLfloat texCoords[] = {0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f};
    glVertexAttribPointer(ATTRIBUTE_TEXCOORD, 2, GL_FLOAT, 0, 0, texCoords);
    glEnableVertexAttribArray(ATTRIBUTE_TEXCOORD);
    pTexture->bindTexture(uniformSampler);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

}

GLuint PngPreviewRenderer::compileShader(int shaderType, const char *shaderRes) {
    GLint status;
    GLuint shader = glCreateShader(shaderType);
    if (shader == 0 || shader == GL_INVALID_VALUE) {
        return 0;
    }
    glShaderSource(shader, 1, &shaderRes, NULL);
    glCompileShader(shader);
    glGetShaderiv(shader, GL_COMPILE_STATUS, &status);
    if (status == GL_FALSE) {
        glDeleteShader(shader);
        return 0;
    }
    return shader;
}

int PngPreviewRenderer::useProgram() {
    program = glCreateProgram();
    glAttachShader(program, vertShader);
    glAttachShader(program, fragShader);
    glBindAttribLocation(program, ATTRIBUTE_VERTEX, "position");
    glBindAttribLocation(program, ATTRIBUTE_TEXCOORD, "texcoord");
    glLinkProgram(program);
    GLint status;
    glGetProgramiv(program, GL_LINK_STATUS, &status);
    if (status == GL_FALSE) {
        LOGE("Failed link program:%d", program);
        return 0;
    }
    glUseProgram(program);
    uniformSampler = glGetUniformLocation(program, "yuvTexSampler");
    return 1;
}
