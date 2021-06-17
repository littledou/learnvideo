//
// Created by loki on 2021/6/8.
//

#ifndef LEARNVIDEO_FFTIME_C
#define LEARNVIDEO_FFTIME_C

#include "time.h"

int64_t GetCurMsTime() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    int64_t ts = tv.tv_sec * 1000 + tv.tv_usec / 1000;
    return ts;
}

#endif //LEARNVIDEO_FFTIME_C
