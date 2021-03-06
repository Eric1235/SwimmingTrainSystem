package com.scnu.swimmingtrainsystem.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.adapter.ScoreDateListAdapter;
import com.scnu.swimmingtrainsystem.entity.QueryScoreEntity;
import com.scnu.swimmingtrainsystem.entity.ScoreDateEntity;
import com.scnu.swimmingtrainsystem.fragment.QueryFragment;
import com.scnu.swimmingtrainsystem.http.JsonTools;
import com.scnu.swimmingtrainsystem.refresh.SwipeRefreshLoadLayout;
import com.scnu.swimmingtrainsystem.utils.AppController;
import com.scnu.swimmingtrainsystem.utils.CommonUtils;
import com.scnu.swimmingtrainsystem.utils.Constants;
import com.scnu.swimmingtrainsystem.utils.NetworkUtil;
import com.scnu.swimmingtrainsystem.utils.VolleyUtil;
import com.scnu.swimmingtrainsystem.view.LoadingDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 执行参数请求,展示请求数据列表
 * 目前程序并没有做下拉选择。
 * 提新需求了啊，要加上下拉刷新
 * Created by lixinkun on 16/2/22.
 */
public class ExcuteQueryActivity extends Activity implements View.OnClickListener{

    private boolean isConnected;
    private List<ScoreDateEntity> dateList;

    private ScoreDateListAdapter mAdapter;

    private QueryScoreEntity mEntity;

    private Toast mToast;

    private MyApplication app;

    /**
     * 下拉刷新的layout
     */
    private SwipeRefreshLoadLayout swipeRefreshLoadLayout;

    private ListView mScoreDateListView;
    private View emptyView;
    private ImageButton btnBack;
    private TextView tvStroke,tvSwimLength,tvIsReset;

