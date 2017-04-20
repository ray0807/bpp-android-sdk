package com.xmtj.bgptest;

import android.app.ActivityManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.xmtj.bgptest.activity.ComicViewAcitivity;
import com.xmtj.bgptest.activity.SpecialAcitivity;
import com.xmtj.bgptest.activity.TestAcitivity;
import com.xmtj.bpgdecoder.BPG;
import com.xmtj.imagedownloader.core.ImageLoader;

public class MainActivity extends AppCompatActivity {


    private String test = "http://oss.mkzcdn.com/image/20170101/5864c89edb375-600x800.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_bpg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TestAcitivity.start(MainActivity.this, TestAcitivity.BGP_TEST);
            }
        });
        findViewById(R.id.btn_normal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TestAcitivity.start(MainActivity.this, TestAcitivity.NORMAL_TEST);

            }
        });
        findViewById(R.id.btn_normal_bpg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TestAcitivity.start(MainActivity.this, TestAcitivity.NORMAL_BGP_TEST);

            }
        });
        findViewById(R.id.btn_load_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SpecialAcitivity.start(MainActivity.this);

            }
        });
        findViewById(R.id.btn_bpg_viewer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ComicViewAcitivity.start(MainActivity.this, ComicViewAcitivity.BPG_VIEWER);
                displayBriefMemory();

            }
        });
        findViewById(R.id.btn_jpg_viewer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ComicViewAcitivity.start(MainActivity.this, ComicViewAcitivity.JPG_VIEWER);
                getWidthAndHeight(test);
            }
        });

    }

    @Override
    protected void onDestroy() {
        BPG.destory();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        ImageLoader.getInstance().stop();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_clear_memory_cache:
                ImageLoader.getInstance().clearMemoryCache();
                return true;
            case R.id.item_clear_disc_cache:
                ImageLoader.getInstance().clearDiskCache();
                return true;
            default:
                return false;
        }
    }


    private int[] getWidthAndHeight(String originUrl) {
        int[] widthAndHeight = new int[2];
        try {
            if (originUrl != null) {
                int index1 = originUrl.indexOf("-");
                int index2 = originUrl.indexOf(".jpg");
                String wh = originUrl.substring(index1 + 1, index2);
                String[] whs = wh.split("x");
                widthAndHeight[0] = Integer.parseInt(whs[0].replace("x", ""));
                widthAndHeight[1] = Integer.parseInt(whs[1].replace("x", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return widthAndHeight;
    }

    private void displayBriefMemory() {

        final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();

        activityManager.getMemoryInfo(info);

        Log.i("wanglei", "系统剩余内存:" + (info.availMem >> 10) + "k");

        Log.i("wanglei", "系统是否处于低内存运行：" + info.lowMemory);

        Log.i("wanglei", "当系统剩余内存低于" + info.threshold + "时就看成低内存运行");

    }
}
