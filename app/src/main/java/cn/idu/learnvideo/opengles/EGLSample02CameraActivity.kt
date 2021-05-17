package cn.idu.learnvideo.opengles

import android.graphics.ImageFormat
import android.hardware.Camera
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import cn.idu.glrenderer.texture.CameraTexture
import cn.idu.learnvideo.databinding.ActivityEGLSample02CameraBinding
import cn.idu.learnvideo.databinding.ActivityGlSample03CameraBinding
import cn.idu.learnvideo.mp.codec.CodecListener
import cn.idu.learnvideo.mp.codec.encoder.VideoEncoder
import cn.idu.learnvideo.opengles.egl.EGLRenderer
import cn.idu.learnvideo.opengles.renderer.SampleRenderer
import cn.readsense.module.base.BaseCoreActivity
import cn.readsense.module.util.DLog
import java.io.File
import java.nio.ByteBuffer

class EGLSample02CameraActivity : BaseCoreActivity() {
    lateinit var binding: ActivityEGLSample02CameraBinding
    lateinit var eglRenderer: EGLRenderer
    var videoEncoder: VideoEncoder? = null

    var camera: Camera? = null
    var capture = false
    override fun getLayoutView(): View {
        binding = ActivityEGLSample02CameraBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        eglRenderer = EGLRenderer()


        val videoTexture = CameraTexture()
        videoTexture.setTextureSize(480, 640)
        videoTexture.getSurfaceTexture { surfaceTexture ->
            surfaceTexture.setOnFrameAvailableListener {
                eglRenderer.updateTexImage()
            }
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
            val parameters = camera!!.parameters
            parameters!!.setPreviewSize(640, 480)
            camera?.parameters = parameters
            camera?.setPreviewTexture(surfaceTexture)
            camera?.addCallbackBuffer(
                ByteArray(
                    640 * 480 * ImageFormat.getBitsPerPixel(
                        ImageFormat.NV21
                    ) / 8
                )
            )
            camera?.setPreviewCallbackWithBuffer { data: ByteArray, camera: Camera ->
                camera.addCallbackBuffer(data)
                if (capture)
                    videoEncoder?.putBuf(data, 0, data.size)
            }
            camera?.startPreview()
        }

        eglRenderer.addTexture(videoTexture)

        eglRenderer.setSurfaceView(binding.surfaceview)

        binding.surfaceview.setOnClickListener {
            if (!capture) {
                //定义视频编码器并启动
                videoEncoder = VideoEncoder()
                videoEncoder?.setUpVideoCodec(640, 480)
                videoEncoder?.start()
                val saveMp4Path = "${filesDir}/test.mp4"
                File(saveMp4Path).deleteOnExit()
                //定义混合器：输出并保存h.264码流为mp4
                val mediaMuxer =
                    MediaMuxer(
                        saveMp4Path,
                        MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
                    )
                var videoTrackIndex = -1

                videoEncoder?.setCodecListener(object : CodecListener {
                    override fun formatUpdate(format: MediaFormat) {
                        DLog.d("formatUpdate")
                        videoTrackIndex = mediaMuxer.addTrack(format)
                        mediaMuxer.start()
                    }

                    override fun bufferUpdate(
                        buffer: ByteBuffer,
                        bufferInfo: MediaCodec.BufferInfo
                    ) {
                        DLog.d("bufferUpdate")
                        mediaMuxer.writeSampleData(videoTrackIndex, buffer, bufferInfo)
                    }

                    override fun bufferOutputEnd() {
                        DLog.d("bufferOutputEnd")
                        mediaMuxer.release()
                        videoEncoder?.stopWorld()
                    }
                })
                capture = true
            } else {
                capture = false
                videoEncoder?.putBufEnd()
            }
        }
    }

    override fun onDestroy() {
        camera?.stopPreview()
        camera?.release()
        super.onDestroy()
    }
}