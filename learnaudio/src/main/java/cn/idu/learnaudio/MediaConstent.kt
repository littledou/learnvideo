package cn.idu.learnaudio

import android.media.AudioFormat

const val SAMPLE_RATE_IN_HZ = 44100 //采样率44.1KHz
const val CHANNEL = AudioFormat.CHANNEL_IN_MONO //单声道，立体声：CHANNEL_IN_STEREO
const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT //每个采样点16bit
