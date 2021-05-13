//
// Created by loki on 2021/4/23.
//
#include "native-lib.h"
#include "PngPreviewController.h"

static PngPreviewController *previewController = 0;
extern "C"
JNIEXPORT void JNICALL
Java_cn_idu_glrenderer_jni_ImagePreviewJni_00024Companion_init(JNIEnv *env, jobject thiz,
                                                           jstring img_path, jobject surface) {
    char *png_path = const_cast<char *>(env->GetStringUTFChars(img_path, 0));
    ANativeWindow *_window = ANativeWindow_fromSurface(env, surface);
    previewController = new PngPreviewController();
    previewController->setWindow(_window);
    previewController->setResource(png_path);
    env->ReleaseStringUTFChars(img_path, png_path);
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_idu_glrenderer_jni_ImagePreviewJni_00024Companion_resize(JNIEnv *env, jobject thiz,
                                                             jint width, jint height) {
    previewController->drawFrame(width, height);
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_idu_glrenderer_jni_ImagePreviewJni_00024Companion_stop(JNIEnv *env, jobject thiz) {
    delete previewController;
    previewController = NULL;
}