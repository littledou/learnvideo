#include <string>
#include "ffuse.h"

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


int testLoading() {
    int ret = -1;
    AVFormatContext *avformatCtx = avformat_alloc_context();
    char *url = "/data/data/cn.idu.learnvideo/files/a1.mp4";
    LOGD("loading [%s]", url);
    //打开文件
    if ((ret = avformat_open_input(&avformatCtx, url, NULL, NULL)) != 0) {
        LOGD("打开文件失败,%d：[%s]", ret, url);
        return -1;
    }
    LOGD("loading success [%s]", url);
    //获取音视频流信息
    if (avformat_find_stream_info(avformatCtx, NULL) != 0) {
        LOGD("Fail to find stream info, %d", ret);
        return -1;
    }
    LOGD("avformat_find_stream_info success");
    //获取音视频在流中的索引
    int audioStreamIndex = -1;
    int videoStreamIndex = -1;
    for (int i = 0; i < avformatCtx->nb_streams; ++i) {
        if (avformatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            audioStreamIndex = i;
            break;
        }
    }

    //获取视视频在流中的索引
    for (int i = 0; i < avformatCtx->nb_streams; ++i) {
        if (avformatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoStreamIndex = i;
            break;
        }
    }

    LOGD("found audiostream :[%d], videostream : [%d]", audioStreamIndex, videoStreamIndex);

    if (audioStreamIndex != -1) {
        //根据index获取各自的解码器参数、解码器、解码器上下文对象
        AVCodecParameters *audioCodecPar = avformatCtx->streams[audioStreamIndex]->codecpar;
        AVCodec *audioCodec = avcodec_find_decoder(audioCodecPar->codec_id);
        AVCodecContext *audioCodecCtx = avcodec_alloc_context3(audioCodec);
        if (avcodec_parameters_to_context(audioCodecCtx, audioCodecPar) != 0) {
            LOGD("加载音频解码器失败");
            return -1;
        }
        LOGD("加载音频解码器成功");
        if (avcodec_open2(audioCodecCtx, audioCodec, NULL) < 0) {
            LOGD("打开音频解码器失败");
            return -1;
        }
        LOGD("打开音频解码器成功");
    }

    if (videoStreamIndex != -1) {
        AVCodecParameters *videoCodecPar = avformatCtx->streams[videoStreamIndex]->codecpar;
        AVCodec *videoCodec = avcodec_find_decoder(videoCodecPar->codec_id);
        AVCodecContext *videoCodecCtx = avcodec_alloc_context3(videoCodec);
        if (avcodec_parameters_to_context(videoCodecCtx, videoCodecPar) != 0) {
            LOGD("加载视频解码器失败");
            return -1;
        }
        LOGD("加载视频解码器成功");
        if ((ret = avcodec_open2(videoCodecCtx, videoCodec, NULL)) < 0) {

            LOGD("打开视频解码器失败:[%d]: %s", ret, av_err2str(ret));
            return -1;
        }
        LOGD("打开视频解码器成功");
    }

    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_idu_newffmpeg_FFNative_00024Companion_testLoading(JNIEnv *env, jobject thiz) {
    testLoading();
}