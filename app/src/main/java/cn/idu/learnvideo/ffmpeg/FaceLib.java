package cn.idu.learnvideo.ffmpeg;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.common.util.ColorFormatUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.readsense.module.util.BitmapUtil;
import cn.readsense.module.util.DLog;
import readsense.api.enity.YMFace;
import readsense.api.enity.YMPerson;
import readsense.api.info.RSConstant;

/**
 * @author liyaotang
 * @date 2021-02-25
 * <p>
 * 算法最基础的接口，这里只涉及算法相关的逻辑，不涉及应用的逻辑
 */
public class FaceLib extends FaceLibBase {

    private static final String TAG = "FaceSetBase2Impl";

    public YMFace getAttr(Object t, int iw, int ih, int imageRotate, float[] rect, float[] landmarks) {
        if (t == null) return null;

        float[] attr = (t instanceof Bitmap) ? rsFaceAttr().rsRunFaceAttrBitmap((Bitmap) t, imageRotate, rect, landmarks) : (t instanceof byte[])
                ? rsFaceAttr().runFaceAttr((byte[]) t, iw, ih, imageRotate, rect, landmarks) : null;
        if (attr == null) return null;

        YMFace face = new YMFace();
        face.setAge((int) attr[2]);
        face.setGender((attr[1] > 90) ? (int) attr[0] : -1);

        return face;
    }

    /**
     * 人脸跟踪，
     *
     * @param iw
     * @param ih
     * @param imageRotate
     * @param t           byte[], invoke runTrack(); Bitmap, invoke runDetect()
     * @param isMulti     是否跟踪多张人脸
     * @return
     */
    public List<YMFace> track(int iw, int ih, int imageRotate, Object t, boolean isMulti) {

        if (null == t) return null;

        List<YMFace> ymFaces;

        if (t instanceof Bitmap) {
            ymFaces = rsDetect().runDetect((Bitmap) t, imageRotate);
        } else if (t instanceof byte[]) {
            byte[] bytes = (byte[]) t;
            byte[] data = new byte[bytes.length];
            System.arraycopy(bytes, 0, data, 0, bytes.length);

            ymFaces = rsTrack().runTrack(data, iw, ih, imageRotate);
        } else {
            return null;
        }

        if (!isMulti && ymFaces.size() > 1) {
            //找到最大人脸框
            int maxIndex = 0;
            for (int i = 1; i < ymFaces.size(); i++) {
                if (ymFaces.get(maxIndex).getRect()[2] <= ymFaces.get(i).getRect()[2]) {
                    maxIndex = i;
                }
            }

            YMFace ymFaceMax = ymFaces.get(maxIndex);
            ymFaces.clear();
            ymFaces.add(ymFaceMax);
        }

        return ymFaces;
    }

    /**
     * 人脸跟踪，
     *
     * @param iw
     * @param ih
     * @param imageRotate
     * @return
     */
    public List<YMFace> track(byte[] data, int iw, int ih, int imageRotate) {

        if (null == data) return null;

        return rsTrack().runTrack(data, iw, ih, imageRotate);

    }


    /**
     * 人脸检测
     *
     * @param t       byte[], invoke runTrack(); Bitmap, invoke runDetect()
     * @param isMulti 是否跟踪多张人脸；false，返回最大人脸
     * @return
     */
    public List<YMFace> faceDetect(int iw, int ih, int imageRotate, Object t, boolean isMulti) {

        List<YMFace> ymFaces = faceDetect(iw, ih, imageRotate, t);

        if (!isMulti && ymFaces != null && !ymFaces.isEmpty()) {
            //找到最大人脸框
            int maxIndex = 0;
            for (int i = 1; i < ymFaces.size(); i++) {
                if (ymFaces.get(maxIndex).getRect()[2] <= ymFaces.get(i).getRect()[2]) {
                    maxIndex = i;
                }
            }

            YMFace ymFaceMax = ymFaces.get(maxIndex);
            ymFaces.clear();
            ymFaces.add(ymFaceMax);
        }

        return ymFaces;
    }

