//
// Created by loki on 2021/4/22.
//

#ifndef LEARNVIDEO_LIBLOG_H
#define LEARNVIDEO_LIBLOG_H

#include <android/log.h>

#define LOG_TAG "elghelp"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))
#endif //LEARNVIDEO_LIBLOG_H
