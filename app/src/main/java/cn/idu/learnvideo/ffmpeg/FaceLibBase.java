package cn.idu.learnvideo.ffmpeg;

import android.util.Log;

import androidx.annotation.NonNull;

import cn.idu.learnvideo.main.ExApplication;
import readsense.api.core.RSAlign;
import readsense.api.core.RSDeepFace;
import readsense.api.core.RSDetect;
import readsense.api.core.RSDetectDark;
import readsense.api.core.RSFaceAttr;
import readsense.api.core.RSFaceAvailability;
import readsense.api.core.RSFaceQuality;
import readsense.api.core.RSFaceRecognition;
import readsense.api.core.RSHelmet;
import readsense.api.core.RSLicense;
import readsense.api.core.RSLivenessDetect;
import readsense.api.core.RSOcclusion;
import readsense.api.core.RSTrack;

/**
 * @author liyaotang
 * @date 2021-02-25
 * <p>
 * 管理算法模块实例，初始化/反初始化
 * <p>
 * 从方法名看到，有一类型的方法
 * rsXXX()：方法返回算法模块的实例，如果实例未初始化即马上初始化并返回实例
 * initXXX()：初始化算法模块实例，通过本类的属性，可以直接引用算法实例
 * releaseXXX()：释放算法模块实例，
 * <p>
 * 特别注意，人脸识别算法模块 {@link #rsFaceRecognition}，如果注册了很多人，初始化比较耗时，所以
 * {@link #rsFaceRecognition()} 是不会初始化识别算法实例，需要提前调用
 * <p>
 * rsXXX() 的设计用意，使用算法模块实例无需额外调用初始化算法的方法，
 * 通过 rsXXX() 方法可以直接使用算法模块的实例，而且是自动按需初始化算法模块实例
 */
public class FaceLibBase {

    private final String TAG = getClass().getSimpleName();

    RSLicense rsLicense = ExApplication.getRsLicense();

    /**
     * 追踪模块
     */
    RSTrack rsTrack = null;
    /**
     * 检测模块
     */
    RSDetect rsDetect = null;
    /**
     * 背光检测模块，能够检测到更远，但精度相对低
     */
    RSDetectDark rsDetectDark = null;
    /**
     * 遮挡检测
     */
    RSOcclusion rsOcclusion = null;
    /**
     * 质量模块
     */
    RSFaceQuality rsFaceQuality = null;
    /**
     * 活体模块
     */
    RSLivenessDetect rsLivenessDetect = null;
    /**
     * ir850活体模块
     */
    RSLivenessDetect rsLivenessDetect850 = null;
    /**
     * ir940活体模块
     */
    RSLivenessDetect rsLivenessDetect940 = null;
    /**
     * 识别模块（包含特征管理）
     */
    RSFaceRecognition rsFaceRecognition = null;
    /**
     * 特征提取模块
     */
    RSDeepFace rsDeepFace = null;
    /**
     * 人脸属性
     */
    RSFaceAttr rsFaceAttr = null;
    /**
     * 判断人脸适用于识别活体
     */
    RSFaceAvailability rsFaceAvailability = null;
    /**
     * 安全帽检测模块
     */
    RSHelmet rsHelmet = null;
    /**
     * 清晰度模糊度
     */
    RSAlign rsAlign = null;

    public @NonNull
    RSTrack rsTrack() {
        if (rsTrack == null) initRsTrack();
        return rsTrack;
    }

    public @NonNull
    RSDetect rsDetect() {
        if (rsDetect == null) initRsDetect();
        return rsDetect;
    }

    public @NonNull
    RSDetectDark rsDetectDark() {
        if (rsDetectDark == null) initRsDetectDark();
        return rsDetectDark;
    }

    public @NonNull
    RSOcclusion rsOcclusion() {
        if (rsOcclusion == null) initRsOcclusion();
        return rsOcclusion;
    }

    public @NonNull
    RSFaceQuality rsFaceQuality() {
        if (rsFaceQuality == null) initRsFaceQuality();
        return rsFaceQuality;
    }

    public @NonNull
    RSLivenessDetect rsLivenessDetect() {
        if (rsLivenessDetect == null) initRsLivenessRgb();
        return rsLivenessDetect;
    }

    public @NonNull
    RSLivenessDetect rsLivenessDetect850() {
        if (rsLivenessDetect850 == null) initRsLiveness850();
        return rsLivenessDetect850;
    }

    public @NonNull
    RSLivenessDetect rsLivenessDetect940() {
        if (rsLivenessDetect940 == null) initRsLiveness940();
        return rsLivenessDetect940;
    }


    public void initRecog(String path) {
        if (rsFaceRecognition == null) {
            rsFaceRecognition = new RSFaceRecognition(rsLicense, path);
            rsFaceRecognition.init();
        }
    }

    public RSFaceRecognition rsFaceRecognition() {
        return rsFaceRecognition;
    }

    public @NonNull
    RSDeepFace rsDeepFace() {
        if (rsDeepFace == null) initRsDeepFace();
        return rsDeepFace;
    }

    public @NonNull
    RSFaceAttr rsFaceAttr() {
        if (rsFaceAttr == null) initRsFaceAttr();
        return rsFaceAttr;
    }

    public @NonNull
    RSFaceAvailability rsFaceAvailability() {
        if (rsFaceAvailability == null) initRsFaceAvailability();
        return rsFaceAvailability;
    }

    public @NonNull
    RSHelmet rsHelmet() {
        if (rsHelmet == null) initRsHelmet();
        return rsHelmet;
    }

    public @NonNull
    RSAlign rsAlign() {
        if (rsAlign == null) initRsAlign();
        return rsAlign;
    }

