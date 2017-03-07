package com.xmtj.bpgdecoder;


import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.xmtj.bpgdecoder.Utils.HttpUtils;
import com.xmtj.bpgdecoder.iInterface.UrlCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class DecoderWrapper {
    protected static native void init(String packageName, String token);

    protected static native boolean getInitState(String packageName, String token);

    private static native int fetchDecodedBufferSize(byte[] encBuffer, int encBufferSize);

    private static native byte[] decodeBuffer(byte[] encBuffer, int encBufferSize);

    public static byte[] decodeBpgBuffer(InputStream input) {

        byte[] bytes = new byte[0];
        try {
            bytes = ByteTools.toByteArray(input);
            if (null == bytes) {
                Log.e(BPG.BPG_TAG, "InputStream format byte failed,please check your image source");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return decodeBuffer(bytes, bytes.length);
    }

    public static void getBpgUrl(final Activity activity, final String url, final UrlCallback callback) {
        if (BPG.getmContext() == null) {
            return;
        }
        if (BPG.getSingleThreadExecutor() != null && getInitState(BPG.getmContext().getPackageName(), BPG.getToken())) {
            final Map<String, String> params = new HashMap<>();
            params.put("app_name", BPG.getmContext().getPackageName());
            params.put("app_key", BPG.getToken());
            params.put("image", url);
            BPG.getSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    final String response = HttpUtils.sendPostData("http://testbpg.mkzcdn.com/sdk/bpg/image", params, "utf-8");
                    if (callback != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (response == null || response.length() == 0) {
                                    callback.onUrlReceive(url);
                                } else {
                                    try {
                                        callback.onUrlReceive(response.replace("\\", "").replace("\"", ""));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        callback.onUrlReceive(url);
                                    }
                                }
                            }
                        });
                    }
                }
            });
        } else {
            Log.e(BPG.BPG_TAG, "please init bpg sdk");
            if (callback != null) {
                callback.onUrlReceive(url);
            }
        }
    }
}