    /**
     * onestage人脸检测
     *
     * @param t       byte[], invoke runTrack(); Bitmap, invoke runDetect()
     * @param isMulti 是否跟踪多张人脸；false，返回最大人脸
     * @return
     */
    public List<YMFace> faceDetectDark(int iw, int ih, int imageRotate, Object t, boolean isMulti) {
        List<YMFace> ymFaces = faceDetectDark(iw, ih, imageRotate, t);

        if (!isMulti && ymFaces != null && !ymFaces.isEmpty()) {
            //找到最大人脸框
            int maxIndex = 0;
            for (int i = 1; i < ymFaces.size(); i++) {
                if (ymFaces.get(maxIndex).getRect()[2] <= ymFaces.get(i).getRect()[2]) {
                    maxIndex = i;
                }
            }

            YMFace ymFaceMax = ymFaces.get(maxIndex);
            ymFaces.clear();
            ymFaces.add(ymFaceMax);
        }

        return ymFaces;
    }

    /**
     * 人脸检测
     *
     * @param t bitmap对象或者byte[]
     * @return
     */
    public List<YMFace> faceDetect(int iw, int ih, int imageRotate, Object t) {

        if (null == t) return null;

        List<YMFace> ymFaces;

        if (t instanceof Bitmap) {
            ymFaces = rsDetect().runDetect((Bitmap) t, imageRotate);
        } else if (t instanceof byte[]) {

            byte[] bytes = (byte[]) t;
            byte[] data = new byte[bytes.length];
            System.arraycopy(bytes, 0, data, 0, bytes.length);

            ymFaces = rsDetect().runDetect(data, iw, ih, imageRotate);
        } else {
            return null;
        }

        return ymFaces;
    }

    /**
     * 图像分割检测，人脸检测
     * 支持检测更小的人脸，即支持更远的检测距离，测试效果30px的人脸，也能稳定检测
     */
    public List<YMFace> faceDetectSegment(int iw, int ih, int imageRotate, Object t) {

        if (null == t) return null;

        // 最终返回的人脸集合
        List<YMFace> faces = new ArrayList<>();

        // 位移宽
        int STRIDE_WIDTH = iw / 4;
        // 位移高
        int STRIDE_HEIGHT = ih / 4;
        // 分割宽度
        int SEGMENT_WIDTH = iw / 2;
        // 分割宽度
        int SEGMENT_HEIGHT = ih / 2;

        // 分割区域的集合
        int[][] segmentRects = {
                // 一整帧
                {0, 0, iw, ih},

                // 裁剪区域检测，长宽(w/2 , h/2)，水平平移步长是 w/4，垂直平移步长 h/4，
                // 总共9个裁剪区域
                {0, 0, SEGMENT_WIDTH, SEGMENT_HEIGHT},
                {0, STRIDE_HEIGHT, SEGMENT_WIDTH, SEGMENT_HEIGHT},
                {0, STRIDE_HEIGHT * 2, SEGMENT_WIDTH, SEGMENT_HEIGHT},
                {STRIDE_WIDTH, 0, SEGMENT_WIDTH, SEGMENT_HEIGHT},
                {STRIDE_WIDTH, STRIDE_HEIGHT, SEGMENT_WIDTH, SEGMENT_HEIGHT},
                {STRIDE_WIDTH, STRIDE_HEIGHT * 2, SEGMENT_WIDTH, SEGMENT_HEIGHT},
                {STRIDE_WIDTH * 2, 0, SEGMENT_WIDTH, SEGMENT_HEIGHT},
                {STRIDE_WIDTH * 2, STRIDE_HEIGHT, SEGMENT_WIDTH, SEGMENT_HEIGHT},
                {STRIDE_WIDTH * 2, STRIDE_HEIGHT * 2, SEGMENT_WIDTH, SEGMENT_HEIGHT},
        };

        int replaceCount = 0, newCount = 0;

        // 对每个分割区域进行人脸检测
        // 区域之间的人脸集合，对比检查重复的人脸，排除重复人脸
        for (int[] segmentRect : segmentRects) {

            List<YMFace> ymFaces = segmentFaceDetect(iw, ih, imageRotate, t, segmentRect);
            Log.e(TAG, "faceDetectSegment: ymFaces" + (ymFaces == null ? null : ymFaces.size()));
            //faces.addAll(ymFaces);

            if (ymFaces == null || ymFaces.isEmpty()) continue;

            if (faces.isEmpty()) {
                // 第一次添加
                faces.addAll(ymFaces);
            } else {
                // 新增的人脸的下标
                List<Integer> newIndex = new ArrayList<>();
                // 检查重复的人脸
                for (int i = 0; i < ymFaces.size(); i++) {
                    YMFace newFace = ymFaces.get(i);
                    int i1;
                    for (i1 = 0; i1 < faces.size(); i1++) {
                        YMFace originalFace = faces.get(i1);
                        int result = checkFace(originalFace, newFace);
                        if (result == 1) {
                            // 是同一张脸，且更新人脸
                            faces.set(i1, newFace);
                            replaceCount++;
                            break;
                        } else if (result == 2) {
                            // 不是同一张脸
                        } else if (result == 3) {
                            // 是同一张脸，不更新人脸
                            break;
                        }
                    }

                    // 遍历原来的脸，没有找到同一张脸，人脸这是新的人脸
                    if (i1 == faces.size()) {
                        newIndex.add(i);
                        newCount++;
                    }
                }

                // 逐一把新增的人脸加到集合当中
                for (Integer index : newIndex) {
                    faces.add(ymFaces.get(index));
                }
            }
        }

        Log.e(TAG, "faceDetectSegment: newCount" + newCount + " replaceCount" + replaceCount);

        return faces;
    }

