package com.xmtj.bpgdecoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.xmtj.bpgdecoder.Utils.HttpUtils;
import com.xmtj.bpgdecoder.Utils.MD5;
import com.xmtj.bpgdecoder.constant.Constants;
import com.xmtj.bpgdecoder.db.DBHelperManager;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
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
    private static String token;
    private static String packageName;
    protected static int MAX_MEMORY_LIMITED = 50;//100M以后不经过bgp处理,因为可能出现oom

    private static boolean isLoadLibrarySuccess = true;

    // Load library
    static {
        try {
            System.loadLibrary("bpg_decoder");

        } catch (Exception e) {
            isLoadLibrarySuccess = false;
            e.printStackTrace();
        }
    }

    protected static String getToken() {
        return null == token ? "" : token;
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
        return null == singleThreadExecutor ? Executors.newSingleThreadExecutor() : singleThreadExecutor;
    }

    /**
     * @param context        application context
     * @param maxMemoryInUse 当手机内存小于maxMemoryInUse将不会解码
     */
    public static void init(Context context, int maxMemoryInUse) {
        MAX_MEMORY_LIMITED = maxMemoryInUse;
        init(context);
    }

    public static void init(Context context) {
        if (!isLoadLibrarySuccess) {
            Log.e(BPG_TAG, "load library failed");
            return;
        }
        if (null == context) {
            Log.e(BPG_TAG, Constants.RUNTIME_TAG);
            return;
        }
        mContext = context;
        if (null == mDBhelperManager) {
            mDBhelperManager = new DBHelperManager(context);
        }
        registerNetworkReceiver(context);

        ApplicationInfo appInfo = null;
        try {
            appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            token = appInfo.metaData.getString(Constants.METADATE_TAG);
            packageName = mContext.getPackageName();
            singleThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i(BPG_TAG, Constants.DECODER_START_INIT);
                    try {
                        DecoderWrapper.init(packageName, token, System.currentTimeMillis() / 1000 + "");
                        uploadAll(packageName, token);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(BPG_TAG, Constants.DECODER_INIT_FAILED);
                    }

                }


            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static boolean isRegister = false;

    private static void registerNetworkReceiver(Context context) {
        if (!isRegister) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            filter.addAction("android.net.wifi.STATE_CHANGE");
            context.registerReceiver(networkReceiver, filter);
            isRegister = true;
        }
    }


    private static void uploadAll(String packageName, String token) {
        if (null != mDBhelperManager) {
            Cursor queryCursor = null;
            try {
                mDBhelperManager.open();
                queryCursor = mDBhelperManager.getAllBpgCount();
                List<Map<String, String>> data = new ArrayList<>();
                if (queryCursor != null) {
                    while (queryCursor.moveToNext()) {
                        String bpg_key = queryCursor.getString(0);//获取第二列的值
                        int count = queryCursor.getInt(1); //获取第一列的值,第一列的索引从0开始
                        Map<String, String> m = new HashMap<>();
                        m.put("id", bpg_key);
                        m.put("count", count + "");
                        data.add(m);
                    }
                    Log.e("wanglei", "data:" + data.toString());
                    if (data.size() > 0) {
                        JSONArray jsonArray = new JSONArray(data);
                        Map<String, String> params = new HashMap<>();
                        String timestamp = System.currentTimeMillis() / 1000 + "";
                        String app_key = MD5.md5(timestamp + token, "utf-8");
                        params.put("data", jsonArray.toString());
                        params.put("app_name", packageName);
                        params.put("app_key", app_key);
                        params.put("app_type", "1");
                        params.put("timestamp", timestamp);
                        HttpUtils.sendPostData(Constants.POST_COUNT_URL, params, "utf-8");
                        mDBhelperManager.removeAllBpgCount();
                    }
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

    public static String getDecodeString(String timestamp, String token) {
        return MD5.md5(timestamp + token, "utf-8");
    }

    public static void destory() {
        mContext = null;
        singleThreadExecutor = null;
        mDBhelperManager = null;
    }

    protected static BroadcastReceiver networkReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                    ConnectivityManager manager = (ConnectivityManager) context
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
                    if (activeNetwork != null) { // connected to the internet
                        if (activeNetwork.isConnected()) {
//                        if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
//                            // connected to wifi
//                            Log.i(BPG_TAG, "当前WiFi连接可用 ");
//                        } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
//                            // connected to the mobile provider's data plan
//                            Log.i(BPG_TAG, "当前移动网络连接可用 ");
//                        }
                            if (null != packageName && null != token && null != singleThreadExecutor) {
                                singleThreadExecutor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Log.e(BPG_TAG, Constants.DECODER_REINIT);
                                            DecoderWrapper.init(packageName, token, System.currentTimeMillis() / 1000 + "");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Log.e(BPG_TAG, Constants.DECODER_INIT_FAILED);
                                        }
                                    }


                                });
                            }
                        } else {
                            Log.e(BPG_TAG, Constants.NETWORK_DISCONNET);
                        }
                    } else {   // not connected to the internet
                        Log.e(BPG_TAG, Constants.NETWORK_DISCONNET);

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    };

    /**
     * 使用同步 避免oom
     *
     * @param input
     * @return
     */
    public static synchronized byte[] decodeBpgBuffer(InputStream input) {
        byte[] bytes = new byte[0];
        try {
            bytes = ByteTools.toByteArray(input);
            if (null == bytes || bytes.length == 0) {
                Log.e(BPG.BPG_TAG, Constants.INPUTSTREAM_FORMAT_FAILED);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(BPG.BPG_TAG, Constants.INPUTSTREAM_FORMAT_FAILED);
            return null;
        }
        return DecoderWrapper.decodeBuffer(bytes, bytes.length);
    }
}