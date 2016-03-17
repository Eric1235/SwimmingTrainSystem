package com.scnu.swimmingtrainsystem.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.activity.ExcuteQueryActivity;
import com.scnu.swimmingtrainsystem.adapter.NormalSpinnerAdapter;
import com.scnu.swimmingtrainsystem.db.DBManager;
import com.scnu.swimmingtrainsystem.entity.QueryScoreEntity;
import com.scnu.swimmingtrainsystem.entity.SpinnerEntity;
import com.scnu.swimmingtrainsystem.model2db.Athlete;
import com.scnu.swimmingtrainsystem.utils.CommonUtils;
import com.scnu.swimmingtrainsystem.utils.NetworkUtil;
import com.scnu.swimmingtrainsystem.utils.SpUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 查询成绩
 * Created by lixinkun on 16/1/7.
 */
public class QueryFragment extends Fragment implements View.OnClickListener {

    public static final int StartTimeRequestCode = 100;
    public static final int EndTimeRequestCode = 200;

    public static final String QUERY_TYPE = "QUERYTYPE";

    private static final String DATE = "date";

    public static final String QUERY_ENTITY = "QUERYENTITY";

    private int mUserId;

    private boolean isReset;

    private Athlete a,tmpAthlete;

    private QueryScoreEntity mEntity;
    private Toast mToast;
//    private List<Athlete> mAthletes;

    private List<SpinnerEntity> mAthletes;
    private List<SpinnerEntity> mStrokes;
    private List<SpinnerEntity> mDistances;
    private List<SpinnerEntity> mPoolLengths;

    private DBManager mDBManager;


//    private AthleteSpinnerAdapter athleteAdapter;
    private NormalSpinnerAdapter athleteSpinnerAdapter;
    private NormalSpinnerAdapter poolLengthSpinnerAdapter;
    private NormalSpinnerAdapter distanceSpinnerAdapter;
    private NormalSpinnerAdapter strokeSpinnerAdapter;


    private DatePickerFragment datePickerFragment;
    private RadioButton rbReset;
    private Button btnStartTime;
    private Button btnEndTime;
    private Spinner poolLengthSpinner;
    private Spinner strokeSpinner;
    private Spinner distanceSpinner;
    private Spinner mAthleteNames;
    private Button btnQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEntity = new QueryScoreEntity();
        isReset = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = View.inflate(getActivity(), R.layout.fragment_query, null);
        btnStartTime = (Button) v.findViewById(R.id.btn_start_time);
        btnEndTime = (Button) v.findViewById(R.id.btn_end_time);
        rbReset = (RadioButton) v.findViewById(R.id.rb_stop_watch);
        poolLengthSpinner = (Spinner) v.findViewById(R.id.pool_length);
        strokeSpinner = (Spinner) v.findViewById(R.id.stroke);
        distanceSpinner = (Spinner) v.findViewById(R.id.sp_swim_length);
        mAthleteNames = (Spinner) v.findViewById(R.id.sp_athlete_name);
        btnQuery = (Button) v.findViewById(R.id.btn_network_query);

        btnStartTime.setOnClickListener(this);
        btnEndTime.setOnClickListener(this);
        rbReset.setOnClickListener(this);
        btnQuery.setOnClickListener(this);