    /**
     * 对图像数据 t，截取区域 rect，进行人脸检测，返回人脸检测结果
     * 人脸坐标是原图内的坐标
     **/
    private List<YMFace> segmentFaceDetect(int iw, int ih, int imageRotate, Object t, int[] segmentRect) {
        if (null == t || segmentRect == null) return null;

        List<YMFace> ymFaces;

        if (t instanceof Bitmap) {
            Bitmap bitmap = (Bitmap) t;
            bitmap = Bitmap.createBitmap(bitmap, segmentRect[0] * 2 / 2, segmentRect[1] * 2 / 2,
                    segmentRect[2] * 2 / 2, segmentRect[3] * 2 / 2);
            ymFaces = rsDetect().runDetect(bitmap, imageRotate);
        } else if (t instanceof byte[]) {
            byte[] bytes = (byte[]) t;
            byte[] data = new byte[bytes.length];
            System.arraycopy(bytes, 0, data, 0, bytes.length);
            data = ColorFormatUtil.cropNV21(data, iw, ih, segmentRect);

            ymFaces = rsDetect().runDetect(data, segmentRect[2], segmentRect[3], imageRotate);
        } else {
            return null;
        }

        if (ymFaces == null || ymFaces.isEmpty()) return ymFaces;

        // 重定向到原图的坐标
        for (int i = 0; i < ymFaces.size(); i++) {
            float[] rect2 = ymFaces.get(i).getRect();
            rect2[0] += segmentRect[0];
            rect2[1] += segmentRect[1];
        }

        return ymFaces;
    }

    /**
     * 检查两张人脸的关系
     *
     * @return 1，同一张脸，且更为新脸；2，不是同一张脸；3，同一张脸，但使用旧脸
     */
    @SuppressLint({"NewApi", "LocalSuppress"})
    private int checkFace(YMFace originalFace, YMFace newFace) {
        float[] rect = originalFace.getRect();
        float[] rect2 = newFace.getRect();

        float acreage1 = (int) (rect[2] * rect[3]);
        float acreage2 = (int) (rect2[2] * rect2[3]);
        float minAcreage = Math.min(acreage1, acreage2);

        float overlapAcreage = overlapAcreage(rect, rect2);

        float proportion = 0.3f;
        // 重合面积，超过较小人脸框面积一定比例，则认为同一张人脸，使用面积较大的人脸框信息
        if (overlapAcreage / minAcreage >= proportion) {
            return acreage1 >= acreage2 ? 3 : 1;
        } else {
            return 2;
        }
    }

