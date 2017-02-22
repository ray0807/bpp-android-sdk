package com.xmtj.bpgdecoder;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;


/**
 * Created by wanglei on 22/01/17.
 */

public class BPG {
    // Load library
    static {
        System.loadLibrary("bpg_decoder");
    }

    public static void init(Context context) {

        Log.e("bpg", "init 包名: " + context.getPackageName());
        ApplicationInfo appInfo = null;
        try {
            appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            String msg = appInfo.metaData.getString("BPG_TOKEN");
            Log.e("bpg", "init  BPG_TOKEN : " + msg);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }
}
