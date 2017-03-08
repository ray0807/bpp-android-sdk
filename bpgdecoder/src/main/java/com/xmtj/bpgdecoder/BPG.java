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
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.xmtj.bpgdecoder.Utils.HttpUtils;
import com.xmtj.bpgdecoder.constant.Constants;
import com.xmtj.bpgdecoder.db.DBHelperManager;
import com.xmtj.bpgdecoder.iInterface.UrlCallback;

import org.json.JSONArray;

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

    // Load library
    static {
        System.loadLibrary("bpg_decoder");
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
        return singleThreadExecutor;
    }

    /**
     * application context
     *
     * @param context
     */
    public static void init(Context context) {
        if (null == context) {
            throw new RuntimeException(Constants.RUNTIME_TAG);
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
                        DecoderWrapper.init(packageName, token);
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
                List<Map<String, Long>> data = new ArrayList<>();
                if (queryCursor != null) {
                    while (queryCursor.moveToNext()) {
                        long bpg_key = queryCursor.getLong(0);//获取第二列的值
                        int count = queryCursor.getInt(1); //获取第一列的值,第一列的索引从0开始
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
                    HttpUtils.sendPostData(Constants.POST_COUNT_URL, params, "utf-8");
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

    protected static BroadcastReceiver networkReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

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
                        if (!DecoderWrapper.getInitState() && null != packageName && null != token) {
                            try {
                                Log.e(BPG_TAG, Constants.DECODER_REINIT);
                                DecoderWrapper.init(packageName, token);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(BPG_TAG, Constants.DECODER_INIT_FAILED);
                            }
                        }
                    } else {
                        Log.e(BPG_TAG, Constants.NETWORK_DISCONNET);
                    }
                } else {   // not connected to the internet
                    Log.e(BPG_TAG, Constants.NETWORK_DISCONNET);

                }
            }


        }
    };
}