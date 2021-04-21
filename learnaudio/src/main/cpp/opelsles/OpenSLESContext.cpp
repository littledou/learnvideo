//
// Created by loki on 2021/4/21.
//

#include "OpenSLESContext.h"


OpenSLESContext *OpenSLESContext::instance = new OpenSLESContext();

OpenSLESContext *OpenSLESContext::GetInstance() {
    if (!instance->isInit) {
        instance->init();
        instance->isInit = true;
    }
    return instance;
}

void OpenSLESContext::init() {
    SLresult sLresult = createEngine();
    if (SL_RESULT_SUCCESS == sLresult) {
        sLresult = realizeObject(engineObject);
        if (SL_RESULT_SUCCESS == sLresult) {
            sLresult = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
        }
    }
}


OpenSLESContext::OpenSLESContext() {
    isInit = false;
}

OpenSLESContext::~OpenSLESContext() {

}


SLresult OpenSLESContext::createEngine() {
    SLEngineOption slEngineOption[] = {
            {
                    SL_ENGINEOPTION_THREADSAFE, SL_BOOLEAN_TRUE
            }
    };
    return slCreateEngine(&engineObject, sizeof(slEngineOption) / sizeof(slEngineOption[0]),
                          slEngineOption,
                          0, 0, 0);
}


SLresult OpenSLESContext::realizeObject(SLObjectItf object) {
    return (*object)->Realize(object, SL_BOOLEAN_FALSE);
}

void OpenSLESContext::destoryObject(SLObjectItf &object) {
    if (0 != object)
        (*object)->Destroy(object);
    object = 0;
}


