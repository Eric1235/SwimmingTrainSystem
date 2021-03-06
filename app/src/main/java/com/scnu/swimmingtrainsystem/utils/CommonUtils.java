package com.scnu.swimmingtrainsystem.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.entity.ExplicitScore;
import com.scnu.swimmingtrainsystem.entity.SmallPlan;
import com.scnu.swimmingtrainsystem.entity.SmallScore;
import com.scnu.swimmingtrainsystem.model2db.Athlete;
import com.scnu.swimmingtrainsystem.model2db.Plan;
import com.scnu.swimmingtrainsystem.model2db.Score;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 其他工具类
 * 
 * @author Littleyte
 * 
 */
@SuppressLint("DefaultLocale")
public class CommonUtils {
	/**
	 * 获取运动员的id
	 * @param lists
	 * @return
	 */
	public static List<Integer> getAthleteIdsByAthletes(List<Athlete> lists){
		List<Integer> ids = new ArrayList<Integer>();
		for(Athlete a : lists){
			ids.add(a.getAid());
		}
		return ids;
	}

	/**
	 * 获取运动员的名字
	 * @param lists
	 * @return
	 */
	public static List<String> getAthleteNamesByAthletes(List<Athlete> lists){
		List<String> names = new ArrayList<String>();
		for(Athlete a : lists){
			names.add(a.getName());
		}
		return  names;
	}

	/**
	 * 格式化上传日期
	 * @param date
	 * @return
	 */
	public static String formatDate(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}

	/**
	 * 通过计算mlCount，生成固定格式的时间字符串
	 *
	 * @return
	 */
	public static String getStrTime(long mlCount) {
		// 秒数
		long time_count_s = mlCount / 1000;
		// 小时数
		long hour = time_count_s / 3600;
		// 分
		long min = time_count_s / 60 - hour * 60;
		// 秒
		long sec = time_count_s - hour * 3600 - min * 60;
		// 毫秒
		long msec = mlCount % 1000 / 10;


		return 	String.format("%1$01d:%2$02d'%3$02d''%4$02d",
				hour, min, sec, msec);
	}

	/**
	 * 转化上传的成绩
	 * @param s
	 * @return
	 */
	public static SmallScore convertScore(Score s){
		SmallScore smScore = new SmallScore();
		smScore.setScore(s.getScore());
		smScore.setDate(s.getDate());
		smScore.setDistance(s.getDistance());
		smScore.setStroke(s.getStroke());
		smScore.setType(s.getType());
		smScore.setAthlete_id(s.getAthlete_id());
		smScore.setTimes(s.getTimes());
		return smScore;
	}

	/**
	 * 转化要上传的plan字段
	 * @param plan
	 * @return
	 */
	public static SmallPlan convertPlan(Plan plan){
		SmallPlan sp = new SmallPlan();
		sp.setDistance(plan.getInterval());
		sp.setPool(plan.getPool());
		sp.setExtra(plan.getExtra());
		sp.setTime(plan.getTime());
		sp.setPdate(plan.getPdate());
		sp.setReset(plan.isReset());
		return sp;
	}

	/**
	 * 转化成绩为score,存储数据到数据库，感觉到要优化
	 * @param responseScore
	 */
	public static Score convertScore(ExplicitScore responseScore){
		Score score = new Score();
		score.setDate(responseScore.getUp_time());
		score.setAthlete_id(responseScore.getAthlete_id());
		score.setDistance(responseScore.getDistance());
		score.setScore(responseScore.getScore());
		score.setPlan_id(responseScore.getPlan_id());
		score.setTimes(responseScore.getTimes());
		score.setType(1);
//		score.save();
		return score;
	}


	public static boolean isAthleteInLocal(List<Athlete> athleteList,int Aid){

		for(int i = 0 ; i < athleteList.size() ; i ++){
			if(athleteList.get(i).getAid() == Aid){
				return true;
			}
		}
		return false;
	}


	/**
	 * 自定义显示Toast
	 * 
	 * @param context
	 * @param mToast
	 * @param text
	 */
	public static void showToast(Context context, Toast mToast, String text) {
		TextView v;
		if (mToast == null) {
			mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
			mToast.setGravity(Gravity.CENTER, 0, 0);
			View view = View.inflate(context,R.layout.item_toast_info,null);
			v = (TextView) view;
			v.setText(text);
			view.setBackgroundResource(R.drawable.bg_toast);
			mToast.setView(view);
		} else {
			View view = View.inflate(context,R.layout.item_toast_info,null);
			v = (TextView) view;
			v.setText(text);
			mToast.setDuration(Toast.LENGTH_SHORT);
		}
		mToast.show();
	}

	/**
	 * 运动员--泳姿转换
	 * @param map
	 * @param aid
	 * @return
	 */
	public static int getAthleteStroke(Map<String,String> map,String aid){
		String stroke = map.get(aid);
		if(stroke != null){
			return Integer.parseInt(stroke);
		}else{
			return  0;
		}
	};

	private static long lastClickTime;

	/**
	 * 防止快速的重复点击出现
	 * 
	 * @return
	 */
	public static boolean isFastDoubleClick() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		if (0 < timeD && timeD < 800) {
			return true;
		}
		lastClickTime = time;
		return false;
	}

	public static void removeAthleteFromList(List<Athlete> list,Athlete a){
		int aid = a.getAid();
		int size = list.size();
		for(int i = 0 ; i < size ; i++){
			if(list.get(i).getAid() == aid){
				list.remove(i);
				break;
			}
		}

	}

	public static boolean ListContainsAthlete(List<Athlete> list,Athlete a){
		int aid = a.getAid();
		int size = list.size();
		for(int i = 0 ; i < size ; i++){
			if(list.get(i).getAid() == aid){
				return true;
			}
		}
		return false;
	}

	public static boolean isMobileNO(String mobiles) {
		Pattern p = Pattern
				.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	public static boolean isEmail(String email) {
		String str = "^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);
		return m.matches();
	}
}
