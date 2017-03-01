package com.xmtj.bpgdecoder;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.util.Log;

import com.xmtj.bpgdecoder.db.DBHelperManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by wanglei on 22/01/17.
 */

public class BPG {


    // Load library
    static {
        System.loadLibrary("bpg_decoder");
    }

    private static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private static Context mContext;
    private static DBHelperManager mDBhelperManager;

    protected static Context getmContext() {
        return mContext;
    }

    protected static DBHelperManager getmDBHelper() {
        return mDBhelperManager;
    }


    protected static ExecutorService getSingleThreadExecutor() {
        return singleThreadExecutor;
    }

    /**
     * application context
     *
     * @param context
     */
    public static void init(Context context) {
        if (null == context) {
            throw new RuntimeException("注册失败：context为null");
        }
        mContext = context;
        if (null == mDBhelperManager) {
            mDBhelperManager = new DBHelperManager(context);
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            final String token = appInfo.metaData.getString("BPG_TOKEN");
            singleThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    DecoderWrapper.init(mContext.getPackageName(), token);
                    uploadAll();
                }


            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static void uploadAll() {
        if (null != mDBhelperManager) {
            Cursor queryCursor = null;
            try {
                mDBhelperManager.open();
                queryCursor = mDBhelperManager.getAllBpgCount();
                if (queryCursor != null) {
                    while (queryCursor.moveToNext()) {
                        String bpg_key = queryCursor.getString(0);//获取第二列的值
                        int count = queryCursor.getInt(1); //获取第一列的值,第一列的索引从0开始
                        Log.e("wanglei", "bpg_key:" + bpg_key);
                        Log.e("wanglei", "count:" + count);

                    }
                    mDBhelperManager.removeAllBpgCount();
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (queryCursor != null) {
                    queryCursor.close();
                }
                if (mDBhelperManager != null) {
                    mDBhelperManager.close();
                }
            }
        }
    }

    public static void destory() {
        mContext = null;
        singleThreadExecutor = null;
        mDBhelperManager = null;
    }
}
