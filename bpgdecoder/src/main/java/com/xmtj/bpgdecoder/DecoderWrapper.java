package com.xmtj.bpgdecoder;


import android.util.Log;


import com.xmtj.bpgdecoder.Utils.MemoryUtils;
import com.xmtj.bpgdecoder.constant.Constants;

import java.io.IOException;
import java.io.InputStream;

public class DecoderWrapper {
    protected static native void init(String packageName, String token, String currentTime);

    public static native boolean getInitState();

    private static native int fetchDecodedBufferSize(byte[] encBuffer, int encBufferSize);

    private static native byte[] decodeBuffer(byte[] encBuffer, int encBufferSize);

    /**
     * 使用同步 避免oom
     *
     * @param input
     * @return
     */
    public static synchronized byte[] decodeBpgBuffer(InputStream input) {

        if (!getInitState()) {
            return null;
        }

        byte[] bytes = new byte[0];
        try {
            bytes = ByteTools.toByteArray(input);
            if (null == bytes) {
                Log.e(BPG.BPG_TAG, Constants.INPUTSTREAM_FORMAT_FAILED);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(BPG.BPG_TAG, Constants.INPUTSTREAM_FORMAT_FAILED);
            return null;
        }
        return decodeBuffer(bytes, bytes.length);
    }


}
