package com.scnu.swimmingtrainsystem.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.scnu.swimmingtrainsystem.R;


/**
 * 保存单个fragment的activity
 * Created by lixinkun on 16/1/17.
 */
public abstract class SingleFragmentActivity extends FragmentActivity {

    protected abstract Fragment createFragment();

    protected abstract int getTitleRes();

    private ImageButton btnBack;

    private TextView tvTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_singlefragment);

        tvTitle = (TextView) findViewById(R.id.tv_title);
        btnBack = (ImageButton)findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_bottom_in,
                        R.anim.slide_top_out);
            }
        });

        setTitleBar(getTitleRes());

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
        if(fragment == null){
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer,fragment)
                    .commit();
        }
    }

    public void setTitleBar(int res){
        tvTitle.setText(getString(res));
    }
}
