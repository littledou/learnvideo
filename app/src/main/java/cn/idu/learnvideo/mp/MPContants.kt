package cn.idu.learnvideo.mp

import android.media.AudioFormat


const val SAMPLE_RATE_IN_HZ = 44100 //采样率44.1KHz
const val CHANNEL = AudioFormat.CHANNEL_IN_MONO //单声道，立体声：CHANNEL_IN_STEREO
const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT //每个采样点16bit
const val DEST_BIT_RATE = 128000 //编码码率

const val NAL_SLICE = 1 //非关键帧
const val NAL_SLICE_DPA = 2
const val NAL_SLICE_DPB = 3
const val NAL_SLICE_DPC = 4
const val NAL_SLICE_IDR = 5 //关键帧
const val NAL_SEI = 6
const val NAL_SPS = 7 //SPS帧
const val NAL_PPS = 8 //PPS帧
const val NAL_AUD = 9
const val NAL_FILLER = 12