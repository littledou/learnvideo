package cn.idu.learnvideo.main;

import android.content.Context;

import cn.readsense.module.base.BaseApp;
import readsense.api.core.RSLicense;

public class ExApplication extends BaseApp {
    private static RSLicense rsLicense;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static RSLicense getRsLicense() {
        if (rsLicense == null) initRsLicense();
        return rsLicense;
    }

    public static void initRsLicense() {
        //测试版无需填写id和secret
        rsLicense = RSLicense.getInstance(context,
                "7d001ce693690aa2960b8f42e400b0d0", "d3f229a1351b3e9bd84e30ee81286db58bad001c");
        rsLicense.init();
        // 部分平台(高通MSM8909，算力低于PX30)，第一次打开 FaceDemoActivity，这里Toast会引起闪退问题
//        Toast.makeText(getContext(), "初始化:" + rsLicense.handle, Toast.LENGTH_SHORT).show();
        System.out.println("init code: " + rsLicense.handle);
    }
}
