//
// Created by loki on 2021/4/24.
//

#ifndef LEARNVIDEO_PNGPREVIEWCONTROLLER_H
#define LEARNVIDEO_PNGPREVIEWCONTROLLER_H

#include "png_decoder.h"
#include "PngPreviewTexture.h"
#include "../src/native-lib.h"
#include "PngPreviewRenderer.h"
#include "EGLController.h"

class PngPreviewController {
public:
    PngPreviewController();

    virtual ~PngPreviewController();

    void setWindow(ANativeWindow *pWindow);

    void setResource(char *imgPath);

    void drawFrame(int width, int height);

private:
    PngPicDecoder *pngPicDecoder;
    PngPreviewTexture *pngPreviewTexture;
    PngPreviewRenderer *pngPreviewRenderer;
    EGLController *eglController;

};


#endif //LEARNVIDEO_PNGPREVIEWCONTROLLER_H
