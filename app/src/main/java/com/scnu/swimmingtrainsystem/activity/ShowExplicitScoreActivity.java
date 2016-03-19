package com.scnu.swimmingtrainsystem.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.adapter.NameScoreListAdapter;
import com.scnu.swimmingtrainsystem.db.DBManager;
import com.scnu.swimmingtrainsystem.entity.ExplicitScore;
import com.scnu.swimmingtrainsystem.entity.ScoreSum;
import com.scnu.swimmingtrainsystem.http.JsonTools;
import com.scnu.swimmingtrainsystem.model2db.Plan;
import com.scnu.swimmingtrainsystem.model2db.Score;
import com.scnu.swimmingtrainsystem.utils.CommonUtils;
import com.scnu.swimmingtrainsystem.utils.Constants;
import com.scnu.swimmingtrainsystem.utils.NetworkUtil;
import com.scnu.swimmingtrainsystem.utils.Statistics;
import com.scnu.swimmingtrainsystem.utils.VolleyUtil;
import com.scnu.swimmingtrainsystem.view.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 展示查询到的成绩，点击可以展开
 * Created by lixinkun on 16/3/2.
 */
public class ShowExplicitScoreActivity extends Activity implements View.OnClickListener{

    private boolean isConnect;
    private boolean isReset;
    private int plan_id;
    private List<Integer> athlete_id;
    private int stroke;
    private int distance;
    private Plan plan;

    private Map<String,Object> getQueryDataMap;
    private DBManager dbManager;


    private LinearLayout containLayout;
    private MyApplication app;
    private TextView tvDetails;
    private ExpandableListView scoreListView;
    private ImageButton btnBack;