    /**
     * 计算两个矩形的重合面积，坐标含义 {x0, y0, width, height}
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private float overlapAcreage(float[] rect, float[] rect2) {

        // 两个矩形的四个水平坐标
        List<Float> horizontal = new ArrayList<>();
        horizontal.add(rect[0]);
        horizontal.add((rect[0] + rect[2]));
        horizontal.add(rect2[0]);
        horizontal.add((rect2[0] + rect2[2]));

        // 两个矩形的四个垂直坐标
        List<Float> vertical = new ArrayList<>();
        vertical.add(rect[1]);
        vertical.add((rect[1] + rect[3]));
        vertical.add(rect2[1]);
        vertical.add((rect2[1] + rect2[3]));

        // 判定是否重合
        if ((horizontal.get(1) <= horizontal.get(2)) || (horizontal.get(0) >= horizontal.get(3)))
            return 0f;
        if ((vertical.get(1) <= vertical.get(2)) || (vertical.get(0) >= vertical.get(3))) return 0f;

        // 水平四个坐标排序
        Collections.sort(horizontal, (o1, o2) -> o1 >= o2 ? -1 : 1);
        // 垂直四个坐标排序
        Collections.sort(vertical, (o1, o2) -> o1 >= o2 ? -1 : 1);

        // 计算重合面积
        return Math.abs(vertical.get(1) - vertical.get(2)) *
                Math.abs(horizontal.get(1) - horizontal.get(2));
    }

    /**
     * 人脸检测 onestage，检测距离远，精度低
     *
     * @param iw
     * @param ih
     * @param imageRotate
     * @param t           bitmap对象或者byte[]
     * @return
     */
    public List<YMFace> faceDetectDark(int iw, int ih, int imageRotate, Object t) {

        if (null == t) return null;

        List<YMFace> ymFaces = null;

        if (t instanceof Bitmap) {
            // todo 需要JNI支持图片检测
        } else if (t instanceof byte[]) {

            byte[] bytes = (byte[]) t;
            byte[] data = new byte[bytes.length];
            System.arraycopy(bytes, 0, data, 0, bytes.length);

            ymFaces = rsDetectDark().runDetect(data, iw, ih, imageRotate);
        } else {
            return null;
        }

        return ymFaces;
    }

    public int getFaceQuality(Object o, int iw, int ih, float[] landmarks, int imageRotate) {
        return (o instanceof byte[]) ? rsFaceQuality().getFaceQuality((byte[]) o, iw, ih, imageRotate, landmarks) : (o instanceof Bitmap) ? rsFaceQuality().getFaceQualityBitmap((Bitmap) o, imageRotate, landmarks) : -1;
    }

    /**
     * 是否有戴安全帽
     */
    public int hasHelmet(Object frame, int iw, int ih, float[] landmarks, int imageRotate) {

        int hasHelmet = -1;
        if (frame instanceof byte[]) {
            hasHelmet = rsHelmet().getHelmet((byte[]) frame, iw, ih, imageRotate, landmarks);
        } else if (frame instanceof Bitmap) {
            Log.e(TAG, "hasHelmet(): Bitmap not support");
        } else {
            Log.e(TAG, "hasHelmet(): data type not support");
        }

        return hasHelmet;
    }

    /**
     * get occlusion for one face
     *
     * @param frame
     * @param iw
     * @param ih
     * @param rect
     * @param imageRotate
     * @return 0，无口罩；1，有口罩；2，未正确佩戴口罩
     */
    public int getOcclusion(byte[] frame, int iw, int ih, float[] rect, int imageRotate) {
        return rsOcclusion().getOcclusion(frame, iw, ih, imageRotate, rect);
    }

    /**
     * getOcclusion for each face
     */
    public List<YMFace> getOcclusion(List<YMFace> ymFaces, int iw, int ih, int imageRotate, Object t) {

        if (null == t || ymFaces == null || ymFaces.isEmpty()) return ymFaces;

        for (YMFace ymFace : ymFaces) {
            int occlusion;
            if (t instanceof Bitmap) {
                occlusion = rsOcclusion().getOcclusionBitmap((Bitmap) t, imageRotate, ymFace.getRect());
            } else if (t instanceof byte[]) {
                byte[] bytes = (byte[]) t;
                occlusion = rsOcclusion().getOcclusion(bytes, iw, ih, imageRotate, ymFace.getRect());
            } else {
                break;
            }
            ymFace.setOcclusion(occlusion);
        }

        return ymFaces;
    }

