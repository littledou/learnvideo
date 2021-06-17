#ifndef LEARNVIDEO_FFLOG_H
#define LEARNVIDEO_FFLOG_H

#include <jni.h>
#include "ffinc.h"
#include <android/log.h>

#define LOG_TAG "ffuse"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))

#endif //LEARNVIDEO_FFLOG_H