package com.xmtj.bgptest.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.ListView;

import com.xmtj.bgptest.R;
import com.xmtj.bgptest.adapter.TestAdapter;

/**
 * Created by wanglei on 08/02/17.
 */

public class TestAcitivity extends Activity {

    public static final String TYPE_TAG = "TestAcitivity";
    public static final int BGP_TEST = 1;
    public static final int NORMAL_TEST = 2;
    public static final int NORMAL_BGP_TEST = 3;
    private ListView list;
    private int type;

    public static void start(Activity activity, int type) {
        Intent intent = new Intent(activity, TestAcitivity.class);
        intent.putExtra(TYPE_TAG, type);
        activity.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        type = getIntent().getIntExtra(TYPE_TAG, -1);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(new TestAdapter(this, type));

    }
}
