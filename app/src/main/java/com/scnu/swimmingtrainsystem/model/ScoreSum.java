package com.scnu.swimmingtrainsystem.model;

/**
 * 这是存储运动员成绩总和的临时对象
 * 保存运动员的名字和本次游泳的总成绩
 * @author LittleByte
 * 修改:李新坤
 * 
 */
public class ScoreSum {

	private String score;
	private String athleteName;

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getAthleteName() {
		return athleteName;
	}

	public void setAthleteName(String athleteName) {
		this.athleteName = athleteName;
	}

	@Override
	public String toString() {
		return "Temp [score=" + score + ", athleteName=" + athleteName + "]";
	}

}