    /**
     * 遮挡检测
     */
    public int occlusionDetect(Bitmap bitmap, int orientation, float[] rect) {
        if (bitmap == null || rect == null) return -1;
        return rsOcclusion().getOcclusionBitmap(bitmap, orientation, rect);
    }

    /**
     * 遮挡检测
     */
    public List<YMFace> occlusionDetect(Bitmap bitmap, int orientation) {
        List<YMFace> ymFaces = faceDetect(0, 0, orientation, bitmap);
        if (ymFaces == null || ymFaces.size() == 0) return ymFaces;

        for (YMFace face : ymFaces) {
            int occlusion = occlusionDetect(bitmap, orientation, face.getRect());
            face.setOcclusion(occlusion);
        }

        return ymFaces;
    }


    public YMFace faceLiveness(Object o, Object oIr, int iw, int ih, YMFace ymFace, int imageRotate, int type) {
        int result = -1;
        try {
            if (null == o) return ymFace;
            int objectType = (o instanceof Bitmap) ? 0 : (o instanceof byte[]) ? 1 : -1;
            if (objectType == -1) return ymFace;
            switch (type) {
                case 0:  //rgb
                    result = objectType == 0 ? rsLivenessDetect().runLivenessDetectBitmap((Bitmap) o, imageRotate, ymFace.getRect()) :
                            rsLivenessDetect().runLivenessDetect((byte[]) o, iw, ih, imageRotate, ymFace.getRect());
                    break;
                case 1:
                    //ir
                    result = objectType == 0 ? rsLivenessDetect850().runLivenessDetectInfraredBitmap((Bitmap) o, imageRotate, ymFace.getRect()) :
                            rsLivenessDetect850().runLivenessDetectInfrared((byte[]) o, iw, ih, imageRotate, ymFace.getRect());
                    break;
                case 2:
                    if (oIr == null) {
                        result = -1;
                    } else {
                        //双目850
                        result = objectType == 0 ? rsLivenessDetect850().runLivenessDetectInfraredBitmap((Bitmap) o, imageRotate, ymFace.getRect()) :
                                rsLivenessDetect850().runLivenessDetectInfrared((byte[]) oIr, iw, ih, imageRotate, ymFace.getRect());
                    }
                    break;
                case 3:
                    if (oIr == null) {
                        result = -1;
                    } else {
                        //双目940
                        if (objectType == 0)
                            result = rsLivenessDetect940().runLivenessDetectInfraredBitmap940(
                                    (Bitmap) o, imageRotate, ymFace.getRect());
                        else
                            result = rsLivenessDetect940().runLivenessDetectInfrared940(
                                    (byte[]) oIr, iw, ih, imageRotate, ymFace.getRect());
                    }
                    break;
            }

        } catch (Exception e) {
            result = -1;
        }
        if (result > 0) {
            //活体通过
        } else {
            //活体不通过
        }
        ymFace.setLiveness(result);
        return ymFace;
    }


    public YMFace faceLiveness(Object o, Object oIr, int iw, int ih, float[] rect, int imageRotate, int type) {
        int result = -1;
        YMFace ymFace = new YMFace();
        try {
            if (null == o) return ymFace;
            int objectType = (o instanceof Bitmap) ? 0 : (o instanceof byte[]) ? 1 : -1;
            if (objectType == -1) return ymFace;
            switch (type) {
                case 0:  //rgb
                    result = objectType == 0 ? rsLivenessDetect().runLivenessDetectBitmap((Bitmap) o, imageRotate, rect) :
                            rsLivenessDetect().runLivenessDetect((byte[]) o, iw, ih, imageRotate, rect);
                    break;
                case 1:
                    //ir
                    result = objectType == 0 ? rsLivenessDetect850().runLivenessDetectInfraredBitmap((Bitmap) o, imageRotate, rect) :
                            rsLivenessDetect850().runLivenessDetectInfrared((byte[]) o, iw, ih, imageRotate, rect);
                    break;
                case 2:
                    if (oIr == null) {
                        result = -1;
                    } else {
                        //双目850
                        result = objectType == 0 ? rsLivenessDetect850().runLivenessDetectInfraredBitmap((Bitmap) o, imageRotate, rect) :
                                rsLivenessDetect850().runLivenessDetectInfrared((byte[]) oIr, iw, ih, imageRotate, rect);
                    }
                    break;
                case 3:
                    if (oIr == null) {
                        result = -1;
                    } else {
                        //双目940
                        if (objectType == 0)
                            result = rsLivenessDetect940().runLivenessDetectInfraredBitmap940(
                                    (Bitmap) o, imageRotate, rect);
                        else
                            result = rsLivenessDetect940().runLivenessDetectInfrared940(
                                    (byte[]) oIr, iw, ih, imageRotate, rect);
                    }
                    break;
            }

        } catch (Exception e) {
            result = -1;
        }
        if (result > 0) {
            //活体通过
        } else {
            //活体不通过
        }
        ymFace.setLiveness(result);
        return ymFace;
    }

