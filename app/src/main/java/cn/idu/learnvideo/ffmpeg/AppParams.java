package cn.idu.learnvideo.ffmpeg;

public class AppParams {
    /**
     * prewviewWidth : 640 预览宽高
     * prewviewHeight : 480
     * colorCameraParams : {"previewOritation":90,"previewFlipx":false,"drawFlipx":false,"x":0,"y":0,"w":640,"h":480}
     * irCameraParams : {"previewOritation":90,"previewFlipx":false,"drawFlipx":false,"x":0,"y":490,"w":640,"h":480}
     * widthIsWidth : false 摄像头预览Size的宽度跟数据的宽度一致
     * rectMappingFlipX : false 红外框是否左右翻转，让送入红外算法的数据和框匹配
     * openRecognize : false 是否启用识别
     * dataOritation : 90 算法对输入数据旋转为正角度，暂时认为可见光和红外公用该参数
     * isTest : false 测试模式下一直识别存储
     * livenessMode : 3 - 1：可见光活体，2：红外活体，3：双目活体
     */
    public int prewviewWidth = 640;
    public int prewviewHeight = 480;
    public CameraParams colorCameraParams = new CameraParams();
    public CameraParams irCameraParams = new CameraParams();
    public boolean widthIsWidth = true;
    public boolean rectMappingFlipX = false;
    public boolean openRecognize = false;
    public int dataOrientation = 0;
    public boolean isTest = false;
    public int livenessMode = 0;
    public int analyzerLimit = 1000;

    public class CameraParams {
        /**
         * previewOritation : 90 摄像头预览顺时针旋转角度
         * previewFlipx : false 渲染时左右翻转
         * drawFlipx : false 绘制时左右翻转
         * x : 0
         * y : 0
         * w : 640
         * h : 480
         */
        public int previewOrientation = 0;
        public boolean previewFlipx = false;
        public boolean drawFlipx = false;
        public int cameraId = 0;
        public int x = 0;
        public int y = 0;
        public int w = 480;
        public int h = 480;

        @Override
        public String toString() {
            return "CameraParams{" +
                    "previewOritation=" + previewOrientation +
                    ", previewFlipx=" + previewFlipx +
                    ", drawFlipx=" + drawFlipx +
                    ", cameraId=" + cameraId +
                    ", x=" + x +
                    ", y=" + y +
                    ", w=" + w +
                    ", h=" + h +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "AppParams{" +
                "prewviewWidth=" + prewviewWidth +
                ", prewviewHeight=" + prewviewHeight +
                ", colorCameraParams=" + colorCameraParams +
                ", irCameraParams=" + irCameraParams +
                ", widthIsWidth=" + widthIsWidth +
                ", rectMappingFlipX=" + rectMappingFlipX +
                ", openRecognize=" + openRecognize +
                ", dataOrientation=" + dataOrientation +
                ", isTest=" + isTest +
                ", livenessMode=" + livenessMode +
                ", analyzerLimit=" + analyzerLimit +
                '}';
    }

    private static final AppParams ourInstance = createAppParams();

    public static AppParams getInstance() {
        return ourInstance;
    }

    private static AppParams createAppParams() {

        return new AppParams();
    }

    private AppParams() {

    }
}
