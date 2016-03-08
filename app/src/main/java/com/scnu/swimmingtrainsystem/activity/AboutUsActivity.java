package com.scnu.swimmingtrainsystem.activity;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.scnu.swimmingtrainsystem.R;


/**
 * Created by lixinkun on 15/12/14.
 */
public class AboutUsActivity extends Activity {

    private MyApplication app;
    private TextView tvAPPVersion;
    private ImageButton btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_about_us);

        initView();

        app = (MyApplication)getApplication();
        app.addActivity(this);
    }

    private void initView(){
        tvAPPVersion = (TextView)findViewById(R.id.tv_app_version);
        tvAPPVersion.setText(getVersion());

        btnBack = (ImageButton) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_bottom_in,
                        R.anim.slide_top_out);
            }
        });
    }

     public String getVersion() {
         try {
                 PackageManager manager = this.getPackageManager();
                 PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
                 String version = info.versionName;
                 return  version;
             } catch (Exception e) {
                     e.printStackTrace();
                 return null;
             }
     }
}