    /**
     * 可见光活体
     */
    public int rgbLivenessDetect(byte[] data, int iw, int ih, float[] rect, int imageRotate) {
        if (null == data) return -1;
        return rsLivenessDetect().runLivenessDetect(data, iw, ih, imageRotate, rect);
    }

    /**
     * 850红外活体
     */
    public int ir850LivenessDetect(byte[] data, int iw, int ih, float[] rect, int imageRotate) {
        if (null == data) return -1;
        return rsLivenessDetect850().runLivenessDetectInfrared(data, iw, ih, imageRotate, rect);
    }

    /**
     * 940红外活体
     */
    public int ir940LivenessDetect(byte[] data, int iw, int ih, float[] rect, int imageRotate) {
        if (null == data) return -1;
        return rsLivenessDetect940().runLivenessDetectInfrared940(data, iw, ih, imageRotate, rect);
    }

    /**
     * 双目活体检测，假定人脸朝上
     */
    public int binocularLiveness(YMFace face, Bitmap irBitmap) {
        if (face == null || irBitmap == null) return -1;
        return rsLivenessDetect850().runLivenessDetectInfraredBitmap(irBitmap, 0, face.getRect());
    }


    public YMFace recognize(Object o, int iw, int ih, float[] rect, int imageRotate) {
        YMFace ymFace = new YMFace();
        if (rsFaceRecognition() == null) return ymFace;
        float[] feature = getFeature(o, iw, ih, imageRotate, rect);
        if (feature == null) return ymFace;
        DLog.d("size: " + rsFaceRecognition().getAlbumSize());
        YMPerson ymPerson = rsFaceRecognition().faceIdentification(feature);
        if (ymPerson != null)
            DLog.d("con:" + ymPerson.getConfidence() + " id:" + ymPerson.getPerson_id());
        if (null != ymPerson && ymPerson.getConfidence() >= 75) {
            //识别出此人
            ymFace.setIdentifiedPerson(ymPerson.getPerson_id(), (int) ymPerson.getConfidence());
        } else {
            //不认识此人
            int confidence = 0;
            if (ymPerson != null) confidence = (int) ymPerson.getConfidence();
            ymFace.setIdentifiedPerson(-111, confidence);
        }
        return ymFace;
    }

    /**
     * 双模型识别，新增支持遮挡识别，
     * 根据遮挡情况，提取不同的特征，送进去识别
     **/
    public YMFace recognizeTwoModel(byte[] bytes, int iw, int ih, YMFace ymFace, int imageRotate) {
        if (rsFaceRecognition() == null) return ymFace;

        // 获取人脸遮挡情况
        int occlusion = rsOcclusion().getOcclusion(bytes, iw, ih, imageRotate, ymFace.getRect());
        ymFace.setOcclusion(occlusion);

        // 提特征
        float[] feature;
        if (occlusion == 1)
            feature = getOcclusionFeature(bytes, iw, ih, imageRotate, ymFace.getRect());
        else
            feature = getFeature(bytes, iw, ih, imageRotate, ymFace.getRect());
        if (feature == null) return ymFace;

        // 特征版本号
        int featureVersion;
        if (occlusion == 1)
            featureVersion = RSConstant.FEATURE_VERSION_OCCLUSION;
        else
            featureVersion = RSConstant.FEATURE_VERSION;

        // 人脸搜索
        YMPerson ymPerson = rsFaceRecognition().faceIdentification(feature, featureVersion);
        if (null != ymPerson && ymPerson.getConfidence() >= 75) {
            //识别出此人
            ymFace.setIdentifiedPerson(ymPerson.getPerson_id(), (int) ymPerson.getConfidence());
        } else {
            //不认识此人
            int confidence = 0;
            if (ymPerson != null) confidence = (int) ymPerson.getConfidence();
            ymFace.setIdentifiedPerson(-111, confidence);
        }

        return ymFace;
    }

