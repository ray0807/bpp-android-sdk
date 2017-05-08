package com.xmtj.bgptest.downloader;

import android.util.Log;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSource;

/**
 * OkHttp的{@link Interceptor}, 通过设置
 * <br/>
 * Created by Ryan on 2015/12/31.
 */
public class HttpLoggingInterceptor implements Interceptor {
    private static final String TAG = "HttpLogging";

    @Override
    public Response intercept(Chain chain) throws IOException {
        //========添加公共参数============
        Request request = chain.request();



        //===========log===========
        long t1 = System.nanoTime();

        Buffer buffer = new Buffer();
        if (request.body() != null) {
            request.body().writeTo(buffer);
            Log.e(TAG, String.format("Sending request %s on %s%n%sRequest Params: %s",
                    request.url(), chain.connection(), request.headers(), buffer.clone().readUtf8()));
            buffer.close();
        }

        Response response = chain.proceed(request);
        long t2 = System.nanoTime();

        BufferedSource source = response.body().source();
        source.request(Long.MAX_VALUE);
        buffer = source.buffer().clone();


//        Log.e(TAG, "==============================");
//        while (buffer.size() > 0) {
//            Log.e(TAG, "" + buffer.readUtf8Line());
//        }
//        Log.e(TAG, "==============================");

        Log.e(TAG, String.format("Received response for %s in %.1fms%n%sResponse Json: %s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers(),
                buffer.readUtf8()));


        return response;
    }
}