    private LoadingDialog mLoadingDialog;
    private Toast mToast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_explicit_score);
        initView();
        initData();
        isConnect = NetworkUtil.isConnected(this);
        if(isConnect){
            if(mLoadingDialog == null){
                mLoadingDialog = LoadingDialog.createDialog(this);
                mLoadingDialog.setMessage(getString(R.string.onQuerying));
                mLoadingDialog.setCanceledOnTouchOutside(false);
            }
            mLoadingDialog.show();
            addQueryExplicitScoreRequest(plan_id,athlete_id,stroke);
        }else{
            CommonUtils.showToast(this, mToast, getString(R.string.network_error));
        }
    }

    /**
     * 初始化控件
     */
    private void initView(){

        btnBack = (ImageButton)findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);
        scoreListView = (ExpandableListView) findViewById(R.id.query_score_list);
        tvDetails = (TextView) findViewById(R.id.show_details);
        containLayout = (LinearLayout) findViewById(R.id.ll_query_score);
    }

    private void initData(){
        app = (MyApplication) getApplication();
        app.addActivity(this);
        dbManager = DBManager.getInstance();
        Intent i = getIntent();
        getQueryDataMap = (Map)i.getSerializableExtra("dataMap");
        athlete_id = (List<Integer>)getQueryDataMap.get("athlete_id");
        plan_id = (Integer)getQueryDataMap.get("plan_id");
        stroke = (Integer)getQueryDataMap.get("stroke");
        isReset = (Boolean) getQueryDataMap.get("isReset");
    }


    private void setDetailTextView(int maxTime, int plan_id){
        /**
         * 先从本地查询
         */
        plan = DataSupport.find(Plan.class, plan_id);
        tvDetails.setVisibility(View.VISIBLE);
        tvDetails.setText(plan.getPool() + "  共" + maxTime + "趟  " + "  目标总距离："
                + plan.getDistance() + "米");
        tvDetails.setOnClickListener(this);
    }

    private void addQueryExplicitScoreRequest(int plan_id,List<Integer> athlete_id,int stroke){

        Map<String,String> map = getDataMap(plan_id,athlete_id,stroke);

        VolleyUtil.ResponseListener listener = new VolleyUtil.ResponseListener() {
            @Override
            public void onSuccess(String response) {

                mLoadingDialog.dismiss();
                JSONObject obj = null;
                try{
                    obj = new JSONObject(response);
                    int resCode = (Integer)obj.get("resCode");
                    /**
                     * 展示的成绩先进行统计
                     * 第一步，得到最大游泳次数
                     * 第二步，把返回的数据组装成为score对象
                     * 第三步，获得运动员列表，这是主要用于运动员的成绩筛选，得到每个运动员的一次游泳的全部成绩
                     * 第四步，对某个运动员的成绩进行统计，得到总运动时间，不过这个是根据停表或者不停表来进一步划分，
                     * 不停表的话，最后一次成绩就是总成绩，不需要统计
                     * 第五步，根据time去梳理出每一趟的成绩，用于展示
                     * 第六步，将所有得到的参数传进去，展示得到的成绩
                     */
                    if(resCode == 1){
                        int maxTime = 0;
                        ExplicitScore[] tmpScores = JsonTools.getObject(
                                obj.getString("dataList").toString(), ExplicitScore[].class);
                        List<Score> mScores = new ArrayList<>();
                        Score s = null;
                        for(ExplicitScore responseScore:tmpScores){
                            int curTime = responseScore.getTimes();
                            maxTime = curTime > maxTime ? curTime : maxTime;
                            /**
                             * 在这里对成绩进行转化
                             */
                            mScores.add(CommonUtils.convertScore(responseScore));
                        }
                        /**
                         * 获得上传时间
                         */
                        List<ScoreSum> sumList = Statistics.getAllScoreSum(mScores, isReset);
                        List<List<Score>> listss = Statistics.getScoresListByTimes(mScores,maxTime);
                        //获得平均成绩，通过统计的总成绩获得
                        List<ScoreSum> avgScores = Statistics.getAvgScore(maxTime,sumList);

                        /**
                         * maxTime加上2就是为了显示
                         */
                        NameScoreListAdapter scoreListAdapter = new NameScoreListAdapter(
                                ShowExplicitScoreActivity.this, listss,
                                sumList, avgScores, maxTime + 2,dbManager);
                        scoreListView
                                .setAdapter(scoreListAdapter);
                        for (int i = 0; i < (maxTime + 2); i++) {
                            scoreListView.expandGroup(i);
                        }
                    }else {

                        CommonUtils.showToast(ShowExplicitScoreActivity.this,mToast,getString(R.string.empty_score));
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String response) {
                mLoadingDialog.dismiss();
                CommonUtils.showToast(ShowExplicitScoreActivity.this, mToast, getString(R.string.unkonwn_error));
            }
        };

        VolleyUtil.httpJson(Constants.GET_SCORE, Request.Method.POST, map, listener, app);
    }

    private Map<String,String> getDataMap(int plan_id,List<Integer> athlete_id,int stroke){
        Map<String,Object> map = new HashMap<>();
        if(athlete_id.get(0) != 0){
            map.put("athlete_id",athlete_id);
        }
        map.put("plan_id",plan_id);
        if(stroke != 0) {
            map.put("stroke", stroke);
        }
        Map<String,String > dataMap = new HashMap<String, String>();
        String data = JsonTools.creatJsonString(map);
        dataMap.put("data", data);
        return dataMap;
    }

    private void showPlanExtra(String s) {
        // TODO Auto-generated method stub
        View view = getLayoutInflater()
                .inflate(R.layout.popupwindow_tips, null);
        TextView tipTextView = (TextView) view.findViewById(R.id.tv_pop_tips);
        tipTextView.setText(s);
        PopupWindow popupWindow = new PopupWindow(view,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.title_function_bg));
        popupWindow.showAsDropDown(tvDetails, containLayout.getWidth() - 50, 0);
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
    protected void onDestroy() {
        super.onDestroy();
        app.removeActivity(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_back:
                exitActivity();
                break;
            case R.id.show_details:
                String extraString = "无备注";
                if (!TextUtils.isEmpty(plan.getExtra().trim())) {
                    extraString = plan.getExtra();
                }
                showPlanExtra(extraString);
            default:
                break;
        }
    }
}
