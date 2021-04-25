package cn.idu.learnaudio

class Mp3Encoder {
    external fun init(
        pcmPath: String,
        audioChannels: Int,
        bitRate: Int,
        sampleRate: Int,
        mp3Path: String
    );

    external fun encode();
    external fun destory();
}