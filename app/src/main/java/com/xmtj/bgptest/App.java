package com.xmtj.bgptest;

import android.app.Application;
import android.content.Context;

import com.squareup.okhttp.OkHttpClient;
import com.xmtj.bgptest.downloader.OkHttpImageDownloader;
import com.xmtj.bpgdecoder.BPG;
import com.xmtj.imagedownloader.cache.disc.naming.Md5FileNameGenerator;
import com.xmtj.imagedownloader.core.ImageLoader;
import com.xmtj.imagedownloader.core.ImageLoaderConfiguration;
import com.xmtj.imagedownloader.core.assist.QueueProcessingType;
import com.xmtj.bgptest.decoder.BGPImageDecoder;

/**
 * Created by wanglei on 08/02/17.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        initImageLoader(this);
        super.onCreate();
    }

    public static void initImageLoader(Context context) {

        //注册解码器
        BPG.init(context);

        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.


        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.imageDecoder(new BGPImageDecoder(true));
        config.imageDownloader(new OkHttpImageDownloader(context, new OkHttpClient()));
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.FIFO);
        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }
}