    private LoadingDialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_excute_query);
        app = (MyApplication) getApplication();

        app.addActivity(this);

        initData();
        initView();

        isConnected = NetworkUtil.isConnected(this);
        if(isConnected){
            if(loadingDialog == null){
                loadingDialog = LoadingDialog.createDialog(this);
                loadingDialog.setMessage(getString(R.string.onQuerying));
                loadingDialog.setCanceledOnTouchOutside(false);
            }
            loadingDialog.show();
            addQueryScoreRequest(mEntity);
        }else{
            CommonUtils.showToast(this, mToast, getString(R.string.network_error));
        }


    }

    private void initView(){
        mScoreDateListView = (ListView) findViewById(R.id.score_list_view);
        swipeRefreshLoadLayout = (SwipeRefreshLoadLayout) findViewById(R.id.swipe_score_date);
        emptyView = View.inflate(this,R.layout.item_score_emptyview,null);
        ((ViewGroup)mScoreDateListView.getParent()).addView(emptyView);
        btnBack = (ImageButton)findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);
        tvIsReset = (TextView) findViewById(R.id.tv_is_reset);
        tvStroke = (TextView) findViewById(R.id.tv_swim_stroke);
        tvSwimLength = (TextView) findViewById(R.id.tv_swim_length);

        tvStroke.setText(getStrokeString(mEntity.getStroke()));
        if(mEntity.getDistance() == 0){
            tvSwimLength.setText(getString(R.string.all));
        }else{
            tvSwimLength.setText(mEntity.getDistance()+"");
        }

        tvIsReset.setText(getResetString(mEntity.isReset()));

    }

    private void initData(){
        Intent i = getIntent();
        mEntity = (QueryScoreEntity)i.getSerializableExtra(QueryFragment.QUERY_ENTITY);
    }

    /**
     * 转换reset为字符串
     * @param isreset
     * @return
     */
    private String getResetString(boolean isreset){
        String reset;
        if(isreset){
            reset = "是";
        }else{
            reset = "否";
        }
        return reset;
    }

    /**
     * 通过stroke数字转换到stroke字符
     * @param stroke
     * @return
     */
    private String getStrokeString(int stroke){
        String[] strokes = getResources().getStringArray(R.array.stroke_array_string);
        return strokes[stroke];
    }

    private void addQueryScoreRequest(final QueryScoreEntity entity){

        Map<String,String> map = getDataMap(entity);

        VolleyUtil.ResponseListener listener = new VolleyUtil.ResponseListener() {
            @Override
            public void onSuccess(String response) {
                loadingDialog.dismiss();
                JSONObject obj;
                try{
                    obj = new JSONObject(response);
                    int resCode = (Integer)obj.get("resCode");
                    if(resCode == 1){
                        JSONArray dateArray = obj.getJSONArray("dataList");
                        dateList = new ArrayList<>();
                        for(int i = 0 ; i < dateArray.length();i++){
                            ScoreDateEntity item = new ScoreDateEntity();
                            item.setDistance(dateArray.getJSONObject(i).getInt("distance"));
                            item.setPdate(dateArray.getJSONObject(i).getString("pdate"));
                            item.setPlan_id(dateArray.getJSONObject(i).getInt("plan_id"));
                            item.setTimes(dateArray.getJSONObject(i).getString("times"));
                            dateList.add(item);
                        }
                        mAdapter = new ScoreDateListAdapter(dateList,ExcuteQueryActivity.this);
                        mScoreDateListView.setAdapter(mAdapter);

                        mScoreDateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                int plan_id , stroke;
                                List<Integer> athlete_id = new ArrayList<Integer>();
                                stroke = mEntity.getStroke();
                                plan_id = dateList.get(position).getPlan_id();
                                athlete_id.add(mEntity.getAthleteId());
                                /**
                                 * 设置点击成绩后变色
                                 */
                                mAdapter.getItem(position).setIsChecked(true);
                                mAdapter.notifyDataSetChanged();
                                HashMap<String,Object> map = initDataMap(plan_id,stroke,athlete_id,entity.isReset());
                                AppController.gotoShowExplicitScoreActivity(ExcuteQueryActivity.this, map);
                            }
                        });

                    }else if(resCode == 0){
                        /**
                         * 在查询返回为空的时候，给界面显示emptyView，提示没有成绩
                         */
                        mScoreDateListView.setEmptyView(emptyView);
                    }else{
                        mScoreDateListView.setEmptyView(emptyView);
                        CommonUtils.showToast(ExcuteQueryActivity.this,mToast,getString(R.string.unkonwn_error));
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(String response) {
                loadingDialog.dismiss();
                CommonUtils.showToast(ExcuteQueryActivity.this, mToast, getString(R.string.unkonwn_error));
            }
        };
        VolleyUtil.httpJson(Constants.GET_SCORE_DATE_LIST, Request.Method.POST, map, listener, app);
    }

    private Map<String,String> getDataMap(QueryScoreEntity entity){
        Map<String,Object> datamap = new HashMap<String, Object>();
        String startTime,endTime;
        int stroke,aid,uid,poolLength,distance;

        stroke = entity.getStroke();
        uid = entity.getUid();
        aid = entity.getAthleteId();
        distance = entity.getDistance();
        poolLength = entity.getPoolLength();
        if(aid != 0){
            datamap.put("athlete_id",aid);
        }
            datamap.put("stroke",stroke);
        if(distance != 0){
            datamap.put("distance",distance);
        }

        if(poolLength != 0){
            datamap.put("pool_length",poolLength);
        }

        if(mEntity.getStartTime() != null){
            startTime = CommonUtils.formatDate(entity.getStartTime());
            datamap.put("start_time",startTime);
        }

        if(mEntity.getEndTime() != null){
            endTime = CommonUtils.formatDate(entity.getEndTime());
            datamap.put("end_time",endTime);
        }

        datamap.put("uid",uid);
        datamap.put("reset",mEntity.isReset());
        String data = JsonTools.creatJsonString(datamap);
        Map<String,String> map = new HashMap<String, String>();
        map.put("data",data);
        return  map;
    }

    private HashMap<String,Object> initDataMap(int plan_id,int stroke,List<Integer> athlete_id,boolean isReset){
        HashMap<String,Object> map = new HashMap<String, Object>();
        map.put("stroke",stroke);
        map.put("plan_id",plan_id);
        map.put("athlete_id",athlete_id);
        map.put("isReset",isReset);
        return map;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.removeActivity(this);
    }


    private void exitActivity(){
        finish();
        overridePendingTransition(R.anim.slide_bottom_in,
                R.anim.slide_top_out);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exitActivity();
            return false;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_back:
                exitActivity();
                break;
        }
    }
}
