package com.xmtj.bpgdecoder;


import java.io.IOException;
import java.io.InputStream;

public class DecoderWrapper {
    protected static native void init(String packageName, String token);

    private static native int fetchDecodedBufferSize(byte[] encBuffer, int encBufferSize);

    private static native byte[] decodeBuffer(byte[] encBuffer, int encBufferSize);

    public static byte[] decodeBpgBuffer(InputStream input) {
        byte[] bytes = new byte[0];
        try {
            bytes = ByteTools.toByteArray(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return decodeBuffer(bytes, bytes.length);
    }
}
