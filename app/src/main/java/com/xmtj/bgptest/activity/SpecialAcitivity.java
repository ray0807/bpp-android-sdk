package com.xmtj.bgptest.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.xmtj.bgptest.R;
import com.xmtj.imagedownloader.core.DisplayImageOptions;
import com.xmtj.imagedownloader.core.ImageLoader;

/**
 * Created by wanglei on 08/02/17.
 */

public class SpecialAcitivity extends Activity {
    private ImageView iv_show;
    private EditText et_image_url;
    private Button btn_load_url;
    private DisplayImageOptions options;


    public static void start(Activity activity) {
        Intent intent = new Intent(activity, SpecialAcitivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special);

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();

        iv_show = (ImageView) findViewById(R.id.iv_show);
        et_image_url = (EditText) findViewById(R.id.et_image_url);
        btn_load_url = (Button) findViewById(R.id.btn_load_url);
        btn_load_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = et_image_url.getText().toString().trim();
                if (url.length() == 0) {
                    Toast.makeText(SpecialAcitivity.this, "请输入图片url", Toast.LENGTH_SHORT).show();
                    return;
                }
                ImageLoader.getInstance().displayImage(url, iv_show, options);

            }
        });
    }
}
