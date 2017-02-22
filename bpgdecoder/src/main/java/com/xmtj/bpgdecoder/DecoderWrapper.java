package com.xmtj.bpgdecoder;

public class DecoderWrapper {


    public static native void init(String packageName, String token);

    public static native int fetchDecodedBufferSize(byte[] encBuffer, int encBufferSize);

    public static native byte[] decodeBuffer(byte[] encBuffer, int encBufferSize);

}
