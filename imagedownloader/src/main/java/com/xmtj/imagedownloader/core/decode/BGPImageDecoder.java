package com.xmtj.imagedownloader.core.decode;


import android.util.Log;

import com.xmtj.bpgdecoder.DecoderWrapper;
import com.xmtj.imagedownloader.utils.IoUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class BGPImageDecoder extends BaseImageDecoder {

    public BGPImageDecoder(boolean loggingEnabled) {
        super(loggingEnabled);
    }

    @Override
    protected InputStream getImageStream(ImageDecodingInfo decodingInfo) throws IOException {

        long t1 = System.currentTimeMillis();
        if (decodingInfo.getOriginalImageUri().contains(".bpg")) {
            InputStream stream = null;
            try {
                Log.e("bpg_test", "t1:" + t1);
                stream = decodingInfo.getDownloader()
                        .getStream(decodingInfo.getImageUri(), decodingInfo.getExtraForDownloader());
                byte[] bytes = toByteArray(stream);
                byte[] decBuffer = DecoderWrapper.decodeBuffer(bytes, bytes.length);
                long t2 = System.currentTimeMillis();
                Log.e("bpg_test", "t2:" + t2);
                Log.e("bpg_test", "解压时间:" + (t2 - t1));
                return new ByteArrayInputStream(decBuffer);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IoUtils.closeSilently(stream);
            }
        }

        return super.getImageStream(decodingInfo);
    }
}