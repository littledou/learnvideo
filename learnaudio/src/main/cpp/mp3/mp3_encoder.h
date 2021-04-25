//
// Created by loki on 2021/4/14.
//

#ifndef LEARNAUDIO_MP3_ENCODER_H
#define LEARNAUDIO_MP3_ENCODER_H


#include <cstdio>
#include <lame/lame.h>

/**
 * 示例：pcm转码为mp3
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


#endif //LEARNAUDIO_MP3_ENCODER_H