    /**
     * 双模型识别，新增支持遮挡识别，
     * 根据遮挡情况，提取不同的特征，送进去识别
     **/
    public YMFace recognizeTwoModel(byte[] bytes, int iw, int ih, float[] rect, int imageRotate) {
        YMFace ymFace = new YMFace();
        if (rsFaceRecognition() == null) return ymFace;

        // 获取人脸遮挡情况
        int occlusion = rsOcclusion().getOcclusion(bytes, iw, ih, imageRotate, rect);
        ymFace.setOcclusion(occlusion);

        // 提特征
        float[] feature;
        if (occlusion == 1)
            feature = getOcclusionFeature(bytes, iw, ih, imageRotate, rect);
        else
            feature = getFeature(bytes, iw, ih, imageRotate, rect);
        if (feature == null) return ymFace;

        // 特征版本号
        int featureVersion;
        if (occlusion == 1)
            featureVersion = RSConstant.FEATURE_VERSION_OCCLUSION;
        else
            featureVersion = RSConstant.FEATURE_VERSION;

        // 人脸搜索
        YMPerson ymPerson = rsFaceRecognition().faceIdentification(feature, featureVersion);
        if (null != ymPerson && ymPerson.getConfidence() >= 75) {
            //识别出此人
            ymFace.setIdentifiedPerson(ymPerson.getPerson_id(), (int) ymPerson.getConfidence());
        } else {
            //不认识此人
            int confidence = 0;
            if (ymPerson != null) confidence = (int) ymPerson.getConfidence();
            ymFace.setIdentifiedPerson(-111, confidence);
        }
        return ymFace;
    }


    public float[] getFeatureCard(Object o, int iw, int ih, int imageRotate, float[] rect) {
        return (o instanceof Bitmap) ? rsDeepFace().getDeepFaceFeatureIdcard((Bitmap) o, imageRotate, rect) : (o instanceof byte[]) ? rsDeepFace().getDeepFaceFeatureIdcard((byte[]) o, iw, ih, imageRotate, rect) : null;
    }

    /**
     * 获取非遮挡模型的特征
     */

    public float[] getFeature(Object o, int iw, int ih, int imageRotate, float[] rect) {
        float[] feature;
        if (o instanceof Bitmap)
            feature = rsDeepFace().getDeepFaceFeature((Bitmap) o, imageRotate, rect);
        else if (o instanceof byte[])
            feature = rsDeepFace().getDeepFaceFeature((byte[]) o, iw, ih, imageRotate, rect);
        else
            feature = null;

        return feature;
    }

    /**
     * 获取遮挡模型的特征
     */
    public float[] getOcclusionFeature(Object o, int iw, int ih, int imageRotate, float[] rect) {
        float[] feature;
        if (o instanceof Bitmap)
            feature = rsDeepFace().getDeepFaceOcclusionFeature((Bitmap) o, imageRotate, rect);
        else if (o instanceof byte[])
            feature = rsDeepFace().getDeepFaceOcclusionFeature((byte[]) o, iw, ih, imageRotate, rect);
        else
            feature = null;

        return feature;
    }

    /**
     * 新增人脸
     */
    public int addPerson(float[] faceFeature, float[] occlusionFeature) {
        if (rsFaceRecognition() == null) return -1;
        if (faceFeature == null || faceFeature.length != RSConstant.FEATURE_SIZE) return -1;
        if (occlusionFeature == null || occlusionFeature.length != RSConstant.FEATURE_SIZE)
            return -1;

        // 注册非遮挡特征(常规的特征)
        int personId = rsFaceRecognition().personCreate(faceFeature, RSConstant.FEATURE_VERSION);
        if (personId > 0) {
            // 注册遮挡特征
            rsFaceRecognition().personAddFace(
                    personId,
                    occlusionFeature,
                    RSConstant.FEATURE_VERSION_OCCLUSION
            );
        }
        return personId;
    }

