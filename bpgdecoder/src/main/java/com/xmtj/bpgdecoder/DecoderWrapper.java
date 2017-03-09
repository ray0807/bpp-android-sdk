package com.xmtj.bpgdecoder;


import android.util.Log;


import com.xmtj.bpgdecoder.constant.Constants;

import java.io.IOException;
import java.io.InputStream;

public class DecoderWrapper {
    protected static native void init(String packageName, String token);

    public static native boolean getInitState();

    private static native int fetchDecodedBufferSize(byte[] encBuffer, int encBufferSize);

    private static native byte[] decodeBuffer(byte[] encBuffer, int encBufferSize);

    public static byte[] decodeBpgBuffer(InputStream input) {

        byte[] bytes = new byte[0];
        try {
            bytes = ByteTools.toByteArray(input);
            if (null == bytes) {
                Log.e(BPG.BPG_TAG, Constants.INPUTSTREAM_FORMAT_FAILED);
                return new byte[0];
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(BPG.BPG_TAG, Constants.INPUTSTREAM_FORMAT_FAILED);
            return new byte[0];
        }
        return decodeBuffer(bytes, bytes.length);
    }
}
