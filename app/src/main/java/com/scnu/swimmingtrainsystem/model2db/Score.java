package com.scnu.swimmingtrainsystem.model2db;

import org.litepal.crud.DataSupport;

/**
 * 成绩实体类
 * 
 * @author LittleByte
 * 
 */
public class Score extends DataSupport {

	/**
	 * 成绩id
	 */
	private long id;
	/**
	 * 本次成绩是第几轮测试结果
	 */
	private int times;
	/**
	 * 具体成绩
	 */
	private String score;
	/**
	 * 创建成绩的日期
	 */
	private String date;

	/**
	 * 创建本次成绩时运动员游泳的距离
	 */
	private int distance;

	/**
	 * 泳姿
	 */
	private int stroke;

	/**
	 * 成绩类型
	 */
	private int type;

	private int athlete_id;

	/**
	 * 本次成绩对应的计划
	 */
	private int plan_id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public int getStroke() {
		return stroke;
	}

	public void setStroke(int stroke) {
		this.stroke = stroke;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getAthlete_id() {
		return athlete_id;
	}

	public void setAthlete_id(int athlete_id) {
		this.athlete_id = athlete_id;
	}

	public int getPlan_id() {
		return plan_id;
	}

	public void setPlan_id(int plan_id) {
		this.plan_id = plan_id;
	}

	@Override
	public String toString() {
		return "Score{" +
				"id=" + id +
				", times=" + times +
				", score='" + score + '\'' +
				", date='" + date + '\'' +
				", distance=" + distance +
				", stroke=" + stroke +
				", type=" + type +
				", athlete_id=" + athlete_id +
				", plan_id=" + plan_id +
				'}';
	}
}