        initData();
        return v;
    }

    private void initData(){

        mUserId = SpUtil.getUID(getActivity());
        mDBManager = DBManager.getInstance();
        initAllOption();

        strokeSpinner.setAdapter(strokeSpinnerAdapter);
        poolLengthSpinner.setAdapter(poolLengthSpinnerAdapter);
        distanceSpinner.setAdapter(distanceSpinnerAdapter);
        mAthleteNames.setAdapter(athleteSpinnerAdapter);
    }

    private void initAllOption(){
        List<Athlete> athletes = mDBManager.getAthletes(mUserId);
        int[] strokes = getResources().getIntArray(R.array.stroke_array_int);
        String[] strokeStrings = getResources().getStringArray(R.array.stroke_array_string);

        int[] poolLengths = getResources().getIntArray(R.array.pool_length_int);
        String[] poolLengthStrings = getResources().getStringArray(R.array.pool_length_str);
        int[] distances = getResources().getIntArray(R.array.swim_distance_int);
        String[] distanceStrings = getResources().getStringArray(R.array.swim_distance_string);



        mAthletes = convertAthleteEntity(athletes);
        mStrokes = convertSpinnerEntity(strokeStrings,strokes);
        mDistances = convertSpinnerEntity(distanceStrings,distances);
        mPoolLengths = convertSpinnerEntity(poolLengthStrings,poolLengths);

        athleteSpinnerAdapter = new NormalSpinnerAdapter(mAthletes,getActivity());
        strokeSpinnerAdapter = new NormalSpinnerAdapter(mStrokes,getActivity());
        distanceSpinnerAdapter = new NormalSpinnerAdapter(mDistances,getActivity());
        poolLengthSpinnerAdapter = new NormalSpinnerAdapter(mPoolLengths,getActivity());

    }

    /**
     * 将运动员列表转换为显示列表，只要名字和aid就够了
     * @param athletes
     * @return
     */
    private List<SpinnerEntity> convertAthleteEntity(List<Athlete> athletes){
        List<SpinnerEntity> mAthleteEntities = new ArrayList<>();
        SpinnerEntity tmpAthlete = new SpinnerEntity();
        tmpAthlete.setDisplayString("所有");
        tmpAthlete.setUsedNo(0);
        mAthleteEntities.add(tmpAthlete);
        for(Athlete a : athletes){
            tmpAthlete = new SpinnerEntity();
            tmpAthlete.setDisplayString(a.getName());
            tmpAthlete.setUsedNo(a.getAid());
            mAthleteEntities.add(tmpAthlete);
        }
        return mAthleteEntities;
    }

    /**
     * 将泳姿选择转换成为展示所需的数据
     * @param
     * @return
     */
    private List<SpinnerEntity> convertSpinnerEntity(String[] disPlayStrings, int[] usedNo){
        List<SpinnerEntity> mEntities = new ArrayList<>();
        SpinnerEntity tmp ;
        for( int i = 0 ; i < disPlayStrings.length ; i ++ ){
            tmp = new SpinnerEntity();
            tmp.setDisplayString(disPlayStrings[i]);
            tmp.setUsedNo(usedNo[i]);
            mEntities.add(tmp);
        }
        return mEntities;

    }


    /**
     * 得到查询的参数
     */
    private void initEntity(){
        mEntity.setUid(mUserId);
        SpinnerEntity athleteEntity,strokeEntity,poolLengthEntity,distanceEntity;
        athleteEntity = (SpinnerEntity)mAthleteNames.getSelectedItem();
        strokeEntity = (SpinnerEntity) strokeSpinner.getSelectedItem();
        poolLengthEntity = (SpinnerEntity) poolLengthSpinner.getSelectedItem();
        distanceEntity = (SpinnerEntity) distanceSpinner.getSelectedItem();
        mEntity.setDistance(distanceEntity.getUsedNo());
        mEntity.setPoolLength(poolLengthEntity.getUsedNo());
        mEntity.setStroke(strokeEntity.getUsedNo());
        mEntity.setAthleteId(athleteEntity.getUsedNo());
    }

    @Override
    public void onClick(View v) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        switch (v.getId()){
            case R.id.btn_start_time:
                Date startTime = mEntity.getStartTime();
                if(startTime == null){
                    startTime = new Date();
                }
                datePickerFragment = DatePickerFragment.newInstance(startTime);
                datePickerFragment.setTargetFragment(QueryFragment.this,StartTimeRequestCode);
                datePickerFragment.show(fm,DATE);
                break;
            case R.id.btn_end_time:
                Date endTime = mEntity.getEndTime();
                if(endTime == null){
                    endTime = new Date();
                }
                datePickerFragment = DatePickerFragment.newInstance(endTime);
                datePickerFragment.setTargetFragment(QueryFragment.this,EndTimeRequestCode);
                datePickerFragment.show(fm,DATE);
                break;
            case R.id.btn_network_query:
                initEntity();
                /**
                 * 提前判断网络是否有连接
                 */
                if(NetworkUtil.isConnected(getActivity()) ){
                    Intent i = new Intent(getActivity(), ExcuteQueryActivity.class);
                    i.putExtra(QUERY_ENTITY,mEntity);
                    startActivity(i);
                }else{
                    CommonUtils.showToast(getActivity(), mToast, getString(R.string.network_error));
                }

                break;
            case R.id.rb_stop_watch:
                setIsReset();
                break;
        }
    }

//    private boolean beforeSubmit(){
//        boolean b = true;
//        if(mEntity.getStartTime() == null){
//            CommonUtils.showToast(getActivity(),mToast,getString(R.string.start_time_not_null));
//            b = false;
//        }
//        if (mEntity.getEndTime() == null){
//            CommonUtils.showToast(getActivity(),mToast,getString(R.string.end_time_not_null));
//            b = false;
//        }
//        return b;
//    }

    private void setIsReset(){
        rbReset.setChecked(!isReset);
        isReset = !isReset;
        mEntity.setIsReset(isReset);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK)
            return;
        if(requestCode == StartTimeRequestCode){
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mEntity.setStartTime(date);
            String startTimeString = CommonUtils.formatDate(date);
            btnStartTime.setText(startTimeString);
        }
        if(requestCode == EndTimeRequestCode){
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mEntity.setEndTime(date);
            String endTimeString = CommonUtils.formatDate(date);
            btnEndTime.setText(endTimeString);
        }
    }
}
