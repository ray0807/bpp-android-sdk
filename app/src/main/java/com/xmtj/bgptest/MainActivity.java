package com.xmtj.bgptest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.xmtj.bgptest.activity.ComicViewAcitivity;
import com.xmtj.bgptest.activity.SpecialAcitivity;
import com.xmtj.bgptest.activity.TestAcitivity;
import com.xmtj.bpgdecoder.BPG;
import com.xmtj.imagedownloader.core.ImageLoader;

public class MainActivity extends AppCompatActivity {

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

            }
        });
        findViewById(R.id.btn_jpg_viewer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ComicViewAcitivity.start(MainActivity.this, ComicViewAcitivity.JPG_VIEWER);

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
}
