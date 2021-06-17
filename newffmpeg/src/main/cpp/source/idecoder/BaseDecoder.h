//
// Created by loki on 2021/5/26.
//

#ifndef LEARNVIDEO_BASEDECODER_H
#define LEARNVIDEO_BASEDECODER_H

#include <jni.h>
#include <thread>
#include <string>
#include "fflog.h"
#include "SafeQueue.h"


typedef void (*CallbackFun)(AVFrame *frame);


class BaseDecoder {
public:
    BaseDecoder(AVCodecParameters *codecPar);

    virtual ~BaseDecoder();

    void setFrameCallback(CallbackFun frameCallback);

//任务队列相关
    SafeQueue *queue;
private:
    bool thread_running = true;

    //解码相关
    AVCodecParameters *codecPar;
    AVCodec *codec;
    AVCodecContext *codecContext;
    CallbackFun frameCallback;

    int prepareCodec();

    static void doDecodec(std::shared_ptr<BaseDecoder> that);

};


#endif //LEARNVIDEO_BASEDECODER_H
