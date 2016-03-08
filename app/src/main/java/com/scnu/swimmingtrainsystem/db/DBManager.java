package com.scnu.swimmingtrainsystem.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.scnu.swimmingtrainsystem.model.Athlete;
import com.scnu.swimmingtrainsystem.model.OtherScore;
import com.scnu.swimmingtrainsystem.model.Plan;
import com.scnu.swimmingtrainsystem.model.Score;
import com.scnu.swimmingtrainsystem.model.ScoreSum;
import com.scnu.swimmingtrainsystem.model.User;
import com.scnu.swimmingtrainsystem.util.CommonUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 数据库操作类
 * 
 * @author LittleByte
 * 
 */
public class DBManager {
	private static DBManager dbManager = new DBManager();

	private DBManager() {

	}

	/**
	 * 获取单例的DBManager
	 * 
	 * @return
	 */
	public static DBManager getInstance() {
		return dbManager;
	}

	
	public User getUserByUid(int uid){
		String userId = String.valueOf(uid);
		List<User> users = DataSupport.where("uid=?",userId).find(User.class);
		if(users.size()>0){
			return users.get(0);
		}
		return null;
	}



	/**
	 * 修改该id的登录密码
	 * 
	 * @param id
	 *            用户id
	 * @param newPassword
	 *            新密码
	 * @return
	 */
	public int modifyUserPassword(long id, String newPassword) {
		ContentValues values = new ContentValues();
		values.put("password", newPassword);
		return DataSupport.update(User.class, values, id);
	}

	/**
	 * 获取当前用户的运动员表中所有运动员
	 * 
	 * @return
	 */
	public List<Athlete> getAthletes(int userId) {
		User user = null ;
		List<User> users = DataSupport.where("uid=?", String.valueOf(userId)).find(User.class);
		if(users.size()>0){
			user = users.get(0);
		}
		List<Athlete> athletes = user.getAthletes();
		return athletes;
	}

	public Athlete getAthleteByAid(int aid) {
		String athlete_id = String.valueOf(aid);
		Athlete athlete = DataSupport.where("aid=?", athlete_id)
				.find(Athlete.class).get(0);
		return athlete;
	}

	/**
	 * 获取该用户的所查询计划的运动员
	 * 
	 * @param athIds
	 * @param userId
	 * @return
	 */
	public List<Athlete> getAthletesByAid(Integer[] athIds, Long userId) {
		String thisUserId = String.valueOf(userId);
		List<Athlete> athleteList = new ArrayList<Athlete>();
		for (int aid : athIds) {
			String id = String.valueOf(aid);
			List<Athlete> athletes = DataSupport.where("user_id=? and aid=? ",
					thisUserId, id).find(Athlete.class);
			if (athletes.size() != 0) {
				athleteList.add(athletes.get(0));
			}
		}
		return athleteList;

	}

	/**
	 * 根据当前用户id和运动员名字查询出运动员信息
	 * 
	 * @param name
	 *            运动员姓名
	 * @return
	 */
	public Athlete getAthleteByName(long userID, String name) {
		String id = String.valueOf(userID);
		List<Athlete> aths = DataSupport.where("user_id=? and name=? ", id,
				name).find(Athlete.class);
		if (aths.size() != 0) {
			Athlete athlete = aths.get(0);
			return athlete;
		}
		return null;
	}


	/**
	 * 通过运动员id来获取运动员列表
	 * @param ids
	 * @return
	 */
	public List<Athlete> getAthleteByIDs(List<Integer> ids){
		List<Athlete> lists = new ArrayList<Athlete>();
		for(int id:ids){
			String idstr = String.valueOf(id);
			Athlete athlete = DataSupport.where("aid=?",idstr).find(Athlete.class).get(0);
			lists.add(athlete);
		}
		return lists;
	}

	/**
	 * 通过关联查询，根据成绩id查找运动员名字
	 * 
	 * @param id
	 *            成绩id
	 * @return 运动员名字
	 */
	public String getAthleteNameByScoreID(long id) {
		int athlete_id = DataSupport.find(Score.class, id, true).getAthlete_id();
		String name = getAthleteByAid(athlete_id).getName();
		return name;

	}



	/**
	 * 批量查询运动员
	 * 
	 * @param athID
	 * @return
	 */
	public List<Athlete> getAthletes(List<Long> athID) {
		List<Athlete> athletes = new ArrayList<Athlete>();
		for (long id : athID) {
			Athlete a = DataSupport.find(Athlete.class, id);
			athletes.add(a);
		}
		return athletes;
	}

	/**
	 * 更新 Athlete表的个人信息
	 * 
	 * @param list
	 * @param postion
	 * @param name
	 * @param age
	 * @param phone
	 * @param extras
	 */
	public void updateAthlete(List<Athlete> list, int postion, String name,
			String age, String gender, String phone, String extras) {

		int id = list.get(postion).getId();
		ContentValues values = new ContentValues();
		values.put("name", name);
		values.put("age", Integer.parseInt(age));
		values.put("gender", gender);
		values.put("phone", phone);
		values.put("extras", extras);
		DataSupport.update(Athlete.class, values, id);
	}

