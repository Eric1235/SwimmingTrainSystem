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
import com.scnu.swimmingtrainsystem.http.JsonTools;
import com.scnu.swimmingtrainsystem.entity.ExplicitScore;
import com.scnu.swimmingtrainsystem.model2db.Plan;
import com.scnu.swimmingtrainsystem.model2db.Score;
import com.scnu.swimmingtrainsystem.entity.ScoreSum;
import com.scnu.swimmingtrainsystem.util.CommonUtils;
import com.scnu.swimmingtrainsystem.util.Constants;
import com.scnu.swimmingtrainsystem.util.NetworkUtil;
import com.scnu.swimmingtrainsystem.util.SpUtil;
import com.scnu.swimmingtrainsystem.util.VolleyUtil;
import com.scnu.swimmingtrainsystem.view.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;

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
    private int poolLength;
    private int distance;
    private int userid;
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
        userid = SpUtil.getUID(this);

        Intent i = getIntent();
        getQueryDataMap = (Map)i.getSerializableExtra("dataMap");
        athlete_id = (List<Integer>)getQueryDataMap.get("athlete_id");
        plan_id = (Integer)getQueryDataMap.get("plan_id");
        stroke = (Integer)getQueryDataMap.get("stroke");
        isReset = (Boolean) getQueryDataMap.get("isReset");

    }


    private void setDetailTextView(int maxTime, final Plan plan){
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
//                        String resDate = tmpScores[0].getUp_time();
                        //获得aid列表
//                        List<Integer> athIds = dbManager
//                                .getAthleteIdInScoreByDate(resDate);
                        //获得每个运动员的总成绩，包括运动员姓名和运动员总成绩
//                        List<ScoreSum> sumList = dbManager
//                                .getAthleteIdInScoreByDate(resDate,
//                                        athIds,isReset);

                        List<ScoreSum> sumList = CommonUtils.getAllScoreSum(mScores,isReset);
                        List<List<Score>> listss = CommonUtils.getScoresListByTimes(mScores,maxTime);
//                        // 根据时间查询成绩
//                        for (int t = 1; t <= maxTime; t++) {
//                            List<Score> sco = dbManager
//                                    .getScoreByDateAndTimes(resDate, t);
//                            listss.add(sco);
//                        }


                        tvDetails.setVisibility(View.VISIBLE);
                        tvDetails.setText(  "  目标总距离:"
                                + distance + "米");

                        //获得平均成绩，通过统计的总成绩获得
                        List<ScoreSum> avgScores = CommonUtils.getAvgScore(maxTime,sumList);
//                        for (ScoreSum ss : sumList) {
//                            ScoreSum scoreSum = new ScoreSum();
//                            //统计得到平均成绩
//                            int msec = CommonUtils
//                                    .timeString2TimeInt(ss.getScore());
//                            int avgsec = msec / maxTime;
//                            String avgScore = CommonUtils
//                                    .timeInt2TimeString(avgsec);
//                            scoreSum.setScore(avgScore);
//                            scoreSum.setAthleteName(ss.getAthleteName());
//                            avgScores.add(scoreSum);
//                        }

                        /**
                         * maxTime加上2就是为了显示
                         */
                        NameScoreListAdapter scoreListAdapter = new NameScoreListAdapter(
                                ShowExplicitScoreActivity.this, listss,
                                sumList, avgScores, maxTime + 2,dbManager);
                        scoreListView
                                .setAdapter(scoreListAdapter);
//                        dbManager.deleteScores(resDate);
                        //最后的两行就是总成绩和平均成绩
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
        Map<String,Object> map = new HashMap<String, Object>();
        if(athlete_id.get(0) != 0){
            map.put("athlete_id",athlete_id);
        }
        map.put("plan_id",plan_id);
        map.put("stroke",stroke);
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

//    class NameScoreListAdapter extends BaseExpandableListAdapter {
//        private Context mContext;
//        private List<List<Score>> mLists = new ArrayList<List<Score>>();
//        private List<ScoreSum> mTemps = new ArrayList<ScoreSum>();
//        private List<ScoreSum> mAvgScores = new ArrayList<ScoreSum>();
//        private int mSwimTime = 0;
//
//        public NameScoreListAdapter(Context mContext, List<List<Score>> mLists,
//                                    List<ScoreSum> mTemps, List<ScoreSum> avgScores, int mSwimTime) {
//            this.mContext = mContext;
//            this.mLists = mLists;
//            this.mTemps = mTemps;
//            this.mAvgScores = avgScores;
//            this.mSwimTime = mSwimTime;
//        }
//
//        @Override
//        public Object getChild(int groupPosition, int childPosition) {
//            // TODO Auto-generated method stub
//            return mLists.get(groupPosition).get(childPosition);
//        }
//
//        @Override
//        public long getChildId(int groupPosition, int childPosition) {
//            return 0;
//        }
//
//        @Override
//        public View getChildView(int groupPosition, int childPosition,
//                                 boolean isLastChild, View convertView, ViewGroup parent) {
//            // TODO Auto-generated method stub
//            ChildHolder childHolder = null;
//            if (convertView == null) {
//                childHolder = new ChildHolder();
//                convertView = View.inflate(mContext,
//                        R.layout.show_score_list_item_sub, null);
//                childHolder.rank = (TextView) convertView
//                        .findViewById(R.id.show_rank);
//                childHolder.score = (TextView) convertView
//                        .findViewById(R.id.show_score);
//                childHolder.name = (TextView) convertView
//                        .findViewById(R.id.show_name);
//                convertView.setTag(childHolder);
//            } else {
//                childHolder = (ChildHolder) convertView.getTag();
//            }
//
//            if (groupPosition < mSwimTime - 2) {
//                childHolder.rank.setText("第" + (childPosition + 1) + "名");
//                childHolder.score.setText(mLists.get(groupPosition)
//                        .get(childPosition).getScore());
//                /**
//                 * 要转换为名字啦
//                 */
//                int athlete_id = mLists.get(groupPosition).get(childPosition).getAthlete_id();
//                childHolder.name.setText(dbManager.getAthleteByAid(athlete_id).getName());
//            } else if (groupPosition == (mSwimTime - 2)) {
//                childHolder.rank.setText("第" + (childPosition + 1) + "名");
//                childHolder.score.setText(mTemps.get(childPosition).getScore());
//                childHolder.name.setText(mTemps.get(childPosition)
//                        .getAthleteName());
//            } else {
//                childHolder.rank.setText("第" + (childPosition + 1) + "名");
//                childHolder.score.setText(mAvgScores.get(childPosition)
//                        .getScore());
//                childHolder.name.setText(mAvgScores.get(childPosition)
//                        .getAthleteName());
//            }
//
//            return convertView;
//        }
//
//        @Override
//        public int getChildrenCount(int groupPosition) {
//            // TODO Auto-generated method stub
//            if (mLists.size() == 0) {
//                return 0;
//            }
//            return mLists.get(0).size();
//        }
//
//        @Override
//        public Object getGroup(int groupPosition) {
//            // TODO Auto-generated method stub
//            return groupPosition;
//        }
//
//        @Override
//        public int getGroupCount() {
//            // TODO Auto-generated method stub
//            return mSwimTime;
//        }
//
//        @Override
//        public long getGroupId(int groupPosition) {
//            // TODO Auto-generated method stub
//            return 0;
//        }
//
//        @Override
//        public View getGroupView(int groupPosition, boolean isExpanded,
//                                 View convertView, ViewGroup parent) {
//            GroupHolder groupHolder = null;
//            if (convertView == null) {
//                groupHolder = new GroupHolder();
//                convertView = View.inflate(mContext,
//                        R.layout.query_score_list_item_head, null);
//                groupHolder.timeTextView = (TextView) convertView
//                        .findViewById(R.id.test_date);
//                groupHolder.curDistance = (TextView) convertView
//                        .findViewById(R.id.test_plan);
//                convertView.setTag(groupHolder);
//            } else {
//                groupHolder = (GroupHolder) convertView.getTag();
//            }
//
//            if (groupPosition < getGroupCount() - 2) {
//                groupHolder.timeTextView.setText("第" + (groupPosition + 1)
//                        + "趟");
//                groupHolder.curDistance.setText("当前距离 "
//                        + mLists.get(groupPosition).get(0).getDistance() + "米");
//            } else if (groupPosition == (getGroupCount() - 2)) {
//                groupHolder.timeTextView.setText("本轮总计");
//            } else {
//                groupHolder.timeTextView.setText("平均成绩");
//            }
//            return convertView;
//        }
//
//        @Override
//        public boolean hasStableIds() {
//            // TODO Auto-generated method stub
//            return false;
//        }
//
//        @Override
//        public boolean isChildSelectable(int groupPosition, int childPosition) {
//            // TODO Auto-generated method stub
//            return false;
//        }
//
//        final class GroupHolder {
//            private TextView timeTextView;
//            private TextView curDistance;
//        }
//
//        final class ChildHolder {
//            private TextView rank;
//            private TextView score;
//            private TextView name;
//        }
//
//    }

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
