//
// Created by loki on 2021/4/21.
//

#ifndef LEARNVIDEO_OPENSLESCONTEXT_H
#define LEARNVIDEO_OPENSLESCONTEXT_H

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

class OpenSLESContext {

public:
    OpenSLESContext();

    virtual ~OpenSLESContext();

    static OpenSLESContext *GetInstance(); //工厂方法(用来获得实例)

    SLEngineItf getEngine() {
        return engineEngine;
    };

private:
    SLObjectItf engineObject;
    SLEngineItf engineEngine;
    static OpenSLESContext *instance;
    bool isInit = false;

    void init();

    SLresult createEngine();


    SLresult realizeObject(SLObjectItf object);

    void destoryObject(SLObjectItf &object);


    void test_SLES() {
        //1. 创建引擎对象接口
        createEngine();

        //2. 实例化引擎对象
        realizeObject(engineObject);
        //3. 获取这个引擎对象的方法接口
        (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
        //4. 创建需要的对象接口
        SLObjectItf outputMixObject;
        (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 0, nullptr, nullptr);
        SLObjectItf audioPlayerObject;
//        (*engineEngine)->CreateAudioPlayer();
        //5. 实例化新的对象
        realizeObject(outputMixObject);
        realizeObject(audioPlayerObject);
        //6. 复杂对象，需要获取新的接口来访问对象的状态或维护对象的状态，比如注册audioplayer的回调
        SLPlayItf audioPlayerPlay;
        (*audioPlayerObject)->GetInterface(audioPlayerObject, SL_IID_PLAY, &audioPlayerPlay);
        //设置播放状态
        (*audioPlayerPlay)->SetPlayState(audioPlayerPlay, SL_PLAYSTATE_PLAYING);
        //设置暂停状态...
        (*audioPlayerPlay)->SetPlayState(audioPlayerPlay, SL_PLAYSTATE_PAUSED);
        //7. 使用完对象，必须释放
        destoryObject(audioPlayerObject);
        destoryObject(outputMixObject);
        destoryObject(engineObject);
    }


};


#endif //LEARNVIDEO_OPENSLESCONTEXT_H
