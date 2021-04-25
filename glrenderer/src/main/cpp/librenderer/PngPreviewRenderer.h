//
// Created by loki on 2021/4/25.
//

#ifndef LEARNVIDEO_PNGPREVIEWRENDERER_H
#define LEARNVIDEO_PNGPREVIEWRENDERER_H


#include "PngPreviewTexture.h"

//Shader.frag文件内容
static const char *PIC_PREVIEW_FRAG_SHADER_2 =
        "varying highp vec2 v_texcoord;\n"
        "uniform sampler2D yuvTexSampler;\n"
        "void main() {\n"
        "  gl_FragColor = texture2D(yuvTexSampler, v_texcoord);\n"
        "}\n";

//Shader.vert文件内容
static const char *PIC_PREVIEW_VERTEX_SHADER_2 =
        "attribute vec4 position;    \n"
        "attribute vec2 texcoord;   \n"
        "varying vec2 v_texcoord;     \n"
        "void main(void)               \n"
        "{                            \n"
        "   gl_Position = position;  \n"
        "   v_texcoord = texcoord;  \n"
        "}                            \n";

class PngPreviewRenderer {
protected:
    GLuint vertShader;
    GLuint fragShader;
    GLuint program;
    GLint uniformSampler;

    GLuint compileShader(int shaderType, const char *shaderRes);

    int useProgram();

public:
    PngPreviewRenderer();

    virtual ~PngPreviewRenderer();

    void initRender();

    void render(PngPreviewTexture *pTexture, int width, int height);
};


#endif //LEARNVIDEO_PNGPREVIEWRENDERER_H
