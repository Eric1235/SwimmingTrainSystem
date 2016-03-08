package com.scnu.swimmingtrainsystem.activity;

import android.support.v4.app.Fragment;
import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.fragment.QueryFragment;


/**
 *查询成绩参数获取
 * Created by lixinkun on 15/12/28.
 */
public class QueryActivity extends SingleFragmentActivity{

    @Override
    protected Fragment createFragment() {
        return new QueryFragment();
    }

    @Override
    protected int getTitleRes() {
        return R.string.queryscore;
    }
}
