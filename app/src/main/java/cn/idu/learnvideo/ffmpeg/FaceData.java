package cn.idu.learnvideo.ffmpeg;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.locks.ReentrantLock;

import cn.readsense.module.util.DLog;

public class FaceData extends Thread {
    private final ReentrantLock lock = new ReentrantLock();

    private final int BUFFER_COUNT = 3;
    private final int DATA_SIZE;
    private final int BUFFER_DATA_STEP;
    private final int BUFFER_INFO_STEP;

    private final String tag;

    private int index = 0;
    private final byte[] byteArray;
    private final float[] rect;
    private final float[] landmark;
    private final MyArrayBlockingQueue<Integer> eventQueue;

    private final ByteBuffer dataBuffer;
    private final FloatBuffer infoBuffer;

    public FaceData(String tag, int w, int h) {
        this.tag = tag;

        DATA_SIZE = w * h * 3 / 2;
        BUFFER_DATA_STEP = DATA_SIZE + 4;
        BUFFER_INFO_STEP = 4 * (4 + 21 * 2);

        byteArray = new byte[DATA_SIZE];
        rect = new float[4];
        landmark = new float[21 * 2];

        eventQueue = new MyArrayBlockingQueue<>(BUFFER_COUNT);
        dataBuffer = ByteBuffer.allocateDirect(BUFFER_DATA_STEP * BUFFER_COUNT);
        infoBuffer = ByteBuffer.allocateDirect(BUFFER_INFO_STEP * BUFFER_COUNT).asFloatBuffer();
    }

    //TODO 采用该方式，在某些设备上会跟camera缓冲读取冲突，导致camera渲染帧率降低，并不可恢复
    public void putBuffer(byte[] data, int trackId, float[] rect, float[] landmark) {
        if (index > BUFFER_COUNT - 1) index = 0;
        if (eventQueue.offerIndex(index)) {//查询是否允许填充节点
//            llog("填充节点：" + index);
            lock.lock();
            try {
                dataBuffer.position(BUFFER_DATA_STEP * index);
                dataBuffer.put(data, 0, DATA_SIZE);
                dataBuffer.putInt(trackId);

                infoBuffer.position(BUFFER_INFO_STEP * index / 4);
                infoBuffer.put(rect);
                if (landmark != null)
                    infoBuffer.put(landmark);
                eventQueue.offer(index);
                index++;
            } finally {
                lock.unlock();
            }
        }
    }

    public FaceInfo getBuffer() throws InterruptedException {
        int index = eventQueue.take();
        try {
            lock.lock();
//                llog("消费节点 " + index);
            dataBuffer.position(BUFFER_DATA_STEP * index);
            dataBuffer.get(byteArray);
            int trackId = dataBuffer.getInt();
            infoBuffer.position(BUFFER_INFO_STEP * index / 4);
            infoBuffer.get(rect);
            infoBuffer.get(landmark);
            return new FaceInfo(trackId, byteArray, rect, landmark);
        } finally {
            lock.unlock();
        }
    }

    private void llog(String msg) {
        DLog.d(tag, msg);
    }

    public void clear() {
        index = 0;
        eventQueue.clear();
    }

    @Override
    public void run() {
        super.run();
        llog("start job");
    }

    public class FaceInfo {
        public int trackId;
        public byte[] data;
        public float[] rect;
        public float[] landmark;

        public FaceInfo(int trackId, byte[] data, float[] rect, float[] landmark) {
            this.trackId = trackId;
            this.data = data;
            this.rect = rect;
            this.landmark = landmark;
        }
    }

}
