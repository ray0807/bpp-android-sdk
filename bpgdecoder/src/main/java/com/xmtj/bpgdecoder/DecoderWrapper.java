package com.xmtj.bpgdecoder;


import java.io.IOException;
import java.io.InputStream;

public class DecoderWrapper {


    public static native void init(String packageName, String token);

    private static native int fetchDecodedBufferSize(byte[] encBuffer, int encBufferSize);

    private static native byte[] decodeBuffer(byte[] encBuffer, int encBufferSize);

    public static byte[] decodeBpgBuffer(InputStream input) throws IOException {
        byte[] bytes = ByteTools.toByteArray(input);
        return decodeBuffer(bytes, bytes.length);
    }






}
