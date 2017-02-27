package com.xmtj.bgptest.decoder;


import android.util.Log;

import com.xmtj.bgptest.App;
import com.xmtj.bpgdecoder.BPG;
import com.xmtj.bpgdecoder.DecoderWrapper;
import com.xmtj.imagedownloader.core.decode.BaseImageDecoder;
import com.xmtj.imagedownloader.core.decode.ImageDecodingInfo;
import com.xmtj.imagedownloader.utils.IoUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class BpgImageDecoder extends BaseImageDecoder {

    public BpgImageDecoder(boolean loggingEnabled) {
        super(loggingEnabled);
    }

    @Override
    protected InputStream getImageStream(ImageDecodingInfo decodingInfo) throws IOException {

        if (decodingInfo.getOriginalImageUri().contains(".bpg")) {
            InputStream stream = null;
            try {
                stream = decodingInfo.getDownloader()
                        .getStream(decodingInfo.getImageUri(), decodingInfo.getExtraForDownloader());
                byte[] bytes = toByteArray(stream);
                byte[] decBuffer = DecoderWrapper.decodeBuffer(bytes, bytes.length);
                //解码器注册失败重新注册
                if (null != stream && null == decBuffer && null != App.getMContext()) {
                    BPG.init(App.getMContext());
                }
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