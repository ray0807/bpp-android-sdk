package com.xmtj.bgptest;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.xmtj.bgptest.downloader.OkHttpImageDownloader;
import com.xmtj.bpgdecoder.BPG;
import com.xmtj.imagedownloader.cache.disc.naming.Md5FileNameGenerator;
import com.xmtj.imagedownloader.core.ImageLoader;
import com.xmtj.imagedownloader.core.ImageLoaderConfiguration;
import com.xmtj.imagedownloader.core.assist.QueueProcessingType;

import okhttp3.OkHttpClient;

/**
 * Created by wanglei on 08/02/17.
 */

public class App extends Application {


    @Override
    public void onCreate() {
        initImageLoader(this);
        super.onCreate();
    }


    public void initImageLoader(Context context) {
        LeakCanary.install(this);
        //注册解码器
        BPG.init(context);

        //监测内存泄漏

        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.


        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
//        config.imageDecoder(new BpgImageDecoder(true));
        config.imageDownloader(new OkHttpImageDownloader(context, new OkHttpClient()));
        config.diskCacheSize(500 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.FIFO);
        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }

}
