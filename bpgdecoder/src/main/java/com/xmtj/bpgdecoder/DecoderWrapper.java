package com.xmtj.bpgdecoder;

public class DecoderWrapper {
    protected static native void init(String packageName, String token, String currentTime);

    private static native int fetchDecodedBufferSize(byte[] encBuffer, int encBufferSize);

    protected static native byte[] decodeBuffer(byte[] encBuffer, int encBufferSize);
}
