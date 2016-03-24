package com.scnu.swimmingtrainsystem.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.activity.MyApplication;
import com.scnu.swimmingtrainsystem.adapter.ChooseAthleteAdapter;
import com.scnu.swimmingtrainsystem.adapter.ShowChosenAthleteAdapter;
import com.scnu.swimmingtrainsystem.db.DBManager;
import com.scnu.swimmingtrainsystem.effect.Effectstype;
import com.scnu.swimmingtrainsystem.effect.NiftyDialogBuilder;
import com.scnu.swimmingtrainsystem.http.JsonTools;
import com.scnu.swimmingtrainsystem.entity.AdapterHolder;
import com.scnu.swimmingtrainsystem.model2db.Athlete;
import com.scnu.swimmingtrainsystem.model2db.Plan;
import com.scnu.swimmingtrainsystem.model2db.User;
import com.scnu.swimmingtrainsystem.utils.AppController;
import com.scnu.swimmingtrainsystem.utils.CommonUtils;
import com.scnu.swimmingtrainsystem.utils.Constants;
import com.scnu.swimmingtrainsystem.utils.SpUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author lixinkun
 *
 * 2015年12月10日
 */
public class TimerFragment extends BaseFragment implements View.OnClickListener{

    private View v;
    private MyApplication app;
    private DBManager dbManager;
    /**
     * 全部运动员
     */
    private List<Athlete> athletes;

    /**
     * 已选中的运动员
     */
    private List<Athlete> chosenAthletes = new ArrayList<Athlete>();

    private SparseBooleanArray map = new SparseBooleanArray();

    /**
     * 用户id
     */
    private int userid;

    private String[] autoStrings;//泳池长度
    private String[] autoIntervals;//游泳长度
    private boolean isReset = false;

    /**
     * 显示在activity上的被选中要计时的运动员数据适配器
     */
    private ShowChosenAthleteAdapter showChosenAthleteAdapter;
    /**
     * 展示全部运动员的adapter
     */
    private ChooseAthleteAdapter allAthleteAdapter;
    private ArrayAdapter<String> tipsAdapter;
    private ArrayAdapter<String> intervalsAdapter;


    private Toast toast;

    private AutoCompleteTextView acTextView, actInterval;//自动填充textview
    private Button btnChooseAthlete;
    private Button btnStartTimer;
    private RadioButton rbStopWatch;
    private EditText remarksEditText;//备注
    //泳池大小选择
    private Spinner poolSpinner;

    //泳姿选择
    private Spinner strokeSpinner;
    /**
     * 显示在activity上的被选中要计时的运动员ListView
     */
    private ListView chosenListView;

