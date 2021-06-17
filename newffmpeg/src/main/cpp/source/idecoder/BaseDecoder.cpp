//
// Created by loki on 2021/5/26.
//

#include "BaseDecoder.h"
#include "../util/fftime.c"

BaseDecoder::BaseDecoder(AVCodecParameters *codecPar) {
    queue = new SafeQueue();
    this->codecPar = codecPar;
    codec = avcodec_find_decoder(codecPar->codec_id);
    codecContext = avcodec_alloc_context3(codec);
    prepareCodec();
    std::shared_ptr<BaseDecoder> that(this);// 使用智能指针，线程结束后，自动删除本类指针
    std::thread t(doDecodec, that);
    t.detach();
}

int BaseDecoder::prepareCodec() {
    int ret = avcodec_parameters_to_context(codecContext, codecPar);
    if (ret != 0) {
        LOGD("加载解码器失败:[%d][%s]: %s", ret, codec->name, av_err2str(ret));
        return -1;
    }
    LOGD("加载解码器成功:[%s]", codec->name);
    if ((ret = avcodec_open2(codecContext, codec, nullptr)) < 0) {
        LOGD("打开解码器失败:[%d][%s]: %s", ret, codec->name, av_err2str(ret));
        return -1;
    }
    LOGD("打开解码器成功:[%s]", codec->name);
    return 0;
}

void BaseDecoder::doDecodec(std::shared_ptr<BaseDecoder> that) {
    while (that->thread_running) {
        AVPacket *packet = that->queue->take();

        //开始解码
        if (packet != nullptr) {
            LOGD("解码数据：【%s】Size:%d", that->codec->name, packet->size);
            avcodec_send_packet(that->codecContext, packet);
            AVFrame *frame = av_frame_alloc();
            while (true) {
                int result = avcodec_receive_frame(that->codecContext, frame);
                if (result == 0) {
                    LOGD("得到解码后的数据：format:[%d】, pixfmt:[%d],sample_fmt:[%d], width:[%d], height:[%d]",
                         frame->format, that->codecContext->pix_fmt, that->codecContext->sample_fmt,
                         frame->width,
                         frame->height);
                    //TODO 回调解码后的数据
                    if (that->frameCallback) {
                        that->frameCallback(frame);
                    }
                } else {
                    LOGD("获取解码后的数据失败，可能是无解码缓冲输出: %s", av_err2str(result));
                    break;
                }
            }
            //释放
            av_frame_free(&frame);
            av_packet_free(&packet);
        } else {
            LOGD("读取队列中指针为空值：【%s】", that->codec->name);
        }
    }
}

BaseDecoder::~BaseDecoder() {
    thread_running = false;
    queue->clear();
    delete queue;
    if (codecContext != nullptr) {
        avcodec_close(codecContext);
        avcodec_free_context(&codecContext);
        delete codecContext;
    }

}

void BaseDecoder::setFrameCallback(CallbackFun frameCallback) {
    this->frameCallback = frameCallback;
}