	public int deleteAthlete(Athlete a){
		String userID = String.valueOf(a.getAid());
		return DataSupport.deleteAll(Athlete.class, "aid=?", userID);
	}

	/**
	 * 判断是否已存在给定名字的运动员
	 * 
	 * @param id
	 * @param name
	 * @return
	 */
	public boolean isAthleteNameExsit(long id, String name) {
		String uid = String.valueOf(id);
		List<Athlete> athletes = DataSupport.where("user_id=?", uid).find(
				Athlete.class);
		for (int i = 0; i < athletes.size(); i++) {
			if (name.equals(athletes.get(i).getName())) {
				return true;
			}
		}
		return false;

	}

	/**
	 * 查找table_plan中当前用户的所有计划
	 * 
	 * @return 计划列表
	 */
	public List<Plan> getUserPlans(long userID) {
		String user = String.valueOf(userID);
		List<Plan> plans = DataSupport.where("user_id=?", user)
				.find(Plan.class);
		return plans;
	}

	/**
	 * 获取最新的计划ID
	 * 
	 * @return 最新的计划id
	 */
	public Long getLatestPlanId() {
		long id = DataSupport.max(Plan.class, "id", Long.class);
		return id;
	}

	/**
	 * 根据计划id查找对应的计划
	 * 
	 * @param id
	 * @return
	 */
	public Plan queryPlan(long id) {
		Plan plan = DataSupport.find(Plan.class, id);
		return plan;
	}

	/**
	 * 通过计划名字获取计划对象
	 * 
	 * @param name
	 *            计划名字
	 * @return 计划对象
	 */
	public List<Plan> getPlanByPName(String name) {
		List<Plan> plans = DataSupport.where("name=?", name).find(Plan.class,
				true);
		return plans;

	}

	/**
	 * 激进查询，查询关联表
	 * 
	 * @param id
	 * @return
	 */
//	public List<Athlete> getAthInPlan(long id) {
//		Plan p = DataSupport.find(Plan.class, id, true);
//		List<Athlete> athletes = p.getAthlete();
//		return athletes;
//	}

	/**
	 * 找出成绩表中指定运动员id的成绩数据集
	 * 
	 * @param id
	 * @return
	 */
	public List<Score> getScoreByAth(Long id) {
		String ids = String.valueOf(id);
		List<Score> s = DataSupport.where("athlete_id=?", ids)
				.find(Score.class);
		return s;
	}

	/**
	 * 通过日期找出普通成绩，需要什么字段都在这里找
	 * 
	 * @param date
	 * @return
	 */
	public List<Score> getScoreByDate(String date) {
		List<Score> list = DataSupport
				.select("score", "distance", "type", "date", "times","stroke","athlete_id")
				.where("date=?", date).find(Score.class);
		return list;
	}

	public List<OtherScore> getOtherScoreByDate(String date){
		List<OtherScore> list = DataSupport.select("score","pdate","athlete_name","athlete_id")
				.where("pdate=?", date).find(OtherScore.class);
		return list;
	}

	/**
	 * 通过日期在成绩表中查找对应的运动员ID，并返回Aid
	 * 
	 * @param date
	 * @return
	 */
	public List<Integer> getAthlteAidInScoreByDate(String date) {
		List<Integer> aidList = new ArrayList<Integer>();
		List<Score> scList = DataSupport.where("date=?", date).find(
				Score.class, true);
		int n = scList.size();
		for (int i = 0; i < n; i++) {
			aidList.add(scList.get(i).getAthlete_id());
		}
		return aidList;
	}

	/**
	 * 通过测试日期和游泳趟数查找成绩
	 * 
	 * @param date
	 * @param times
	 * @return List<Score> score
	 */
	public List<Score> getScoreByDateAndTimes(String date, int times) {
		String str = String.valueOf(times);
		List<Score> scores = DataSupport.where("date=? and times=?", date, str)
				.find(Score.class, true);
		return scores;
	}

	/**
	 * 通过日期查找成绩
	 * 
	 * @param date
	 * @param athId
	 * @return
	 */
	public List<List<Score>> getScoreByDate(List<String> date, long athId) {
		String athleteID = String.valueOf(athId);
		List<List<Score>> s = new ArrayList<List<Score>>();
		for (String str : date) {
			s.add(DataSupport.where("date=? and athlete_id=?", str, athleteID)
					.find(Score.class));
		}
		return s;
	}

	public Plan getPlanByPid(int pid) {
		String planid = String.valueOf(pid);
		Plan plan = DataSupport.where("pid=?", planid).find(Plan.class).get(0);
		return plan;
	}

	/**
	 * 通过查找成绩表中的日期获得对应计划id
	 * 
	 * @param date
	 *            测试日期
	 * @param athId
	 *            运动员id
	 * @return 计划id
	 */
	public List<Integer> getPlanInScoreByDate(List<String> date, long athId) {
		String athleteID = String.valueOf(athId);
		List<Integer> plans = new ArrayList<Integer>();
		for (String str : date) {
			List<Score> list = DataSupport.where(
					"date=? and athlete_id=? and times=?", str, athleteID, "1")
					.find(Score.class, true);
			for (Score s : list) {
				int plan_id = DataSupport.find(Score.class, s.getId(), true).getPlan_id();
				plans.add(plan_id);
			}

		}
		return plans;

	}

