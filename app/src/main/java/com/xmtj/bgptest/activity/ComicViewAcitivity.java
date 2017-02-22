package com.xmtj.bgptest.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.xmtj.bgptest.Constant;
import com.xmtj.bgptest.R;
import com.xmtj.bgptest.adapter.ComicViewerAdapter;
import com.xmtj.bgptest.widget.ScrollZoomListView;

/**
 * Created by wanglei on 08/02/17.
 */

public class ComicViewAcitivity extends Activity {

    public static final String TYPE_TAG = "ComicViewAcitivity";
    public static final int BPG_VIEWER = 1;
    public static final int JPG_VIEWER = 2;

    private ScrollZoomListView rlv_viewer_list;
    private int type;


    public static void start(Activity activity, int type) {
        Intent intent = new Intent(activity, ComicViewAcitivity.class);
        intent.putExtra(TYPE_TAG, type);
        activity.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_viewer);
        type = getIntent().getIntExtra(TYPE_TAG, 1);

        rlv_viewer_list = (ScrollZoomListView) findViewById(R.id.rlv_viewer_list);
        rlv_viewer_list.setAdapter(new ComicViewerAdapter(this, type == BPG_VIEWER ? Constant.BGP_VIEWER_IMAGES : Constant.JPG_VIEWER_IMAGES));

    }
}
