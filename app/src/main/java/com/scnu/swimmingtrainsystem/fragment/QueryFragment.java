package com.scnu.swimmingtrainsystem.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.activity.ExcuteQueryActivity;
import com.scnu.swimmingtrainsystem.db.DBManager;
import com.scnu.swimmingtrainsystem.model.Athlete;
import com.scnu.swimmingtrainsystem.model.QueryScoreEntity;
import com.scnu.swimmingtrainsystem.util.CommonUtils;
import com.scnu.swimmingtrainsystem.util.NetworkUtil;
import com.scnu.swimmingtrainsystem.util.SpUtil;
import com.scnu.swimmingtrainsystem.adapter.SpinnerAdapter;

import java.util.ArrayList;
import java.util.Collections;
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

    private DBManager mDBManager;
    private Toast mToast;
    private List<Athlete> mAthletes;

    private SpinnerAdapter athleteAdapter;


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

        initData();
        return v;
    }

    private void initData(){

        mUserId = SpUtil.getUID(getActivity());
        mDBManager = DBManager.getInstance();
        initAllOption();
        mAthletes =  mDBManager.getAthletes(mUserId);
        mAthletes.add(0,tmpAthlete);



        List<String> poolLength = new ArrayList<String>();
        List<String> stroke = new ArrayList<String>();
        List<String> distance = new ArrayList<String>();


        String[] strokes = getResources().getStringArray(R.array.strokestrarray);
        String[] poolLengths = getResources().getStringArray(R.array.pool_length);
        String[] distances = getResources().getStringArray(R.array.query_swim_length);

        Collections.addAll(poolLength, poolLengths);
        Collections.addAll(stroke, strokes);
        Collections.addAll(distance, distances);

        ArrayAdapter<String> strokeAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, stroke);
        ArrayAdapter<String> poolLengthadapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, poolLength);

        ArrayAdapter<String> distanceAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item,distance);

        strokeSpinner.setAdapter(strokeAdapter);
        poolLengthSpinner.setAdapter(poolLengthadapter);
        distanceSpinner.setAdapter(distanceAdapter);

        athleteAdapter = new SpinnerAdapter(getActivity(),mAthletes);
        mAthleteNames.setAdapter(athleteAdapter);

        btnStartTime.setOnClickListener(this);
        btnEndTime.setOnClickListener(this);
        rbReset.setOnClickListener(this);
        btnQuery.setOnClickListener(this);

    }

    private void initAllOption(){
        tmpAthlete = new Athlete();
        tmpAthlete.setAid(0);
        tmpAthlete.setName(getString(R.string.all_athlete));
        tmpAthlete.setId(0);
        tmpAthlete.setNumber("0");
    }

    private void initEntity(){
        mEntity.setUid(mUserId);
        mEntity.setDistance(distanceSpinner.getSelectedItem().toString());
        mEntity.setPoolLength(poolLengthSpinner.getSelectedItem().toString());
        mEntity.setStroke(strokeSpinner.getSelectedItemPosition()+1);
        a = (Athlete)mAthleteNames.getSelectedItem();
        mEntity.setAthleteId(a.getAid());
    }

    @Override
    public void onClick(View v) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        switch (v.getId()){
            case R.id.btn_start_time:
                datePickerFragment = DatePickerFragment.newInstance(mEntity.getStartTime());
                datePickerFragment.setTargetFragment(QueryFragment.this,StartTimeRequestCode);
                datePickerFragment.show(fm,DATE);
                break;
            case R.id.btn_end_time:
                datePickerFragment = DatePickerFragment.newInstance(mEntity.getEndTime());
                datePickerFragment.setTargetFragment(QueryFragment.this,EndTimeRequestCode);
                datePickerFragment.show(fm,DATE);
                break;
            case R.id.btn_network_query:
                initEntity();
                /**
                 * 提前判断网络是否有连接
                 */
                if(NetworkUtil.isConnected(getActivity()) && beforeSubmit()){
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

    private boolean beforeSubmit(){
        boolean b = true;
        if(mEntity.getStartTime() == null){
            CommonUtils.showToast(getActivity(),mToast,getString(R.string.start_time_not_null));
            b = false;
        }
        if (mEntity.getEndTime() == null){
            CommonUtils.showToast(getActivity(),mToast,getString(R.string.end_time_not_null));
            b = false;
        }
        return b;
    }

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
            btnStartTime.setText(mEntity.getStartTime().toLocaleString());
        }
        if(requestCode == EndTimeRequestCode){
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mEntity.setEndTime(date);
            btnEndTime.setText(mEntity.getEndTime().toLocaleString());
        }
    }
}
