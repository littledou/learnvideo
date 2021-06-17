package cn.idu.learnvideo.ffmpeg;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.idu.facecommon.FaceExtensionKt;
import cn.idu.learnvideo.main.ExApplication;
import cn.readsense.module.util.BitmapUtil;
import cn.readsense.module.util.DLog;
import readsense.api.enity.YMFace;

/**
 * 人脸识别流程
 * 流程一《本示例》
 * 1. 可见光活体识别，共识别5次，超过5次仍为非活体，则认为非活体
 * 2. 人脸识别，共识别5次，超过5次仍未识别，则不认识此人
 * <p>
 * 流程二《可扩展》
 * 1：可见光活体识别，非活体则一直识别，直至活体通过则开始人脸识别
 * 2：人脸识别，一直识别，直至识别通过结束，否则不结束
 */
public class FaceInfoViewModel extends ViewModel {
    private static final String TAG = "Camera2ViewModel";

    private final FaceLib faceLib = new FaceLib();

    public AppParams params;
    private boolean threadRunning = true;

    /**
     * 人脸识别
     */
    private FaceData faceDataRecognition;

    /**
     * todo 把向外推送的数据，和本类里面的逻辑 flag 分析
     */
    public MutableLiveData<FaceInfoFlag> faceInfoFlag = new MutableLiveData<>();

    /**
     * 用户应用数据库，存放(personId, userName)键值对
     */
    private Map<Integer, String> userMap = new HashMap<>();

    public void init(Context context) throws IOException {

        faceInfoFlag.setValue(new FaceInfoFlag(0));

        // 加载(personId, userName)的数据库
//        new Thread(() -> userMap = new UserRepository(application).getAllUser()).start();

        // 初始化人脸识别算法模块，必须在这里提前初始化
        faceLib.initRecog(context.getExternalFilesDir("img").getAbsolutePath());

//        Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets().open("1080x1442.jpg"));
//        List<YMFace> faces = faceLib.rsDetect().runDetect(bitmap, 0);
//        if (faces != null && faces.size() > 0) {
//            float[] feature = faceLib.rsDeepFace().getDeepFaceFeature(bitmap, 0, faces.get(0).getRect());
//            faceLib.rsFaceRecognition().resetAlbum();
//            faceLib.rsFaceRecognition().personCreate(feature);
//            DLog.d("db size" + faceLib.rsFaceRecognition().getAlbumSize());
//        } else {
//            DLog.d("register null");
//        }

        // 初始化需要的算法模块，可以不提前初始化算法模块，第一次调用即初始化
        /*
        faceLib.initRsTrack();
        faceLib.initRsDeepFace();
        faceLib.initRsLiveness();
        faceLib.initRsLiveness850();
        faceLib.initRsOcclusion();
        faceLib.initRsAlign();
         */

        params = AppParams.getInstance();

        faceDataRecognition = new FaceData("recognition", params.prewviewWidth, params.prewviewHeight) {
            @Override
            public void run() {
                super.run();
                while (threadRunning) {
                    FaceInfoFlag faceInfo = faceInfoFlag.getValue();
                    // 如果识别通过，则不再识别
                    if (faceInfo.post) continue;

                    FaceInfo info;
                    try {
                        info = getBuffer();
                    } catch (InterruptedException e) {
                        break;
                    }
                    byte[] data = info.data;
                    float[] rect = info.rect;


                    if (!faceInfo.post) {
//                        if (!faceInfo.colorLivenessInfo.isOk()) {//人脸活体判定
//                            long time = System.currentTimeMillis();
//                            int livenessResult = faceLib.rgbLivenessDetect(
//                                    data,
//                                    params.prewviewWidth,
//                                    params.prewviewHeight,
//                                    rect,
//                                    params.dataOrientation
//                            );
//                            DLog.d("rgbLivenessDetect cost: " + (System.currentTimeMillis() - time) + " ms , ret: " + livenessResult);
//
//                            if (livenessResult == 1) {
//                                faceInfo.colorLivenessInfo.setOK();
//                            } else {
//                                faceInfo.colorLivenessInfo.increase();
//                            }
//                        }
                        if (!faceInfo.recoInfo.isOk()) {//人脸识别判定
                            long time = System.currentTimeMillis();
                            // 双模型识别
                            YMFace face = faceLib.recognize(
                                    data,
                                    params.prewviewWidth,
                                    params.prewviewHeight,
                                    rect,
                                    params.dataOrientation
                            );
                            faceInfo.confidence = face.getConfidence();
                            faceInfo.personId = face.getPersonId();
                            DLog.d("recognizeTwoModel cost: " + (System.currentTimeMillis() - time) + " ms , confidence: " + face.getConfidence() + " personId:" + faceInfo.personId);

                            // 判断识别是否通过
                            if (face.getConfidence() >= 75 && face.getPersonId() > 0) {
                                faceInfo.recoInfo.setOK();
                                // 通过 personId，查找数据库，找到对应的 faceInfo.userName
                                String userName = userMap.get(faceInfo.personId);
                                if (userName != null) faceInfo.userName = userName;
                            } else {
                                // 累计不通过的次数
                                faceInfo.recoInfo.increase();
                            }
                        }

                        //---判定是否需要推送消息---
                        /*
                         * 1. 活体和识别在指定识别次数内都成功了
                         * 2. 活体在指定次数内识别成功，识别未成功
                         * 3. 活体未成功，识别在指定次数内识别成功
                         * 4. 活体识别在指定次数内均未成功
                         */
                        if (faceInfo.recoInfo.canPost()) {
                            faceInfo.post = true;
                            faceInfoFlag.postValue(faceInfo);
                        }
                    }
                }

            }
        };

        faceDataRecognition.start();

        Objects.requireNonNull(faceInfoFlag.getValue()).clear(-1);
    }


