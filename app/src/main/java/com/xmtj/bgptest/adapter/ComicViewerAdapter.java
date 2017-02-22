package com.xmtj.bgptest.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.xmtj.bgptest.R;
import com.xmtj.bgptest.utils.DisplayUtil;
import com.xmtj.bgptest.utils.ViewHolder;
import com.xmtj.imagedownloader.core.DisplayImageOptions;
import com.xmtj.imagedownloader.core.ImageLoader;
import com.xmtj.imagedownloader.core.assist.FailReason;
import com.xmtj.imagedownloader.core.display.CircleBitmapDisplayer;
import com.xmtj.imagedownloader.core.listener.ImageLoadingListener;

/**
 * Created by wanglei on 14/02/17.
 */

public class ComicViewerAdapter extends BaseAdapter {
    private Context context;
    private String datas[];
    private DisplayImageOptions options;
    private int screenWidth = 0;

    public ComicViewerAdapter(Context context, String[] datas) {
        this.context = context;
        this.datas = datas;
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        screenWidth = DisplayUtil.getScreenWidth(context);
    }

    @Override
    public int getCount() {
        return datas.length;
    }

    @Override
    public Object getItem(int i) {
        return datas[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {

            view = LayoutInflater.from(context).inflate(R.layout.item_viewer, viewGroup, false);
        }
        final ImageView iv_comic_viewer = ViewHolder.get(view, R.id.iv_comic_viewer);
        ImageLoader.getInstance().displayImage(datas[position], iv_comic_viewer, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                try {
                    if (bitmap != null) {
                        ViewGroup.LayoutParams params = iv_comic_viewer.getLayoutParams();
                        params.width = screenWidth;
                        params.height = bitmap.getHeight() * screenWidth / bitmap.getWidth();
                        iv_comic_viewer.setLayoutParams(params);
                    }
                } catch (Exception e) {

                }

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
        return view;
    }


}
