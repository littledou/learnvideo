//
// Created by loki on 2021/4/24.
//

#include "PngPreviewController.h"

/**
 * 1. 创建EGL环境
 * 2. 将EGL环境与Java端传过来端Surface绑定，
 * 3. 创建2D纹理
 * 4. 加载PNG数据源，并通过glTexImage2D方法绑定到2D纹理上
 * 5. 刷新window，将2D纹理提交给OpenGL ES渲染
 *
 */
//TODO ???: 暂未达成自由resize，另外现在并未开辟新的线程来执行OpenGL ES渲染
PngPreviewController::PngPreviewController() {
    pngPicDecoder = new PngPicDecoder();

    eglController = new EGLController();
    eglController->init();

    pngPreviewTexture = new PngPreviewTexture();
    pngPreviewRenderer = new PngPreviewRenderer();
}

PngPreviewController::~PngPreviewController() {
    LOGD("~PngPreviewController");
    delete pngPreviewTexture;
    pngPreviewTexture = NULL;

    delete pngPreviewRenderer;
    pngPreviewRenderer = NULL;

    delete eglController;
    eglController = NULL;

    delete pngPicDecoder;
    pngPicDecoder = NULL;
}

void PngPreviewController::setWindow(ANativeWindow *pWindow) {
    eglController->eglCreateSurface(pWindow);
    pngPreviewTexture->createTexture();
    pngPreviewRenderer->initRender();
}

void PngPreviewController::setResource(char *imgPath) {
    pngPicDecoder->openFile(imgPath);//加载数据
    const RawImageData rawImageData = pngPicDecoder->getRawImageData();
    LOGD("raw_image_data Meta: width is %d height is %d size is %d colorFormat is %d",
         rawImageData.width, rawImageData.height, rawImageData.size,
         rawImageData.gl_color_format);
    LOGD("colorFormat GL_RGBA is %d", GL_RGBA);
    //TODO 将数据提交给texture
    pngPreviewTexture->updateTexImage(rawImageData.data, rawImageData.width, rawImageData.height);
    //释放数据
    pngPicDecoder->releaseRawImageData(&rawImageData);
}

void PngPreviewController::drawFrame(int width, int height) {
    eglController->makeCurrent();
    pngPreviewRenderer->render(pngPreviewTexture, width, height);
    eglController->drawFrame();
}