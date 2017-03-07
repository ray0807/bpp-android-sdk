package com.xmtj.bpgdecoder;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Debug;
import android.util.Log;

import com.xmtj.bpgdecoder.Utils.HttpUtils;
import com.xmtj.bpgdecoder.db.DBHelperManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by wanglei on 22/01/17.
 */

public class BPG {

    public static final String BPG_TAG = "xmtj_bpgdecoder";

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
                    Log.i(BPG_TAG, "bpg start init");
                    DecoderWrapper.init(mContext.getPackageName(), token);
                    uploadAll(mContext.getPackageName(), token);
                }


            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }


    private static void uploadAll(String packageName, String token) {
        if (null != mDBhelperManager) {
            Cursor queryCursor = null;
            try {
                mDBhelperManager.open();
                queryCursor = mDBhelperManager.getAllBpgCount();
                List<Map<String, Long>> data = new ArrayList<>();
                if (queryCursor != null) {

                    while (queryCursor.moveToNext()) {
                        long bpg_key = queryCursor.getLong(0);//获取第二列的值
                        int count = queryCursor.getInt(1); //获取第一列的值,第一列的索引从0开始
                        Log.e(BPG_TAG, "bpg_key:" + bpg_key);
                        Log.e(BPG_TAG, "count:" + count);
                        Map<String, Long> m = new HashMap<>();
                        m.put("id", bpg_key);
                        m.put("count", (long) count);
                        data.add(m);
                    }
                    JSONArray jsonArray = new JSONArray(data);
                    Map<String, String> params = new HashMap<>();
                    params.put("data", jsonArray.toString());
                    params.put("app_name", packageName);
                    params.put("app_key", token);
                    HttpUtils.sendPostData("http://testbpg.mkzcdn.com/sdk/index/dec_count", params, "utf-8");
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
