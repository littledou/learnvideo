//
// Created by loki on 2021/4/14.
//

#ifndef KOTLINNATIVETEST_MP3_ENCODER_H
#define KOTLINNATIVETEST_MP3_ENCODER_H


#include <cstdio>
#include <lame/lame.h>

/**
 * 示例：
 */
class Mp3Encoder {

private:
    FILE *pcmFile{};
    FILE *mp3File{};
    lame_t lameClient{};

public:
    Mp3Encoder();

    ~Mp3Encoder();

    int Init(const char *pcmFilePath, const char *mp3FilePath, int sampleRate, int channels,
             int bitRate);

    void Encode();

    void Destory();
};


#endif //KOTLINNATIVETEST_MP3_ENCODER_H