    YMFace putColorBuffer(byte[] data) {
        List<YMFace> faces = faceLib.track(
                data,
                params.prewviewWidth,
                params.prewviewHeight,
                params.dataOrientation
        );
        if (!faces.isEmpty()) {
            // 只对最大人脸，跑识别和活体
            YMFace face = FaceExtensionKt.findMaxFace(faces);
            if (FaceExtensionKt.faceInScreenCenter(face,
                    params.prewviewWidth,
                    params.prewviewHeight,
                    params.widthIsWidth)) {
                if (FaceExtensionKt.faceIsPositive(face)) {
                    // 获取人脸的模糊度和亮度
                    YMFace newFace = faceLib.getFaceBlurAndBrightness(
                            data,
                            params.prewviewWidth,
                            params.prewviewHeight,
                            params.dataOrientation,
                            face.getRect()
                    );
                    face.setBlur(newFace.getBlur());
                    face.setBrightness(newFace.getBrightness());

                    if (FaceExtensionKt.faceIsClear(face)) {

                        // 新的人脸，新的 trackId，清空上一个 trackId 相关的信息
                        if (faceInfoFlag.getValue().trackId != face.getTrackId()) {
                            faceDataRecognition.clear();
                            faceInfoFlag.getValue().clear(face.getTrackId());
                        }
                        int trackId = face.getTrackId();
                        float[] rect = face.getRect();
                        float[] landmarks = face.getLandmarks();
                        faceDataRecognition.putBuffer(data, trackId, rect, landmarks);
                    }
                }
            }
            return face;
        } else {
            return null;
        }
    }


    public class FaceInfoFlag {
        public int trackId = -1;
        public int personId = -1;
        public int confidence = -1;
        public String userName = "";

        public FaceInfoFlag(int trackId) {
            this.trackId = trackId;
        }

        public Info recoInfo = new Info(0, 0);
        public Info colorLivenessInfo = new Info(0, 0);

        public long timeIn = -1L;
        public boolean post = false;

        void clear(int trackId) {
            this.trackId = trackId;
            post = false;
            recoInfo.clear();
            colorLivenessInfo.clear();
            timeIn = System.currentTimeMillis();

            personId = -1;
            confidence = -1;
            userName = "";
        }

        public class Info {

            int count;
            int flag;

            public Info(int count, int flag) {
                this.count = count;
                this.flag = flag;
            }

            private final static int SUCCESS = 1;
            private final static int LIMIT = 5;


            void clear() {
                count = 0;
                flag = 0;
            }

            void increase() {
                count += 1;
            }

            void setOK() {
                flag = SUCCESS;
            }

            boolean canPost() {
                return count >= LIMIT || flag == SUCCESS;
            }

            public boolean isOk() {
                return flag == SUCCESS;
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        threadRunning = false;
        if (faceDataRecognition.isAlive()) faceDataRecognition.interrupt();
        faceDataRecognition.clear();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        faceLib.releaseAll();
    }
}
