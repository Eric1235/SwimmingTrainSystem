package com.scnu.swimmingtrainsystem.util;

import android.content.Context;
import android.content.Intent;

import com.scnu.swimmingtrainsystem.activity.AboutUsActivity;
import com.scnu.swimmingtrainsystem.activity.AthleteActivity;
import com.scnu.swimmingtrainsystem.activity.EachTimeScoreActivity;
import com.scnu.swimmingtrainsystem.activity.HomeActivity;
import com.scnu.swimmingtrainsystem.activity.LoginActivity;
import com.scnu.swimmingtrainsystem.activity.ModifyPassActivity;
import com.scnu.swimmingtrainsystem.activity.MyApplication;
import com.scnu.swimmingtrainsystem.activity.OtherFunctionActivity;
import com.scnu.swimmingtrainsystem.activity.QueryActivity;
import com.scnu.swimmingtrainsystem.activity.QuestionHelpActivity;
import com.scnu.swimmingtrainsystem.activity.RegistAcyivity;
import com.scnu.swimmingtrainsystem.activity.ShowExplicitScoreActivity;
import com.scnu.swimmingtrainsystem.activity.ShowScoreActivity;
import com.scnu.swimmingtrainsystem.activity.TimerActivity;

import java.util.HashMap;

/**
 * Created by lixinkun on 15/12/14.
 * 程序跳转控制器
 */
public class AppController {

    /**
     * 跳转到运动员界面
     * @param context
     */
    public static void gotoAthlete(Context context){
        Intent i = new Intent(context, AthleteActivity.class);
        context.startActivity(i);
    }

    /**
     * 跳转到成绩查询界面
     * @param context
     */
    public static void gotoQueryScore(Context context){
        Intent i = new Intent(context, QueryActivity.class);
        context.startActivity(i);
    }

    /**
     * 跳转到小功能界面
     * @param context
     */
    public static void gotoOtherFunction(Context context){
        Intent i = new Intent(context, OtherFunctionActivity.class);
        context.startActivity(i);
    }

    /**
     * 跳转到修改密码界面
     * @param context
     */
    public static void gotoModifyPwd(Context context){
        Intent i = new Intent(context, ModifyPassActivity.class);
        context.startActivity(i);
    }

    public static void gotoAboutUs(Context context){
        Intent i = new Intent(context, AboutUsActivity.class);
        context.startActivity(i);
    }

    public static void gotoQuestionHelp(Context context){
        Intent i = new Intent(context, QuestionHelpActivity.class);
        context.startActivity(i);
    }

    public static void gotoLogin(Context context){
        Intent i = new Intent(context, LoginActivity.class);
        context.startActivity(i);
    }

    public static void gotoHomeActivity(Context context){
        Intent i = new Intent(context, HomeActivity.class);
        context.startActivity(i);
    }

    public static void gotoShowScoreActivity(Context context,HashMap<String,Object> map){
        Intent intent = new Intent(context,
                ShowScoreActivity.class);
        intent.putExtra("dataMap",map);
        context.startActivity(intent);
    }

    public static void gotoShowExplicitScoreActivity(Context context,HashMap<String,Object> map){
        Intent i = new Intent(context, ShowExplicitScoreActivity.class);
        i.putExtra("dataMap",map);
        context.startActivity(i);
    }

    public static void gotoTimerActivity(Context context,boolean isReset){
        Intent i = new Intent(context, TimerActivity.class);
        i.putExtra(Constants.WATCHISRESET,isReset);
        context.startActivity(i);
    }

    public static void gotoEachTimeScoreActivity(Context context,boolean isReset){
        Intent i = new Intent(context, EachTimeScoreActivity.class);
        i.putExtra("isReset",isReset);
        context.startActivity(i);
    }

    public static void gotoRegister(Context context){
        Intent i = new Intent(context, RegistAcyivity.class);
        context.startActivity(i);
    }




    /**
     * 重置app
     * @param app
     */
    public static void reset(MyApplication app){
        app.getMap().put(Constants.CURRENT_SWIM_TIME, 0);
        app.getMap().put(Constants.PLAN_ID, 0);
        app.getMap().put(Constants.TEST_DATE, "");
        app.getMap().put(Constants.DRAG_NAME_LIST, null);
        app.getMap().put(Constants.CURRENT_USER_ID, "");
        app.getMap().put(Constants.IS_CONNECT_SERVER, true);
        app.getMap().put(Constants.COMPLETE_NUMBER, 0);
        app.getMap().put(Constants.INTERVAL, 0);
    }

    //登出的时候初始化数据
    public static void logout(Context context){
        SpUtil.SaveLoginInfo(context,false);
        SpUtil.saveLoginSucceed(context, false);
        SpUtil.saveSelectedPool(context, 0);
        SpUtil.saveSelectedStroke(context, 0);
        SpUtil.saveUID(context, 0);
        SpUtil.saveUserId(context, 0);
        SpUtil.saveDistance(context, "", "");
//        SpUtil.SaveLoginInfo(context, "", "");
    }

}
