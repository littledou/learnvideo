#include <string>
#include <unistd.h>
#include "fflog.h"
#include "pthread.h"
#include "BaseDecoder.h"

static JavaVM *savedVm;

void setVM(JavaVM *vm) {
    savedVm = vm;
}

JavaVM *getVM() {
    return savedVm;
}

JNIEnv *getEnv() {
    JNIEnv *env = NULL;
    if (savedVm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        env = NULL;
    }
    return env;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {

    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    setVM(vm);
    return JNI_VERSION_1_6;
}

jobject global_callback = NULL;
jmethodID onFrame = NULL;

extern "C"
JNIEXPORT jstring JNICALL
Java_cn_idu_newffmpeg_FFNative_00024Companion_ffInfo(JNIEnv *env, jobject thiz) {
    char info[40000] = {0};
    AVCodec *c_temp = av_codec_next(NULL);

    while (c_temp != NULL) {
        if (c_temp->decode != NULL) {
            LOGD("[AVCodec Dec ] type:%d, %10s", c_temp->type, c_temp->name);
        } else {
            LOGD("[AVCodec ENC ] type:%d, %10s", c_temp->type, c_temp->name);
        }
        c_temp = c_temp->next;
    }

    AVFilter *f_temp = (AVFilter *) avfilter_next(NULL);
    while (f_temp != NULL) {
        LOGD("[AVFilter %10s]", f_temp->name);
        f_temp = f_temp->next;
    }

    struct URLProtocol *pub = NULL;
    struct URLProtocol **p_temp = &pub;
    avio_enum_protocols(reinterpret_cast<void **>(p_temp), 0);

    while ((*p_temp) != NULL) {
        LOGD("[protocols In ][%10s]", avio_enum_protocols(reinterpret_cast<void **>(p_temp), 0));
    }

    pub = NULL;
    avio_enum_protocols((void **) p_temp, 1);
    while ((*p_temp) != NULL) {
        LOGD("[protocols Out ][%10s]", avio_enum_protocols(reinterpret_cast<void **>(p_temp), 1));
    }

    AVInputFormat *if_temp = av_iformat_next(NULL);
    AVOutputFormat *of_temp = av_oformat_next(NULL);
    //Input
    while (if_temp != NULL) {
        LOGD("[AvFormat In ][%10s]", if_temp->name);
        if_temp = if_temp->next;
    }
    //Output
    while (of_temp != NULL) {
        LOGD("[AvFormat Out ][%10s]", of_temp->name);
        of_temp = of_temp->next;
    }

    return env->NewStringUTF(info);
}


void audioFrameCallback(AVFrame *frame) {
    //TODO ????????????
}

void videoFrameCallback(AVFrame *frame) {
    //TODO ??????????????? ??????callback???java
    JavaVM *vm = getVM();
    JNIEnv *env;
    vm->AttachCurrentThread(&env, nullptr);
    int y_len = frame->width * frame->height;
    int uv_len = frame->width * frame->height / 4;
    int buf_len = y_len * 3 / 2;
    jbyteArray data0 = env->NewByteArray(buf_len);

    env->SetByteArrayRegion(data0, 0, y_len, reinterpret_cast<const jbyte *>(frame->data[0]));
    env->SetByteArrayRegion(data0, y_len, uv_len, reinterpret_cast<const jbyte *>(frame->data[1]));
    env->SetByteArrayRegion(data0, y_len + uv_len, uv_len,
                            reinterpret_cast<const jbyte *>(frame->data[2]));
    env->CallVoidMethod(global_callback, onFrame, data0, frame->width, frame->height);
    env->DeleteLocalRef(data0);
    vm->DetachCurrentThread();
}


int testLoadingFile(const char *url) {
    int ret = -1;
    AVFormatContext *avformatCtx = avformat_alloc_context();
//    char *url = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
//    char *url = "rtsp://admin:wwj6184074@192.168.2.108:554/cam/realmonitor?channel=1&subtype=1";
    LOGD("loading [%s]", url);
    //????????????
    if ((ret = avformat_open_input(&avformatCtx, url, NULL, NULL)) != 0) {
        LOGD("??????????????????,%d,%s???[%s]", ret, av_err2str(ret), url);
        return -1;
    }
    LOGD("loading success [%s]", url);
    //????????????????????????
    if (avformat_find_stream_info(avformatCtx, NULL) != 0) {
        LOGD("Fail to find stream info, %d", ret);
        return -1;
    }
    LOGD("avformat_find_stream_info success,duration:[%lld]", avformatCtx->duration);
    //?????????????????????????????????
    int audioStreamIndex = -1;
    int videoStreamIndex = -1;
    for (int i = 0; i < avformatCtx->nb_streams; ++i) {
        if (avformatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            audioStreamIndex = i;
            break;
        }
    }

    //?????????????????????????????????
    for (int i = 0; i < avformatCtx->nb_streams; ++i) {
        if (avformatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoStreamIndex = i;
            break;
        }
    }

    LOGD("found audiostream :[%d], videostream : [%d]", audioStreamIndex, videoStreamIndex);

    BaseDecoder *audioDecoder;
    if (audioStreamIndex != -1) {
        //??????index?????????????????????????????????????????????????????????????????????
        AVCodecParameters *audioCodecPar = avformatCtx->streams[audioStreamIndex]->codecpar;
        audioDecoder = new BaseDecoder(audioCodecPar);
        audioDecoder->setFrameCallback(audioFrameCallback);
    }
    BaseDecoder *videoDecoder;
    if (videoStreamIndex != -1) {
        AVCodecParameters *videoCodecPar = avformatCtx->streams[videoStreamIndex]->codecpar;
        videoDecoder = new BaseDecoder(videoCodecPar);
        videoDecoder->setFrameCallback(videoFrameCallback);
    }

    //????????????????????????packet
//    AVPacket *packet = av_packet_alloc();   //????????????????????????
//    AVFrame *frame = av_frame_alloc();     //????????????????????????
    while (true) {
        //??????????????????????????????????????????????????????????????????????????????
        if (videoDecoder->queue->size() > 300) {
            sleep(1);
            LOGD("???????????????????????????????????????????????????");
            continue;
        }
        AVPacket *packet = av_packet_alloc();   //????????????????????????
        if (av_read_frame(avformatCtx, packet) == 0) {
            if (videoStreamIndex != -1 && packet->stream_index == videoStreamIndex) {
                videoDecoder->queue->offer(packet);
                LOGD("??????????????????, queue size:[%d]", videoDecoder->queue->size());
            } else if (audioStreamIndex != -1 && packet->stream_index == audioStreamIndex) {
//                audioDecoder->queue->offer(packet);
//                LOGD("??????????????????, queue size:[%d]", audioDecoder->queue->size());
            } else {
                LOGD("?????? packet ??????: %d", packet->stream_index);
            }
        } else {
            LOGD("?????????????????????????????????");
            break;
        }
//        av_packet_unref(packet);//????????????packet?????????????????????
    }

    //???????????????
    if (avformatCtx != nullptr) {
        avformat_close_input(&avformatCtx);
        avformat_free_context(avformatCtx);
        delete avformatCtx;
    }


    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_idu_newffmpeg_FFNative_00024Companion_testLoading(JNIEnv *env, jobject thiz) {
    testLoadingFile("/sdcard/a1.mp4");
}extern "C"
JNIEXPORT void JNICALL
Java_cn_idu_newffmpeg_FFNative_00024Companion_testLoadingRTSP(JNIEnv *env, jobject thiz,
                                                              jstring path, jobject callback) {
    global_callback = env->NewGlobalRef(callback);
    jclass clazz = env->GetObjectClass(global_callback);
    onFrame = env->GetMethodID(clazz, "onFrame", "([BII)V");
    const char *rtspUrl = env->GetStringUTFChars(path, 0);
    testLoadingFile(rtspUrl);
    env->ReleaseStringUTFChars(path, rtspUrl);

    env->DeleteGlobalRef(global_callback);
}