    /**
     * 展示全部运动员的ListView
     */
    private ListView athleteListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        app = (MyApplication) getActivity().getApplication();
        dbManager = DBManager.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(v == null){
            v = inflater.inflate(R.layout.fragment_timer,null);
            chosenListView = (ListView) v.findViewById(R.id.list_choosed);
            poolSpinner = (Spinner) v.findViewById(R.id.pool_length);
            strokeSpinner = (Spinner) v.findViewById(R.id.stroke);
            acTextView = (AutoCompleteTextView) v.findViewById(R.id.tv_distance);
            actInterval = (AutoCompleteTextView) v.findViewById(R.id.act_interval);
            remarksEditText = (EditText) v.findViewById(R.id.et_remarks);
            btnChooseAthlete = (Button) v.findViewById(R.id.bt_choose_athlete);
            btnStartTimer = (Button) v.findViewById(R.id.btn_start_timer);
            rbStopWatch = (RadioButton) v.findViewById(R.id.rb_stop_watch);

            rbStopWatch.setOnClickListener(this);

            btnChooseAthlete.setOnClickListener(this);
            btnStartTimer.setOnClickListener(this);

        }
        return v;
    }

    private void initData(){
        //泳池长度
        autoStrings = getResources().getStringArray(R.array.swim_length);

        autoIntervals = getResources().getStringArray(R.array.swim_length);

        tipsAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, autoStrings);
        intervalsAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line,autoIntervals);

        String swimDistance = SpUtil.getDistance(getActivity());
        String swimInterval = SpUtil.getSwimInterval(getActivity());
        actInterval.setText(swimInterval);
        acTextView.setText(swimDistance);

        int selectedPositoin = SpUtil.getSelectedPosition(getActivity());

        /**
         * 初始化app里面的全局数据,一共6个
         */
        app.getMap().put(Constants.CURRENT_SWIM_TIME, 0);
        app.getMap().put(Constants.DRAG_NAME_LIST_IDS,null);
        app.getMap().put(Constants.DRAG_NAME_LIST,null);
        app.getMap().put(Constants.TEST_DATE,"");
        app.getMap().put(Constants.INTERVAL,null);
        app.getMap().put(Constants.PLAN_ID,null);

        userid = SpUtil.getUID(getActivity());

        acTextView.setAdapter(tipsAdapter);
        acTextView.setDropDownHeight(350);
        acTextView.setThreshold(1);

        actInterval.setAdapter(intervalsAdapter);
        actInterval.setDropDownHeight(300);
        actInterval.setThreshold(1);


        List<String> poolLength = new ArrayList<String>();
        List<String> stroke = new ArrayList<String>();
        String[] strokes = getResources().getStringArray(R.array.strokestrarray);
        String[] poolLengths = getResources().getStringArray(R.array.pool_length);
        Collections.addAll(poolLength, poolLengths);
        Collections.addAll(stroke, strokes);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(),
                R.layout.item_spinner, stroke);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getActivity(),
                R.layout.item_spinner, poolLength);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        poolSpinner.setAdapter(adapter1);
        poolSpinner.setSelection(selectedPositoin);
        strokeSpinner.setAdapter(adapter2);
        strokeSpinner.setSelection(selectedPositoin);


        showChosenAthleteAdapter = new ShowChosenAthleteAdapter(
                getActivity(), chosenAthletes);
        chosenListView.setAdapter(showChosenAthleteAdapter);
    }

    /**
     * 更新运动员列表
     */
    private void upDateAthleteList(){
        athletes = dbManager.getAthletes(userid);
    }

    /**
     * 选择运动员
     */
    private void chooseAthlete(){
        upDateAthleteList();
        final NiftyDialogBuilder selectDialog = NiftyDialogBuilder
                .getInstance(getActivity());
        Effectstype effect = Effectstype.Fall;
        selectDialog.setCustomView(R.layout.dialog_choose_athlete, getActivity());
        Window window = selectDialog.getWindow();
        athleteListView = (ListView) window.findViewById(R.id.choose_list);

        allAthleteAdapter = new ChooseAthleteAdapter(getActivity(), athletes, map);
        athleteListView.setAdapter(allAthleteAdapter);
        athleteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                AdapterHolder holder = (AdapterHolder) arg1.getTag();
                // 改变CheckBox的状态
                holder.cb.toggle();
                Athlete a = (Athlete) allAthleteAdapter.getItem(position);
                if (holder.cb.isChecked()) {
                    if(!CommonUtils.ListContainsAthlete(chosenAthletes, a)){
                        chosenAthletes.add(a);
                    }

                } else {
                    if(CommonUtils.ListContainsAthlete(chosenAthletes,a)){
                        CommonUtils.removeAthleteFromList(chosenAthletes,a);
                    }

                }
                // 将CheckBox的选中状况记录下来
                map.put(position, holder.cb.isChecked());
            }
        });
        selectDialog.withTitle(getString(R.string.choose_athlete)).withMessage(null)
                .withIcon(getResources().getDrawable(R.drawable.ic_launcher))
                .isCancelableOnTouchOutside(false).withDuration(500)
                .withEffect(effect).withButton1Text(getString(R.string.back))
                .withButton2Text(Constants.OK_STRING)
                .setButton1Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectDialog.dismiss();
                    }
                }).setButton2Click(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                showChosenAthleteAdapter = new ShowChosenAthleteAdapter(
                        getActivity(), chosenAthletes);
                chosenListView.setAdapter(showChosenAthleteAdapter);
                selectDialog.dismiss();
            }

        }).show();

    }

    /**
     * 开始计时,对数据进行存储
     */
    private void startTiming(){
        String totalDistance = acTextView.getText().toString().trim();
        String intervalDistance = actInterval.getText().toString().trim();

        if (TextUtils.isEmpty(intervalDistance)) {
            CommonUtils.showToast(getActivity(), toast, getString(R.string.set_timer_interval_length));
        } else if (TextUtils.isEmpty(totalDistance)) {
            CommonUtils.showToast(getActivity(), toast, getString(R.string.set_total_length));
        } else if (Integer.parseInt(totalDistance) < Integer
                .parseInt(intervalDistance.replace("米", ""))) {
            CommonUtils.showToast(getActivity(), toast, getString(R.string.interval_length_cannot_loger_than_total_length));
        } else if (chosenAthletes.size() == 0) {
            CommonUtils.showToast(getActivity(), toast, getString(R.string.add_athlete_before_timer));
        } else {
            // 保存这一次的配置到sp
            SpUtil.saveSelectedPool(getActivity(),
                    poolSpinner.getSelectedItemPosition());
            Map<String,String> map = showChosenAthleteAdapter.getAthleteWithStroke();
            app.getMap().put(Constants.ATHLETE_STROKE_MAP,map);
            SpUtil.saveDistance(getActivity(), totalDistance, intervalDistance);
            SpUtil.saveSelectedAthlete(getActivity(),
                    JsonTools.creatJsonString(map));

            String date = CommonUtils.formatDate(new Date());
            // 保存计时日期
            app.getMap().put(Constants.TEST_DATE, date);
            //保存计时间隔
            app.getMap().put(Constants.INTERVAL, intervalDistance);

            List<String> athleteNames = new ArrayList<String>();

            List<Integer> athleteIds = new ArrayList<Integer>();

            for (Athlete ath : chosenAthletes) {
                athleteNames.add(ath.getName());
                athleteIds.add(ath.getAid());
            }
            // 报存显示在成绩运动员匹配页面的运动员名字
            app.getMap().put(Constants.DRAG_NAME_LIST, athleteNames);
            /**
             * 顺便保存id吧
             */
            app.getMap().put(Constants.DRAG_NAME_LIST_IDS,athleteIds);

            String poolString = (String) poolSpinner.getSelectedItem();
//            int strokeNumber = strokeSpinner.getSelectedItemPosition();
            String extra = remarksEditText.getText().toString();
            // 将配置保存到数据库计划表中
            savePlan(poolString, totalDistance, extra,intervalDistance, isReset,chosenAthletes);
            /**
             * 带上是否停表的设置过去
             */
            AppController.gotoTimerActivity(getActivity(), isReset);
        }
    }

    private void savePlan(String pool, String distance, String extra,String interval,
                          boolean isReset, List<Athlete> athlete) {
        // TODO Auto-generated method stub
        User user = dbManager.getUserByUid(userid);
        //time是总距离除以计时次数得到的
        int time = (Integer.parseInt(distance))/(Integer.parseInt(interval));
        Plan plan = new Plan();
        plan.setPool(pool);
        plan.setReset(isReset);
        plan.setInterval(Integer.parseInt(interval));
        plan.setDistance(Integer.parseInt(distance));
        plan.setExtra(extra);
//        plan.setUser(user);
//        plan.setAthlete(athlete);
        plan.setUid(userid);
        plan.setTime(time);
        plan.save();
        //存储计划id
        app.getMap().put(Constants.PLAN_ID, plan.getId());
    }

    /**
     * fragment重新显示的时候，做更新操作
     */
    @Override
    public void onReShow() {
//        upDateAthleteList();
    }

    /**
     * 在这里进行数据刷新
     */
    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    private void setIsReset(){
        rbStopWatch.setChecked(!isReset);
        isReset = !isReset;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_choose_athlete:
                chooseAthlete();
                break;
            case R.id.btn_start_timer:
                startTiming();
                break;
            case R.id.rb_stop_watch:
               setIsReset();
                break;
        }
    }
}
