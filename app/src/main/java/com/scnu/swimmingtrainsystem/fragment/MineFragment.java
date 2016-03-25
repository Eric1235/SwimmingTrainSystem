package com.scnu.swimmingtrainsystem.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.activity.MyApplication;
import com.scnu.swimmingtrainsystem.db.DBManager;
import com.scnu.swimmingtrainsystem.model2db.User;
import com.scnu.swimmingtrainsystem.utils.AppController;
import com.scnu.swimmingtrainsystem.utils.Constants;
import com.scnu.swimmingtrainsystem.utils.SpUtil;


/**
 * 
 * @author lixinkun
 *
 * 2015年12月10日
 */
public class MineFragment extends BaseFragment implements View.OnClickListener{

    private DBManager mDBManager;
    private int mAthleteNum;
    private int mUserId;
    private User mUser;

    private View v;
    private LinearLayout gotoAboutUs;
    private LinearLayout gotoModifyPwd;
    private LinearLayout gotoLogout;
    private LinearLayout gotoQuestionHelp;
    private RelativeLayout gotoAthlete;
    private RelativeLayout gotoPlan;

    private TextView tvAthleteNum;
    private TextView tvPlanNum;
    private TextView tvUserName;
    private TextView tvUserPhone;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDBManager = DBManager.getInstance();
        mUserId = SpUtil.getUID(getActivity());
        mUser = mDBManager.getUserByUid(mUserId);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(v == null){
            v = inflater.inflate(R.layout.fragment_mine,null);

            tvAthleteNum = (TextView) v.findViewById(R.id.tv_athlete_num);
            tvPlanNum = (TextView) v.findViewById(R.id.tv_user_plans);
            tvUserName = (TextView) v.findViewById(R.id.tv_user_name);
            tvUserPhone = (TextView) v.findViewById(R.id.tv_user_phone);

            gotoQuestionHelp = (LinearLayout) v.findViewById(R.id.question_help);
            gotoModifyPwd = (LinearLayout) v.findViewById(R.id.setting_modify_password);
            gotoAboutUs = (LinearLayout) v.findViewById(R.id.about_us);
            gotoLogout = (LinearLayout) v.findViewById(R.id.logout);
            gotoAthlete = (RelativeLayout) v.findViewById(R.id.user_athlete_num_layout);
            gotoPlan = (RelativeLayout) v.findViewById(R.id.user_plans_layout);
            gotoAthlete.setOnClickListener(this);
            gotoPlan.setOnClickListener(this);
            gotoLogout.setOnClickListener(this);
            gotoQuestionHelp.setOnClickListener(this);
            gotoModifyPwd.setOnClickListener(this);
            gotoAboutUs.setOnClickListener(this);
        }
        return v;
    }




    @Override
    public void onResume() {
        super.onResume();
        mAthleteNum = mDBManager.getAthletes(mUserId).size();
        tvAthleteNum.setText(mAthleteNum+"");
        tvUserName.setText(mUser.getUsername());
        tvUserPhone.setText(mUser.getPhone());
        tvPlanNum.setText(mUser.getPlans().size()+"");
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
            case R.id.setting_modify_password:
                AppController.gotoModifyPwd(getActivity());
                break;
            case R.id.question_help:
                AppController.gotoQuestionHelp(getActivity());
                break;
            case R.id.about_us:
                AppController.gotoAboutUs(getActivity());
                break;
            case R.id.logout:
                createDialog();
                break;
            case R.id.user_athlete_num_layout:
                AppController.gotoAthlete(getActivity());
                break;
            case R.id.user_plans_layout:
                break;
            default:
                break;
        }
    }

    /**
     * 创建注销对话框
     */
    private void createDialog() {
        AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
        build.setTitle(getString(R.string.system_hint)).setMessage(getString(R.string.confirm_to_logout));
        build.setPositiveButton(Constants.OK_STRING,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyApplication app = (MyApplication) getActivity().getApplication();
                        AppController.reset(app);
                        //恢复数据
                        AppController.logout(getActivity());
                        AppController.gotoLogin(getActivity());
                        getActivity().finish();
                    }
                });
        build.setNegativeButton(Constants.CANCLE_STRING,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
}
