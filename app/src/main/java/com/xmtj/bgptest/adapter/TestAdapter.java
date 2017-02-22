package com.xmtj.bgptest.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xmtj.bgptest.Constant;
import com.xmtj.bgptest.R;
import com.xmtj.bgptest.activity.TestAcitivity;
import com.xmtj.imagedownloader.core.DisplayImageOptions;
import com.xmtj.imagedownloader.core.ImageLoader;
import com.xmtj.imagedownloader.core.display.CircleBitmapDisplayer;

/**
 * Created by wanglei on 08/02/17.
 */

public class TestAdapter extends BaseAdapter {
    private Context context;
    private int type = -1;
    private String[] datas;


    private DisplayImageOptions options;

    public TestAdapter(Context context, int type) {
        this.context = context;
        this.type = type;
        switch (type) {
            case TestAcitivity.BGP_TEST:
                datas = Constant.BGP_IMAGES;
                break;
            case TestAcitivity.NORMAL_TEST:
                datas = Constant.NORMAL_IMAGES;
                break;
            case TestAcitivity.NORMAL_BGP_TEST:
                datas = Constant.NORMAL_BGP_IMAGES;
                break;
        }
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new CircleBitmapDisplayer(Color.WHITE, 5))
                .build();
    }

    @Override
    public int getCount() {
        return datas.length;
    }

    @Override
    public String getItem(int i) {
        return datas[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_test, parent, false);
            holder = new ViewHolder();
            holder.text = (TextView) view.findViewById(R.id.text);
            holder.image = (ImageView) view.findViewById(R.id.image);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.text.setText("Item " + (position + 1));

        ImageLoader.getInstance().displayImage(datas[position], holder.image, options);
        return view;
    }

    static class ViewHolder {
        TextView text;
        ImageView image;
    }
}