    public long initRsDeepFace() {
        if (rsDeepFace == null)
            rsDeepFace = new RSDeepFace(rsLicense);
        rsDeepFace.init();
        return rsDeepFace.handle;
    }

    public long initRsLivenessRgb() {
        if (rsLivenessDetect == null)
            rsLivenessDetect = new RSLivenessDetect(rsLicense);
        rsLivenessDetect.init();
        return rsLivenessDetect.handle;
    }

    public long initRsLiveness850() {
        if (rsLivenessDetect850 == null)
            rsLivenessDetect850 = new RSLivenessDetect(rsLicense);
        rsLivenessDetect850.initInfrared();
        return rsLivenessDetect850.handle;
    }

    public long initRsLiveness940() {
        if (rsLivenessDetect940 == null)
            rsLivenessDetect940 = new RSLivenessDetect(rsLicense);
        rsLivenessDetect940.initInfrared940();
        return rsLivenessDetect940.handle;
    }

    public long initRsFaceQuality() {
        if (rsFaceQuality == null)
            rsFaceQuality = new RSFaceQuality(rsLicense);
        rsFaceQuality.init();
        return rsFaceQuality.handle;
    }

    public long initRsDetect() {
        if (rsDetect == null)
            rsDetect = new RSDetect(rsLicense);
        rsDetect.init();
        return rsDetect.handle;
    }

    public long initRsDetectDark() {
        if (rsDetectDark == null)
            rsDetectDark = new RSDetectDark(rsLicense);
        rsDetectDark.init();
        return rsDetectDark.handle;
    }

    public long initRsOcclusion() {
        if (rsOcclusion == null)
            rsOcclusion = new RSOcclusion(rsLicense);
        rsOcclusion.init();
        return rsOcclusion.handle;
    }

    public long initRsTrack() {
        if (rsTrack == null)
            rsTrack = new RSTrack(rsLicense);
        rsTrack.init();
        return rsTrack.handle;
    }

    public long initRsFaceAttr() {
        if (rsFaceAttr == null)
            rsFaceAttr = new RSFaceAttr(rsLicense);
        rsFaceAttr.init();
        return rsFaceAttr.handle;
    }

    public long initRsFaceAvailability() {
        if (rsFaceAvailability == null)
            rsFaceAvailability = new RSFaceAvailability(rsLicense);
        rsFaceAvailability.init();
        return rsFaceAvailability.handle;
    }

    public long initRsHelmet() {
        if (rsHelmet == null)
            rsHelmet = new RSHelmet(rsLicense);
        rsHelmet.init();
        return rsHelmet.handle;
    }

    public long initRsAlign() {
        if (rsAlign == null)
            rsAlign = new RSAlign(rsLicense);
        rsAlign.init();
        return rsAlign.handle;
    }

    public void releaseRsDeepFace() {
        if (rsDeepFace != null) {
            rsDeepFace.unInit();
            rsDeepFace = null;
        }
    }

    public void releaseRsFaceRecognition() {
        if (rsFaceRecognition != null) {
            rsFaceRecognition.unInit();
            rsFaceRecognition = null;
        }
    }

    public void releaseRsLiveness() {
        if (rsLivenessDetect != null) {
            rsLivenessDetect.unInit();
            rsLivenessDetect = null;
        }
    }

    public void releaseRsLiveness850() {
        if (rsLivenessDetect850 != null) {
            rsLivenessDetect850.unInit();
            rsLivenessDetect850 = null;
        }
    }

    public void releaseRsLiveness940() {
        if (rsLivenessDetect940 != null) {
            rsLivenessDetect940.unInit();
            rsLivenessDetect940 = null;
        }
    }

    public void releaseRsFaceQuality() {
        if (rsFaceQuality != null) {
            rsFaceQuality.unInit();
            rsFaceQuality = null;
        }
    }

    public void releaseRsDetect() {
        if (rsDetect != null) {
            rsDetect.unInit();
            rsDetect = null;
        }
    }

    public void releaseRsDetectDark() {
        if (rsDetectDark != null) {
            rsDetectDark.unInit();
            rsDetectDark = null;
        }
    }

    public void releaseRsOcclusion() {
        if (rsOcclusion != null) {
            rsOcclusion.unInit();
            rsOcclusion = null;
        }
    }

    public void releaseRsTrack() {
        if (rsTrack != null) {
            rsTrack.unInit();
            rsTrack = null;
        }
    }

    public void releaseRsFaceAttr() {
        if (rsFaceAttr != null) {
            rsFaceAttr.unInit();
            rsFaceAttr = null;
        }
    }

    public void releaseRsFaceAvailability() {
        if (rsFaceAvailability != null) {
            rsFaceAvailability.unInit();
            rsFaceAvailability = null;
        }
    }

    public void releaseRsHelmet() {
        if (rsHelmet != null) {
            rsHelmet.unInit();
            rsHelmet = null;
        }
    }

    public void releaseRsAlign() {
        if (rsAlign != null) {
            rsAlign.unInit();
            rsAlign = null;
        }
    }

    /**
     * 释放所有已经初始化的实例
     */
    public void releaseAll() {
        releaseRsDeepFace();
        releaseRsDetect();
        releaseRsTrack();
        releaseRsDetectDark();
        releaseRsFaceAttr();
        releaseRsFaceAvailability();
        releaseRsFaceQuality();
        releaseRsFaceRecognition();
        releaseRsHelmet();
        releaseRsLiveness();
        releaseRsLiveness850();
        releaseRsLiveness940();
        releaseRsOcclusion();
        releaseRsAlign();
    }

}