	/**
	 * 查找特定日期中一轮测试的运动员id,可以用来计算总成绩
	 * 
	 * @param date
	 * @return
	 */
	public List<Integer> getAthleteNumberInScoreByDate(String date) {
		List<Integer> athIds = new ArrayList<Integer>();
		List<Score> list = DataSupport.select("athlete_id")
				.where("date=? and times=?", date, "1").find(Score.class, true);
		for (Score s : list) {
			athIds.add(s.getAthlete_id());
		}
		return athIds;
	}

	/**
	 * 通过日期查找成绩表存在的运动员id,然后再通过日期和运动员id查找成绩表查找其对应的 多次成绩并计算出总和
	 * 修改
	 * @param
	 * @return
	 */
	public List<ScoreSum> getAthleteIdInScoreByDate(String date, List<Integer> list,boolean isReset) {
		List<ScoreSum> temps = new ArrayList<ScoreSum>();
		for (int athlete_id : list) {
			/**
			 * 查询出来的成绩按照times再次进行排序
			 */
			List<Score> scores = DataSupport.select("score", "athlete_id")
					.where("date=? and athlete_id=?", date, String.valueOf(athlete_id))
					.order("times")
					.find(Score.class, true);

			ScoreSum p = new ScoreSum();
			p.setAthleteName(getAthleteByAid(athlete_id).getName());
			/**
			 * 本次成绩是间歇的
			 */
			if(isReset){
				List<String> sum = new ArrayList<String>();
				for (Score s : scores) {
					sum.add(s.getScore());
				}
				/**
				 * 对成绩进行累加
				 */
				p.setScore(CommonUtils.scoreSum(sum));
			}
			/**
			 * 本次成绩不是间歇的，直接获取最后一个成绩就是总成绩，不需要累加
			 */
			else{
				p.setScore(scores.get(scores.size()-1).getScore());
			}

			temps.add(p);
		}
		/**
		 * 对成绩进行排序
		 */
		Collections.sort(temps, new ScoreComparable());
		return temps;
	}

	/**
	 * 获取属于该用户指定类型的成绩日期
	 * 
	 * @param userId
	 * @param type
	 * @param offset
	 * @return
	 */
	public List<String> getScoresByUserId(long userId, int type, int offset) {
		String userString = String.valueOf(userId);
		String typeString = String.valueOf(type);
		List<String> dates = new ArrayList<String>();
		String sql = "select distinct date from score where user_id='"
				+ userString + "' and type='" + typeString
				+ "' order by date desc limit " + offset + ",20";
		Cursor cursor = DataSupport.findBySQL(sql);
		while (cursor.moveToNext()) {
			String date = cursor.getString(cursor.getColumnIndex("date"));
			dates.add(date);
		}

		return dates;
	}

	/**
	 * 获取指定日期该轮的所有运动员id
	 * 
	 * @param date
	 * @return
	 */
	public List<Integer> getAthleteIdInScoreByDate(String date) {
		List<Integer> athleteIds = new ArrayList<Integer>();
		String sql = "select distinct athlete_id from score where  date='"
				+ date + "'";
		Cursor cursor = DataSupport.findBySQL(sql);
		while (cursor.moveToNext()) {
			int athlete_id = cursor.getInt(cursor
					.getColumnIndex("athlete_id"));
			athleteIds.add(athlete_id);
		}
		return athleteIds;
	}

	/**
	 * 获取该用户普通成绩的最新日期
	 * 
	 * @param user_id
	 *            用戶id
	 * @return
	 */
	public int getScoreDateNumberbyUid(long user_id) {
		String userString = String.valueOf(user_id);
		List<String> dates = new ArrayList<String>();
		String sql = "select distinct date from score where user_id='"
				+ userString + "' and type='1'";
		Cursor cursor = DataSupport.findBySQL(sql);
		while (cursor.moveToNext()) {
			String date = cursor.getString(cursor.getColumnIndex("date"));
			dates.add(date);
		}
		return dates.size();
	}

	public void deleteScores(String date) {
		List<Score> scores = DataSupport.select("id").where("date=?", date)
				.find(Score.class);
		for (Score s : scores) {
			DataSupport.delete(Score.class, s.getId());
		}
	}

	/**
	 * 成绩比较
	 * 
	 * @author LittleByte
	 * 
	 */
	class ScoreComparable implements Comparator<ScoreSum> {

		@Override
		public int compare(ScoreSum lhs, ScoreSum rhs) {
			// TODO Auto-generated method stub
			ScoreSum temp1 = lhs;
			ScoreSum temp2 = rhs;
			int num = temp1.getScore().compareTo(temp2.getScore());
			if (num == 0)
				return temp1.getAthleteName().compareTo(temp2.getAthleteName());
			return num;
		}

	}
}
