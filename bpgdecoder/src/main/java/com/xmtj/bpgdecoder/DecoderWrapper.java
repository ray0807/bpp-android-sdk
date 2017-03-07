package com.xmtj.bpgdecoder;


import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.xmtj.bpgdecoder.Utils.HttpUtils;
import com.xmtj.bpgdecoder.iInterface.UrlCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DecoderWrapper {
    protected static native void init(String packageName, String token);

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

    public static void getBpgUrl(final String url, final UrlCallback callback) {
        Log.e("wanglei", "url:" + url);
        if (BPG.getmContext() == null) {
            return;
        }
        if (BPG.getSingleThreadExecutor() != null) {
            final Map<String, String> params = new HashMap<>();
            params.put("app_name", BPG.getmContext().getPackageName());
            params.put("app_key", BPG.getToken());
            params.put("image", url);
            BPG.getSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    String response = HttpUtils.sendPostData("http://testbpg.mkzcdn.com/sdk/bpg/image", params, "utf-8");
                    Log.e("wanglei", "response origin url:" + url);
                    Log.e("wanglei", "response:" + response);
                    if (callback != null) {
                        callback.onUrlReceive(response);
                    }
                }
            });
        }
    }
}
