#include <jni.h>
#include <string>

#include "mp3_encoder.h"

extern "C" JNIEXPORT jstring JNICALL
Java_cn_idu_kotlinnativetest_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

Mp3Encoder *mp3Encoder;

extern "C"
JNIEXPORT void JNICALL
Java_cn_idu_kotlinnativetest_Mp3Encoder_init(JNIEnv *env, jobject thiz, jstring pcm_path,
                                             jint audio_channels, jint bit_rate, jint sample_rate,
                                             jstring mp3_path) {
    const char *pcmPath = env->GetStringUTFChars(pcm_path, NULL);
    const char *mp3Path = env->GetStringUTFChars(mp3_path, NULL);
    mp3Encoder = new Mp3Encoder();
    mp3Encoder->Init(pcmPath, mp3Path, sample_rate, audio_channels, bit_rate);
    env->ReleaseStringUTFChars(pcm_path, pcmPath);
    env->ReleaseStringUTFChars(mp3_path, mp3Path);
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_idu_kotlinnativetest_Mp3Encoder_encode(JNIEnv *env, jobject thiz) {
    mp3Encoder->Encode();
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_idu_kotlinnativetest_Mp3Encoder_destory(JNIEnv *env, jobject thiz) {
    mp3Encoder->Destory();
}