    public boolean deleteUser(int personId) {
        if (rsFaceRecognition() == null) return false;
        if (rsFaceRecognition().personDelete(personId) == 0) {
            return true;
        }
        return false;
    }


    public List<Integer> getAllPersonId() {
        if (rsFaceRecognition() == null) return null;
        return rsFaceRecognition().getEnrolledPersonIds();
    }


    public int getUserCount() {
        return rsFaceRecognition() == null ? 0 : rsFaceRecognition().getAlbumSize();
    }


    public boolean deleteAllUser() {
        if (rsFaceRecognition() == null) return false;

        rsFaceRecognition().resetAlbum();
        return true;
    }


    public List<float[]> getFaceFeaturesByPersonId(int personId) {
        if (rsFaceRecognition() == null) return null;
        return rsFaceRecognition().getFaceFeaturesByPersonId(personId);
    }


    public int getFaceFeaturesCountByPersonId(int personId) {
        if (rsFaceRecognition() == null)
            return -1;
        return rsFaceRecognition().getFaceCountByPersonId(personId);
    }


    public List<YMPerson> findSimilarPerson(float[] faceFeature) {
        if (rsFaceRecognition() == null) return null;
        return rsFaceRecognition().findSimilarPerson(faceFeature);
    }


    public YMPerson faceIdentification(float[] faceFeature) {
        if (rsFaceRecognition() == null) return null;
        return rsFaceRecognition().faceIdentification(faceFeature);
    }

    /**
     * 假定传入的Bitmap 都是正脸朝上
     */
    public List<YMFace> identifyFace(Bitmap bitmap) {
        if (bitmap == null) return null;
        int imageRotate = 0;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        byte[] bytes = BitmapUtil.bitmapToNv21(bitmap, width, height);

        List<YMFace> ymFaces = faceDetect(width, height, imageRotate, bytes);
        if (ymFaces == null || ymFaces.size() == 0) {
            Log.e(TAG, "identifyFace: no face detect");
            return null;
        }

        for (YMFace face : ymFaces) {
            recognizeTwoModel(bytes, width, height, face, imageRotate);
            int faceQuality = getFaceQuality(bytes, width, height, face.getLandmarks(), imageRotate);
            face.setFaceQuality(faceQuality);
            // RGB 活体检测
            //faceLiveness(rgbBytes, null, width, height, face, imageRotate, 0);
            int helmet = hasHelmet(bytes, width, height, face.getLandmarks(), 0);
            face.setHelmet(helmet);
        }

        return ymFaces;
    }

    /**
     * Blur 和 Bright 的值，可以用于判断人脸是否适合跑识别和活体算法
     */
    public YMFace getFaceBlurAndBrightness(byte[] data, int width, int height, int orientation, float[] rect) {
        return rsAlign().runFaceAlign(data, width, height, orientation, rect);
    }

    /**
     * 人脸检测+活体
     *
     * @param rgbFrame     rgb 帧数据，Bitmap 对象或者 bytes[]
     * @param irFrame      ir 帧数据
     * @param iw           宽
     * @param ih           高
     * @param orientation  识别方向
     * @param livenessType 0，rgb活体；1，ir活体；2，双目活体
     * @return 包含活体结果的人脸数据
     */
    public List<YMFace> livenessDetect(Object rgbFrame, Object irFrame, int iw, int ih, int orientation, int livenessType) {
        // 人脸检测
        // rgb活体，用rgbFrame；IR活体，用rgbFrame；双目活体，用rgbFrame和irFrame
        List<YMFace> ymFaces = faceDetect(iw, ih, orientation, rgbFrame, false);
        if (ymFaces == null || ymFaces.size() == 0) return ymFaces;

        // 活体检测
        faceLiveness(rgbFrame, irFrame, iw, ih, ymFaces.get(0), orientation, livenessType);

        return ymFaces;
    }
}
