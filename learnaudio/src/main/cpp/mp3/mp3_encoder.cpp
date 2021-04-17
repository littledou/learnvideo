//
// Created by loki on 2021/4/14.
//

#include "mp3_encoder.h"

int Mp3Encoder::Init(const char *pcmFilePath, const char *mp3FilePath, int sampleRate, int channels,
                     int bitRate) {
    int ret = -1;
    pcmFile = fopen(pcmFilePath, "rb");
    if (pcmFile) {
        mp3File = fopen(mp3FilePath, "wb");
        if (mp3File) {
            lameClient = lame_init();
            lame_set_in_samplerate(lameClient, sampleRate);
            lame_set_out_samplerate(lameClient, sampleRate);
            lame_set_num_channels(lameClient, channels);
            //设置比特率控制模式，默认是CBR(同样质量占用空间大)，但是通常我们都会设置VBR
            lame_set_VBR(lameClient, vbr_mtrh);
//            lame_set_brate(lameClient, bitRate / 1000);//设置CBR的比特率，只有在CBR模式下才生效
//            lame_set_VBR_mean_bitrate_kbps(lameClient, );
            lame_init_params(lameClient);
            ret = 0;
        }
    }
    return ret;
}

void Mp3Encoder::Encode() {
    int bufferSize = 1024 * 256;
    short *buffer = new short[bufferSize / 2];
    short *leftBuffer = new short[bufferSize / 4];
    short *rightBuffer = new short[bufferSize / 4];

    unsigned char *mp3_buffer = new unsigned char[bufferSize];

    size_t readBufferSize = 0;
    while ((readBufferSize = fread(buffer, 2, bufferSize / 2, pcmFile)) > 0) {
        for (int i = 0; i < readBufferSize; ++i) {
            if (i % 2 == 0) {
                leftBuffer[i / 2] = buffer[i];
            } else {
                rightBuffer[i / 2] = buffer[i];
            }
        }
        size_t wroteSize = lame_encode_buffer(lameClient, leftBuffer, rightBuffer,
                                              readBufferSize / 2, mp3_buffer, bufferSize);
        fwrite(mp3_buffer, 1, wroteSize, mp3File);
    }

    delete[] buffer;
    delete[] leftBuffer;
    delete[] rightBuffer;
    delete[] mp3_buffer;
}

void Mp3Encoder::Destory() {
    if (pcmFile) {
        fclose(pcmFile);
    }

    if (mp3File) {
        fclose(mp3File);
        lame_close(lameClient);
    }

}

Mp3Encoder::Mp3Encoder() {

}

Mp3Encoder::~Mp3Encoder() {

}
