package com.xmtj.bpgdecoder;


import android.app.Activity;
import android.content.pm.PackageManager;
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

    protected static native boolean getInitState();

    private static native int fetchDecodedBufferSize(byte[] encBuffer, int encBufferSize);

    private static native byte[] decodeBuffer(byte[] encBuffer, int encBufferSize);

    public static byte[] decodeBpgBuffer(InputStream input) {

        byte[] bytes = new byte[0];
        try {
            bytes = ByteTools.toByteArray(input);
            if (null == bytes) {
                Log.e(BPG.BPG_TAG, "InputStream format byte failed,please check your image source");
                BPG.reInit();
            }
        } catch (IOException e) {
            e.printStackTrace();
            BPG.reInit();
        }
        return decodeBuffer(bytes, bytes.length);
    }
}
