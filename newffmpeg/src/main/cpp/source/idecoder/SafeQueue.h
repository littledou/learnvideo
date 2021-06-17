//
// Created by loki on 2021/6/8.
//

#ifndef LEARNVIDEO_SAFEQUEUE_H
#define LEARNVIDEO_SAFEQUEUE_H

#include <pthread.h>
#include <queue>

using namespace std;

extern "C"
{
#include "libavcodec/avcodec.h"
};

class SafeQueue {

private:
    queue<AVPacket *> queue;
    pthread_mutex_t mute;
    pthread_cond_t cond;
    bool clearFlag = false;

public:
    SafeQueue() {
        pthread_mutex_init(&mute, 0);
        pthread_cond_init(&cond, 0);
    }

    virtual ~SafeQueue() {
        pthread_mutex_destroy(&mute);
        pthread_cond_destroy(&cond);
    }

    int size() {
        pthread_mutex_lock(&mute);
        int size = queue.size();
        pthread_mutex_unlock(&mute);
        return size;
    }

    void clear() {
        clearFlag = true;
        pthread_cond_broadcast(&cond);
        pthread_mutex_lock(&mute);
        while (!queue.empty()) {
            AVPacket *packet = queue.front();
            queue.pop();
            av_packet_free(&packet);
        }
        pthread_mutex_unlock(&mute);
    }

    void offer(AVPacket *t) {
        pthread_mutex_lock(&mute);
        queue.push(t);

//        pthread_cond_signal(&cond);
        pthread_cond_broadcast(&cond);
        pthread_mutex_unlock(&mute);
    }

    AVPacket *take() {
        pthread_mutex_lock(&mute);
        while (queue.empty()) {
            pthread_cond_wait(&cond, &mute);
            if (clearFlag) {
                pthread_mutex_unlock(&mute);
                return NULL;
            }
        }
        AVPacket *avPacket = queue.front();
        queue.pop();
        pthread_mutex_unlock(&mute);

        return avPacket;
    }

};


#endif //LEARNVIDEO_SAFEQUEUE_H
